package com.limegroup.gnutella.gui.upload;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPopupMenu;
import javax.swing.table.TableCellRenderer;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.limewire.io.ConnectableImpl;
import org.limewire.util.FileUtils;

import com.aelitis.azureus.core.AzureusCore;
import com.frostwire.bittorrent.AzureusStarter;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.limegroup.bittorrent.BTDownloaderImpl;
import com.limegroup.bittorrent.BTMetaInfo;
import com.limegroup.gnutella.PushEndpoint;
import com.limegroup.gnutella.UploadServicesImpl;
import com.limegroup.gnutella.Uploader;
import com.limegroup.gnutella.downloader.CoreDownloaderFactory;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.PaddedPanel;
import com.limegroup.gnutella.gui.dnd.DNDUtils;
import com.limegroup.gnutella.gui.search.SearchMediator;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;
import com.limegroup.gnutella.gui.tables.LimeJTable;
import com.limegroup.gnutella.gui.tables.ProgressBarHolder;
import com.limegroup.gnutella.gui.tables.ProgressBarRenderer;
import com.limegroup.gnutella.gui.tables.TableSettings;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.uploader.HTTPUploader;

/**
 * This class acts as a mediator between all of the components of the
 * upload window.  It also constructs all of the upload window
 * components.
 */
public final class UploadMediator extends AbstractTableMediator<UploadModel, UploadDataLine, Uploader> {
    private boolean _seedingHostilesTxt = false;
    
	/**
	 * Variable for the total number of uploads that have been added in this
	 * session.
	 */
	private static int _totalUploads = 0;

	/**
	 * A progress bar renderer specific for the uploads table.
	 */
	private static ProgressBarRenderer PROGRESS_BAR_RENDERER;

	/**
	 * Variables so we only need one listener for both ButtonRow & PopupMenu
	 */
	ActionListener CHAT_LISTENER;
	ActionListener CLEAR_LISTENER;
	ActionListener BROWSE_LISTENER;

	private CoreDownloaderFactory _coreDownloaderFactory;

	private static final String UPLOAD_TITLE =
	    I18n.tr("Uploads");
    private static final String ACTIVE = 
        I18n.tr("Active");
    private static final String QUEUED =
        I18n.tr("Queued");

    /**
     * instance, for singleton acces
     */
    private static UploadMediator _instance;


    /**
     * Variable for whether or not chat is enabled for the selected host.
     */
    private static boolean _chatEnabled;

    /**
     * Variable for whether or not browse host is enabled for the selected 
     * host.
     */
    private static boolean _browseEnabled;
    
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
	 * Build some extra listeners
	 */
	protected void buildListeners() {
	    super.buildListeners();
	    CHAT_LISTENER = new ChatListener(this);
	    CLEAR_LISTENER = new ClearListener(this);
	    BROWSE_LISTENER = new BrowseListener(this);
	}

	/**
	 * Set us up the constants
	 */
	protected void setupConstants() {
		MAIN_PANEL = new PaddedPanel(UPLOAD_TITLE);
		DATA_MODEL = new UploadModel();
		TABLE = new LimeJTable(DATA_MODEL);
		BUTTON_ROW = (new UploadButtons(this)).getComponent();
    }
	
	@Override
	protected void setupDragAndDrop() {
		TABLE.setTransferHandler(DNDUtils.DEFAULT_TRANSFER_HANDLER);
	}

	/**
	 * Use our custom progress bar renderer
	 */
	protected void setDefaultRenderers() {
		super.setDefaultRenderers();
		TABLE.setDefaultRenderer(ProgressBarHolder.class, getProgressBarRenderer());
	}
	
	/**
	 * Update the splash screen
	 */
	protected void updateSplashScreen() {
		GUIMediator.setSplashScreenString(
            I18n.tr("Loading Upload Window..."));
    }

	/**
	 * Constructs all of the elements of the upload window, including
	 * the table, the buttons, etc.
	 */
	@Inject
	public UploadMediator() {
	    super("UPLOAD_MEDIATOR");
	    _coreDownloaderFactory = GuiCoreMediator.getCore().getCoreDownloaderFactory();
	    
	    GUIMediator.addRefreshListener(this);
	    ThemeMediator.addThemeObserver(this);

	    restoreSeedingTorrents();
	}

	private void restoreSeedingTorrents() {
	    AzureusCore azureusCore = AzureusStarter.getAzureusCore();
	    @SuppressWarnings("unchecked")
		List<DownloadManager> downloadManagers = (List<DownloadManager>) azureusCore.getGlobalManager().getDownloadManagers();
	    
	    if (downloadManagers.size() > 0) {
	    	
		    for (DownloadManager dlManager : downloadManagers) {
		    	BTMetaInfo info = null;
				try {
					
					if (dlManager.getState() != 70 &&
					    dlManager.getState() != 75 ) {
						continue;
					}
					
			    	byte [] b = FileUtils.readFileFully(new File(dlManager.getTorrentFileName()));
					info = BTMetaInfo.readFromBytes(b);
					
					BTDownloaderImpl btDownloader = (BTDownloaderImpl) _coreDownloaderFactory.createBTDownloader(info);
			    	btDownloader.initBtMetaInfo(info);
			    	
			    	add(btDownloader.createUploader());
			    	
				} catch (IOException e) {
					e.printStackTrace();					
				}
		    }
	    }
	}

	/**
	 * Override the default refresh so we can set the clear button.
	 */
	public void doRefresh() {
	    boolean inactivePresent =
	        ((Boolean)DATA_MODEL.refresh()).booleanValue();
	    setButtonEnabled(UploadButtons.CLEAR_BUTTON, inactivePresent);
	    
	    int numUploads = GuiCoreMediator.getUploadServices().getNumUploads();
	    
	    if (_seedingHostilesTxt) numUploads--;
	    
	    MAIN_PANEL.setTitle(UPLOAD_TITLE + " (" +
	        numUploads + " " +
	        ACTIVE + ", " +
			GuiCoreMediator.getUploadServices().getNumQueuedUploads() + " " +
	        QUEUED + ")");
	}

	/**
	 * Returns the total number of Uploads that have occurred in this session.
	 *
	 * @return the total number of Uploads that have occurred in this session
	 */
    public int getTotalUploads() {
        return _totalUploads;
    }

 	/**
	 * Returns the total number of current Uploads.
	 *
	 * @return the total number of current Uploads
	 */
    public int getCurrentUploads() {
        return DATA_MODEL.getCurrentUploads();
    }

 	/**
	 * Returns the total number of active Uploads.
     * This includes anything that is still viewable in the Uploads view.
	 *
	 * @return the total number of active Uploads
	 */
    public int getActiveUploads() {
        return DATA_MODEL.getRowCount();
    }
    
    /**
     * Returns the aggregate amount of bandwidth being consumed by active uploads.
     *  
     * @return the total amount of bandwidth being consumed by active uploads.
     */
    public double getActiveUploadsBandwidth() {
        return DATA_MODEL.getActiveUploadsBandwidth();
    }

    /**
     * Override the default add.
     *
	 * Adds a new Uploads to the list of Uploads, obtaining the necessary
	 * information from the supplied <tt>Uploader</tt>.
	 *
	 * If the upload is not already in the list, then it is added.
     *  <p>
     * With HTTP1.1 support, swarm downloads, and chunking, it becomes
     * important that the GUI should not get updated whenever a little
     * chunk of a file is uploaded.
	 */
    public void add(Uploader uploader) {
        //don't show system update on upload tab
        if (uploader.getFileName().startsWith("hostiles.txt.")) {
            _seedingHostilesTxt = true;
            UploadServicesImpl.IS_SEEDING_HOSTILES_TXT = true;
            //System.out.println("UploadMediator.add() - Skipping "+uploader.getFileName()+" - no need to show");
            return;
        }
        
        if ( !DATA_MODEL.contains(uploader) ) {
            //attempt to update an existing uploader
            int idx = DATA_MODEL.update(uploader);
            
            if ( idx == -1 ) {
                //if we couldn't find one to update, add it as new
                _totalUploads++;
                super.add(uploader);
    	    }
        }
    }

	/**
	 * Override the default remove
	 *
	 * Removes a upload from the list if the user has configured their system
	 * to automatically clear completed upload and if the upload is
	 * complete.
	 *
	 * @param uploader the <tt>Uploader</tt> to remove from the list if it is
	 *  complete.
	 */
    public void remove(Uploader uploader) {
		if (SharingSettings.CLEAR_UPLOAD.getValue() &&
            uploader.isInactive()) {
            // This is called when the upload is finished, either because
            // the user clicked 'Kill', something was interupted, etc..
            // It doesn't matter that we always setPersistConnect(true),
            // because if the upload was already killed, the sockets
            // are already closed.
            // The flow of a manually killed upload goes like this:
            //  RemoveListener.actionPerformed() ->
            //  UploadMediator.removeSelection() ->
            //  (for each row selected)...
            //    UploadDataLine.cleanup()
            //    (core notices socket closed, marks interupted)
            //    VisualConnectionCallBack.removeUploader(Uploader) ->
            //    UploadMediator.remove(uploader)
            //    (if the user has clear completed checked)...
            //      UploadDataLine.setPersistConnection(true)
            //      AbstractTableMediator.removeRow( uploader's row )
            // A remotely-terminated download follows the same path,
            // but starts at the 'core notices socket closed'.
            int i = DATA_MODEL.getRow(uploader);
            if( i != -1 ) {
                // tell the DataLine that we don't want to clean up.
                // necessary for chunked transfers.
                DATA_MODEL.get(i).setPersistConnection(true);
                super.removeRow(i);
            }
		} else {
		    //if we're not removing it, note the time at which it ended.
		    UploadDataLine udl = DATA_MODEL.get(uploader);
		    if (udl != null) udl.setEndTime( System.currentTimeMillis() );
	    }
    }

    /**
     * Override the default remove to not actually remove,
     * but instead just call 'cleanup'.
     * If the user has 'Clear Completed Uploads' checked,
     * they'll be removed.  Otherwise, they'll show as interupted.
     */
    public void removeSelection() {
		int[] sel = TABLE.getSelectedRows();
		Arrays.sort(sel);
		for( int counter = sel.length - 1; counter >= 0; counter--) {
			int i = sel[counter];
			DATA_MODEL.get(i).cleanup();
		}
    }

	/**
	 * Opens up a chat session with the selected hosts in the upload
	 * window.
	 */
	void chatWithSelectedUploads() {
		int[] sel = TABLE.getSelectedRows();
		for (int i =0; i<sel.length; i++) {
            Uploader uploader = DATA_MODEL.get(sel[i]).getInitializeObject();
			if (uploader.isChatEnabled() ) {
			    String host = uploader.getHost();
				int port = uploader.getGnutellaPort();
				GUIMediator.createChat(host, port);
			}
		}
	}

	/**
	 * Browses all selected hosts (only once per host)
	 * Moves display to the search window if a browse host was triggered
	 */
	void browseWithSelectedUploads() {
	    boolean found = false;
	    int[] sel = TABLE.getSelectedRows();
	    Set<String> searched = new HashSet<String>( sel.length );
	    for( int i = 0; i < sel.length; i++ ) {
            Uploader uploader = DATA_MODEL.get(sel[i]).getInitializeObject();
	        if ( uploader.isBrowseHostEnabled() ) {
                String host = uploader.getHost();
                if (host == null) {
                    // host is null for firewalled downloader
                    // TODO come up with a clean desing in the new UI where we don't have to know about the uploader
                    // see LWC-834 for possible solutions
                    if (uploader instanceof HTTPUploader) {
                        PushEndpoint pushEndpoint = ((HTTPUploader)uploader).getPushEndpoint();
                        if (!searched.contains(pushEndpoint.httpStringValue())) {
                            SearchMediator.doBrowseHost(pushEndpoint);
                            searched.add(pushEndpoint.httpStringValue());
                            found = true;
                        }
                    }
                } else if ( !searched.contains(host)) {
	                SearchMediator.doBrowseHost(new ConnectableImpl(uploader), null );
	                searched.add( host );
	                found = true;
	            }
	        }
	    }
	    if ( found ) GUIMediator.instance().setWindow(GUIMediator.Tabs.SEARCH);
	}

	/**
	 * Don't do anything on a double click.
	 */
	public void handleActionKey() { }

	/**
	 * Clears the uploads in the upload window that have completed.
	 */
	void clearCompletedUploads() {
		DATA_MODEL.clearCompleted();
		clearSelection();
        setButtonEnabled(UploadButtons.CLEAR_BUTTON, false);
	}

    // inherit doc comment
    protected JPopupMenu createPopupMenu() {
        JPopupMenu menu = (new UploadPopupMenu(this)).getComponent();
		menu.getComponent(UploadPopupMenu.KILL_INDEX).
            setEnabled(!TABLE.getSelectionModel().isSelectionEmpty());
		menu.getComponent(UploadPopupMenu.CHAT_INDEX).
            setEnabled(_chatEnabled);
		menu.getComponent(UploadPopupMenu.BROWSE_INDEX).
            setEnabled(_browseEnabled);   
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

		UploadDataLine dataLine = DATA_MODEL.get(row);
		_chatEnabled = dataLine.isChatEnabled();
		_browseEnabled = dataLine.isBrowseEnabled();

		setButtonEnabled(UploadButtons.KILL_BUTTON, 
                         !TABLE.getSelectionModel().isSelectionEmpty());
		setButtonEnabled(UploadButtons.BROWSE_BUTTON, _browseEnabled);
	}

	/**
	 * Handles the deselection of all rows in the upload table,
	 * disabling all necessary buttons and menu items.
	 */
	public void handleNoSelection() {
		_chatEnabled = false;
		_browseEnabled = false;
		setButtonEnabled(UploadButtons.KILL_BUTTON, false);
		setButtonEnabled(UploadButtons.BROWSE_BUTTON, false);
	}
	
	@Override
	protected TableCellRenderer getProgressBarRenderer() {
	    if (PROGRESS_BAR_RENDERER == null) {
	        PROGRESS_BAR_RENDERER = new UploadProgressBarRenderer();
	    }
	    return PROGRESS_BAR_RENDERER;
	}
	
	public static UploadMediator instance() {
		if (_instance == null) {
			_instance = new UploadMediator();
		}
		return _instance;
	}
}
