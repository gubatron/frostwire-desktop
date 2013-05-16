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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pure java based HTTP client with resume capabilities.
 * @author gubatron
 * @author aldenml
 *
 */
final class FWHttpClient implements HttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(FWHttpClient.class);

    private static final int DEFAULT_TIMEOUT = 10000;
    private static final String DEFAULT_USER_AGENT = UserAgentGenerator.getUserAgent();
    private HttpClientListener listener;

    private boolean canceled;

    public String get(String url) {
        return get(url, DEFAULT_TIMEOUT, DEFAULT_USER_AGENT);
    }

    public String get(String url, int timeout) {
        return get(url, timeout, DEFAULT_USER_AGENT);
    }

    public String get(String url, int timeout, String userAgent) {
        return get(url, timeout, userAgent, null);
    }

    public String get(String url, int timeout, String userAgent, String referrer) {
        String result = null;

        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            get(url, baos, timeout, userAgent, referrer, -1);

            result = new String(baos.toByteArray(), "UTF-8");
        } catch (Throwable e) {
            LOG.error("Error getting string from http body response: " + e.getMessage(),e);
        } finally {
            closeQuietly(baos);
        }

        return result;
    }
    
    public byte[] getBytes(String url, int timeout, String userAgent, String referrer) {
        byte[] result = null;

        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            get(url, baos, timeout, userAgent, referrer, -1);

            result = baos.toByteArray();
        } catch (Throwable e) {
            LOG.warn("Error getting string from http body response: " + e.getMessage());
        } finally {
            closeQuietly(baos);
        }

        return result;
    }

    public void save(String url, File file, boolean resume) throws IOException {
        save(url, file, resume, DEFAULT_TIMEOUT, DEFAULT_USER_AGENT);
    }

    public void save(String url, File file, boolean resume, int timeout, String userAgent) throws IOException {
        save(url, file, resume, timeout, userAgent, null);
    }

    public void save(String url, File file, boolean resume, int timeout, String userAgent, String referrer) throws IOException {
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

            get(url, fos, timeout, userAgent, referrer, rangeStart);
        } finally {
            closeQuietly(fos);
        }
    }

    private String buildRange(int rangeStart, int rangeLength) {
        String prefix = "bytes=" + rangeStart + "-";
        return prefix + ((rangeLength > -1) ? (rangeStart + rangeLength) : "");
    }

    private void get(String url, OutputStream out, int timeout, String userAgent, String referrer, int rangeStart) throws IOException {
        get(url, out, timeout, userAgent, referrer, rangeStart, -1);
    }

    private void get(String url, OutputStream out, int timeout, String userAgent, String referrer, int rangeStart, int rangeLength) throws IOException {
        canceled = false;
        URL u = new URL(url);
        URLConnection conn = u.openConnection();

        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setRequestProperty("User-Agent", userAgent);

        if (referrer != null) {
            conn.setRequestProperty("Referer", referrer);
        }

        if (conn instanceof HttpsURLConnection) {
            setHostnameVerifier((HttpsURLConnection) conn);
        }

        if (rangeStart > 0) {
            conn.setRequestProperty("Range", buildRange(rangeStart, rangeLength));
        }

        InputStream in = conn.getInputStream();

        int httpResponseCode = getResponseCode(conn);

        if (httpResponseCode != HttpURLConnection.HTTP_OK && 
            httpResponseCode != HttpURLConnection.HTTP_PARTIAL &&
            httpResponseCode != HttpURLConnection.HTTP_MOVED_PERM &&
            httpResponseCode != HttpURLConnection.HTTP_MOVED_TEMP &&
            httpResponseCode != -1) { //some servers return -1 on 302
            throw new ResponseCodeNotSupportedException(httpResponseCode);
        }
        
        if (httpResponseCode == HttpURLConnection.HTTP_MOVED_PERM ||
            httpResponseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
            httpResponseCode == -1) {
            String newLocation = null;
            if (httpResponseCode == -1) {
                String outStr = readString(in);
                String[] split = outStr.split("\r\n");
                for (String line : split) {
                    if (line.contains("Location:")) {
                        newLocation = line.substring(8).trim();
                        LOG.info("Got HTTP " + newLocation + " Redirect response to " + newLocation);
                        get(newLocation.trim(),out,timeout,userAgent,referrer,rangeStart,rangeLength);
                    }
                }
                throw new MissingFormatArgumentException("HTTP " + httpResponseCode + " response missing 'Location:' header for redirect.");
                
            } else if (conn.getHeaderFields().containsKey("Location") &&
                !StringUtils.isNullOrEmpty(newLocation = conn.getHeaderField("Location"))) {
                LOG.info("Got HTTP " + newLocation + " Redirect response to " + newLocation);
                get(newLocation.trim(),out,timeout,userAgent,referrer,rangeStart,rangeLength);
            } else {
                throw new MissingFormatArgumentException("HTTP " + httpResponseCode + " response missing 'Location:' header for redirect.");
            }
        } else {

            onHeaders(conn.getHeaderFields());

            checkRangeSupport(rangeStart, conn);
    
            try {
                byte[] b = new byte[4096];
                int n = 0;
                while (!canceled && (n = in.read(b, 0, b.length)) != -1) {
                    if (!canceled) {
                        out.write(b, 0, n);
                        onData(b, 0, n);
                    }
                }
    
                closeQuietly(out);
    
                if (canceled) {
                    onCancel();
                } else {
                    onComplete();
                }
            } catch (Exception e) {
                onError(e);
            } finally {
                closeQuietly(in);
                closeQuietly(conn);
            }
        }
    }

    private String readString(InputStream in) throws IOException {
        char[] buffer = new char[2048]; 
        new InputStreamReader(in).read(buffer, 0, 2048);
        String outStr = new String(buffer);
        return outStr;
    }

    private void setHostnameVerifier(HttpsURLConnection conn) {
        conn.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    private int getResponseCode(URLConnection conn) {
        try {
            return ((HttpURLConnection) conn).getResponseCode();
        } catch (IOException e) {
            return -1;
        }
    }

    private void checkRangeSupport(int rangeStart, URLConnection conn) throws HttpRangeOutOfBoundsException, RangeNotSupportedException {

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

    private void closeQuietly(URLConnection conn) {
        if (conn instanceof HttpURLConnection) {
            try {
                ((HttpURLConnection) conn).disconnect();
            } catch (Throwable e) {
                LOG.debug("Error closing http connection", e);
            }
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
        canceled = true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }
}