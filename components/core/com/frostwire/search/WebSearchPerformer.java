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

package com.frostwire.search;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frostwire.util.HttpClient;
import com.frostwire.util.HttpClientFactory;
import com.frostwire.util.URLUtils;
import com.frostwire.util.UserAgentGenerator;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public abstract class WebSearchPerformer extends AbstractSearchPerformer {

    private static final Logger LOG = LoggerFactory.getLogger(WebSearchPerformer.class);

    private static final String DEFAULT_USER_AGENT = UserAgentGenerator.getUserAgent();

    private final String keywords;
    private final String encodedKeywords;
    private final int timeout;
    private final HttpClient client;

    public WebSearchPerformer(long token, String keywords, int timeout) {
        super(token);
        this.keywords = keywords;
        this.encodedKeywords = URLUtils.encode(keywords);
        this.timeout = timeout;
        this.client = HttpClientFactory.newDefaultInstance();
    }

    public final String getKeywords() {
        return keywords;
    }

    public final String getEncodedKeywords() {
        return encodedKeywords;
    }

    @Override
    public void crawl(CrawlableSearchResult sr) {
        LOG.warn("Review your logic, calling deep search without implementation for: " + sr);
    }

    /**
     * Allow to perform the HTTP operation using the same internal http client.
     * 
     * @param url
     * @return the web page (html)
     */
    public String fetch(String url) {
        return fetch(url, null, null);
    }

    public String fetch(String url, String cookie, Map<String, String> customHeaders) {
        return client.get(url, timeout, DEFAULT_USER_AGENT, null, cookie, customHeaders);
    }

    
    /**
     * Allow to perform the HTTP operation using the same internal http client.
     * 
     * @param url
     * @return the raw bytes from the http connection
     */
    public final byte[] fetchBytes(String url) {
        return fetchBytes(url, null, timeout);
    }

    protected final byte[] fetchBytes(String url, String referrer, int timeout) {
        if (url.startsWith("htt")) { // http(s)
            return client.getBytes(url, timeout, DEFAULT_USER_AGENT, referrer);
        } else {
            return null;
        }
    }
}
