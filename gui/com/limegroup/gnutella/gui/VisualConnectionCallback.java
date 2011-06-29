package com.limegroup.gnutella.gui;

import java.io.File;

import javax.swing.SwingUtilities;

import org.gudy.azureus2.core3.download.DownloadManager;

import com.frostwire.gui.bittorrent.BTDownload;
import com.limegroup.gnutella.ActivityCallback;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.browser.MagnetOptions;
import com.limegroup.gnutella.gui.search.SearchInformation;
import com.limegroup.gnutella.gui.search.SearchMediator;
import com.limegroup.gnutella.util.QueryUtils;
import com.limegroup.gnutella.version.UpdateInformation;

/**
 * This class is the gateway from the backend to the frontend.  It
 * delegates all callbacks to the appropriate frontend classes, and it
 * also handles putting calls onto the Swing thread as necessary.
 * 
 * It implements the <tt>ActivityCallback</tt> callback interface, designed
 * to make it easy to swap UIs.
 */
public final class VisualConnectionCallback implements ActivityCallback {
    
    private static VisualConnectionCallback INSTANCE;
    
    public static VisualConnectionCallback instance() {
        if (INSTANCE == null) {
            INSTANCE = new VisualConnectionCallback();
        }
        return INSTANCE;
    }
    
    private VisualConnectionCallback() {
        
    }
	
	
	///////////////////////////////////////////////////////////////////////////
	//  Files-related callbacks
	///////////////////////////////////////////////////////////////////////////
	
    /** 
	 * This method notifies the frontend that the data for the 
	 * specified shared <tt>File</tt> instance has been 
	 * updated.
	 *
	 * @param file the <tt>File</tt> instance for the shared file whose
	 *  data has been updated
	 */
    public void handleSharedFileUpdate(final File file) {
        /**
         * NOTE: Pass this off directly to the library
         * so it can discard the update if the directory
         * of the file isn't selected.
         * This reduces the amount of Runnables created
         * by a very large amount.
         */
         mf().getLibraryMediator().updateSharedFile(file);
    }
    
    public void fileManagerLoading() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                mf().getLibraryMediator().clearLibrary();
            }
        });
    }
    

	///////////////////////////////////////////////////////////////////////////
	//  Download-related callbacks
	///////////////////////////////////////////////////////////////////////////
    
    public void addDownload(BTDownload mgr) {
        Runnable doWorkRunnable = new AddDownload(mgr);
        
        SwingUtilities.invokeLater(doWorkRunnable);
    }
    
    public void downloadsComplete() {
        Finalizer.setDownloadsComplete();
    }

	/**
	 *  Show active downloads
	 */
	public void showDownloads() {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
		        GUIMediator.instance().setWindow(GUIMediator.Tabs.SEARCH);	
            }
        });
	}	
    
    private class AddDownload implements Runnable {
        private BTDownload mgr;
        public AddDownload(BTDownload mgr) {
            this.mgr = mgr;
        }
        public void run() {
            mf().getBTDownloadMediator().add(mgr);
		}
    }
    
    private class AddDownloadManager implements Runnable {
        private DownloadManager mgr;
        public AddDownloadManager(DownloadManager mgr) {
            this.mgr = mgr;
        }
        public void run() {
            mf().getBTDownloadMediator().addDownloadManager(mgr);
        }
    }

	
	///////////////////////////////////////////////////////////////////////////
	//  Upload-related callbacks
	///////////////////////////////////////////////////////////////////////////
    
    public void uploadsComplete() {
        Finalizer.setUploadsComplete();
    }
    	
	///////////////////////////////////////////////////////////////////////////
	//  Other stuff
	///////////////////////////////////////////////////////////////////////////
	
    /**
     * Notification that the address has changed.
     */
    public void handleAddressStateChanged() {
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                // don't touch GUI code if it isn't constructed.
//                // this is necessary here only because addressStateChanged
//                // is triggered by Acceptor, which is init'd prior to the
//                // GUI actually existing.
//                if (GUIMediator.isConstructed())
//                    SearchMediator.addressChanged();
//            }
//        });
    }
    
    public void handleNoInternetConnection() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (GUIMediator.isConstructed())
                    GUIMediator.disconnected();
            }
        });
    }

	/**
     * Notification that a new update is available.
     */
    public void updateAvailable(UpdateInformation update) {
        GUIMediator.instance().showUpdateNotification(update);
    }

	/**
	 *  Tell the GUI to deiconify.
	 */  
	public void restoreApplication() {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
		        GUIMediator.restoreView();
            }
        }); 
	}
	
	/**
	 * Notification of a component loading.
	 */
	public void componentLoading(final String component) {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
                GUIMediator.setSplashScreenString(
                    I18n.tr(component));
            }
        });
    }       
	
	/**
	 * Indicates that the firewalled state of this has changed. 
	 */
	public void acceptedIncomingChanged(final boolean status) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIMediator.instance().getStatusLine().updateFirewallLabel(status);
			}
		});
	}
    
    /**
     * Returns the MainFrame.
     */
    private MainFrame mf() {
        return GUIMediator.instance().getMainFrame();
    }

	/**
	 * Returns true since we want to kick off the magnet downloads ourselves.
	 */
	public boolean handleMagnets(final MagnetOptions[] magnets) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				boolean oneSearchStarted = false;
				for (int i = 0; i < magnets.length; i++) {
					// spawn search for keyword only magnet
					if (magnets[i].isKeywordTopicOnly() && !oneSearchStarted) {
						String query = QueryUtils.createQueryString
							(magnets[i].getKeywordTopic());
						SearchInformation info = 
							SearchInformation.createKeywordSearch
							(query, null, MediaType.getAnyTypeMediaType());
						if (SearchMediator.validateInfo(info) 
							== SearchMediator.QUERY_VALID) {
							oneSearchStarted = true;
							SearchMediator.triggerSearch(info);
						}
					}
					else {
						//DownloaderUtils.createDownloader(magnets[i]);
					}
				}
				if (magnets.length > 0) {
					GUIMediator.instance().setWindow(GUIMediator.Tabs.SEARCH);
				}
			}
		});
		return true;
	}
	
	public void handleTorrent(final File torrentFile) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIMediator.instance().openTorrentFile(torrentFile);
			}
		});
	}

	public void handleTorrentMagnet(String request, boolean partialDownload) {
		GUIMediator.instance().openTorrentURI(request, partialDownload);
	}

    public void addDownloadManager(DownloadManager dm) {
        Runnable doWorkRunnable = new AddDownloadManager(dm);
        
        SwingUtilities.invokeLater(doWorkRunnable);
    }
    
    public boolean isRemoteDownloadsAllowed() {
        return GUIMediator.instance().isRemoteDownloadsAllowed();
    }
}