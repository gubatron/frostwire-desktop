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

package com.frostwire.search;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public abstract class RegexSearchPerformer<T extends CrawlableSearchResult> extends CrawlPagedWebSearchPerformer<T> {

    private final int regexMaxResults;

    public RegexSearchPerformer(long token, String keywords, int timeout, int pages, int numCrawls, int regexMaxResults) {
        super(token, keywords, timeout, pages, numCrawls);
        this.regexMaxResults = regexMaxResults;
    }

    @Override
    protected final List<? extends SearchResult> searchPage(String page) {
        List<SearchResult> result = new LinkedList<SearchResult>();

        Matcher matcher = getPattern().matcher(page);

        int max = regexMaxResults;

        int i = 0;

        while (matcher.find() && i < max && !isStopped()) {
            SearchResult sr = fromMatcher(matcher);
            if (sr != null) {
                result.add(sr);
                i++;
            }
        }

        return result;
    }

    protected abstract Pattern getPattern();

    protected abstract T fromMatcher(Matcher matcher);
}
