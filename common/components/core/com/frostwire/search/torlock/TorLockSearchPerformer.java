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

package com.frostwire.search.torlock;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.frostwire.search.CrawlRegexSearchPerformer;
import com.frostwire.search.CrawlableSearchResult;
import com.frostwire.search.PerformersHelper;
import com.frostwire.search.SearchResult;
import com.frostwire.search.torrent.TorrentCrawlableSearchResult;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class TorLockSearchPerformer extends CrawlRegexSearchPerformer<CrawlableSearchResult> {

    private static final int MAX_RESULTS = 10;

    public TorLockSearchPerformer(long token, String keywords, int timeout) {
        super(token, keywords, timeout, 1, 2 * MAX_RESULTS, MAX_RESULTS);
    }

    private static final String REGEX = "(?is)<a href=/torrent/([0-9]*?/.*?\\.html)>";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private static final String HTML_REGEX = "(?is).*?<td><b>Name:</b></td><td>(.*?).torrent</td>.*?<td><b>Size:</b></td><td>(.*?) in .*? files</td>.*?<td><b>Added:</b></td><td>Uploaded on (.*?) by .*?</td>.*?<font color=#FF5400><b>(.*?)</b></font> seeders.*?<td align=center><a href=\"/tor/(.*?).torrent\"><img src=http://www.torlock.com/images/dlbutton2.png></a></td>.*?";
    private static final Pattern HTML_PATTERN = Pattern.compile(HTML_REGEX);

    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    @Override
    public CrawlableSearchResult fromMatcher(Matcher matcher) {
        String itemId = matcher.group(1);
        return new TorLockTempSearchResult(itemId);
    }

    @Override
    protected String getUrl(int page, String encodedKeywords) {
        return "http://www.torlock.com/?sort=seeds&c=&search=Search&q=" + encodedKeywords;
    }

    @Override
    protected String getCrawlUrl(CrawlableSearchResult sr) {
        String crawlUrl = null;

        if (sr instanceof TorLockTempSearchResult) {
            crawlUrl = sr.getDetailsUrl();
        } else if (sr instanceof TorLockSearchResult) {
            crawlUrl = ((TorLockSearchResult) sr).getTorrentUrl();
        }

        return crawlUrl;
    }

    @Override
    protected List<? extends SearchResult> crawlResult(CrawlableSearchResult sr, byte[] data) throws Exception {
        List<SearchResult> list = new LinkedList<SearchResult>();

        if (sr instanceof TorLockTempSearchResult) {
            String html = new String(data, "UTF-8");

            Matcher matcher = HTML_PATTERN.matcher(html);

            if (matcher.find()) {
                list.add(new TorLockSearchResult(sr.getDetailsUrl(), matcher));
            }
        } else if (sr instanceof TorLockSearchResult) {
            list.addAll(PerformersHelper.crawlTorrent(this, (TorrentCrawlableSearchResult) sr, data));
        }

        return list;
    }
}
