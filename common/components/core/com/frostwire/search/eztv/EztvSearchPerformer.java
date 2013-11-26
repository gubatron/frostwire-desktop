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

package com.frostwire.search.eztv;

import java.util.HashMap;
import java.util.Map;

import com.frostwire.search.CrawlableSearchResult;
import com.frostwire.search.SearchMatcher;
import com.frostwire.search.domainalias.DomainAliasManager;
import com.frostwire.search.torrent.TorrentRegexSearchPerformer;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class EztvSearchPerformer extends TorrentRegexSearchPerformer<EztvSearchResult> {

    private static final int MAX_RESULTS = 20;
    private static final String REGEX = "(?is)<a href=\"(/ep/.*?)\"";
    private static final String HTML_REGEX = "(?is)<td class=\"section_post_header\" colspan=\"2\"><b>(.*?)</b></td>.*?<td class=\"section_post_header\">Download Links</td>.*?<a href=\"(http://.*?torrent)\".*?<a href=\"magnet:\\?xt=urn:btih:(.*?)&.*?\".*?<b>Released:</b> (.*?)<br />.*?<b>Filesize:</b> (.*?)<br />";

    public EztvSearchPerformer(DomainAliasManager domainAliasManager, long token, String keywords, int timeout) {
        super(domainAliasManager, token, keywords, timeout, 1, 2 * MAX_RESULTS, MAX_RESULTS, REGEX, HTML_REGEX); 
    }

    @Override
    protected String fetchSearchPage(String url) {
        Map<String, String> formData = new HashMap<String, String>();
        formData.put("SearchString1", getEncodedKeywords());
        formData.put("SearchString", "");
        formData.put("search", "Search");
        return post(url, formData);
    }

    @Override
    protected String getUrl(int page, String encodedKeywords) {
        return "http://"+getDomainNameToUse()+"/search/";
    }

    @Override
    public CrawlableSearchResult fromMatcher(SearchMatcher matcher) {
        String itemId = matcher.group(1);
        return new EztvTempSearchResult(getDomainNameToUse(),itemId);
    }

    @Override
    protected EztvSearchResult fromHtmlMatcher(CrawlableSearchResult sr, SearchMatcher matcher) {
        return new EztvSearchResult(sr.getDetailsUrl(), matcher);
    }
}
