package com.limegroup.gnutella;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.io.NetworkUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.http.HTTPConnectionData;
import com.limegroup.gnutella.settings.SSLSettings;

/**
 * Manages state for push upload requests.
 */
@Singleton
public final class PushManager {
    
    private static final Log LOG = LogFactory.getLog(PushManager.class);

    /**
     * The timeout for the connect time while establishing the socket. Set to
     * the same value as NORMAL_CONNECT_TIME is ManagedDownloader.
     */
    private static final int CONNECT_TIMEOUT = 10000;//10 secs
    
    private final Provider<FileManager> fileManager;
    
    /**
     * @param fileManager
     * @param socketsManager
     * @param httpAcceptor
     */
    @Inject
    public PushManager(Provider<FileManager> fileManager) {
        this.fileManager = fileManager;
    }    

	/**
	 * Accepts a new push upload.
     * NON-BLOCKING: creates a new thread to transfer the file.
	 * <p>
     * The thread connects to the other side, waits for a GET/HEAD,
     * and delegates to the UploaderManager.acceptUpload method with the
     * socket it created.
     * Essentially, this is a reverse-Acceptor.
     * <p>
     * No file and index are needed since the GET/HEAD will include that
     * information.  Just put in our first file and filename to create a
     * well-formed.
	 * @param host the ip address of the host to upload to
	 * @param port the port over which the transfer will occur
	 * @param guid the unique identifying client guid of the uploading client
     * @param lan whether or not this is a request over a local network (
     * (force the UploadManager to accept this request when it comes back)
     * @param isFWTransfer whether or not to use a UDP pipe to service this
     * upload.
	 */
	public void acceptPushUpload(final String host, 
                                 final int port, 
                                 final String guid,
                                 final boolean lan,
                                 final boolean isFWTransfer,
                                 final boolean tlsCapable) {
        if (LOG.isDebugEnabled())
            LOG.debug("Accepting Push Upload from ip:" + host + " port:" + port + " FW:" + isFWTransfer);
                                    
        if( host == null )
            throw new NullPointerException("null host");
        if( !NetworkUtils.isValidPort(port) )
            throw new IllegalArgumentException("invalid port: " + port);
        if( guid == null )
            throw new NullPointerException("null guid");
        
        // TODO: why is this check here?  it's a tiny optimization,
        // but could potentially kill any sharing of files that aren't
        // counted in the library.
        if (fileManager.get().getNumFiles() < 1 && fileManager.get().getNumIncompleteFiles() < 1)
            return;

        // We used to have code here that tested if the guy we are pushing to is
        // 1) hammering us, or 2) is actually firewalled.  1) is done above us
        // now, and 2) isn't as much an issue with the advent of connectback
        
        PushData data = new PushData(host, port, guid, lan);
        
        // If the transfer is to be done using FW-FW, then immediately start a new thread
        // which will connect using FWT.  Otherwise, do a non-blocking connect and have
        // the observer spawn the thread only if it succesfully connected.
//        if(isFWTransfer) {
//            if(LOG.isDebugEnabled())
//                LOG.debug("Adding push observer FW-FW to host: " + host + ":" + port);
//            // TODO: should FW-FW connections also use TLS?
//            NBSocket socket = udpSelectorProvider.get().openSocketChannel().socket();
//            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT*2, new PushObserver(data, true, httpAcceptor.get()));
//        } else {
//            if (LOG.isDebugEnabled())
//                LOG.debug("Adding push observer to host: " + host + ":" + port);
//            try {
//                ConnectType type = tlsCapable && SSLSettings.isOutgoingTLSEnabled() ? ConnectType.TLS : ConnectType.PLAIN;
//                socketsManager.get().connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT, new PushObserver(data, false, httpAcceptor.get()), type);
//            } catch(IOException iox) {
//            }
        //}
    }
    
    /** A simple collection of Push information */
    private static class PushData {
        private final String host;
        private final int port;
        private final String guid;
        private final boolean lan;
        
        PushData(String host, int port, String guid, boolean lan) {
            this.host = host;
            this.port = port;
            this.guid = guid;
            this.lan = lan;
        }
        
        public boolean isLan() {
            return lan;
        }
        public String getGuid() {
            return guid;
        }
        public String getHost() {
            return host;
        }
        public int getPort() {
            return port;
        }
    }


}
