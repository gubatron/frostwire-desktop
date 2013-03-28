/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
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

package com.frostwire.search.youtube2;

import java.util.LinkedList;
import java.util.List;

import com.frostwire.search.CrawlPagedWebSearchPerformer;
import com.frostwire.search.SearchResult;
import com.frostwire.util.JsonUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class YouTubeSearchPerformer extends CrawlPagedWebSearchPerformer<YouTubeSearchResult> {

    private static final int MAX_RESULTS = 15;

    public YouTubeSearchPerformer(long token, String keywords, int timeout) {
        super(token, keywords, timeout, 1, MAX_RESULTS);
    }

    @Override
    protected String getCrawlUrl(YouTubeSearchResult sr) {
        return sr.getDetailsUrl();
    }

    @Override
    protected List<? extends SearchResult> crawlResult(YouTubeSearchResult sr, byte[] data) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getUrl(int page, String encodedKeywords) {
        return String.format("https://gdata.youtube.com/feeds/api/videos?q=%s&orderby=relevance&start-index=1&max-results=%d&alt=json&prettyprint=true&v=2", encodedKeywords, MAX_RESULTS);
    }

    @Override
    protected List<? extends SearchResult> searchPage(String page) {
        List<SearchResult> result = new LinkedList<SearchResult>();

        String json = fixJson(page);
        YouTubeResponse response = JsonUtils.toObject(json, YouTubeResponse.class);

        for (YouTubeEntry entry : response.feed.entry) {
            if (!isStopped()) {
                YouTubeSearchResult sr = new YouTubeSearchResult(entry);
                result.add(sr);
            }
        }

        return result;
    }

    private String fixJson(String json) {
        return json.replace("\"$t\"", "\"title\"").replace("\"yt$userId\"", "\"ytuserId\"");
    }
}
