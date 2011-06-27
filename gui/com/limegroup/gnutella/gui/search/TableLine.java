package com.limegroup.gnutella.gui.search;

import java.awt.Color;
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
import com.limegroup.gnutella.gui.IconManager;
import com.limegroup.gnutella.gui.search.Selector.PropertyType;
import com.limegroup.gnutella.gui.tables.AbstractDataLine;
import com.limegroup.gnutella.gui.tables.ActionIconAndNameHolder;
import com.limegroup.gnutella.gui.tables.IconAndNameHolder;
import com.limegroup.gnutella.gui.tables.IconAndNameHolderImpl;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.Linkable;
import com.limegroup.gnutella.gui.tables.SizeHolder;
import com.limegroup.gnutella.settings.SearchSettings;

/** 
 * A single line of a search result.
 */
public final class TableLine extends AbstractDataLine<SearchResult> implements Linkable {
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

    /**
     * The quality of this line.
     */
    private int _quality;

    /**
     * The date this was added to the network.
     */
    private long _addedOn;

    public TableLine(SearchTableColumns stc) {
        COLUMNS = stc;
    }

    /**
     * Initializes this line with the specified search result.
     */
    public void initialize(SearchResult sr) {
        super.initialize(sr);
        initilizeStart(sr);
        sr.initialize(this);
        initializeEnd();
    }

    private void initilizeStart(SearchResult sr) {
        RESULT = sr;
        _mediaType = NamedMediaType.getFromExtension(getExtension());
        _speed = new ResultSpeed(sr.getSpeed(), sr.isMeasuredSpeed());
        _quality = sr.getQuality();
    }

    private void initializeEnd() {
        updateLicense();
    }

    public boolean isLink() {
        if (RESULT instanceof Linkable)
            return ((Linkable) RESULT).isLink();
        else
            return false;
    }

    public String getLinkUrl() {
        if (RESULT instanceof Linkable)
            return ((Linkable) RESULT).getLinkUrl();
        else
            return null;
    }

    /**
     * Adds a new SearchResult to this TableLine.
     */
    void addNewResult(SearchResult sr, MetadataModel mm) {

        if (_otherResults == null)
            _otherResults = new LinkedList<SearchResult>();
        _otherResults.add(sr);

        // Set the speed correctly.
        ResultSpeed newSpeed = new ResultSpeed(sr.getSpeed(), sr.isMeasuredSpeed());
        // if we're changing a property, update the metadata model.
        if (_speed.compareTo(newSpeed) < 0) {
            if (mm != null)
                mm.updateProperty(PropertyType.SPEED.getKey(), _speed, newSpeed, this);
            _speed = newSpeed;
        }

        // Set the quality correctly.
        _quality = Math.max(sr.getQuality(), _quality);

        if (sr.getCreationTime() > 0)
            _addedOn = Math.min(_addedOn, sr.getCreationTime());
    }

    /**
     * Updates cached data about this line.
     */
    public void update() {
        updateLicense();
    }

    /**
     * Updates the license status.
     */
    private void updateLicense() {
        //        if(_doc != null && _sha1 != null) {
        //            String licenseString = _doc.getLicenseString();
        //            LicenseFactory factory = _doc.getLicenseFactory();
        //            if(licenseString != null) {
        //                if(factory.isVerifiedAndValid(_sha1, licenseString))
        //                    _licenseState = License.VERIFIED;
        //                else
        //                    _licenseState = License.UNVERIFIED;
        //                _licenseName = factory.getLicenseName(licenseString);
        //            }
        //        }
    }

    /**
     * Gets the speed of this line.
     */
    ResultSpeed getSpeed() {
        return _speed;
    }

    /**
     * Gets the creation time.
     */
    Date getAddedOn() {
        if (_addedOn > 0)
            return new Date(_addedOn);
        else
            return null;
    }

    /**
     * Gets the quality of this line.
     */
    int getQuality() {
        if (isDownloading()) {
            return QualityRenderer.DOWNLOADING_FILE_QUALITY;
        } else if (SearchSettings.ENABLE_SPAM_FILTER.getValue() && SpamFilter.isAboveSpamThreshold(this)) {
            return QualityRenderer.SPAM_FILE_QUALITY;
        } else {
            return _quality;
        }
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
     * Gets the filename without the extension.
     */
    String getFilenameNoExtension() {
        return RESULT.getFilenameNoExtension();
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
        return RESULT.getVendor();
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
        case SearchTableColumns.QUALITY_IDX:
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
        case SearchTableColumns.QUALITY_IDX:
            return new Integer(getQuality());
        case SearchTableColumns.COUNT_IDX:
            return new Integer(RESULT.getSeeds());
        case SearchTableColumns.TYPE_IDX:
            return getIcon();
        case SearchTableColumns.NAME_IDX:
            return new ActionIconAndNameHolder(getIcon(), getFilenameNoExtension());
            //return new ResultNameHolder(this);
        case SearchTableColumns.SIZE_IDX:
            return new SizeHolder(getSize());
        case SearchTableColumns.SOURCE_IDX:
            return RESULT.getVendor();
        case SearchTableColumns.ADDED_IDX:
            return getAddedOn();
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
    public final boolean isSameKindAs(TableLine line) {
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

    public final boolean isOverrideRowColor() {
        return RESULT.isOverrideRowColor();
    }

    /**
     * Returns the color for painting an even row. Delegates to the member
     * {@link SearchResult}.
     * 
     * @return the color for painting an even row. Delegates to the member
     *         {@link SearchResult}
     */
    public final Color getEvenRowColor() {
        return RESULT.getEvenRowColor();
    }

    /**
     * Returns the color for painting an odd row. Delegates to the member
     * {@link SearchResult}.
     * 
     * @return the color for painting an odd row. Delegates to the member
     *         {@link SearchResult}
     */
    public final Color getOddRowColor() {
        return RESULT.getOddRowColor();
    }

    /**
     * Delegate to the {@link #RESULT} to take some action, such as download or
     * display in browser, etc.
     * 
     * @see SearchResult#takeAction(TableLine, GUID, File, String, boolean,
     *      SearchInformation)
     */
    public final void takeAction(TableLine line, GUID guid, File saveDir, String fileName, boolean saveAs, SearchInformation searchInfo) {
        RESULT.takeAction(line, guid, saveDir, fileName, saveAs, searchInfo);
    }

    /* -----------------------------------------------------------------------------
     * These were exposed to give a {@link SearchResult} access to this
     * TableLine in initialization.
     * ----------------------------------------------------------------------------- 
     */

    /**
     * Sets the new 'added on' date.
     * 
     * @param creationTime new 'added on' data
     */
    final void setAddedOn(long creationTime) {
        _addedOn = creationTime;
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
