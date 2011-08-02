package com.frostwire.gui.bittorrent;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.limewire.util.OSUtils;

import com.aelitis.azureus.core.AzureusCore;
import com.frostwire.AzureusStarter;
import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.PaddedPanel;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.dnd.FileTransfer;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;
import com.limegroup.gnutella.gui.tables.LimeJTable;
import com.limegroup.gnutella.gui.tables.TableSettings;
import com.limegroup.gnutella.gui.themes.SkinMenu;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;
import com.limegroup.gnutella.gui.themes.SkinPopupMenu;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.settings.ApplicationSettings;
import com.limegroup.gnutella.settings.QuestionsHandler;

/**
 * This class acts as a mediator between all of the components of the
 * download window.  It also constructs all of the download window
 * components.
 */
public final class BTDownloadMediator extends AbstractTableMediator<BTDownloadModel, BTDownloadDataLine, BTDownload> {

    /**
     * instance, for singleton access
     */
    private static BTDownloadMediator INSTANCE;

    public static BTDownloadMediator instance() {

        AzureusStarter.start();

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
    private Action resumeAction;
    private Action pauseAction;
    private Action exploreAction;
    private Action _copyMagnetAction;
    private Action _copyHashAction;
    private Action _shareTorrentAction;

    /** The actual download buttons instance.
     */
    private BTDownloadButtons _downloadButtons;

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

        removeAction = BTDownloadActions.REMOVE_ACTION;
        resumeAction = BTDownloadActions.RESUME_ACTION;
        pauseAction = BTDownloadActions.PAUSE_ACTION;
        exploreAction = BTDownloadActions.EXPLORE_ACTION;
        _copyMagnetAction = BTDownloadActions.COPY_MAGNET_ACTION;
        _copyHashAction = BTDownloadActions.COPY_HASH_ACTION;
        _shareTorrentAction = BTDownloadActions.SHARE_TORRENT_ACTION;
    }

    /**
     * Returns the most prominent actions that operate on the download table.
     * @return
     */
    public Action[] getActions() {
        Action[] actions;
        if (OSUtils.isWindows() || OSUtils.isMacOSX())
            actions = new Action[] { resumeAction, pauseAction, exploreAction, removeAction };
        else
            actions = new Action[] { resumeAction, pauseAction, removeAction };

        return actions;
    }

    /**
     * Set up the necessary constants.
     */
    protected void setupConstants() {
        MAIN_PANEL = new PaddedPanel(I18n.tr("Transfers"));
        DATA_MODEL = new BTDownloadModel();
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
	class SeedingFilter extends RowFilter<BTDownloadModel, Integer> {
		@Override
		public boolean include(
				javax.swing.RowFilter.Entry<? extends BTDownloadModel, ? extends Integer> rowFilterEntry) {

			if (rowFilterEntry == null || rowFilterEntry.getModel()==null) {
				return false;
			}
			
			BTDownloadDataLine dataline = rowFilterEntry.getModel().getDataline(rowFilterEntry.getIdentifier());
			return !dataline.isSeeding();
		}
	}


    public void updateTableFilters() {
    	if (TABLE == null || DATA_MODEL == null) {
    		return;
    	}
    	
		//show seeds
		if (ApplicationSettings.SHOW_SEEDING_TRANSFERS.getValue()) {
			TABLE.setRowSorter(null);
		} 
		// don't show seeds
		else {
			TableRowSorter<BTDownloadModel> sorter = new TableRowSorter<BTDownloadModel>();
			sorter.setRowFilter(new SeedingFilter());
			sorter.setModel(DATA_MODEL);
			TABLE.setRowSorter(sorter);
		}		
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
        ThemeMediator.addThemeObserver(this);
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
     * Returns the set of filenames of all downloads
     * This includes anything that is still viewable in the Downloads view.
     *
     * @return Set of filenames (String) of all downloads
     */

    //    public Set<String> getFileNames() {
    //    	Set<String> names = new HashSet<String>();
    //    	for(int c = 0;c < DATA_MODEL.getRowCount(); c++) {
    //    	    names.add(DATA_MODEL.get(c).getFileName());
    //        }
    //    	return names;
    //    }

    public double getDownloadsBandwidth() {
        return DATA_MODEL.getDownloadsBandwidth() / 1000;
    }

    public double getUploadsBandwidth() {
        return DATA_MODEL.getUploadsBandwidth() / 1000;
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
            super.add(downloader);
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
        if (exploreAction.isEnabled())
            exploreAction.actionPerformed(null);
    }

    protected JPopupMenu createPopupMenu() {

        JPopupMenu menu = new SkinPopupMenu();

        menu.add(new SkinMenuItem(resumeAction));
        menu.add(new SkinMenuItem(pauseAction));
        if (OSUtils.isWindows() || OSUtils.isMacOSX()) {
            menu.add(new SkinMenuItem(exploreAction));
        }
        
        menu.addSeparator();
        menu.add(new SkinMenuItem(_shareTorrentAction));
        menu.add(new SkinMenuItem(_copyMagnetAction));
        menu.add(new SkinMenuItem(_copyHashAction));
        menu.addSeparator();
        menu.add(new SkinMenuItem(removeAction));
        menu.add(new SkinMenuItem(BTDownloadActions.REMOVE_TORRENT_ACTION));
        menu.add(new SkinMenuItem(BTDownloadActions.REMOVE_TORRENT_AND_DATA_ACTION));

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

        removeAction.putValue(Action.NAME, I18n.tr("Cancel Download"));
        removeAction.putValue(LimeAction.SHORT_NAME, I18n.tr("Cancel"));
        removeAction.putValue(Action.SHORT_DESCRIPTION, I18n.tr("Cancel Selected Downloads"));
        exploreAction.setEnabled(false);

        removeAction.setEnabled(true);
        resumeAction.setEnabled(resumable);
        pauseAction.setEnabled(pausable);
        _copyMagnetAction.setEnabled(true);
        _copyHashAction.setEnabled(true);
        
		_shareTorrentAction.setEnabled(getSelectedDownloaders().length == 1
				&& dataLine.getInitializeObject().isPausable());
    }

    /**
     * Handles the deselection of all rows in the download table,
     * disabling all necessary buttons and menu items.
     */
    public void handleNoSelection() {
        removeAction.setEnabled(false);
        resumeAction.setEnabled(false);
        pauseAction.setEnabled(false);
        exploreAction.setEnabled(false);
        _copyMagnetAction.setEnabled(false);
        _copyHashAction.setEnabled(false);
        _shareTorrentAction.setEnabled(false);
    }

    public void openTorrentSearchResult(final WebSearchResult webSearchResult, final boolean partialDownload, final ActionListener postPartialDownloadAction) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                BTDownload downloader = new TorrentFetcherDownload(webSearchResult.getTorrentURI(), webSearchResult.getFilenameNoExtension(), webSearchResult
                        .getHash(), webSearchResult.getSize(), partialDownload, postPartialDownloadAction);
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

    public void openTorrentFileForSeed(final File torrentFile, final File saveDir) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                try {
                    BTDownloadCreator creator = new BTDownloadCreator(torrentFile, saveDir, true, null);
                    if (!creator.isTorrentInGlobalManager()) {
                        BTDownload download = creator.createDownload();
                        add(download);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!e.toString().contains("No files selected by user")) {
                        // could not read torrent file or bad torrent file.
                        GUIMediator.showError(
                                I18n.tr("FrostWire was unable to load the torrent file \"{0}\", - it may be malformed or FrostWire does not have permission to access this file.",
                                        torrentFile.getName()), QuestionsHandler.TORRENT_OPEN_FAILURE);
                        //System.out.println("***Error happened from Download Mediator: " +  ioe);
                        //GUIMediator.showMessage("Error was: " + ioe); //FTA: debug
                    }
                }

            }
        });
    }

    public void openTorrentFile(final File torrentFile, final boolean partialDownload) {
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
                    if (!creator.isTorrentInGlobalManager()) {
                        BTDownload download = creator.createDownload();
                        add(download);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (!e.toString().contains("No files selected by user")) {
                        // could not read torrent file or bad torrent file.
                        GUIMediator.showError(
                                I18n.tr("FrostWire was unable to load the torrent file \"{0}\", - it may be malformed or FrostWire does not have permission to access this file.",
                                        torrentFile.getName()), QuestionsHandler.TORRENT_OPEN_FAILURE);
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

    public long getTotalBytesDownloaded() {
        AzureusCore azureusCore = AzureusStarter.getAzureusCore();
        if (azureusCore == null)
            return 0;
        return azureusCore.getGlobalManager().getStats().getTotalDataBytesReceived();
    }

    public long getTotalBytesUploaded() {
        AzureusCore azureusCore = AzureusStarter.getAzureusCore();
        if (azureusCore == null)
            return 0;
        return azureusCore.getGlobalManager().getStats().getTotalDataBytesSent();
    }

    //
    //	public void removeCompleted() {
    //		int n = DATA_MODEL.getRowCount();
    //		for (int i=n-1; i >= 0; i--) {
    //			BTDownloadDataLine btDownloadDataLine = DATA_MODEL.get(i);
    //			BTDownloader initializeObject = btDownloadDataLine.getInitializeObject();
    //			if (initializeObject.isCompleted()) {
    //				DATA_MODEL.remove(i);
    //			}			
    //		}		
    //	}
    //	
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
            add(BTDownloadCreator.createDownload(mgr));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
