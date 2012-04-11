/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
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

package com.frostwire;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.limewire.util.FileUtils;

/**
 * A Blocking HttpClient.
 * Use fetch() to retrieve the byte[]
 * @author gubatron
 * @author aldenml
 *
 */
public class HttpFetcher {

    private static final String DEFAULT_USER_AGENT = UserAgentGenerator.getUserAgent();
    private static final int DEFAULT_TIMEOUT = 10000;

    private static HttpClient DEFAULT_HTTP_CLIENT;
    private static HttpClient DEFAULT_HTTP_CLIENT_GZIP;

    private final URI _uri;
    private final String _userAgent;
    private final int _timeout;

    private byte[] body = null;

    static {
        setupHttpClients();
    }

    public HttpFetcher(URI uri, String userAgent, int timeout) {
        _uri = uri;
        _userAgent = userAgent;
        _timeout = timeout;
    }

    public HttpFetcher(URI uri, String userAgent) {
        this(uri, userAgent, DEFAULT_TIMEOUT);
    }

    public HttpFetcher(URI uri, int timeout) {
        this(uri, DEFAULT_USER_AGENT, timeout);
    }

    public HttpFetcher(URI uri) {
        this(uri, DEFAULT_USER_AGENT);
    }

    public HttpFetcher(String uri) {
        this(convert(uri));
    }

    public HttpFetcher(String uri, int timeout) {
        this(convert(uri), timeout);
    }

    public Object[] fetch(boolean gzip) throws IOException {
        return fetch(gzip, null);
    }

    public Object[] fetch(boolean gzip, String referer) throws IOException {
        HttpHost httpHost = new HttpHost(_uri.getHost(), _uri.getPort());
        HttpGet httpGet = new HttpGet(_uri);
        httpGet.addHeader("Connection", "close");
        if (referer != null) {
            httpGet.addHeader("Referer", referer);
        }

        HttpParams params = httpGet.getParams();
        HttpConnectionParams.setConnectionTimeout(params, _timeout);
        HttpConnectionParams.setSoTimeout(params, _timeout);
        HttpConnectionParams.setStaleCheckingEnabled(params, true);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpClientParams.setRedirecting(params, true);
        HttpProtocolParams.setUseExpectContinue(params, false);
        HttpProtocolParams.setUserAgent(params, _userAgent);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {

            HttpResponse response = (gzip ? DEFAULT_HTTP_CLIENT_GZIP : DEFAULT_HTTP_CLIENT).execute(httpHost, httpGet);

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

            if (response.getEntity() != null) {
                if (gzip) {
                    String str = EntityUtils.toString(response.getEntity());
                    baos.write(str.getBytes());
                } else {
                    response.getEntity().writeTo(baos);
                }
            }

            body = baos.toByteArray();

            if (body == null || body.length == 0) {
                throw new IOException("invalid response");
            }

            return new Object[] { body, date };

        } finally {
            try {
                baos.close();
            } catch (IOException e) {
            }
        }
    }

    public byte[] fetch() {
        Object[] objArray = null;
        try {
            objArray = fetch(false);
        } catch (IOException e) {
            // ignore
        }

        if (objArray != null) {
            return (byte[]) objArray[0];
        }

        return null;
    }

    public void save(File file) throws IOException {
        HttpHost httpHost = new HttpHost(_uri.getHost(), _uri.getPort());
        HttpGet httpGet = new HttpGet(_uri);
        httpGet.addHeader("Connection", "close");

        HttpParams params = httpGet.getParams();
        HttpConnectionParams.setConnectionTimeout(params, _timeout);
        HttpConnectionParams.setSoTimeout(params, _timeout);
        HttpConnectionParams.setStaleCheckingEnabled(params, true);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpClientParams.setRedirecting(params, true);
        HttpProtocolParams.setUseExpectContinue(params, false);
        HttpProtocolParams.setUserAgent(params, _userAgent);

        FileOutputStream output = null;

        try {

            output = new FileOutputStream(file);

            HttpResponse response = DEFAULT_HTTP_CLIENT.execute(httpHost, httpGet);

            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                throw new IOException("bad status code, downloading file " + response.getStatusLine().getStatusCode());
            }

            if (response.getEntity() != null) {
                writeEntity(response.getEntity(), output);
            }

        } finally {
            FileUtils.close(output);
        }
    }

    public byte[] postJSON(String json) throws IOException {
        return post(json, "application/json");
    }

    public byte[] post(String postBody, String contentType) throws IOException {
        HttpHost httpHost = new HttpHost(_uri.getHost(), _uri.getPort());
        HttpPost httpPost = new HttpPost(_uri);

        StringEntity stringEntity = new StringEntity(postBody);
        httpPost.setHeader("Accept", contentType);
        httpPost.setHeader("Content-type", contentType);
        httpPost.setEntity(stringEntity);

        HttpParams params = httpPost.getParams();
        HttpConnectionParams.setConnectionTimeout(params, _timeout);
        HttpConnectionParams.setSoTimeout(params, _timeout);
        HttpConnectionParams.setStaleCheckingEnabled(params, true);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpClientParams.setRedirecting(params, true);
        HttpProtocolParams.setUseExpectContinue(params, false);
        HttpProtocolParams.setUserAgent(params, DEFAULT_USER_AGENT);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {

            HttpResponse response = DEFAULT_HTTP_CLIENT.execute(httpHost, httpPost);

            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300)
                throw new IOException("bad status code, upload file " + response.getStatusLine().getStatusCode());

            if (response.getEntity() != null) {
                response.getEntity().writeTo(baos);
            }

            body = baos.toByteArray();

            if (body == null || body.length == 0) {
                throw new IOException("invalid response");
            }

            return body;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            new IOException("Http error: " + e.getMessage(), e);
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
            }
        }

        return null;
    }

    public void post(File file) throws IOException {
        HttpHost httpHost = new HttpHost(_uri.getHost(), _uri.getPort());
        HttpPost httpPost = new HttpPost(_uri);
        FileEntity fileEntity = new FileEntity(file, "binary/octet-stream");
        fileEntity.setChunked(true);
        httpPost.setEntity(fileEntity);

        HttpParams params = httpPost.getParams();
        HttpConnectionParams.setConnectionTimeout(params, _timeout);
        HttpConnectionParams.setSoTimeout(params, _timeout);
        HttpConnectionParams.setStaleCheckingEnabled(params, true);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpClientParams.setRedirecting(params, true);
        HttpProtocolParams.setUseExpectContinue(params, false);
        HttpProtocolParams.setUserAgent(params, DEFAULT_USER_AGENT);

        try {

            HttpResponse response = DEFAULT_HTTP_CLIENT.execute(httpHost, httpPost);

            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300)
                throw new IOException("bad status code, upload file " + response.getStatusLine().getStatusCode());

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            new IOException("Http error: " + e.getMessage(), e);
        } finally {
            //
        }
    }

    public void post(FileEntity fileEntity) throws IOException {
        HttpClient httpClient = setupHttpClient(false);
        
        HttpHost httpHost = new HttpHost(_uri.getHost(), _uri.getPort());
        HttpPost httpPost = new HttpPost(_uri);
        httpPost.setEntity(fileEntity);

        HttpParams params = httpPost.getParams();
        HttpConnectionParams.setConnectionTimeout(params, _timeout);
        HttpConnectionParams.setSoTimeout(params, _timeout);
        HttpConnectionParams.setStaleCheckingEnabled(params, true);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpClientParams.setRedirecting(params, true);
        HttpProtocolParams.setUseExpectContinue(params, false);
        HttpProtocolParams.setUserAgent(params, DEFAULT_USER_AGENT);

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

    public void asyncPostJSON(String json, HttpFetcherListener listener) {
        asyncPost(json, "application/json", listener);
    }

    public void asyncPost(final String body, final String contentType, final HttpFetcherListener listener) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] post = post(body, contentType);
                    if (listener != null) {
                        listener.onSuccess(post);
                    }
                } catch (Throwable e) {
                    listener.onError(e);
                }
            }
        });
        thread.setName("HttpFetcher-asyncPost");
        thread.start();
    }

    public void asyncRequest(HttpRequestInfo reqInfo, HttpFetcherListener listener) {
        if (reqInfo.isGET()) {
            asyncGet(listener);
        } else {
            asyncPost(reqInfo.getBody(), reqInfo.getContentType(), listener);
        }
    }

    private void asyncGet(final HttpFetcherListener listener) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                byte[] fetch = fetch();
                if (fetch == null) {
                    listener.onError(new Exception("HttpFetch.fetch() failed."));
                } else {
                    listener.onSuccess(fetch);
                }
            }

        });
        thread.setName("HttpFetcher-asyncGet");
        thread.start();
    }

    private static void setupHttpClients() {
        DEFAULT_HTTP_CLIENT = setupHttpClient(false);
        DEFAULT_HTTP_CLIENT_GZIP = setupHttpClient(true);
    }

    private static HttpClient setupHttpClient(boolean gzip) {

        SSLSocketFactory.getSocketFactory().setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        BasicHttpParams params = new BasicHttpParams();
        params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(20));
        params.setIntParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 200);
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

        DefaultHttpClient httpClient = new DefaultHttpClient(cm, new BasicHttpParams());
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));

        if (gzip) {
            httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
                public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                    if (!request.containsHeader("Accept-Encoding")) {
                        request.addHeader("Accept-Encoding", "gzip");
                    }
                }
            });

            httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
                public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
                    HttpEntity entity = response.getEntity();
                    Header ceheader = entity.getContentEncoding();
                    if (ceheader != null) {
                        HeaderElement[] codecs = ceheader.getElements();
                        for (int i = 0; i < codecs.length; i++) {
                            if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                                response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                                return;
                            }
                        }
                    }
                }
            });
        }

        return httpClient;
    }

    public static void writeEntity(final HttpEntity entity, OutputStream output) throws IOException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        InputStream instream = entity.getContent();
        if (instream == null) {
            return;
        }
        int i = (int) entity.getContentLength();
        if (i < 0) {
            i = 4096;
        }
        try {
            byte[] tmp = new byte[4096];
            int l;
            while ((l = instream.read(tmp)) != -1) {
                output.write(tmp, 0, l);
                output.flush();
            }
        } finally {
            instream.close();
        }
    }

    private static URI convert(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static class HttpRequestInfo {

        private boolean _isGet;
        private String _body;
        private String _contentType;

        public HttpRequestInfo(boolean get, String body, String type) {
            _isGet = get;
            _body = body;
            _contentType = type;
        }

        public String getBody() {
            return _body;
        }

        public String getContentType() {
            return _contentType;
        }

        public boolean isGET() {
            return _isGet;
        }

    }

    private static final class GzipDecompressingEntity extends HttpEntityWrapper {

        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }
}
