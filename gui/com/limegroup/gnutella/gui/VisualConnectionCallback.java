/*
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

package com.limegroup.gnutella.gui;

import java.io.File;

import javax.swing.SwingUtilities;

import org.gudy.azureus2.core3.download.DownloadManager;

import com.frostwire.AzureusStarter;
import com.frostwire.gui.bittorrent.BTDownload;
import com.limegroup.gnutella.ActivityCallback;
import com.limegroup.gnutella.MagnetOptions;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.UpdateInformation;
import com.limegroup.gnutella.gui.search.SearchInformation;
import com.limegroup.gnutella.gui.search.SearchMediator;
import com.limegroup.gnutella.util.QueryUtils;

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
    
    private class AddDownloadManager implements Runnable {
        private DownloadManager mgr;
        public AddDownloadManager(DownloadManager mgr) {
            this.mgr = mgr;
        }
        public void run() {
            mf().getBTDownloadMediator().addDownloadManager(mgr);
        }
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
     * Returns the MainFrame.
     */
    private MainFrame mf() {
        return GUIMediator.instance().getMainFrame();
    }

	
	public void handleTorrent(final File torrentFile) {
	    new AzureusStarter.AzureusCoreWaiter("VisualConnectionCallback::handleTorrent()") {
            @Override
            public void onAzureusCoreStarted() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        GUIMediator.instance().openTorrentFile(torrentFile, false);
                    }
                });
            }
	    }.start();
	}

	public void handleTorrentMagnet(final String request, final boolean partialDownload) {
	    new AzureusStarter.AzureusCoreWaiter("VisualConnectionCallback::handleTorrentMagnet()") {
            @Override
            public void onAzureusCoreStarted() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        GUIMediator.instance().setRemoteDownloadsAllowed(partialDownload);
                            System.out.println("VisualConnectionCallback about to call openTorrentURI of request.");
                            System.out.println(request);
                        GUIMediator.instance().openTorrentURI(request, partialDownload);
                    }
                });
            }
	    }.start();
	}
	
    public void addDownloadManager(DownloadManager dm) {
        Runnable doWorkRunnable = new AddDownloadManager(dm);
        GUIMediator.safeInvokeAndWait(doWorkRunnable);
    }

    public boolean isRemoteDownloadsAllowed() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    GUIMediator.instance();
                }
            });
        } catch (Exception e) {
            System.out.println("Failed to create GUIMediator");
            e.printStackTrace();
        }
        
        return GUIMediator.instance().isRemoteDownloadsAllowed();
    }
}
