package com.frostwire;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.limewire.io.IOUtils;

import com.limegroup.gnutella.SaveLocationException;
import com.limegroup.gnutella.http.HTTPHeaderName;
import com.limegroup.gnutella.http.HttpClientListener;
import com.limegroup.gnutella.http.HttpExecutor;

/**
 * A Blocking HttpClient.
 * Use fetch() to retrieve the byte[]
 * @author gubatron
 *
 */
public class HttpFileFetcher implements HttpClientListener {
	
	private static final int TIMEOUT = 5000;
	private final URI _uri;

	private byte[] body = null;
	private HttpExecutor _httpExecutor;

	public HttpFileFetcher(URI uri) {
		this._uri = uri;
		_httpExecutor = CoreFrostWireUtils.getHTTPExecutor();
	}
	
	public byte[] fetch() {
		HttpGet get = new HttpGet(_uri);
		get.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506");
		get.addHeader(HTTPHeaderName.CONNECTION.httpStringValue(),"close");
        
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        HttpClientParams.setRedirecting(params, true);
        
        _httpExecutor.executeBlocking(get, params, this);
        return body;
	}
	
	
	public boolean requestComplete(HttpUriRequest method, HttpResponse response) {
		try {
            if(response.getEntity() != null) {
                body = IOUtils.readFully(response.getEntity().getContent());
            }
            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300)
				throw new IOException("bad status code, downloading .torrent file "
				+ response.getStatusLine().getStatusCode());
			if (body == null || body.length == 0)
				throw new IOException("invalid response");
            
		} catch (SaveLocationException security) {
			//GUIMediator.showWarning(I18n.tr("The selected .torrent file may contain a security hazard."));
		} catch (IOException iox) {
			//GUIMediator.showWarning(I18n.tr("FrostWire could not fetch bytes from the provided URL."));
		} finally {
			//GuiCoreMediator.getHttpExecutor().releaseResources(response);
			_httpExecutor.releaseResources(response);
		}
		
        
        return false;
	}

	@Override
	public boolean requestFailed(HttpUriRequest request, HttpResponse response,
			IOException exc) {
		body = null;
		return false;
	}
}
