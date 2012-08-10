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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;

import com.frostwire.gui.bittorrent.BTDownloadMediator;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.IconManager;
import com.limegroup.gnutella.gui.search.Selector.PropertyType;
import com.limegroup.gnutella.gui.tables.AbstractDataLine;
import com.limegroup.gnutella.gui.tables.ActionIconAndNameHolder;
import com.limegroup.gnutella.gui.tables.IconAndNameHolder;
import com.limegroup.gnutella.gui.tables.IconAndNameHolderImpl;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.SizeHolder;

/** 
 * A single line of a search result.
 */
public final class SearchResultDataLine extends AbstractDataLine<SearchResult> {
    /**
     * The SearchTableColumns.
     */
    private final SearchTableColumns COLUMNS;

    /**
     * The SearchResult that created this particular line.
     */
    private SearchResult RESULT;

    /**
     * The list of other SearchResults that match this line.
     */
    private List<SearchResult> _otherResults;

    /**
     * The media type of this document.
     */
    private NamedMediaType _mediaType;

    /**
     * The speed of this line.
     */
    private ResultSpeed _speed = null;

    private ActionListener _torrentDetailsAction;

    /**
     * The quality of this line.
     */
    private int _quality;

    /**
     * The date this was added to the network.
     */
    private Date addedOn;
    
    private SearchResultNameHolder nameHolder;

    public SearchResultDataLine(SearchTableColumns stc) {
        COLUMNS = stc;
    }

    /**
     * Initializes this line with the specified search result.
     */
    public void initialize(SearchResult sr) {
        super.initialize(sr);

        RESULT = sr;
        _mediaType = NamedMediaType.getFromExtension(getExtension());
        _speed = new ResultSpeed(sr.getSpeed(), sr.isMeasuredSpeed());
        _quality = sr.getQuality();
        _torrentDetailsAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RESULT.showDetails(true);
            }
        };
        addedOn = sr.getCreationTime() > 0 ? new Date(sr.getCreationTime()) : null;
        nameHolder = new SearchResultNameHolder(sr);
    }

    /**
     * Updates cached data about this line.
     */
    public void update() {

    }

    public String toString() {
        return getFilename() + " (" + getSeeds() + ")";
    };

    /**
     * Gets the speed of this line.
     */
    ResultSpeed getSpeed() {
        return _speed;
    }

    private boolean isDownloading() {
        if (RESULT.getHash() != null) {
            return BTDownloadMediator.instance().isDownloading(RESULT.getHash());
        } else {
            return false;
        }
    }

    /**
     * Returns the NamedMediaType.
     */
    public NamedMediaType getNamedMediaType() {
        return _mediaType;
    }

    /**
     * Gets the other results for this line.
     */
    List<SearchResult> getOtherResults() {
        if (_otherResults == null) {
            return Collections.emptyList();
        } else {
            return _otherResults;
        }
    }

    /**
     * Determines if this line is launchable.
     */
    boolean isLaunchable() {
        return false;
    }

    /**
     * Returns the icon & extension.
     */
    IconAndNameHolder getIconAndExtension() {
        String ext = getExtension();
        return new IconAndNameHolderImpl(getIcon(), ext);
    }

    /**
     * Returns the icon.
     */
    Icon getIcon() {

        //gubs: seems like this didn't fly
        //maybe the icon isn't refreshed.
        //see MetadataModel.addProperties()
        if (isDownloading()) {
            return GUIMediator.getThemeImage("downloading");
        }

        String ext = getExtension();

        //let's try to extract the extension from inside the torrent name
        if (ext.equals("torrent")) {
            String filename = getFilename().replace(".torrent", "");

            Matcher fileExtensionMatcher = Pattern.compile(".*\\.(\\S*)$").matcher(filename);

            if (fileExtensionMatcher.matches()) {
                ext = fileExtensionMatcher.group(1);
            }

        }

        return IconManager.instance().getIconForExtension(ext);
    }

    /**
     * Returns the extension of this result.
     */
    String getExtension() {
        return RESULT.getExtension();
    }

    /**
     * Returns this filename, as passed to the constructor.  Limitation:
     * if the original filename was "a.", the returned value will be
     * "a".
     */
    public String getFilename() {
        return RESULT.getFileName();
    }

    public String getDisplayName() {
        return RESULT.getDisplayName();
    }

    /**
     * Gets the size of this TableLine.
     */
    public long getSize() {
        return RESULT.getSize();
    }

    /**
     * Returns the vendor code of the result.
     */
    String getVendor() {
        return RESULT.getSource();
    }

    /**
     * Gets the LimeTableColumn for this column.
     */
    public LimeTableColumn getColumn(int idx) {
        return COLUMNS.getColumn(idx);
    }

    /**
     * Returns the number of columns.
     */
    public int getColumnCount() {
        return SearchTableColumns.COLUMN_COUNT;
    }

    /**
     * Determines if the column is dynamic.
     */
    public boolean isDynamic(int idx) {
        return false;
    }

    /**
     * Determines if the column is clippable.
     */
    public boolean isClippable(int idx) {
        switch (idx) {
        case SearchTableColumns.COUNT_IDX:
        case SearchTableColumns.TYPE_IDX:
            return false;
        default:
            return true;
        }
    }

    public int getTypeAheadColumn() {
        return SearchTableColumns.NAME_IDX;
    }

    /**
     * Gets the value for the specified idx.
     */
    public Object getValueAt(int index) {
        switch (index) {
        case SearchTableColumns.COUNT_IDX:
            if (RESULT.getSeeds() < 0) {
                return "";
            } else {
                return new Integer(RESULT.getSeeds());
            }
        case SearchTableColumns.TYPE_IDX:
            return getIcon();
        case SearchTableColumns.NAME_IDX:
            return nameHolder;
        case SearchTableColumns.SIZE_IDX:
            return new SizeHolder(getSize());
        case SearchTableColumns.SOURCE_IDX:
            return new ActionIconAndNameHolder(null, _torrentDetailsAction, "<html><a href=\"#\">" + RESULT.getSource() + "</a></html>");
        case SearchTableColumns.ADDED_IDX:
            return addedOn;
        case SearchTableColumns.EXTENSION_IDX:
            return getExtension();
        default:
            return null;
        }
    }

    /**
     * Returns <code>true</code> if <code>this</code> {@link SearchResult}
     * is the same kind as <code>line</code>'s, e.g. one from gnutella and
     * one from gnutella. Currently we compare classes.
     * 
     * @param line line to which we compare
     * @return <code>true</code> if <code>this</code> {@link SearchResult}
     *         is the same kind as <code>line</code>'s, e.g. one from
     *         gnutella and one from gnutella
     */
    public final boolean isSameKindAs(SearchResultDataLine line) {
        return getSearchResult().getClass().equals(line.getSearchResult().getClass());
    }

    /**
     * Returns the underlying search result.  This is needed by {@link StoreResultPanel}.
     * 
     * @return the underlying search result
     */
    public final SearchResult getSearchResult() {
        return RESULT;
    }

    public int getSeeds() {
        return RESULT.getSeeds();
    }

    public String getHash() {
        return RESULT.getHash();
    }

    public SearchEngine getSearchEngine() {
        return RESULT.getSearchEngine();
    }
}
