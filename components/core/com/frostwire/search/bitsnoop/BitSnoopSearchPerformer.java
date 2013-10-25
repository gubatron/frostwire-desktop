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

package com.frostwire.search.bitsnoop;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.frostwire.search.CrawlRegexSearchPerformer;
import com.frostwire.search.SearchResult;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class BitSnoopSearchPerformer extends CrawlRegexSearchPerformer<BitSnoopTempSearchResult> {

    private static final int MAX_RESULTS = 10;

    public BitSnoopSearchPerformer(long token, String keywords, int timeout) {
        super(token, keywords, timeout, 1, MAX_RESULTS, MAX_RESULTS);
    }

    
    private static final String REGEX = "(?is)<span class=\"icon cat.*?href=\"(.*?)\">.*?<div class=\"torInfo\"";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private static final String HTML_REGEX = "(?is).*?Help</a>, <a href=\"magnet:.*?urn:btih:(.*?)&dn=(.*?)\" onclick=\".*?Magnet</a>.*?<a href=\"(.*?)\" title=\".*?\" class=\"dlbtn.*?title=\"Torrent Size\"><strong>(.*?)</strong>.*?<span class=\"seeders\" title=\"Seeders\">(.*?)</span>.*?<li>Added to index &#8212; (.*?) \\(.*? ago\\)</li>.*?";
    private static final Pattern HTML_PATTERN = Pattern.compile(HTML_REGEX);

    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    @Override
    public BitSnoopTempSearchResult fromMatcher(Matcher matcher) {
        String itemId = matcher.group(1);
        return new BitSnoopTempSearchResult(itemId);
    }

    @Override
    protected String getUrl(int page, String encodedKeywords) {
        return "http://bitsnoop.com/search/all/"+encodedKeywords+"/c/d/"+page+"/";
    }

    @Override
    protected String getCrawlUrl(BitSnoopTempSearchResult sr) {
        return sr.getDetailsUrl();
    }

    @Override
    protected List<? extends SearchResult> crawlResult(BitSnoopTempSearchResult sr, byte[] data) throws Exception {
        List<BitSnoopSearchResult> list = new LinkedList<BitSnoopSearchResult>();

        String html = new String(data, "UTF-8");

        Matcher matcher = HTML_PATTERN.matcher(html);

        if (matcher.find()) {
            list.add(new BitSnoopSearchResult(sr.getDetailsUrl(), matcher));
        }

        return list;
    }
}
