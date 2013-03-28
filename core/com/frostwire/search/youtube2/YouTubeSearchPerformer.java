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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.linkcrawler.LinkCrawler;
import jd.controlling.linkcrawler.PackageInfo;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.jdownloader.controlling.filter.LinkFilterController;

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
        List<YouTubeCrawledSearchResult> list = new LinkedList<YouTubeCrawledSearchResult>();

        LinkCollector collector = LinkCollector.getInstance();
        LinkCrawler crawler = new LinkCrawler();
        crawler.setFilter(LinkFilterController.getInstance());
        crawler.crawl(sr.getDetailsUrl());
        crawler.waitForCrawling();

        final List<FilePackage> packages = new ArrayList<FilePackage>();

        for (CrawledLink link : crawler.getCrawledLinks()) {
            CrawledPackage parent = PackageInfo.createCrawledPackage(link);
            parent.setControlledBy(collector);
            link.setParentNode(parent);
            ArrayList<CrawledLink> links = new ArrayList<CrawledLink>();
            links.add(link);
            packages.add(createFilePackage(parent, links));
        }

        for (FilePackage p : packages) {
            //no youtube mp3
            if (p.getChildren().get(0).getFileOutput().endsWith(".mp3")) {
                continue;
            }

            list.add(new YouTubeCrawledSearchResult(sr, p));
        }

        return list;
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

    private FilePackage createFilePackage(final CrawledPackage pkg, ArrayList<CrawledLink> plinks) {
        FilePackage ret = FilePackage.getInstance();
        /* set values */
        ret.setName(pkg.getName());
        ret.setDownloadDirectory(pkg.getDownloadFolder());
        ret.setCreated(pkg.getCreated());
        ret.setExpanded(pkg.isExpanded());
        ret.setComment(pkg.getComment());
        synchronized (pkg) {
            /* add Children from CrawledPackage to FilePackage */
            ArrayList<DownloadLink> links = new ArrayList<DownloadLink>(pkg.getChildren().size());
            List<CrawledLink> pkgLinks = pkg.getChildren();
            if (plinks != null && plinks.size() > 0)
                pkgLinks = new ArrayList<CrawledLink>(plinks);
            for (CrawledLink link : pkgLinks) {
                /* extract DownloadLink from CrawledLink */
                DownloadLink dl = link.getDownloadLink();
                if (dl != null) {
                    /*
                     * change filename if it is different than original
                     * downloadlink
                     */
                    if (link.isNameSet())
                        dl.forceFileName(link.getName());
                    /* set correct enabled/disabled state */
                    //dl.setEnabled(link.isEnabled());
                    /* remove reference to crawledLink */
                    dl.setNodeChangeListener(null);
                    dl.setCreated(link.getCreated());
                    links.add(dl);
                    /* set correct Parent node */
                    dl.setParentNode(ret);
                }
            }
            /* add all children to FilePackage */
            ret.getChildren().addAll(links);
        }
        return ret;
    }
}
