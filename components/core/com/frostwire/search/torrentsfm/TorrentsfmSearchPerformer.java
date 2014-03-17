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

package com.frostwire.search.torrentsfm;

import com.frostwire.search.CrawlableSearchResult;
import com.frostwire.search.SearchMatcher;
import com.frostwire.search.domainalias.DomainAliasManager;
import com.frostwire.search.torrent.TorrentRegexSearchPerformer;

/**
 * Search Performer for torrents.com / torrents.fm
 * @author gubatron
 * @author aldenml
 *
 */
public class TorrentsfmSearchPerformer extends TorrentRegexSearchPerformer<TorrentsfmSearchResult> {

    private static final int MAX_RESULTS = 20;
    private static final String REGEX = "(?is)<li class=\"grid_6 alpha omega\"><div class=\"grid_3 alpha omega\"><a title=\"(.*?)\" href=\'(.*?)\'>.*?</span> Download</a></div></li>";
    private static final String HTML_REGEX = "(?is)<section id=\"download\" class=\"grid_24\">.*?<div class=\"grid_17 omega\"><h1>(.*?)</h1>.*?<div class=\"size\">(.*?)</div>.*?<span title=\"([0-9]*) seeds / [0-9]* leechers\">.*?<dl class=\"date\"><dt>Created</dt><dd>(.*?)</dd></dl>.*?<a class=\"download\".*?data-track=\"Download,Magnet,File / Big Button\" data-downloader=\"1\" href=\"(.*?)\"><span class=\"icon download-button\">";

    // matcher groups: 1 -> title
    //                 2 -> file size (needs parsing)
    //                 3 -> seeds
    //                 4 -> creation date, e.g. 2013-10-17 11:53:27
    //                 5 -> magnet url

    public TorrentsfmSearchPerformer(DomainAliasManager domainAliasManager, long token, String keywords, int timeout) {
        super(domainAliasManager, token, keywords, timeout, 1, 2 * MAX_RESULTS, MAX_RESULTS, REGEX, HTML_REGEX);
    }

    @Override
    protected String getUrl(int page, String encodedKeywords) {
        String searchParameter = encodedKeywords.replace(" ", "_");
        return "http://" + getDomainNameToUse() + "/search/" + searchParameter;
    }

    @Override
    public CrawlableSearchResult fromMatcher(SearchMatcher matcher) {
        String displayName = matcher.group(1);
        String itemId = matcher.group(2);
        return new TorrentsfmTempSearchResult(getDomainNameToUse(), itemId, displayName);
    }

    @Override
    protected TorrentsfmSearchResult fromHtmlMatcher(CrawlableSearchResult sr, SearchMatcher matcher) {
        return new TorrentsfmSearchResult(getDomainNameToUse(), sr.getDetailsUrl(), matcher);
    }
}