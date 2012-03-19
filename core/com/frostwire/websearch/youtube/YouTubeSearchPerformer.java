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

package com.frostwire.websearch.youtube;

import java.util.ArrayList;
import java.util.List;

import jd.controlling.linkcollector.LinkCollectingJob;
import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.linkcrawler.LinkCrawler;
import jd.plugins.FilePackage;

import com.frostwire.HttpFetcher;
import com.frostwire.JsonEngine;
import com.frostwire.bittorrent.websearch.WebSearchPerformer;
import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.limegroup.gnutella.settings.SearchEnginesSettings;
import com.limegroup.gnutella.util.UrlUtils;

public class YouTubeSearchPerformer implements WebSearchPerformer {

    public List<WebSearchResult> search(String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();

        YouTubeResponse response = searchYouTube(keywords);

        if (response != null && response.feed != null && response.feed.entry != null)
            for (YouTubeEntry entry : response.feed.entry) {

                WebSearchResult sr = new YouTubeSearchResult(entry);

                result.add(sr);
            }

        return result;
    }

    private YouTubeResponse searchYouTube(String keywords) {
        String q = UrlUtils.encode(keywords);
        int maxResults = SearchEnginesSettings.YOUTUBE_WEBSEARCHPERFORMER_MAX_RESULTS.getValue();
        String url = String.format("https://gdata.youtube.com/feeds/api/videos?q=%s&orderby=relevance&start-index=1&max-results=%d&alt=json&prettyprint=true&v=2", q, maxResults);

        HttpFetcher fetcher = new HttpFetcher(url, HTTP_TIMEOUT);
        byte[] jsonBytes = fetcher.fetch();

        if (jsonBytes == null) {
            return null;
        }

        String json = new String(jsonBytes);

        json = fixJson(json);

        JsonEngine engine = new JsonEngine();

        YouTubeResponse response = null;

        try {
            response = engine.toObject(json, YouTubeResponse.class);
        } catch (Exception e) {
            return null;
        }

        return response;
    }

    private String fixJson(String json) {
        return json.replace("\"$t\"", "\"title\"");
    }

    private List<WebSearchResult> crawlLinks(YouTubeEntry entry) {

        LinkCollector collector = LinkCollector.getInstance();
        LinkCrawler crawler = collector.addCrawlerJob(new LinkCollectingJob(readVideoUrl(entry)));

        crawler.waitForCrawling();

        List<FilePackage> packages = new ArrayList<FilePackage>();
        for (CrawledPackage pkg : new ArrayList<CrawledPackage>(collector.getPackages())) {
            for (CrawledLink link : new ArrayList<CrawledLink>(pkg.getChildren())) {
                ArrayList<CrawledLink> links = new ArrayList<CrawledLink>();
                links.add(link);
                packages.addAll(collector.removeAndConvert(links));
            }
        }

        List<WebSearchResult> results = new ArrayList<WebSearchResult>();

        for (FilePackage pkg : packages) {
            //results.add(new YouTubeSearchResult(entry, pkg));
        }

        return results;
    }

    private String readVideoUrl(YouTubeEntry entry) {
        String url = null;

        for (YouTubeEntryLink link : entry.link) {
            if (link.rel.equals("alternate")) {
                url = link.href;
            }
        }

        url = url.replace("https://", "http://").replace("&feature=youtube_gdata", "");

        return url;
    }
}
