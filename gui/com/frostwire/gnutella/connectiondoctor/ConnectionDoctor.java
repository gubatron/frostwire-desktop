package com.frostwire.gnutella.connectiondoctor;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.limewire.util.CommonUtils;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
/**
 * @author Fernando 'FTA' Toussaint
 * 
 * @author Gubatron [Nov-4-2009] 
 * Revision: 1262
 * Added 'Refresh Gnutella.net' to the 'Tools' menu. Re-implemented 
 * ConnectionDoctor.loadHosts(). This method was overly complex and 
 * the http client it used was giving out absurd exceptions. 
 * Much cleaner, no recursion. However sometimes it seems that 
 * http://doctor.frostwire.com/hosts does not return the proper 
 * response and redirects the HTTP response to 
 * http://www.frostwire.com which can cause the gnutella.net file 
 * that's saved to have HTML, instead of a valid gnutella.net file. 
 * Gotta check the server side with FTA.
 *  
 * This class checks the frostwire server to solve connection problems. 
 */
public final class ConnectionDoctor { //made public for being available from everywhere

    // Time to wait before fetching the hosts file (gnutella.net) again
	public static int WAITING_TIME = (System.getProperty("debug")!=null) ? 6 : 15;

	// The strategy is to wait a few seconds, try to fetch host file, reconnect
    
	// Maximum number of attempts to fetch the hosts file. 
	private int MAXTRY = 4;
	
	//Current count of attempts to fetch the hosts file
    private int CURRTRY = 0;
	
	private Thread _initializer; //will make initializer thread a member just to 
	                             //make sure it wont die right after initialize()
	                             //exits.
	
	private boolean failed;

	/**
	 * Connection Doctor URL in the FrostWire server.
	 */
	private static final String CONNECTION_DR_URL =
		"http://doctor.frostwire.com/hosts/";  //initial request request

	/**
	 * Constant number of milliseconds to wait before timing out the
	 * connection to the servlet.
	 */
	private static final int CONNECT_TIMEOUT = 3 * 1000; // 10 seconds.

	/**
	 * Gets the new ip addresses from the remote reserver
	 * @throws HttpException 
	 **/

	public synchronized boolean loadHosts() throws Exception {     
		return loadHosts(CONNECTION_DR_URL);
	}
	
    /**
     * Gets the new ip addresses from the remote server
     * Returns false if it fails and increments CURRTRY
     * @throws Exception 
     **/	
	public synchronized boolean loadHosts(String doctorUrl) throws Exception {
	    failed = false;
	    HttpURLConnection connection = null;
	    
	    try {
	        //System.out.println("ConnectionDoctor.loadHosts("+doctorUrl+")");
	        connection = (HttpURLConnection) (new URL(doctorUrl)).openConnection();
	        connection.setReadTimeout(CONNECT_TIMEOUT);
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String gnutellaNetLocation = br.readLine().trim();
	        
	        if (!gnutellaNetLocation.startsWith("http://")) {
	            failed = true;
	            CURRTRY++;
	            throw new Exception("ConnectionDoctor.loadHosts() - " + gnutellaNetLocation + " not a valid gnutella.net location");
	        }

	        br.close();
	        connection.disconnect();
	        
	        //Now download the gnutella.net
	        connection = (HttpURLConnection) (new URL(gnutellaNetLocation)).openConnection();
	        br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        StringBuffer body = new StringBuffer();
	        String line = br.readLine();
	        
	        while (line != null) {
	            //valid gnutella.net line looks like this
	            //124.189.65.115:43537,,1256662587078,,,en,,,,,
	            //try a rough match, just to make sure its not HTML
	            if (!line.matches("(.*)\\:(.*)\\,(.*)")) {
	                failed = true;
	                br.close();
	                connection.disconnect();
	                throw new Exception("ConnectionDoctor.loadHosts() - invalid gnutella.net from server");
	            }
	            
	            body.append(line + "\n");
	            line = br.readLine();
	        }
	        
	        //System.out.println("gnutella.net >>>\n" + body);
	        
	        FileWriter out = new FileWriter(CommonUtils.getUserSettingsDir() + "/gnutella.net");
            out.write(body.toString());
            out.flush();
            System.out.println("Got Gnutella");
            failed = false;
            // System.out.println(body);
            out.close();
            br.close();
            connection.disconnect();
            connection = null;
            br = null;
            
            //System.out.println("Disconnecting...");
            //System.out.println(body);
            GUIMediator.instance().disconnect();
            //System.out.println("Reconnecting...");
            GUIMediator.instance().connect();
	        System.gc();
	        CURRTRY+=1;
	    } catch (Exception e) {
	        CURRTRY+=1;
	        
	        failed = true;
	        
	        if (connection != null)
	            connection.disconnect();
	        throw e;
	    }
	    
	    return true;
	} //loadHosts

	public boolean failed() { return failed; }

	/** 
	 * Initialize connection Doctor	 
	 */
	public void initialize() {
		//We'll try to get the external ipAddress from RouterService
		//and try, and try, 1 second at the time.
		_initializer = new Thread(new Runnable() {
			public void run() {
				boolean ConnDocEnded=false;
				if (GuiCoreMediator.getConnectionServices().isConnected() == true)
					ConnDocEnded=true;

				//WAITING_TIME = this.WAITING_TIME;
				//System.out.println("Initializing Connection Doctor...");

				int seconds=0;
				
				//while UI and Core aren't ready, or we haven't tried hard enough...
				while (!(GUIMediator.isConstructed() && 
						GuiCoreMediator.getCore() != null && 
						GuiCoreMediator.getLifecycleManager().isLoaded()) || 
						(ConnDocEnded == false && 
						GuiCoreMediator.getConnectionServices().isConnected() == false &&
						CURRTRY < MAXTRY) 
						) {
					//System.out.println("is FrostWire currently connecting?: " + GuiCoreMediator.getConnectionServices().isConnecting() + " Currently connected: " + GuiCoreMediator.getConnectionServices().isConnected() + " Should be ended?: " + ConnDocEnded);
					
					if (GuiCoreMediator.getConnectionServices().isConnecting() && !GuiCoreMediator.getConnectionServices().isConnected()) {
						seconds = seconds + 1;
						//System.out.println("FrostWire's connection doctor will be started at "+  WAITING_TIME + " seconds, now waiting: " + seconds + " seconds");
						if (CURRTRY >= MAXTRY) {
							GUIMediator.showError(I18n.tr("FrostWire could not detect an internet connection. Please, check your firewall settings and try to restart FrostWire."));
							ConnDocEnded=true;
							return;
						}
						if (seconds >= WAITING_TIME) { // try to fix connection problems after WAITING_TIME seconds of no connection
							//new ConnectionDoctor().loadHosts();
							//System.out.println("****************Getting new hosts from Connection Doctor...*************");
						    try {
						        ConnDocEnded=loadHosts();
						    } catch (Exception e) {
						        ConnDocEnded = false;
						    }
							seconds=0;
						}
						
						if (GuiCoreMediator.getConnectionServices().isConnected() == true)
							ConnDocEnded=true;

					}
					
					try { Thread.sleep(1000); } catch (Exception e) {};

				}
				
				/*
				boolean condA = !(GUIMediator.isConstructed() && 
                    GuiCoreMediator.getCore() != null && 
                    GuiCoreMediator.getLifecycleManager().isLoaded());
				*/
				
				//System.out.println("Connection Doctor timer is now ended.\n\tConnDocEnded=" + ConnDocEnded +
				//        "\n\tCURRTRY="+CURRTRY+"\n\tCondA="+condA);

			}
		});
		_initializer.start();
	}

}
