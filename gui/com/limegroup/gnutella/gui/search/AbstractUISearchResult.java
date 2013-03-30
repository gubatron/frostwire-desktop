/*
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

package com.limegroup.gnutella.gui.search;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.settings.SearchSettings;

/**
 * A single SearchResult. These are returned in the {@link SearchInputPanel} and
 * are used to create {@link SearchResultDataLine}s to show search results.
 * 
 * (A collection of RemoteFileDesc, HostData, and Set of alternate locations.)
 */
public abstract class AbstractUISearchResult implements UISearchResult {

    private String query;

    public AbstractUISearchResult(String query) {
        this.query = query;
    }

    @Override
    public String getExtension() {
        String fullname = getFilename();
        if (fullname == null) {
            throw new NullPointerException("getFileName() can't return a null result");
        }
        int i = fullname.lastIndexOf(".");
        if (i < 0) {
            return "";
        }
        return fullname.substring(i + 1);
    }

    @Override
    public void showDetails(boolean now) {
        if (now) {
            GUIMediator.openURL(getSearchResult().getDetailsUrl());
        } else {
            if (SearchSettings.SHOW_DETAIL_PAGE_AFTER_DOWNLOAD_START.getValue()) {
                GUIMediator.openURL(getSearchResult().getDetailsUrl(), SearchSettings.SHOW_DETAILS_DELAY);
            }
        }
    }

    @Override
    public String getQuery() {
        return query;
    }
}