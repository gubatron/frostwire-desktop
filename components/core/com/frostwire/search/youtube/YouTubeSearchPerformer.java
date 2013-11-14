/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
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

package com.frostwire.search.youtube;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.frostwire.search.CrawlPagedWebSearchPerformer;
import com.frostwire.search.SearchResult;
import com.frostwire.search.extractors.YouTubeExtractor;
import com.frostwire.search.extractors.YouTubeExtractor.LinkInfo;
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
        return null;
    }

    @Override
    protected List<? extends SearchResult> crawlResult(YouTubeSearchResult sr, byte[] data) throws Exception {
        List<YouTubeCrawledSearchResult> list = new LinkedList<YouTubeCrawledSearchResult>();

        List<LinkInfo> infos = new YouTubeExtractor().extract(sr.getDetailsUrl());
        List<YouTubeDownloadLink> ytLinks = new LinkedList<YouTubeDownloadLink>();
        for (LinkInfo inf : infos) {
            YouTubeDownloadLink dl = new YouTubeDownloadLink(inf.filename, inf.format, inf.size, inf.link, inf.fmt);
            ytLinks.add(dl);
        }

        for (YouTubeDownloadLink link : ytLinks) {
            list.add(new YouTubeCrawledSearchResult(sr, link));
        }

        //        YouTubeDownloadLink audioLink = getAudioLink(ytLinks);
        //        if (audioLink != null) {
        //            list.add(new YouTubeCrawledSearchResult(sr, audioLink));
        //        }

        return list;
    }

    @Override
    protected String getUrl(int page, String encodedKeywords) {
        return String.format(Locale.US, "https://gdata.youtube.com/feeds/api/videos?q=%s&orderby=relevance&start-index=1&max-results=%d&alt=json&prettyprint=true&v=2", encodedKeywords, MAX_RESULTS);
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

    /**
     * Picks the highest quality audio link at the lowest size possible.
     * @param list
     * @return
     */
    //    private YouTubeDownloadLink getAudioLink(List<YouTubeDownloadLink> list) {
    //
    //        YouTubeDownloadLink result = null;
    //        YouTubeDownloadLink result1 = null;
    //        YouTubeDownloadLink result2 = null;
    //        YouTubeDownloadLink result3 = null;
    //        String qualityStr = null;
    //
    //        for (YouTubeDownloadLink link : list) {
    //            int iTag = link.getITag();
    //            if (iTag == 22) {
    //                result1 = link;
    //            }
    //
    //            if (iTag == 37) {
    //                result2 = link;
    //            }
    //
    //            if (iTag == 18) {
    //                result3 = link;
    //            }
    //        }
    //
    //        if (result1 != null) {
    //            result = result1;
    //            qualityStr = "_192k.m4a";
    //        } else if (result2 != null) {
    //            result = result2;
    //            qualityStr = "_192k.m4a";
    //
    //        } else if (result3 != null) {
    //            result = result3;
    //            qualityStr = "_96k.m4a";
    //        }
    //
    //        if (result != null) {
    //            String filename = FilenameUtils.getBaseName(result.getFilename()) + qualityStr;
    //            result = new YouTubeDownloadLink(filename, result.getSize(), result.getDownloadUrl(), result.getITag(), true);
    //        }
    //
    //        return result;
    //    }

}
