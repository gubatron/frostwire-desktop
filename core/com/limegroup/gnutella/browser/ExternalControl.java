package com.limegroup.gnutella.browser;

import java.io.File;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.i18n.I18nMarker;
import org.limewire.io.NetworkUtils;
import org.limewire.service.ErrorService;
import org.limewire.service.MessageService;
import org.limewire.util.OSUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.ActivityCallback;
import com.limegroup.gnutella.SaveLocationException;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.settings.ConnectionSettings;

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
	public void checkForActiveLimeWire() {
	    if( testForLimeWire(null) ) {
		    System.exit(0);	
		}
	}

	public void checkForActiveLimeWire(String arg) {
	    if ((OSUtils.isWindows() || OSUtils.isLinux()) && testForLimeWire(arg)) {
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
		System.out.println("ExternalControl.runQueuedControlRequest() - " + enqueuedRequest);
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

	    ActivityCallback callback = restoreApplication();
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
	private boolean testForLimeWire(String arg) {
		Socket socket = null;
		int port = ConnectionSettings.PORT.getValue();
		// Check to see if the port is valid.
		// If it is not, revert it to the default value.
		// This has the side effect of possibly allowing two 
		// FrostWires to start if somehow the existing one
		// set its port to 0, but that should not happen
		// in normal program flow.
		String type = isTorrentRequest(arg) ? "TORRENT" : "MAGNET";
		if( !NetworkUtils.isValidPort(port) ) {
		    ConnectionSettings.PORT.revertToDefault();
		    port = ConnectionSettings.PORT.getValue();
        }   
//		try {
//			socket = socketsManager.connect(new InetSocketAddress(LOCALHOST, port), 1000);
//			InputStream istream = socket.getInputStream(); 
//			socket.setSoTimeout(1000); 
//		    ByteReader byteReader = new ByteReader(istream);
//		    OutputStream os = socket.getOutputStream();
//		    OutputStreamWriter osw = new OutputStreamWriter(os);
//		    BufferedWriter out = new BufferedWriter(osw);
//		    out.write(type+" "+arg+" ");
//		    out.write("\r\n");
//		    out.flush();
//		    String str = byteReader.readLine();
//		    return(str != null && str.startsWith(CommonUtils.getUserName()));
//		} catch (IOException e2) {
//		} finally {
//		    if(socket != null) {
//		        try {
//                    socket.close();
//                } catch (IOException e) {
//                    // nothing we can do
//                }
//            }
//        }
        
	    return false;
	}
}
