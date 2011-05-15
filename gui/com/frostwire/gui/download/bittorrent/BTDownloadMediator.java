package com.frostwire.gui.download.bittorrent;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloader;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderCallBackInterface;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderFactory;
import org.limewire.i18n.I18nMarker;
import org.limewire.inspection.InspectablePrimitive;
import org.limewire.util.FileUtils;
import org.limewire.util.OSUtils;

import com.aelitis.azureus.core.AzureusCore;
import com.frostwire.CoreFrostWireUtils;
import com.frostwire.GuiFrostWireUtils;
import com.frostwire.HttpFetcher;
import com.frostwire.bittorrent.AzureusStarter;
import com.frostwire.guice.FrostWireCoreModule;
import com.limegroup.bittorrent.gui.TorrentDownloadFactory;
import com.limegroup.bittorrent.gui.TorrentFileFetcher;
import com.limegroup.bittorrent.settings.BittorrentSettings;
import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.Endpoint;
import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.FileDetails;
import com.limegroup.gnutella.FileManager;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.SaveLocationException;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.gui.DialogOption;
import com.limegroup.gnutella.gui.FileChooserHandler;
import com.limegroup.gnutella.gui.FileDetailsProvider;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.MessageService;
import com.limegroup.gnutella.gui.PaddedPanel;
import com.limegroup.gnutella.gui.actions.BitziLookupAction;
import com.limegroup.gnutella.gui.actions.CopyMagnetLinkToClipboardAction;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.actions.SearchAction;
import com.limegroup.gnutella.gui.dnd.FileTransfer;
import com.limegroup.gnutella.gui.dock.DockIcon;
import com.limegroup.gnutella.gui.dock.DockIconFactoryImpl;
import com.limegroup.gnutella.gui.download.DownloaderUtils;
import com.limegroup.gnutella.gui.options.OptionsMediator;
import com.limegroup.gnutella.gui.search.SearchInformation;
import com.limegroup.gnutella.gui.search.SearchMediator;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;
import com.limegroup.gnutella.gui.tables.ColumnPreferenceHandler;
import com.limegroup.gnutella.gui.tables.LimeJTable;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.SimpleColumnListener;
import com.limegroup.gnutella.gui.tables.TableSettings;
import com.limegroup.gnutella.gui.themes.SkinMenu;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;
import com.limegroup.gnutella.gui.themes.SkinPopupMenu;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeSettings;
import com.limegroup.gnutella.gui.util.CoreExceptionHandler;
import com.limegroup.gnutella.gui.util.GUILauncher;
import com.limegroup.gnutella.gui.util.GUILauncher.LaunchableProvider;
import com.limegroup.gnutella.settings.QuestionsHandler;
import com.limegroup.gnutella.settings.SearchSettings;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.util.FrostWireUtils;
import com.limegroup.gnutella.util.QueryUtils;

/**
 * This class acts as a mediator between all of the components of the
 * download window.  It also constructs all of the download window
 * components.
 */
public final class BTDownloadMediator extends AbstractTableMediator<BTDownloadModel, BTDownloadDataLine, BTDownloader> implements FileDetailsProvider {

    private static final Log LOG = LogFactory.getLog(BTDownloadMediator.class);

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
    private Action launchAction;
    private Action resumeAction;
    private Action pauseAction;
    private Action exploreAction;

    /** The actual download buttons instance.
     */
    private BTDownloadButtons _downloadButtons;

    private AzureusCore _azureusCore;

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

        removeAction = new RemoveAction();
        launchAction = new LaunchAction();
        resumeAction = new ResumeAction();
        pauseAction = new PauseAction();
        exploreAction = new ExploreAction();
    }

    /**
     * Returns the most prominent actions that operate on the download table.
     * @return
     */
    public Action[] getActions() {
        Action[] actions;
        if (OSUtils.isWindows() || OSUtils.isMacOSX())
            actions = new Action[] { resumeAction, pauseAction, launchAction, exploreAction, removeAction };
        else
            actions = new Action[] { resumeAction, pauseAction, launchAction, removeAction };

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
    }

    /**
     * Sets up the table headers.
     */
    protected void setupTableHeaders() {
        super.setupTableHeaders();

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
        boolean inactivePresent = ((Boolean) DATA_MODEL.refresh()).booleanValue();

        int[] selRows = TABLE.getSelectedRows();

        if (selRows.length > 0) {
            BTDownloadDataLine dataLine = DATA_MODEL.get(selRows[0]);

            BTDownloader dl = dataLine.getInitializeObject();

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
        return DATA_MODEL.getDownloadsBandwidth();
    }

    public double getUploadsBandwidth() {
        return DATA_MODEL.getUploadsBandwidth();
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
    public void add(BTDownloader downloader) {
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
    public void remove(BTDownloader dloader) {
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
        BTDownloader dl = DATA_MODEL.get(sel[sel.length - 1]).getInitializeObject();
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
            BTDownloader downloader = dd.getInitializeObject();
            downloader.resume();
        }
    }

    /**
     * Handles a double-click event in the table.
     */
    public void handleActionKey() {
        if (launchAction.isEnabled())
            launchSelectedDownloads();
    }

    /**
     * Returns the selected {@link FileDetails}.
     */
    public FileDetails[] getFileDetails() {
        int[] sel = TABLE.getSelectedRows();
        FileManager fmanager = GuiCoreMediator.getFileManager();
        List<FileDetails> list = new ArrayList<FileDetails>(sel.length);
        //        for(int i = 0; i < sel.length; i++) {
        //            URN urn = DATA_MODEL.get(sel[i]).getDownloader().getSha1Urn();
        //			if (urn != null) {
        //				FileDesc fd = fmanager.getFileDescForUrn(urn);
        //				if (fd != null) {
        //				    // DPINJ:  Use passed in LocalFileDetailsFactory
        //					list.add(GuiCoreMediator.getLocalFileDetailsFactory().create(fd));
        //				}
        //				else if (LOG.isDebugEnabled()) {
        //					LOG.debug("not filedesc for urn " + urn);
        //				}
        //			}
        //			else if (LOG.isDebugEnabled()) {
        //				LOG.debug("no urn");
        //			}
        //		}
        return list.toArray(new FileDetails[0]);
    }

    // inherit doc comment
    protected JPopupMenu createPopupMenu() {

        JPopupMenu menu = new SkinPopupMenu();
        menu.add(new SkinMenuItem(removeAction));
        menu.add(new SkinMenuItem(resumeAction));
        menu.add(new SkinMenuItem(pauseAction));
        menu.add(new SkinMenuItem(launchAction));
        if (OSUtils.isWindows() || OSUtils.isMacOSX())
            menu.add(new SkinMenuItem(exploreAction));
        menu.addSeparator();
        //menu.add(createSearchMenu());
        //		menu.addSeparator();
        //		menu.add(createAdvancedSubMenu());

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

        if (dataLine.isCompleted()) {
            removeAction.putValue(Action.NAME, I18n.tr("Clear Download"));
            removeAction.putValue(LimeAction.SHORT_NAME, I18n.tr("Clear"));
            removeAction.putValue(Action.SHORT_DESCRIPTION, I18n.tr("Clear Selected Downloads"));
            launchAction.putValue(Action.NAME, I18n.tr("Launch Download"));
            launchAction.putValue(LimeAction.SHORT_NAME, I18n.tr("Launch"));
            launchAction.putValue(Action.SHORT_DESCRIPTION, I18n.tr("Launch Selected Downloads"));
            exploreAction.setEnabled(TABLE.getSelectedRowCount() == 1);
        } else {
            removeAction.putValue(Action.NAME, I18n.tr("Cancel Download"));
            removeAction.putValue(LimeAction.SHORT_NAME, I18n.tr("Cancel"));
            removeAction.putValue(Action.SHORT_DESCRIPTION, I18n.tr("Cancel Selected Downloads"));
            launchAction.putValue(Action.NAME, I18n.tr("Preview Download"));
            launchAction.putValue(LimeAction.SHORT_NAME, I18n.tr("Preview"));
            launchAction.putValue(Action.SHORT_DESCRIPTION, I18n.tr("Preview Selected Downloads"));
            exploreAction.setEnabled(false);
        }

        removeAction.setEnabled(true);
        resumeAction.setEnabled(resumable);
        pauseAction.setEnabled(pausable);
        //launchAction.setEnabled(dl.isLaunchable());
    }

    /**
     * Handles the deselection of all rows in the download table,
     * disabling all necessary buttons and menu items.
     */
    public void handleNoSelection() {
        removeAction.setEnabled(false);
        resumeAction.setEnabled(false);
        launchAction.setEnabled(false);
        pauseAction.setEnabled(false);
        exploreAction.setEnabled(false);
    }

    private abstract class RefreshingAction extends AbstractAction {
        /**
         * 
         */
        private static final long serialVersionUID = -937688457597255711L;

        public final void actionPerformed(ActionEvent e) {
            performAction(e);
            doRefresh();
        }

        protected abstract void performAction(ActionEvent e);
    }

    private class RemoveAction extends RefreshingAction {

        /**
         * 
         */
        private static final long serialVersionUID = -1742554445891016991L;

        public RemoveAction() {
            putValue(Action.NAME, I18n.tr("Cancel Download"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Cancel"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Cancel Selected Downloads"));
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_KILL");
        }

        public void performAction(ActionEvent e) {
            removeSelection();
        }
    }

    private class LaunchAction extends RefreshingAction {

        /**
         * 
         */
        private static final long serialVersionUID = -567893064454697074L;

        public LaunchAction() {
            putValue(Action.NAME, I18n.tr("Preview Download"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Preview"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Preview Selected Downloads"));
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_LAUNCH");
        }

        public void performAction(ActionEvent e) {
            launchSelectedDownloads();
        }
    }

    private class ResumeAction extends RefreshingAction {

        /**
         * 
         */
        private static final long serialVersionUID = -4449981369424872994L;

        public ResumeAction() {
            putValue(Action.NAME, I18n.tr("Resume Download"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Resume"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Reattempt Selected Downloads"));
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_FILE_MORE_SOURCES");
        }

        public void performAction(ActionEvent e) {
            resumeSelectedDownloads();
        }
    }

    private class PauseAction extends RefreshingAction {

        /**
         * 
         */
        private static final long serialVersionUID = 4682149704934484393L;

        public PauseAction() {
            putValue(Action.NAME, I18n.tr("Pause Download"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Pause"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Pause Selected Downloads"));
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_PAUSE");
        }

        public void performAction(ActionEvent e) {
            pauseSelectedDownloads();
        }
    }

    private class ExploreAction extends RefreshingAction {
        /**
         * 
         */
        private static final long serialVersionUID = -4648558721588938475L;

        public ExploreAction() {
            putValue(Action.NAME, I18n.tr("Explore"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Explore"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Open Folder Containing the File"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_EXPLORE");
        }

        public void performAction(ActionEvent e) {
            launchExplorer();
        }
    }

    public void openTorrentURI(URI uri) {
        TorrentDownloader downloader = TorrentDownloaderFactory.create(new TorrentDownloaderCallBackInterface() {
            public void TorrentDownloaderEvent(int state, TorrentDownloader inf) {
                if (state == TorrentDownloader.STATE_FINISHED) {
                    openTorrent(inf.getFile());
                } else if (state == TorrentDownloader.STATE_ERROR) {
                    // Error
                }
            }
        }, uri.toString(), null, SharingSettings.getFileSettingForMediaType(MediaType.TYPE_TORRENTS).getValue().getAbsolutePath());

        downloader.start();
    }

    public void openTorrent(File file) {
        try {
            BTDownloaderFactory factory = new BTDownloaderFactory(AzureusStarter.getAzureusCore().getGlobalManager(), file);
            BTDownloader downloader = BTDownloaderUtils.createDownloader(factory);
            
            add(downloader);

        } catch (Exception ioe) {
            ioe.printStackTrace();
            if (!ioe.toString().contains("No files selected by user")) {
                // could not read torrent file or bad torrent file.
                GUIMediator
                        .showError(
                                I18n.tr("FrostWire was unable to load the torrent file \"{0}\", - it may be malformed or FrostWire does not have permission to access this file.",
                                        file.getName()), QuestionsHandler.TORRENT_OPEN_FAILURE);
                //System.out.println("***Error happened from Download Mediator: " +  ioe);
                //GUIMediator.showMessage("Error was: " + ioe); //FTA: debug
            }
        }
    }
}
