/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.bittorrent;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPopupMenu;

import jd.controlling.downloadcontroller.DownloadController;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.plugins.FilePackage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.limewire.util.FilenameUtils;
import org.limewire.util.OSUtils;

import com.aelitis.azureus.core.AzureusCore;
import com.frostwire.AzureusStarter;
import com.frostwire.core.FileDescriptor;
import com.frostwire.gui.bittorrent.BTDownloadActions.PlaySingleMediaFileAction;
import com.frostwire.gui.components.slides.Slide;
import com.frostwire.gui.filters.TableLineFilter;
import com.frostwire.gui.library.LibraryUtils;
import com.frostwire.gui.player.MediaPlayer;
import com.frostwire.gui.theme.SkinMenu;
import com.frostwire.gui.theme.SkinMenuItem;
import com.frostwire.gui.theme.SkinPopupMenu;
import com.frostwire.gui.transfers.PeerHttpUpload;
import com.frostwire.search.torrent.TorrentSearchResult;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.PaddedPanel;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.dnd.FileTransfer;
import com.limegroup.gnutella.gui.search.SoundcloudUISearchResult;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;
import com.limegroup.gnutella.gui.tables.LimeJTable;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.TableSettings;
import com.limegroup.gnutella.settings.ApplicationSettings;
import com.limegroup.gnutella.settings.BittorrentSettings;
import com.limegroup.gnutella.settings.QuestionsHandler;
import com.limegroup.gnutella.settings.TablesHandlerSettings;
import com.limegroup.gnutella.settings.UpdateManagerSettings;

/**
 * This class acts as a mediator between all of the components of the
 * download window.  It also constructs all of the download window
 * components.
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class BTDownloadMediator extends AbstractTableMediator<BTDownloadRowFilteredModel, BTDownloadDataLine, BTDownload> {

    private static final Log LOG = LogFactory.getLog(BTDownloadMediator.class);

    public static final int MIN_HEIGHT = 150;

    /**
     * instance, for singleton access
     */
    private static BTDownloadMediator INSTANCE;

    public static BTDownloadMediator instance() {
        if (INSTANCE == null) {
            INSTANCE = new BTDownloadMediator();
        }
        return INSTANCE;
    }

    /**
     * Variables so only one ActionListener needs to be created for both
     * the buttons & popup menu.
     */
    private Action removeAction;
    private Action removeYouTubeAction;
    private Action resumeAction;
    private Action pauseAction;
    private Action exploreAction;
    private Action copyMagnetAction;
    private Action copyHashAction;
    private Action shareTorrentAction;
    private Action showInLibraryAction;
    private Action clearInactiveAction;

    /** The actual download buttons instance.
     */
    private BTDownloadButtons _downloadButtons;
    private SeedingFilter _seedingFilter;

    private Action sendToItunesAction;

    private PlaySingleMediaFileAction playSingleMediaFileAction;

    /**
     * Overriden to have different default values for tooltips.
     */
    protected void buildSettings() {
        SETTINGS = new TableSettings(ID) {
            public boolean getDefaultTooltips() {
                return false;
            }
        };
    }

    /**
     * Sets up drag & drop for the table.
     */
    protected void setupDragAndDrop() {
        TABLE.setDragEnabled(true);
        TABLE.setTransferHandler(new BTDownloadTransferHandler());
    }

    /**
     * Build some extra listeners
     */
    protected void buildListeners() {
        super.buildListeners();

        clearInactiveAction = BTDownloadActions.CLEAR_INACTIVE_ACTION;
        removeAction = BTDownloadActions.REMOVE_ACTION;
        removeYouTubeAction = BTDownloadActions.REMOVE_YOUTUBE_ACTION;
        resumeAction = BTDownloadActions.RESUME_ACTION;
        pauseAction = BTDownloadActions.PAUSE_ACTION;
        exploreAction = BTDownloadActions.EXPLORE_ACTION;
        showInLibraryAction = BTDownloadActions.SHOW_IN_LIBRARY_ACTION;
        copyMagnetAction = BTDownloadActions.COPY_MAGNET_ACTION;
        copyHashAction = BTDownloadActions.COPY_HASH_ACTION;
        shareTorrentAction = BTDownloadActions.SHARE_TORRENT_ACTION;
        sendToItunesAction = BTDownloadActions.SEND_TO_ITUNES_ACTION;
        playSingleMediaFileAction = BTDownloadActions.PLAY_SINGLE_AUDIO_FILE_ACTION;
    }

    /**
     * Returns the most prominent actions that operate on the download table.
     * @return
     */
    public Action[] getActions() {
        Action[] actions;
        if (OSUtils.isWindows() || OSUtils.isMacOSX())
            actions = new Action[] { resumeAction, pauseAction, showInLibraryAction, exploreAction, removeAction, clearInactiveAction };
        else
            actions = new Action[] { resumeAction, pauseAction, removeAction };

        return actions;
    }

    /**
     * Set up the necessary constants.
     */
    protected void setupConstants() {
        MAIN_PANEL = new PaddedPanel(I18n.tr("Transfers"));
        _seedingFilter = new SeedingFilter();
        DATA_MODEL = new BTDownloadRowFilteredModel(_seedingFilter);//new BTDownloadModel();
        TABLE = new LimeJTable(DATA_MODEL);
        _downloadButtons = new BTDownloadButtons(this);
        BUTTON_ROW = _downloadButtons.getComponent();

        updateTableFilters();
    }

    /**
     * Filter out all the models who are being seeded.
     * @author gubatron
     *
     */
    class SeedingFilter implements TableLineFilter<BTDownloadDataLine> {
        @Override
        public boolean allow(BTDownloadDataLine node) {
            if (ApplicationSettings.SHOW_SEEDING_TRANSFERS.getValue()) {
                return true;
            }

            if (node == null) {
                return false;
            }

            return !node.isSeeding();
        }
    }

    public void updateTableFilters() {

        if (TABLE == null || DATA_MODEL == null) {
            return;
        }

        DATA_MODEL.filtersChanged();
    }

    /**
     * Notification that a filter on this panel has changed.
     *
     * Updates the data model with the new list, maintains the selection,
     * and moves the viewport to the first still visible selected row.
    */
    boolean filterChanged() {
        // store the selection & visible rows
        int[] rows = TABLE.getSelectedRows();
        BTDownloadDataLine[] lines = new BTDownloadDataLine[rows.length];
        List<BTDownloadDataLine> inView = new LinkedList<BTDownloadDataLine>();
        for (int i = 0; i < rows.length; i++) {
            int row = rows[i];
            BTDownloadDataLine line = DATA_MODEL.get(row);
            lines[i] = line;
            if (TABLE.isRowVisible(row))
                inView.add(line);
        }

        // change the table.
        DATA_MODEL.filtersChanged();

        // reselect & move the viewpoint to the first still visible row.
        for (int i = 0; i < rows.length; i++) {
            BTDownloadDataLine line = lines[i];
            int row = DATA_MODEL.getRow(line);
            if (row != -1) {
                TABLE.addRowSelectionInterval(row, row);
                if (inView != null && inView.contains(line)) {
                    TABLE.ensureRowVisible(row);
                    inView = null;
                }
            }
        }

        return true;
    }

    /**
     * Update the splash screen.
     */
    protected void updateSplashScreen() {
        GUIMediator.setSplashScreenString(I18n.tr("Loading Download Window..."));
    }

    /**
     * Constructs all of the elements of the download window, including
     * the table, the buttons, etc.
     */
    private BTDownloadMediator() {
        super("DOWNLOAD_TABLE");
        GUIMediator.addRefreshListener(this);

        restoreSorting();
    }

    /**
     * Override the default refreshing so that we can
     * set the clear button appropriately.
     */
    public void doRefresh() {
        DATA_MODEL.refresh();

        int[] selRows = TABLE.getSelectedRows();

        if (selRows.length > 0) {
            BTDownloadDataLine dataLine = DATA_MODEL.get(selRows[0]);

            BTDownload dl = dataLine.getInitializeObject();

            boolean resumable = dl.isResumable();
            boolean pausable = dl.isPausable();
            boolean completed = dl.isCompleted();

            resumeAction.setEnabled(resumable);
            pauseAction.setEnabled(pausable);
            exploreAction.setEnabled(completed);
            showInLibraryAction.setEnabled(completed);
        }

        int n = DATA_MODEL.getRowCount();
        boolean anyClearable = false;
        for (int i = n - 1; i >= 0; i--) {
            BTDownloadDataLine btDownloadDataLine = DATA_MODEL.get(i);
            BTDownload initializeObject = btDownloadDataLine.getInitializeObject();
            if (isClearable(initializeObject)) {
                anyClearable = true;
                break;
            }
        }

        clearInactiveAction.setEnabled(anyClearable);

        try {
            if (OSUtils.isWindows() && UpdateManagerSettings.SHOW_FROSTWIRE_RECOMMENDATIONS.getValue()) {
                //TipsClient.instance().call();
            }
        } catch (Throwable e) {
            LOG.debug("Error using tips framework: " + e.getMessage());
        }
    }

    public int getActiveDownloads() {
        return DATA_MODEL.getActiveDownloads();
    }

    public int getActiveUploads() {
        return DATA_MODEL.getActiveUploads();
    }

    public int getTotalDownloads() {
        return DATA_MODEL.getTotalDownloads();
    }

    /**
     * Returns the aggregate amount of bandwidth being consumed by active downloads.
     *  
     * @return the total amount of bandwidth being consumed by active downloads.
     */
    private double getBandwidth(boolean download) {
        if (!AzureusStarter.isAzureusCoreStarted()) {
            return 0;
        }

        AzureusCore azureusCore = AzureusStarter.getAzureusCore();

        if (azureusCore == null) {
            return 0;
        }

        return (download) ? azureusCore.getGlobalManager().getStats().getDataReceiveRate() : azureusCore.getGlobalManager().getStats().getDataSendRate();
    }

    /** bytes/sec */
    private int getCloudDownloadsBandwidth() {
        return DownloadWatchDog.getInstance().getDownloadSpeedManager().getSpeed();
    }

    public double getDownloadsBandwidth() {
        return (getBandwidth(true) + getCloudDownloadsBandwidth()) / 1000;
    }

    public double getUploadsBandwidth() {
        return getBandwidth(false) / 1000 + PeerHttpUpload.getUploadsBandwidth();
    }

    /**
     * Overrides the default add.
     *
     * Adds a new Downloads to the list of Downloads, obtaining the necessary
     * information from the supplied <tt>Downloader</tt>.
     *
     * If the download is not already in the list, then it is added.
     *  <p>
     */
    public void add(BTDownload downloader) {
        if (!DATA_MODEL.contains(downloader)) {
            super.add(downloader, DATA_MODEL.getRowCount());
            if (DATA_MODEL.getRowCount() > 0) {
                int row = DATA_MODEL.getRow(downloader);
                if (row != -1) {
                    TABLE.setSelectedRow(row);
                    TABLE.ensureSelectionVisible();
                }
            }
        }
    }

    /**
     * Overrides the default remove.
     *
     * Takes action upon downloaded theme files, asking if the user wants to
     * apply the theme.
     *
     * Removes a download from the list if the user has configured their system
     * to automatically clear completed download and if the download is
     * complete.
     *
     * @param downloader the <tt>Downloader</tt> to remove from the list if it is
     *  complete.
     */
    public void remove(BTDownload dloader) {
        //        DownloadStatus state = dloader.getState();
        //        
        //        if (state == DownloadStatus.COMPLETE 
        //        		&& isThemeFile(dloader.getSaveFile().getName())) {
        //        	File themeFile = dloader.getDownloadFragment();
        //        	themeFile = copyToThemeDir(themeFile);
        //        	// don't allow changing of theme while options are visible,
        //        	// but notify the user how to change the theme
        //        	if (OptionsMediator.instance().isOptionsVisible()) {
        //        		GUIMediator.showMessage(I18n.tr("You have downloaded a skin titled \"{0}\", you can activate the new skin by clicking \"{1}\" in the \"{2}\"->\"{3}\" menu and then selecting it from the list of available skins.",
        //        		        ThemeSettings.formatName(dloader.getSaveFile().getName()),
        //        				I18n.tr("&Refresh Skins"),
        //        				I18n.tr("&View"),
        //        				I18n.tr("&Apply Skins")));
        //        	}
        //        	else {
        //        	    DialogOption response = GUIMediator.showYesNoMessage(
        //        				I18n.tr("You have downloaded a new skin titled {0}. Would you like to use this new skin?",
        //        				        ThemeSettings.formatName(dloader.getSaveFile().getName())),
        //        				QuestionsHandler.THEME_DOWNLOADED, DialogOption.YES
        //        				);
        //        		if( response == DialogOption.YES ) {
        //        			//ThemeMediator.changeTheme(themeFile);
        //        		}
        //        	}
        //        }
        //        
        //        if (state == DownloadStatus.COMPLETE &&
        //        		BittorrentSettings.TORRENT_AUTO_START.getValue() &&
        //        		isTorrentFile(dloader.getSaveFile().getName())) 
        //        	GUIMediator.instance().openTorrent(dloader.getSaveFile());
        //        
        //        if(SharingSettings.CLEAR_DOWNLOAD.getValue()
        //           && ( state == DownloadStatus.COMPLETE ||
        //                state == DownloadStatus.ABORTED ) ) {
        //        	super.remove(dloader);
        //        } else {
        //            DownloadDataLine ddl = DATA_MODEL.get(dloader);
        //            if (ddl != null) ddl.setEndTime(System.currentTimeMillis());
        //        }

        super.remove(dloader);

        dloader.remove();
    }

    /**
     * Launches the selected files in the <tt>Launcher</tt> or in the built-in
     * media player.
     */
    void launchSelectedDownloads() {
        //        int[] sel = TABLE.getSelectedRows();
        //        if (sel.length == 0) {
        //        	return;
        //        }
        //        LaunchableProvider[] providers = new LaunchableProvider[sel.length];
        //        for (int i = 0; i < sel.length; i++) {
        //        	providers[i] = new DownloaderProvider(DATA_MODEL.get(sel[i]).getDownloader());
        //        }
        //        GUILauncher.launch(providers);
    }

    /**
     * Pauses all selected downloads.
     */
    void pauseSelectedDownloads() {
        int[] sel = TABLE.getSelectedRows();
        for (int i = 0; i < sel.length; i++) {
            DATA_MODEL.get(sel[i]).getInitializeObject().pause();
        }
    }

    /**  
     * Launches explorer
     */
    void launchExplorer() {
        int[] sel = TABLE.getSelectedRows();
        BTDownload dl = DATA_MODEL.get(sel[sel.length - 1]).getInitializeObject();
        File toExplore = dl.getSaveLocation();

        if (toExplore == null) {
            return;
        }

        GUIMediator.launchExplorer(toExplore);
    }

    FileTransfer[] getSelectedFileTransfers() {
        int[] sel = TABLE.getSelectedRows();
        ArrayList<FileTransfer> transfers = new ArrayList<FileTransfer>(sel.length);
        //    	for (int i = 0; i < sel.length; i++) {
        //    		DownloadDataLine line = DATA_MODEL.get(sel[i]);
        //    		Downloader downloader = line.getDownloader();
        //    		// ignore if save file of complete downloader has already been moved
        //    		if (downloader.getState() == DownloadStatus.COMPLETE
        //    				&& !downloader.getSaveFile().exists()) {
        //    			continue;
        //    		}
        //        	if (downloader.isLaunchable()) {
        //        		transfers.add(line.getFileTransfer());
        //        	}
        //    	}
        return transfers.toArray(new FileTransfer[transfers.size()]);
    }

    public BTDownload[] getSelectedBTDownloads() {
        int[] sel = TABLE.getSelectedRows();
        ArrayList<BTDownload> btdownloadList = new ArrayList<BTDownload>(sel.length);
        for (int i = 0; i < sel.length; i++) {
            BTDownloadDataLine btDownloadDataLine = DATA_MODEL.get(sel[i]);
            if (btDownloadDataLine.getInitializeObject().isCompleted()) {
                btdownloadList.add(btDownloadDataLine.getInitializeObject());
            }
        }
        return btdownloadList.toArray(new BTDownload[btdownloadList.size()]);
    }

    /**
     * Forces the selected downloads in the download window to resume.
     */
    void resumeSelectedDownloads() {
        int[] sel = TABLE.getSelectedRows();
        for (int i = 0; i < sel.length; i++) {
            BTDownloadDataLine dd = DATA_MODEL.get(sel[i]);
            BTDownload downloader = dd.getInitializeObject();
            downloader.resume();
        }
    }

    /**
     * Handles a double-click event in the table.
     */
    public void handleActionKey() {

        BTDownload[] selectedDownloaders = getSelectedDownloaders();

        if (selectedDownloaders.length == 1) {
            playSingleMediaFileAction.setEnabled(selectionHasMediaFiles(selectedDownloaders[0]));
        }

        if (playSingleMediaFileAction.isEnabled()) {
            playSingleMediaFileAction.actionPerformed(null);
        }

        if (showInLibraryAction.isEnabled()) {
            showInLibraryAction.actionPerformed(null);
        }
    }

    protected JPopupMenu createPopupMenu() {

        JPopupMenu menu = new SkinPopupMenu();

        if (playSingleMediaFileAction.isEnabled()) {
            menu.add(new SkinMenuItem(playSingleMediaFileAction));
        }

        menu.add(new SkinMenuItem(resumeAction));
        menu.add(new SkinMenuItem(pauseAction));

        if (OSUtils.isWindows() || OSUtils.isMacOSX()) {
            menu.add(new SkinMenuItem(showInLibraryAction));
            menu.add(new SkinMenuItem(exploreAction));
        }

        menu.addSeparator();
        menu.add(new SkinMenuItem(shareTorrentAction));

        if (OSUtils.isMacOSX() || OSUtils.isWindows()) {
            menu.add(new SkinMenuItem(sendToItunesAction));
        }

        menu.add(new SkinMenuItem(copyMagnetAction));
        menu.add(new SkinMenuItem(copyHashAction));
        SkinMenu addToPlaylistMenu = BTDownloadMediatorAdvancedMenuFactory.createAddToPlaylistSubMenu();
        if (addToPlaylistMenu != null) {
            menu.add(addToPlaylistMenu);
        }
        menu.addSeparator();
        menu.add(new SkinMenuItem(removeAction));
        menu.add(new SkinMenuItem(BTDownloadActions.REMOVE_TORRENT_ACTION));
        menu.add(new SkinMenuItem(BTDownloadActions.REMOVE_TORRENT_AND_DATA_ACTION));
        menu.add(new SkinMenuItem(removeYouTubeAction));

        menu.addSeparator();

        menu.add(new SkinMenuItem(BTDownloadActions.TOGGLE_SEEDS_VISIBILITY_ACTION));

        SkinMenu advancedMenu = BTDownloadMediatorAdvancedMenuFactory.createAdvancedSubMenu();
        if (advancedMenu != null) {
            menu.addSeparator();
            menu.add(advancedMenu);
        }

        return menu;
    }

    /**
     * Handles the selection of the specified row in the download window,
     * enabling or disabling buttons and chat menu items depending on
     * the values in the row.
     *
     * @param row the selected row
     */
    public void handleSelection(int row) {

        BTDownloadDataLine dataLine = DATA_MODEL.get(row);

        boolean pausable = dataLine.getInitializeObject().isPausable();
        boolean resumable = dataLine.getInitializeObject().isResumable();
        boolean isTransferFinished = dataLine.getInitializeObject().isCompleted();

        File saveLocation = dataLine.getInitializeObject().getSaveLocation();

        boolean hasMediaFiles = selectionHasMediaFiles(dataLine.getInitializeObject());
        boolean hasMP4s = selectionHasMP4s(saveLocation);

        boolean isSingleFile = selectionIsSingleFile(saveLocation);

        removeAction.putValue(Action.NAME, I18n.tr("Cancel Download"));
        removeAction.putValue(LimeAction.SHORT_NAME, I18n.tr("Cancel"));
        removeAction.putValue(Action.SHORT_DESCRIPTION, I18n.tr("Cancel Selected Downloads"));

        BTDownload dl = dataLine.getInitializeObject();

        exploreAction.setEnabled(dl.isCompleted());
        showInLibraryAction.setEnabled(dl.isCompleted());

        removeAction.setEnabled(true);
        resumeAction.setEnabled(resumable);
        pauseAction.setEnabled(pausable);
        copyMagnetAction.setEnabled(!isHttpTransfer(dataLine.getInitializeObject()));
        copyHashAction.setEnabled(!isHttpTransfer(dataLine.getInitializeObject()));

        sendToItunesAction.setEnabled(isTransferFinished && (hasMediaFiles || hasMP4s));

        shareTorrentAction.setEnabled(getSelectedDownloaders().length == 1 && dataLine.getInitializeObject().isPausable());

        playSingleMediaFileAction.setEnabled(getSelectedDownloaders().length == 1 && hasMediaFiles && isSingleFile);

        removeYouTubeAction.setEnabled(isYouTubeTransfer(dataLine.getInitializeObject()));
        BTDownloadActions.REMOVE_TORRENT_ACTION.setEnabled(!isHttpTransfer(dataLine.getInitializeObject()));
        BTDownloadActions.REMOVE_TORRENT_AND_DATA_ACTION.setEnabled(!isHttpTransfer(dataLine.getInitializeObject()));
    }

    private boolean selectionHasMP4s(File saveLocation) {
        boolean hasMP4Files = saveLocation != null && (LibraryUtils.directoryContainsExtension(saveLocation, 4, "mp4") || (saveLocation.isFile() && FilenameUtils.hasExtension(saveLocation.getAbsolutePath(), "mp4")));
        return hasMP4Files;
    }

    private boolean selectionIsSingleFile(File saveLocation) {
        boolean isSingleFile = saveLocation != null && saveLocation.isFile();
        return isSingleFile;
    }

    private boolean selectionHasMediaFiles(BTDownload d) {
        if (d instanceof SoundcloudTrackDownload) {
            return true;
        }
        File saveLocation = d.getSaveLocation();
        boolean hasAudioFiles = saveLocation != null && (LibraryUtils.directoryContainsAudio(saveLocation, 4) || (saveLocation.isFile() && MediaPlayer.isPlayableFile(saveLocation)));
        return hasAudioFiles;
    }

    private boolean isHttpTransfer(BTDownload d) {
        return isYouTubeTransfer(d) || d instanceof SoundcloudTrackUrlDownload || d instanceof SoundcloudTrackDownload || d instanceof BTPeerHttpUpload;
    }

    private boolean isYouTubeTransfer(BTDownload d) {
        return d instanceof YouTubeVideoUrlDownload || d instanceof YouTubeItemDownload;
    }

    /**
     * Handles the deselection of all rows in the download table,
     * disabling all necessary buttons and menu items.
     */
    public void handleNoSelection() {
        removeAction.setEnabled(false);
        resumeAction.setEnabled(false);
        clearInactiveAction.setEnabled(false);
        pauseAction.setEnabled(false);
        exploreAction.setEnabled(false);
        showInLibraryAction.setEnabled(false);
        copyMagnetAction.setEnabled(false);
        copyHashAction.setEnabled(false);
        shareTorrentAction.setEnabled(false);
        sendToItunesAction.setEnabled(false);
        playSingleMediaFileAction.setEnabled(false);

        BTDownloadActions.REMOVE_TORRENT_ACTION.setEnabled(false);
        BTDownloadActions.REMOVE_TORRENT_AND_DATA_ACTION.setEnabled(false);
        removeYouTubeAction.setEnabled(false);
    }

    public void openTorrentSearchResult(final TorrentSearchResult sr, final boolean partialDownload, final ActionListener postPartialDownloadAction) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                BTDownload downloader = new TorrentFetcherDownload(sr.getTorrentUrl(), sr.getDetailsUrl(), sr.getDisplayName(), sr.getHash(), sr.getSize(), partialDownload, postPartialDownloadAction, null);
                add(downloader);
            }
        });
    }

    public void openTorrentURI(final String uri, final boolean partialDownload, final ActionListener postPartialDownloadAction) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                BTDownload downloader = new TorrentFetcherDownload(uri, partialDownload, postPartialDownloadAction);
                add(downloader);
            }
        });
    }

    public void openTorrentURI(final String uri, final String relativePath, final ActionListener postPartialDownloadAction) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                BTDownload downloader = new TorrentFetcherDownload(uri, relativePath, postPartialDownloadAction);
                add(downloader);
            }
        });
    }

    public void openTorrentURI(final String uri, final String referrer, final String relativePath, final String hash, final ActionListener postPartialDownloadAction) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                BTDownload downloader = new TorrentFetcherDownload(uri, referrer, relativePath, hash, postPartialDownloadAction);
                add(downloader);
            }
        });
    }

    public void openTorrentFileForSeed(final File torrentFile, final File saveDir) {
        if (!AzureusStarter.isAzureusCoreStarted()) {
            LOG.error("Azureus core not started");
            return;
        }
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                try {
                    BTDownloadCreator creator = new BTDownloadCreator(torrentFile, saveDir, true, null);
                    BTDownload download = creator.createDownload();

                    if (!(download instanceof DuplicateDownload)) {
                        add(download);
                    } else {
                        selectRowByDownload(download);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!e.toString().contains("No files selected by user")) {
                        // could not read torrent file or bad torrent file.
                        GUIMediator.showError(I18n.tr("FrostWire was unable to load the torrent file \"{0}\", - it may be malformed or FrostWire does not have permission to access this file.", torrentFile.getName()), QuestionsHandler.TORRENT_OPEN_FAILURE);
                        //System.out.println("***Error happened from Download Mediator: " +  ioe);
                        //GUIMediator.showMessage("Error was: " + ioe); //FTA: debug
                    }
                }

            }
        });
    }

    protected void selectRowByDownload(BTDownload download) {
        for (int i = 0; i < TABLE.getRowCount(); i++) {
            BTDownloadDataLine btDownloadDataLine = DATA_MODEL.get(i);
            if (download.getHash().equals(btDownloadDataLine.getInitializeObject().getHash())) {
                btDownloadDataLine.getInitializeObject().getSize(true);
                btDownloadDataLine.getInitializeObject().updateDownloadManager(download.getDownloadManager());
                TABLE.setSelectedRow(i);
                return;
            }
        }

    }

    public void openTorrentFile(final File torrentFile, final boolean partialDownload) {
        if (!AzureusStarter.isAzureusCoreStarted()) {
            LOG.error("Azureus core not started");
            return;
        }
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                try {

                    boolean[] filesSelection = null;

                    if (partialDownload) {
                        PartialFilesDialog dlg = new PartialFilesDialog(GUIMediator.getAppFrame(), torrentFile);
                        dlg.setVisible(true);
                        filesSelection = dlg.getFilesSelection();
                        if (filesSelection == null) {
                            return;
                        }
                    }

                    BTDownloadCreator creator = new BTDownloadCreator(torrentFile, null, false, filesSelection);
                    BTDownload download = creator.createDownload();
                    if (!(download instanceof DuplicateDownload)) {
                        add(download);
                    } else {
                        selectRowByDownload(download);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (!e.toString().contains("No files selected by user")) {
                        // could not read torrent file or bad torrent file.
                        GUIMediator.showError(I18n.tr("FrostWire was unable to load the torrent file \"{0}\", - it may be malformed or FrostWire does not have permission to access this file.", torrentFile.getName()), QuestionsHandler.TORRENT_OPEN_FAILURE);
                        //System.out.println("***Error happened from Download Mediator: " +  ioe);
                        //GUIMediator.showMessage("Error was: " + ioe); //FTA: debug
                    }
                }
            }
        });
    }

    public BTDownload[] getSelectedDownloaders() {
        int[] sel = TABLE.getSelectedRows();
        ArrayList<BTDownload> downloaders = new ArrayList<BTDownload>(sel.length);
        for (int i = 0; i < sel.length; i++) {
            BTDownloadDataLine line = DATA_MODEL.get(sel[i]);
            BTDownload downloader = line.getInitializeObject();
            downloaders.add(downloader);
        }
        return downloaders.toArray(new BTDownload[0]);
    }

    public List<BTDownload> getDownloads() {
        int count = TABLE.getRowCount();
        List<BTDownload> downloads = new ArrayList<BTDownload>(count);
        for (int i = 0; i < count; i++) {
            BTDownloadDataLine line = DATA_MODEL.get(i);
            BTDownload downloader = line.getInitializeObject();
            downloads.add(downloader);
        }
        return downloads;
    }

    public long getTotalBytesDownloaded() {
        if (!AzureusStarter.isAzureusCoreStarted()) {
            return 0;
        }
        AzureusCore azureusCore = AzureusStarter.getAzureusCore();
        if (azureusCore == null) {
            return 0;
        }
        return azureusCore.getGlobalManager().getStats().getTotalDataBytesReceived();
    }

    public long getTotalBytesUploaded() {
        if (!AzureusStarter.isAzureusCoreStarted()) {
            return 0;
        }
        AzureusCore azureusCore = AzureusStarter.getAzureusCore();
        if (azureusCore == null) {
            return 0;
        }
        return azureusCore.getGlobalManager().getStats().getTotalDataBytesSent();
    }

    public boolean isClearable(BTDownload initializeObject) {
        int state = initializeObject.getState();
        return state != DownloadManager.STATE_SEEDING && state != DownloadManager.STATE_CHECKING && initializeObject.isCompleted();
    }

    public void removeCompleted() {
        int n = DATA_MODEL.getRowCount();
        for (int i = n - 1; i >= 0; i--) {
            BTDownloadDataLine btDownloadDataLine = DATA_MODEL.get(i);
            BTDownload initializeObject = btDownloadDataLine.getInitializeObject();

            if (isClearable(initializeObject)) {
                DATA_MODEL.remove(i);
            }
        }
    }

    public void stopCompleted() {
        int n = DATA_MODEL.getRowCount();
        for (int i = n - 1; i >= 0; i--) {
            BTDownloadDataLine btDownloadDataLine = DATA_MODEL.get(i);
            BTDownload initializeObject = btDownloadDataLine.getInitializeObject();
            if (initializeObject.isCompleted()) {
                initializeObject.pause();
            }
        }

    }

    public boolean isDownloading(String hash) {
        return DATA_MODEL.isDownloading(hash);
    }

    public void addDownloadManager(DownloadManager mgr) {
        try {
            add(BTDownloadCreator.createDownload(mgr, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load from the last settings saved the previous sorting preferences of this mediator.
     */
    public void restoreSorting() {
        int sortIndex = BittorrentSettings.BTMEDIATOR_COLUMN_SORT_INDEX.getValue();
        boolean sortOrder = BittorrentSettings.BTMEDIATOR_COLUMN_SORT_ORDER.getValue();

        LimeTableColumn column = BTDownloadDataLine.staticGetColumn(sortIndex);

        if (sortIndex != -1 && column != null && TablesHandlerSettings.getVisibility(column.getId(), column.getDefaultVisibility()).getValue()) {
            DATA_MODEL.sort(sortIndex); //ascending

            if (!sortOrder) { //descending
                DATA_MODEL.sort(sortIndex);
            }
        } else {
            DATA_MODEL.sort(BTDownloadDataLine.DATE_CREATED_INDEX);
        }
    }

    public void openYouTubeVideoUrl(final String videoUrl) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                BTDownload downloader = new YouTubeVideoUrlDownload(videoUrl);
                add(downloader);
            }
        });
    }

    public void openSoundcloudTrackUrl(final String trackUrl, final String title, final SoundcloudUISearchResult sr) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                BTDownload downloader = new SoundcloudTrackUrlDownload(trackUrl, title, sr);
                add(downloader);
            }
        });
    }

    public void openYouTubeItem(final FilePackage filePackage) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                try {
                    List<FilePackage> pks = new ArrayList<FilePackage>(DownloadController.getInstance().getPackages());
                    for (FilePackage p : pks) {
                        if (p.getChildren().get(0).getName().equals(filePackage.getChildren().get(0).getName())) {
                            System.out.println("YouTube download duplicated");
                            return;
                        }
                    }
                } catch (Throwable e) {
                    // ignore
                }
                BTDownload downloader = new YouTubeItemDownload(filePackage);
                add(downloader);
            }
        });
    }

    public void openSoundcloudItem(final FilePackage filePackage, final String title, final SoundcloudUISearchResult sr) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                try {
                    List<FilePackage> pks = new ArrayList<FilePackage>(DownloadController.getInstance().getPackages());
                    for (FilePackage p : pks) {
                        if (p.getChildren().get(0).getName().equals(filePackage.getChildren().get(0).getName())) {
                            System.out.println("Soundcloud download duplicated");
                            return;
                        }
                    }
                } catch (Throwable e) {
                    // ignore
                }
                BTDownload downloader = new SoundcloudTrackDownload(filePackage, title, sr);
                add(downloader);
            }
        });
    }

    public PeerHttpUpload upload(FileDescriptor fd) {
        final BTPeerHttpUpload d = new BTPeerHttpUpload(fd);

        GUIMediator.safeInvokeLater(new Runnable() {
            @Override
            public void run() {
                add(d);
            }
        });

        return d.getUpload();
    }

    public void openSlide(final Slide slide) {
        GUIMediator.safeInvokeLater(new Runnable() {
            @Override
            public void run() {
                SlideDownload downloader = new SlideDownload(slide);
                add(downloader);
            }
        });
    }

    public void openHttp(final String httpUrl, final String title, final String saveFileAs, final long fileSize) {
        GUIMediator.safeInvokeLater(new Runnable() {
            @Override
            public void run() {
                HttpDownload downloader = new HttpDownload(httpUrl, title, saveFileAs, fileSize, null, false, true);
                add(downloader);
            }
        });
    }
}