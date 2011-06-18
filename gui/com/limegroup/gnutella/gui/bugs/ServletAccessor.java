package com.limegroup.gnutella.gui.bugs;


/**
 * This class handles accessing the servlet, sending it data about the client
 * configuration, and obtaining information about the next time this or any
 * bug can be sent.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
final class ServletAccessor {
    
    /**
	 * Constant for the servlet url.
	 */
	private static final String DEFAULT_SERVLET_URL =
		"http://doctor.frostwire.com/bug-manager"; //"http://www.cybercultura.net/fwbugman.py";
	
	/** Constructs an accessor that can use NIO */
	ServletAccessor() {
	    this(true);
    }
    
    /** Constructs an accessor that may or may not use NIO.  Use true if it can. */
    ServletAccessor(boolean allowNIO) {
        this(allowNIO, DEFAULT_SERVLET_URL);        
    }
    
    /** Constructs an accessor that may or may not use NIO.  Use true if it can. 
     *  Also takes a URL value as a parameter to be assigned to SERVLET_URL */
    ServletAccessor(boolean allowNIO, String servlet_url) {
        
    }

	/**
	 * Contacts the application servlet and sends it the information 
	 * contained in the <tt>LocalClientInfo</tt> object.  This method 
	 * also builds a <tt>RemoteClientInfo</tt> object from the information 
	 * obtained from the servlet.
	 *
	 * @return a <tt>RemoteClientInfo</tt> object that encapsulates the 
	 *         data about when to next send a bug.
	 * @param localInfo is an object encapsulating information about the
	 *                  local machine to send to the remote server
	 */
	synchronized RemoteClientInfo getRemoteBugInfo(LocalClientInfo localInfo) {
//        RemoteClientInfo remoteInfo = new RemoteClientInfo();
//        HttpResponse response = null;
//        LimeHttpClient client = ALLOW_NIO ? 
//                GuiCoreMediator.getLimeHttpClient() :
//                new SimpleLimeHttpClient();
//        try {
//            List<NameValuePair> params = localInfo.getPostRequestParams();
//            HttpPost post = new HttpPost(SERVLET_URL);
//            post.addHeader("Cache-Control", "no-cache");
//            post.addHeader("User-Agent", FrostWireUtils.getHttpServer());
//            post.addHeader("Content-Type",
//                    "application/x-www-form-urlencoded; charset=UTF-8");
//            post.setEntity(new UrlEncodedFormEntity(params));
//
//            HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECT_TIMEOUT);
//            HttpClientParams.setRedirecting(client.getParams(), false);
//
//            response = client.execute(post);
//            String result;
//            if (response.getEntity() != null) {
//                result = EntityUtils.toString(response.getEntity());
//            } else {
//                result = null;
//            }
//            String body = result;
//            if (LOG.isDebugEnabled())
//                LOG.debug("Got response: " + response);
//            // process results if valid status code
//            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
//                remoteInfo.addRemoteInfo(body);
//                // otherwise mark as server down.
//            else {
//                if (LOG.isWarnEnabled())
//                    LOG.warn("Servlet connect failed, code: " +
//                            response.getStatusLine().getStatusCode());
//                remoteInfo.connectFailed();
//            }
//		} catch(IOException e) {
//            fail(remoteInfo, e);
//        } finally {
//            client.releaseConnection(response);
//        }
//        return remoteInfo;
	    return null;
    }
}

