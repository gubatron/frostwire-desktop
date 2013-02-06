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
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A pure java based HTTP client with resume capabilities.
 * @author gubatron
 * @author aldenml
 *
 */
final class FWHttpClient implements HttpClient {

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
        URL u = new URL(url);
        URLConnection conn = u.openConnection();
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setRequestProperty("User-Agent", userAgent);

        if (rangeStart > 0) {
            conn.setRequestProperty("Range", buildRange(rangeStart, rangeLength));
        }

        InputStream in = conn.getInputStream();

        long expectedFileSize = getContentLength(conn);

        if (expectedFileSize > -1) {
            onContentLength(expectedFileSize);
        }

        if (rangeStart > 0 && rangeStart > expectedFileSize) {
            HttpRangeOutOfBoundsException httpRangeOutOfBoundsException = new HttpRangeOutOfBoundsException(rangeStart, expectedFileSize);
            listener.onError(this, httpRangeOutOfBoundsException);
            throw httpRangeOutOfBoundsException;
        }

        if (rangeStart > 0 && !conn.getHeaderField("Accept-Ranges").equals("bytes")) {
            RangeNotSupportedException rangeNotSupportedException = new RangeNotSupportedException("Server does not support bytes range request");
            listener.onError(this, rangeNotSupportedException);
            throw rangeNotSupportedException;
        }

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

    public static class HttpRangeException extends IOException {

        /**
         * 
         */
        private static final long serialVersionUID = 1891038288667531894L;

        public HttpRangeException(String message) {
            super(message);
        }
    }

    public static final class RangeNotSupportedException extends HttpRangeException {

        private static final long serialVersionUID = -3356618211960630147L;

        public RangeNotSupportedException(String message) {
            super(message);
        }
    }

    public static final class HttpRangeOutOfBoundsException extends HttpRangeException {

        private static final long serialVersionUID = -335661829606230147L;

        public HttpRangeOutOfBoundsException(int rangeStart, long expectedFileSize) {
            super("HttpRange Out of Bounds error: start=" + rangeStart + " expected file size=" + expectedFileSize);
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