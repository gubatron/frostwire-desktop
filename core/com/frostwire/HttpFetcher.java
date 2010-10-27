package com.frostwire;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.limegroup.gnutella.http.HTTPHeaderName;

/**
 * A Blocking HttpClient.
 * Use fetch() to retrieve the byte[]
 * @author gubatron
 *
 */
public class HttpFetcher {
	
	private static final int TIMEOUT = 5000;
	private final URI _uri;

	private byte[] body = null;

	public HttpFetcher(URI uri) {
		this._uri = uri;
	}
	
	public byte[] fetch() {
        
        DefaultHttpClient defaultHttpClient = new DefaultHttpClient();

		HttpHost httpHost = new HttpHost(_uri.getHost(), _uri.getPort());
		HttpGet httpGet = new HttpGet(_uri);
		httpGet.addHeader(HTTPHeaderName.CONNECTION.httpStringValue(), "close");
		
		HttpParams params = httpGet.getParams();
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        HttpConnectionParams.setStaleCheckingEnabled(params, true);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpClientParams.setRedirecting(params, true);
        HttpProtocolParams.setUseExpectContinue(params, false);
        HttpProtocolParams.setUserAgent(params, "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			
			HttpResponse response = defaultHttpClient.execute(httpHost, httpGet);
			
			if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300)
				throw new IOException("bad status code, downloading file " + response.getStatusLine().getStatusCode());
			
			if(response.getEntity() != null) {
				response.getEntity().writeTo(baos);
			}
			
			body = baos.toByteArray();
			
			if (body == null || body.length == 0) {
				throw new IOException("invalid response");
			}

			return body;
			
		} catch (Exception e) {
			System.out.println("Http error: " + e.getMessage());
		} finally {
			defaultHttpClient.getConnectionManager().shutdown();
			try {
				baos.close();
			} catch (IOException e) {
			}
		}
		
		return null;
	}
}
