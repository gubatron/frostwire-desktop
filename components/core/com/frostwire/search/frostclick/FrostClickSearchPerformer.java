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

package com.frostwire.search.frostclick;

import java.util.List;

import com.frostwire.search.PagedWebSearchPerformer;
import com.frostwire.search.SearchResult;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class FrostClickSearchPerformer extends PagedWebSearchPerformer {
    private static final int MAX_RESULTS = 25;

    public FrostClickSearchPerformer(long token, String keywords, int timeout) {
        super(token, keywords, timeout, MAX_RESULTS);
    }

    @Override
    protected String getUrl(int page, String encodedKeywords) {
        return "http://search.frostclick.com/?page=" + page + "&q=" + encodedKeywords;
    }

    @Override
    protected List<? extends SearchResult> searchPage(String page) {
        return null;
    }
}