package com.limegroup.gnutella.browser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.Debug;
import org.limewire.util.OSUtils;

import com.limegroup.gnutella.ActivityCallback;

public class ExternalControl {
    
    private static final Log LOG = LogFactory.getLog(ExternalControl.class);
    
    private static ExternalControl INSTANCE;
    
    public static ExternalControl instance(ActivityCallback activityCallback) {
        if (INSTANCE == null) {
            INSTANCE = new ExternalControl(activityCallback);
        }
        return INSTANCE;
    }

	private final String LOCALHOST = "127.0.0.1";
    private boolean initialized = false;
    private volatile String  enqueuedRequest = null;
    
    private final ActivityCallback activityCallback;
    
    private ExternalControl(ActivityCallback activityCallback) {
        this.activityCallback = activityCallback;
        
        startServer();
    }
    
    private void startServer() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(45099, 50, InetAddress.getByName("127.0.0.1"));

                    while (true) {
                        final Socket socket = serverSocket.accept();
                        new Thread(new Runnable() {
                            public void run() {

                                boolean closeSocket = false;
                                try {
                                    String address = socket.getInetAddress().getHostAddress();

                                    if (address.equals("localhost") || address.equals("127.0.0.1")) {

                                        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), Constants.DEFAULT_ENCODING));

                                        String line = br.readLine();

                                        if (line != null) {

                                            if (line.toUpperCase().startsWith("GET ")) {

                                                line = line.substring(4);

                                                int pos = line.lastIndexOf(' ');

                                                line = line.substring(0, pos);

                                                closeSocket = process(line, br, socket.getOutputStream());

                                            }
                                        }

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    if (closeSocket) {
                                        try {
                                            socket.close();
                                        } catch (Exception e) {

                                        }
                                    }
                                }
                            }
                        }).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean process(String get, BufferedReader is, OutputStream os)

    throws IOException {
        //System.out.println( "get = " + get );

        // magnet:?xt=urn:sha1:YNCKHTQCWBTRNJIV4WNAE52SJUQCZO5C

        Map<String, String> original_params = new HashMap<String, String>();
        Map<String, String> lc_params = new HashMap<String, String>();

        List<String> source_params = new ArrayList<String>();

        int pos = get.indexOf('?');

        String arg_str;

        if (pos == -1) {

            arg_str = "";

        } else {

            arg_str = get.substring(pos + 1);

            pos = arg_str.lastIndexOf(' ');

            if (pos >= 0) {

                arg_str = arg_str.substring(0, pos).trim();
            }

            StringTokenizer tok = new StringTokenizer(arg_str, "&");

            while (tok.hasMoreTokens()) {

                String arg = tok.nextToken();

                pos = arg.indexOf('=');

                if (pos == -1) {

                    String lhs = arg.trim();

                    original_params.put(lhs, "");

                    lc_params.put(lhs.toLowerCase(MessageText.LOCALE_ENGLISH), "");

                } else {

                    try {
                        String lhs = arg.substring(0, pos).trim();
                        String lc_lhs = lhs.toLowerCase(MessageText.LOCALE_ENGLISH);

                        String rhs = URLDecoder.decode(arg.substring(pos + 1).trim(), Constants.DEFAULT_ENCODING);

                        original_params.put(lhs, rhs);

                        lc_params.put(lc_lhs, rhs);

                        if (lc_lhs.equals("xsource")) {

                            source_params.add(rhs);
                        }
                    } catch (UnsupportedEncodingException e) {

                        Debug.printStackTrace(e);
                    }
                }
            }
        }

        if (get.startsWith("/download")) {

            String hash = (String) lc_params.get("hash");

            if (hash != null) {
                handleTorrentMagnetRequest("magnet:?xt=urn:btih:" + hash);
                return true;
            }
        }

        return true;
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
		activityCallback.restoreApplication();
		activityCallback.showDownloads();
		return activityCallback;
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
