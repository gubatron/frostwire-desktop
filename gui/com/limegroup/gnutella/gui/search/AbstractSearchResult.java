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

import java.awt.Color;

import org.limewire.collection.ApproximateMatcher;
import org.limewire.util.I18NConvert;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.themes.ThemeSettings;
import com.limegroup.gnutella.settings.SearchSettings;

/**
 * A single SearchResult. These are returned in the {@link SearchInputPanel} and
 * are used to create {@link SearchResultDataLine}s to show search results.
 * 
 * (A collection of RemoteFileDesc, HostData, and Set of alternate locations.)
 */
public abstract class AbstractSearchResult implements SearchResult {

    /**
     * The processed version of the filename used for approximate matching.
     * Not allocated until a match must be done.  The assumption here is that
     * all matches will use the same ApproximateMatcher.
     * 
     * TODO: when we move to Java 1.3, this should be a weak reference so the
     * memory is reclaimed after GC. */
    private String processedFilename;

    public String getFilenameNoExtension() {
        String fullname = getFileName();
        if (fullname == null) {
            throw new NullPointerException("getFileName() can't return a null result");
        }
        int i = fullname.lastIndexOf(".");
        if (i < 0) {
            return fullname;
        }
        return I18NConvert.instance().compose(fullname.substring(0, i));
    }

    public String getExtension() {
        String fullname = getFileName();
        if (fullname == null) {
            throw new NullPointerException("getFileName() can't return a null result");
        }
        int i = fullname.lastIndexOf(".");
        if (i < 0) {
            return "";
        }
        return fullname.substring(i + 1);
    }

    /**
     * Gets the processed filename.
     */
    private String getProcessedFilename(ApproximateMatcher matcher) {
        if (processedFilename != null) {
            return processedFilename;
        }
        return processedFilename = matcher.process(getFilenameNoExtension());
    }

    public final int match(SearchResult sr, final ApproximateMatcher matcher) {

        if (!(sr instanceof AbstractSearchResult)) {
            return 3;
        }

        AbstractSearchResult o = (AbstractSearchResult) sr;

        // Same file type?
        if (!getExtension().equals(o.getExtension())) {
            return 1;
        }

        long thisSize = getSize();
        long thatSize = o.getSize();

        // Sizes same?
        if (thisSize != thatSize) {
            return 2;
        }

        // Preprocess the processed fileNames
        getProcessedFilename(matcher);
        o.getProcessedFilename(matcher);

        // Filenames close?  This is the most expensive test, so it should go
        // last.  Allow 5% edit difference in filenames or 4 characters,
        // whichever is smaller.
        int allowedDifferences = Math.round(Math.min(0.10f * (getFilenameNoExtension().length()), 0.10f * (o.getFilenameNoExtension().length())));
        allowedDifferences = Math.min(allowedDifferences, 4);
        if (!matcher.matches(getProcessedFilename(matcher), o.getProcessedFilename(matcher), allowedDifferences)) {
            return 3;
        }

        return 0;
    }

    /**
     * Wether or not this result can be marked as Junk.
     * @return
     */
    public boolean canBeMarkedAsJunk() {
        return false;
    }

    public boolean isOverrideRowColor() {
        return false;
    }

    public Color getEvenRowColor() {
        return ThemeSettings.DEFAULT_TABLE_EVEN_ROW_COLOR.getValue();
    }

    public Color getOddRowColor() {
        return ThemeSettings.DEFAULT_TABLE_ODD_ROW_COLOR.getValue();
    }

    public void showDetails(boolean now) {
        if (now) {
            GUIMediator.openURL(getWebSearchResult().getTorrentDetailsURL());
        } else {
            if (SearchSettings.SHOW_DETAIL_PAGE_AFTER_DOWNLOAD_START.getValue()) {
                GUIMediator.openURL(getWebSearchResult().getTorrentDetailsURL(), SearchSettings.SHOW_DETAILS_DELAY);
            }
        }
    }
}