package com.frostwire;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.cookie.DateUtils;
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
	
	private static final int TIMEOUT = 10000;
	
	private URI _uri;
	private String _userAgent;

	private byte[] body = null;

	public HttpFetcher(URI uri, String userAgent) {
		_uri = uri;
		_userAgent = userAgent;
	}
	
	public HttpFetcher(URI uri) {
	    this(uri, "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506");
	}
	
	public Object[] fetchWithDate() {
        
        DefaultHttpClient httpClient = new DefaultHttpClient();

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
        HttpProtocolParams.setUserAgent(params, _userAgent);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			
			HttpResponse response = httpClient.execute(httpHost, httpGet);
			
			if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
				throw new IOException("bad status code, downloading file " + response.getStatusLine().getStatusCode());
			}
			
			Long date = Long.valueOf(0);
            
            Header[] headers = response.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].getName().startsWith("Last-Modified")) {
                    try {
                        date = DateUtils.parseDate(headers[i].getValue()).getTime();
                    } catch (Exception e) {
                    }
                    break;
                }
            }
			
			if(response.getEntity() != null) {
				response.getEntity().writeTo(baos);
			}
			
			body = baos.toByteArray();
			
			if (body == null || body.length == 0) {
				throw new IOException("invalid response");
			}

			return new Object[]{ body, date};
			
		} catch (Exception e) {
			System.out.println("Http error: " + e.getMessage());
		} finally {
			httpClient.getConnectionManager().shutdown();
			try {
				baos.close();
			} catch (IOException e) {
			}
		}
		
		return null;
	}
	
	public byte[] fetch() {
	    return (byte[]) fetchWithDate()[0];
	}
	
	public void post(File file) throws IOException {
        
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));

        HttpHost httpHost = new HttpHost(_uri.getHost(), _uri.getPort());
		HttpPost httpPost = new HttpPost(_uri);
		FileEntity fileEntity = new FileEntity(file, "binary/octet-stream");
		fileEntity.setChunked(true);
		httpPost.setEntity(fileEntity);
		
		HttpParams params = httpPost.getParams();
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        HttpConnectionParams.setStaleCheckingEnabled(params, true);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpClientParams.setRedirecting(params, true);
        HttpProtocolParams.setUseExpectContinue(params, false);
        HttpProtocolParams.setUserAgent(params, "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506");
        
		try {
			
			HttpResponse response = httpClient.execute(httpHost, httpPost);
			
			if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300)
				throw new IOException("bad status code, upload file " + response.getStatusLine().getStatusCode());

		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			new IOException("Http error: " + e.getMessage(), e);
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}
	
	public void post(FileEntity fileEntity) throws IOException {
        
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));

        HttpHost httpHost = new HttpHost(_uri.getHost(), _uri.getPort());
		HttpPost httpPost = new HttpPost(_uri);
		httpPost.setEntity(fileEntity);
		
		HttpParams params = httpPost.getParams();
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        HttpConnectionParams.setStaleCheckingEnabled(params, true);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpClientParams.setRedirecting(params, true);
        HttpProtocolParams.setUseExpectContinue(params, false);
        HttpProtocolParams.setUserAgent(params, "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506");
        
		try {
			
			HttpResponse response = httpClient.execute(httpHost, httpPost);
			
			if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300)
				throw new IOException("bad status code, upload file " + response.getStatusLine().getStatusCode());

		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			new IOException("Http error: " + e.getMessage(), e);
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}
}
