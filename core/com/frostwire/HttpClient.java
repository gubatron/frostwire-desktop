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

package com.frostwire;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class HttpClient {

    private static final int DEFAULT_TIMEOUT = 10000;
    private static final String DEFAULT_USER_AGENT = UserAgentGenerator.getUserAgent();

    public String get(String url) {
        return get(url, DEFAULT_TIMEOUT, DEFAULT_USER_AGENT);
    }

    public String get(String url, int timeout, String userAgent) {
        String result = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            get(url, baos, timeout, userAgent, -1);

            result = new String(baos.toByteArray(), "UTF-8");
        } catch (Throwable e) {
            // ignore
        }

        return result;
    }

    public void save(String url, File file, boolean resume) throws IOException {
        save(url, file, resume, DEFAULT_TIMEOUT, DEFAULT_USER_AGENT);
    }

    public void save(String url, File file, boolean resume, int timeout, String userAgent) throws IOException {
        FileOutputStream fos = null;
        int rangeStart = 0;

        if (resume && file.exists()) {
            fos = new FileOutputStream(file, true);
            rangeStart = (int) file.length();
        } else {
            fos = new FileOutputStream(file);
            rangeStart = -1;
        }

        get(url, fos, timeout, userAgent, rangeStart);
    }

    private void get(String url, OutputStream out, int timeout, String userAgent, int rangeStart) throws IOException {
        URL u = new URL(url);
        URLConnection conn = u.openConnection();
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setRequestProperty("User-Agent", userAgent);

        if (rangeStart > 0) {
            conn.setRequestProperty("Range", "bytes=" + rangeStart + "-");
        }

        InputStream in = conn.getInputStream();

        if (rangeStart > 0 && !conn.getHeaderField("Accept-Ranges").equals("bytes")) {
            throw new IOException("Server does not support bytes range request");
        }

        try {

            byte[] b = new byte[1024];
            int n = 0;
            while ((n = in.read(b, 0, b.length)) != -1) {
                out.write(b, 0, n);
            }
        } finally {
            try {
                out.close();
            } catch (Throwable e) {
                // ignore   
            }
            try {
                in.close();
            } catch (Throwable e) {
                // ignore   
            }
        }
    }
}
