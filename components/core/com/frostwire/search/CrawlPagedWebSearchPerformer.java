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

package com.frostwire.search;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public abstract class CrawlPagedWebSearchPerformer<T extends CrawlableSearchResult> extends PagedWebSearchPerformer {

    private static final Logger LOG = LoggerFactory.getLogger(CrawlPagedWebSearchPerformer.class);

    private static final int DEFAULT_NUM_CRAWLS = 6;
    private static final int DEFAULT_CRAWL_TIMEOUT = 10000; // 10 seconds

    private static final CrawlCache cache = CrawlCacheFactory.newInstance();

    private int numCrawls;

    public CrawlPagedWebSearchPerformer(long token, String keywords, int timeout, int pages, int numCrawls) {
        super(token, keywords, timeout, pages);
        this.numCrawls = numCrawls;
    }

    public CrawlPagedWebSearchPerformer(long token, String keywords, int timeout, int pages) {
        this(token, keywords, timeout, pages, DEFAULT_NUM_CRAWLS);
    }

    public static CrawlCache getCache() {
        return cache;
    }

    @Override
    public void crawl(CrawlableSearchResult sr) {
        if (numCrawls > 0) {
            numCrawls--;

            T obj = cast(sr);
            if (obj != null) {

                String url = getCrawlUrl(obj);

                byte[] data = cacheGet(url);

                if (data == null) { // not a big deal about synchronization here
                    LOG.debug("Downloading data for: " + url);
                    data = fetchBytes(url, sr.getDetailsUrl(), DEFAULT_CRAWL_TIMEOUT);
                    if (data != null) {
                        cachePut(url, data);
                    } else {
                        LOG.warn("Failed to download data: " + url);
                    }
                }

                try {
                    if (data != null) {
                        List<? extends SearchResult> results = crawlResult(obj, data);
                        if (results != null) {
                            onResults(this, results);
                        }
                    }
                } catch (Throwable e) {
                    LOG.warn("Error creating crawled results from downloaded data: " + e.getMessage());
                    cacheRemove(url); // invalidating cache data
                }
            }
        }
    }

    protected abstract String getCrawlUrl(T sr);

    protected abstract List<? extends SearchResult> crawlResult(T sr, byte[] data) throws Exception;

    private byte[] cacheGet(String key) {
        synchronized (cache) {
            return cache.get(key);
        }
    }

    private void cachePut(String key, byte[] data) {
        synchronized (cache) {
            cache.put(key, data);
        }
    }

    private void cacheRemove(String key) {
        synchronized (cache) {
            cache.remove(key);
        }
    }

    @SuppressWarnings("unchecked")
    private T cast(CrawlableSearchResult sr) {
        try {
            return (T) sr;
        } catch (ClassCastException e) {
            LOG.warn("Something wrong with the logic, need to pass a crawlable search result with the correct type");
        }

        return null;
    }
}
