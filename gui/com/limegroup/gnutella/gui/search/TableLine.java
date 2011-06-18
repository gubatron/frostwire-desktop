package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;

import org.limewire.collection.NameValue;

import com.frostwire.gui.bittorrent.BTDownloadMediator;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.IconManager;
import com.limegroup.gnutella.gui.search.Selector.PropertyType;
import com.limegroup.gnutella.gui.tables.AbstractDataLine;
import com.limegroup.gnutella.gui.tables.IconAndNameHolder;
import com.limegroup.gnutella.gui.tables.IconAndNameHolderImpl;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.Linkable;
import com.limegroup.gnutella.gui.tables.SizeHolder;
import com.limegroup.gnutella.gui.xml.XMLValue;
import com.limegroup.gnutella.licenses.License;
import com.limegroup.gnutella.settings.SearchSettings;
import com.limegroup.gnutella.xml.LimeXMLDocument;
import com.limegroup.gnutella.xml.LimeXMLSchema;
import com.limegroup.gnutella.xml.SchemaFieldInfo;

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
     * The LimeXMLDocument for this line.
     */
    private LimeXMLDocument _doc;

    /**
     * The date this was added to the network.
     */
    private long _addedOn;

    /** License info. */
    private int _licenseState = License.NO_LICENSE;
    private String _licenseName = null;

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
        _doc = sr.getXMLDocument();
        if (_doc != null)
            _mediaType = NamedMediaType.getFromDescription(_doc.getSchemaDescription());
        else
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
            return _doc != null && !"".equals(_doc.getAction());
    }

    public String getLinkUrl() {
        if (RESULT instanceof Linkable)
            return ((Linkable) RESULT).getLinkUrl();
        else
            return _doc.getAction();
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

        updateXMLDocument(sr.getXMLDocument(), mm);
    }

    /**
     * Updates the XMLDocument and the MetadataModel.
     */
    private void updateXMLDocument(LimeXMLDocument newDoc, MetadataModel mm) {
        // If nothing new, nothing to do.
        if (newDoc == null)
            return;

        // If no document exists, just set it to be the new doc
        if (_doc == null) {
            _doc = newDoc;
            updateLicense();
            if (mm != null) {
                _mediaType = NamedMediaType.getFromDescription(_doc.getSchemaDescription());
                mm.addNewDocument(_doc, this);
            }
            return;
        }

        // Otherwise, if a document does exist in the group, see if the line
        // has extra fields that can be added to the group.

        // Must have the same schema...
        if (!_doc.getSchemaURI().equals(newDoc.getSchemaURI()))
            return;

        Set<String> oldKeys = _doc.getNameSet();
        Set<String> newKeys = newDoc.getNameSet();
        // if the we already have everything in new, do nothing
        if (oldKeys.containsAll(newKeys))
            return;

        // Now we want to add the values of newKeys that weren't
        // already in oldKeys.
        newKeys = new HashSet<String>(newKeys);
        newKeys.removeAll(oldKeys);
        // newKeys now only has brand new elements.
        Map<String, String> newMap = new HashMap<String, String>(oldKeys.size() + newKeys.size());
        for (Map.Entry<String, String> entry : _doc.getNameValueSet())
            newMap.put(entry.getKey(), entry.getValue());

        LimeXMLSchema schema = _doc.getSchema();
        for (SchemaFieldInfo sfi : schema.getCanonicalizedFields()) {
            String key = sfi.getCanonicalizedFieldName();
            if (newKeys.contains(key)) {
                String value = newDoc.getValue(key);
                if (mm != null)
                    mm.addField(sfi, key, value, this);
            }
        }

        _doc = GuiCoreMediator.getLimeXMLDocumentFactory().createLimeXMLDocument(newMap.entrySet(), _doc.getSchemaURI());
        updateLicense();
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
     * Determines if a license is available.
     */
    boolean isLicenseAvailable() {
        return _licenseState != License.NO_LICENSE;
    }

    /**
     * Gets the license associated with this line.
     */
    License getLicense() {
        //        if(_doc != null && _sha1 != null)
        //            return _doc.getLicense();
        //        else
        return null;
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
     * Gets the LimeXMLDocument for this line.
     */
    LimeXMLDocument getXMLDocument() {
        return _doc;
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
     * Returns the license name of null if File(s) have no license
     */
    String getLicenseName() {
        return _licenseName;
    }

    /**
     * Determines if this line is launchable.
     */
    boolean isLaunchable() {
        return _doc != null && _doc.getAction() != null && !"".equals(_doc.getAction());
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
        case SearchTableColumns.ICON_IDX:
        case SearchTableColumns.LICENSE_IDX:
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
        case SearchTableColumns.ICON_IDX:
            return getIcon();
        case SearchTableColumns.NAME_IDX:
            return new ResultNameHolder(this);
        case SearchTableColumns.SIZE_IDX:
            return new SizeHolder(getSize());
        case SearchTableColumns.SOURCE_IDX:
            return RESULT.getVendor();
        case SearchTableColumns.ADDED_IDX:
            return getAddedOn();
        case SearchTableColumns.LICENSE_IDX:
            return new NameValue.ComparableByName<Integer>(_licenseName, new Integer(_licenseState));
        default:
            if (_doc == null || index == -1) // no column, no value.
                return null;
            XMLSearchColumn ltc = (XMLSearchColumn) getColumn(index);
            return new XMLValue(_doc.getValue(ltc.getId()), ltc.getSchemaFieldInfo());
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
     * Gets all RemoteFileDescs for this line.
     */
    RemoteFileDesc[] getAllRemoteFileDescs() {
        //        GnutellaSearchResult sr = (GnutellaSearchResult)RESULT;
        //        int size = getOtherResults().size() + 1;
        //        RemoteFileDesc[] rfds = new RemoteFileDesc[size];
        //        rfds[0] = sr.getRemoteFileDesc();
        //        int j = 1;
        //        for(Iterator<?> i = getOtherResults().iterator(); i.hasNext(); j++)
        //            rfds[j] = ((GnutellaSearchResult)i.next()).getRemoteFileDesc();
        //        return rfds;
        return null;
    }

    /**
     * Returns the rfd of the search result for which this download was enabled
     * @return
     */
    RemoteFileDesc getRemoteFileDesc() {
        //        return RESULT instanceof GnutellaSearchResult 
        //                ? ((GnutellaSearchResult)RESULT).getRemoteFileDesc() 
        //                : null;
        return null;
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
