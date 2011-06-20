package com.limegroup.gnutella.browser;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.limewire.util.OSUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.ActivityCallback;

@Singleton
public class ExternalControl {
    
    private static final Log LOG = LogFactory.getLog(ExternalControl.class);

	private final String LOCALHOST = "127.0.0.1";
    private boolean initialized = false;
    private volatile String  enqueuedRequest = null;
    
    private final Provider<ActivityCallback> activityCallback;
    
    @Inject
    public ExternalControl(
            Provider<ActivityCallback> activityCallback) {
        this.activityCallback = activityCallback;
    }

    public String preprocessArgs(String args[]) {
	    LOG.trace("enter proprocessArgs");

	    StringBuilder arg = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			arg.append(args[i]);
		}
		return arg.toString();
	}
	
    /**
     * Uses the magnet infrastructure to check if FrostWire is running.
     * If it is, it is restored and this instance exits.
     * Note that the already-running FrostWire is not checked
     * for 'allow multiple instances' -- only the instance that was just
     * started.
     */
	public void checkForActiveFrostWire() {
	    if( testForFrostWire(null) ) {
		    System.exit(0);	
		}
	}

	public void checkForActiveFrostWire(String arg) {
	    if ((OSUtils.isWindows() || OSUtils.isLinux()) && testForFrostWire(arg)) {
		    System.exit(0);	
		}
	}

	public boolean  isInitialized() {
		return initialized;
	}
	public void enqueueControlRequest(String arg) {
	    LOG.trace("enter enqueueControlRequest");
		enqueuedRequest = arg;
	}

	public void runQueuedControlRequest() {
		initialized = true;
	    if ( enqueuedRequest != null ) {
			String request   = enqueuedRequest;
			enqueuedRequest = null;
			
			if (isTorrentMagnetRequest(request)) {
				System.out.println("ExternalControl.runQueuedControlRequest() handleTorrentMagnetRequest() - " + request);
				handleTorrentMagnetRequest(request);
			}
			else if (isTorrentRequest(request)) {
				System.out.println("ExternalControl.runQueuedControlRequest() handleTorrentRequest() - " + request);
				handleTorrentRequest(request);
			}
			else {
				System.out.println("ExternalControl.runQueuedControlRequest() handleMagnetRequest() - " + request);
				handleMagnetRequest(request);
			}
		}
	}
	
	private boolean isTorrentMagnetRequest(String request) {
		return request.startsWith("magnet:?xt=urn:btih");
	}
	
	private void handleTorrentMagnetRequest(String request) {
		LOG.trace("enter handleTorrentMagnetRequest");
		ActivityCallback callback = restoreApplication();
		callback.handleTorrentMagnet(request);
	}
	
	/**
	 * @return true if this is a torrent request.  
	 */
	private boolean isTorrentRequest(String arg) {
		if (arg == null) 
			return false;
		arg = arg.trim().toLowerCase();
		// magnets pointing to .torrent files are just magnets for now
		return arg.endsWith(".torrent") && !arg.startsWith("magnet:");
	}
	
	//refactored the download logic into a separate method
	public void handleMagnetRequest(String arg) {
	    LOG.trace("enter handleMagnetRequest");
	    
	    if (isTorrentMagnetRequest(arg)) {
	    	System.out.println("ExternalControl.handleMagnetRequest("+arg+") -> handleTorrentMagnetRequest()");
	    	handleTorrentMagnetRequest(arg);
	    	return;
	    }

	    //ActivityCallback callback = restoreApplication();
	    MagnetOptions options[] = MagnetOptions.parseMagnet(arg);

		if (options.length == 0) {
		    if(LOG.isWarnEnabled())
		        LOG.warn("Invalid magnet, ignoring: " + arg);
			return;
        }
//		
//		// ask callback if it wants to handle the magnets itself
//		if (!callback.handleMagnets(options)) {
//		    downloadMagnet(options);
//		}
	}
	
	private ActivityCallback restoreApplication() {
		activityCallback.get().restoreApplication();
		activityCallback.get().showDownloads();
		return activityCallback.get();
	}
	
	private void handleTorrentRequest(String arg) {
		LOG.trace("enter handleTorrentRequest");
		ActivityCallback callback = restoreApplication();
		File torrentFile = new File(arg.trim());
		callback.handleTorrent(torrentFile);
	}
	
	/**  Check if the client is already running, and if so, pop it up.
	 *   Sends the MAGNET message along the given socket. 
	 *   @returns  true if a local FrostWire responded with a true.
	 */
	private boolean testForFrostWire(String arg) {
		Socket socket = null;
		int port = COConfigurationManager.getIntParameter("TCP.Listen.Port");
		try {
		    socket = new Socket();
			socket.connect(new InetSocketAddress(LOCALHOST, port), 1000);
			return true;
		} catch (IOException e2) {
		} finally {
		    if(socket != null) {
		        try {
                    socket.close();
                } catch (IOException e) {
                    // nothing we can do
                }
            }
        }
        
	    return false;
	}
}
