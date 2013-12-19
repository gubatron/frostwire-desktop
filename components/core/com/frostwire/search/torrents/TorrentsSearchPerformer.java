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

package com.frostwire.search.torrents;

import java.util.List;

import com.frostwire.search.CrawlableSearchResult;
import com.frostwire.search.PerformersHelper;
import com.frostwire.search.SearchMatcher;
import com.frostwire.search.domainalias.DomainAliasManager;
import com.frostwire.search.domainalias.DomainAliasManagerBroker;
import com.frostwire.search.torrent.TorrentRegexSearchPerformer;

/**
 * Search Performer for torrents.com / torrents.fm
 * @author gubatron
 * @author aldenml
 *
 */
public class TorrentsSearchPerformer extends TorrentRegexSearchPerformer<TorrentsSearchResult> {

    private static final int MAX_RESULTS = 20;
    private static final String REGEX = "(?is)><td><span class=\"icon-.*?\"></span></td><td><a title=\"(.*?)\" href=\'(.*?)\'>.*?</a><a title=\"Share on Facebook\".*?<a title=\"Download magnet\".*?</td></tr>";
    private static final String HTML_REGEX = "(?is)alpha omega\"><h1>(.*?)</h1></h1></div><div class=.*?<div class=\"size\">(.*?)</div>.*?<span title=\"([0-9]*) seeds / [0-9]* leechers\">.*?<dl class=\"date\"><dt>Created</dt><dd>(.*?)</dd></dl>";
    // matcher groups: 1 -> title
    //                 2 -> file size (needs parsing)
    //                 3 -> seeds
    //                 4 -> creation date, e.g. 2013-10-17 11:53:27
    //
    

    public TorrentsSearchPerformer(DomainAliasManager domainAliasManager, long token, String keywords, int timeout) {
        super(domainAliasManager, token, keywords, timeout, 1, 2 * MAX_RESULTS, MAX_RESULTS, REGEX, HTML_REGEX); 
    }

    @Override
    protected String getUrl(int page, String encodedKeywords) {
        String searchParameter = encodedKeywords.replace(" ", "_");
        return "http://"+getDomainNameToUse()+"/search/"+searchParameter;
    }

    @Override
    public CrawlableSearchResult fromMatcher(SearchMatcher matcher) {
        String displayName = matcher.group(1);
        String itemId = matcher.group(2);
        return new TorrentsTempSearchResult(getDomainNameToUse(),itemId,displayName);
    }

    @Override
    protected TorrentsSearchResult fromHtmlMatcher(CrawlableSearchResult sr, SearchMatcher matcher) {
        return new TorrentsSearchResult(getDomainNameToUse(), sr.getDetailsUrl(), matcher);
    }
    
    public static void main(String[] args) throws Exception {
        //TEST
        
        DomainAliasManagerBroker broker = new DomainAliasManagerBroker();
        TorrentsSearchPerformer performer = new TorrentsSearchPerformer(broker.getDomainAliasManager("torrents.fm"),0,"frostclick",5000);

        String htmlSearchPage = performer.fetch("http://torrents.fm/search/ubuntu");
        //String htmlSearchPage = FileUtils.readFileToString(new File("/Users/gubatron/Desktop/torrents.html"));
        //System.out.println("\n" + htmlSearchPage);
        
        @SuppressWarnings("unchecked")
        List<TorrentsTempSearchResult> searchResults = (List<TorrentsTempSearchResult>) PerformersHelper.searchPageHelper(performer, htmlSearchPage, 20);
        
        if (searchResults.isEmpty()) {
            System.out.println("No results");
        } else {
            int n = 1;
            for (TorrentsTempSearchResult sr : searchResults) {
                System.out.println(n + ". [" + sr.getDisplayName() + "] => " + sr.getDetailsUrl());
                n++;
                //byte[] detailsData = performer.fetchBytes(sr.getDetailsUrl());
                //List<? extends SearchResult> crawlResult = performer.crawlResult(sr, detailsData);
            }
        }
    }
}