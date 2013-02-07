/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A pure java based HTTP client with resume capabilities.
 * @author gubatron
 * @author aldenml
 *
 */
final class FWHttpClient implements HttpClient {

    static {
        sun.net.www.protocol.https.HttpsURLConnectionImpl.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        });
    }

    private static final Log LOG = LogFactory.getLog(FWHttpClient.class);

    private static final int DEFAULT_TIMEOUT = 10000;
    private static final String DEFAULT_USER_AGENT = UserAgentGenerator.getUserAgent();
    private HttpClientListener listener;

    private boolean cancel;

    public String get(String url) {
        return get(url, DEFAULT_TIMEOUT, DEFAULT_USER_AGENT);
    }

    public String get(String url, int timeout, String userAgent) {
        String result = null;

        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            get(url, baos, timeout, userAgent, -1);

            result = new String(baos.toByteArray(), "UTF-8");
        } catch (Throwable e) {
            // ignore
        } finally {
            closeQuietly(baos);
        }

        return result;
    }

    public void save(String url, File file, boolean resume) throws IOException {
        save(url, file, resume, DEFAULT_TIMEOUT, DEFAULT_USER_AGENT);
    }

    public void save(String url, File file, boolean resume, int timeout, String userAgent) throws IOException {
        FileOutputStream fos = null;
        int rangeStart = 0;

        try {
            if (resume && file.exists()) {
                fos = new FileOutputStream(file, true);
                rangeStart = (int) file.length();
            } else {
                fos = new FileOutputStream(file, false);
                rangeStart = -1;
            }

            get(url, fos, timeout, userAgent, rangeStart);
        } finally {
            closeQuietly(fos);
        }
    }

    private String buildRange(int rangeStart, int rangeLength) {
        String prefix = "bytes=" + rangeStart + "-";
        return prefix + ((rangeLength > -1) ? (rangeStart + rangeLength) : "");
    }

    private void get(String url, OutputStream out, int timeout, String userAgent, int rangeStart) throws IOException {
        get(url, out, timeout, userAgent, rangeStart, -1);
    }

    private void get(String url, OutputStream out, int timeout, String userAgent, int rangeStart, int rangeLength) throws IOException {
        cancel = false;
        URL u = new URL(url);
        URLConnection conn = (java.net.URLConnection) u.openConnection();

        System.out.println(conn.getClass());

        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setRequestProperty("User-Agent", userAgent);

        if (rangeStart > 0) {
            conn.setRequestProperty("Range", buildRange(rangeStart, rangeLength));
        }

        InputStream in = conn.getInputStream();
        
        int httpResponseCode = getResponseCode(conn);
        
        if (httpResponseCode != HttpURLConnection.HTTP_OK &&
            httpResponseCode != HttpURLConnection.HTTP_PARTIAL) {
            throw new ResponseCodeNotSupportedException(httpResponseCode);
        }

        onHeaders(conn.getHeaderFields());

        long expectedFileSize = getContentLength(conn);

        if (expectedFileSize > -1) {
            onContentLength(expectedFileSize);
        }

        checkRangeSupport(rangeStart, conn, expectedFileSize);

        try {
            byte[] b = new byte[4096];
            int n = 0;
            while (!cancel && (n = in.read(b, 0, b.length)) != -1) {
                if (!cancel) {
                    out.write(b, 0, n);
                    onData(b, 0, n);
                }
            }

            closeQuietly(out);

            if (cancel) {
                onCancel();
            } else {
                onComplete();
            }
        } catch (Exception e) {
            onError(e);
        } finally {
            closeQuietly(in);
        }
    }

    private int getResponseCode(URLConnection conn) {
           try {
            return ((HttpURLConnection) conn).getResponseCode();
        } catch (IOException e) {
            return -1;
        }
    }

    private void checkRangeSupport(int rangeStart, URLConnection conn, long expectedFileSize) throws HttpRangeOutOfBoundsException, RangeNotSupportedException {
        int responseCode = getResponseCode(conn);
        
        if (rangeStart > 0 && rangeStart > expectedFileSize) {
            HttpRangeOutOfBoundsException httpRangeOutOfBoundsException = new HttpRangeOutOfBoundsException(rangeStart, expectedFileSize);
            onError(httpRangeOutOfBoundsException);
            throw httpRangeOutOfBoundsException;
        }

        boolean hasContentRange = conn.getHeaderField("Content-Range") != null;
        boolean hasAcceptRanges = conn.getHeaderField("Accept-Ranges") != null && conn.getHeaderField("Accept-Ranges").equals("bytes");
        
        if (rangeStart > 0 && !hasContentRange && !hasAcceptRanges) {
            RangeNotSupportedException rangeNotSupportedException = new RangeNotSupportedException("Server does not support bytes range request");
            onError(rangeNotSupportedException);
            throw rangeNotSupportedException;
        }
    }

    private void onHeaders(Map<String, List<String>> headerFields) {
        if (getListener() != null) {
            try {
                getListener().onHeaders(this, headerFields);
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }

    private void onCancel() {
        if (getListener() != null) {
            try {
                getListener().onCancel(this);
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }

    private void onContentLength(long contentLength) {
        if (getListener() != null) {
            try {
                getListener().onContentLength(contentLength);
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }

    private long getContentLength(URLConnection conn) {
        long length = -1;
        String headerValue = conn.getHeaderField("Content-Length");

        if (headerValue != null) {
            try {
                length = Long.parseLong(headerValue);
            } catch (NumberFormatException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        return length;
    }

    private void onData(byte[] b, int i, int n) {
        if (getListener() != null) {
            try {
                getListener().onData(this, b, 0, n);
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }

    protected void onError(Exception e) {
        if (getListener() != null) {
            try {
                getListener().onError(this, e);
            } catch (Exception e2) {
                LOG.warn(e2.getMessage(), e2);
            }
        }
    }

    protected void onComplete() {
        if (getListener() != null) {
            try {
                getListener().onComplete(this);
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    @Override
    public void setListener(HttpClientListener listener) {
        this.listener = listener;
    }

    @Override
    public HttpClientListener getListener() {
        return listener;
    }

    @Override
    public void cancel() {
        cancel = true;
    }
}