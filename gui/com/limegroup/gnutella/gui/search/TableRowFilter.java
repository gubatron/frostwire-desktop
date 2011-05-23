package com.limegroup.gnutella.gui.search;

import java.util.ArrayList;
import java.util.List;

import com.limegroup.gnutella.search.QueryHandler;
import com.limegroup.gnutella.settings.SearchSettings;

/**
 * Filters out certain rows from the data model.
 *
 * @author Sumeet Thadani, Sam Berlin
 */
public class TableRowFilter extends ResultPanelModel {
    
    /**
     * 
     */
    private static final long serialVersionUID = -7810977044778830969L;

    /**
     * The filter to use in this row filter.
     */
    private final TableLineFilter FILTER;
    
    /**
     * The Junk Filter
     */
    private TableLineFilter junkFilter = AllowFilter.instance();
    
    /**
     * A list of all filtered results.
     */
    protected final List<TableLine> HIDDEN;
    
    /**
     * The number of sources in the hidden list.
     */
    private int _numSources;

    /**
     * Constructs a TableRowFilter with the specified TableLineFilter.
     */
    public TableRowFilter(TableLineFilter f) {
        super();

        if(f == null)
            throw new NullPointerException("null filter");

        FILTER = f;
        HIDDEN = new ArrayList<TableLine>(QueryHandler.ULTRAPEER_RESULTS);
        _numSources = 0;
    }
    
    /**
     * Returns true if Table is sorted which means either
     * it is really sorted OR 'move junk to bottom' is
     * selected which is also some kind of sorting!
     */
    public boolean isSorted() {
        return super.isSorted() || SearchSettings.moveJunkToBottom();
    }


    /**
     * Gets the amount of filtered sources.
     */
    public int getFilteredSources() {
        return super.getTotalSources();
    }
    
    /**
     * Gets the total amount of sources.
     */
    public int getTotalSources() {
        return getFilteredSources() + _numSources;
    }
    
    public int addNewResult(TableLine tl, SearchResult sr) {
        // If we're hiding junk check if TableLine's rating turns into
        // junk when a new SearchResult is added...
        if (SearchSettings.hideJunk()) {
            int added = super.addNewResult(tl, sr);
            
            // If so, remove the row from the Table...!
            if (!junkFilter.allow(tl)) {
                int row = getRow(tl);
                remove(row);
                METADATA.remove(tl);
                _numSources += tl.getLocationCount();
                return 0;
            } else {
                return added;
            }
        
        // If we're moving junk results to the bottom check if the 
        // TableLine rating changes from NOT junk to junk then remove
        // it from the current position in the Table and move it to
        // the bottom
        } else if (SearchSettings.moveJunkToBottom()) {
            boolean wasNotJunk = junkFilter.allow(tl);
            int added = super.addNewResult(tl, sr);
            if (wasNotJunk && !junkFilter.allow(tl)) {
                int row = getRow(tl);
                remove(row);
                return super.add(tl, getSortedPosition(tl)); // re-add
            } else {
                return added;
            }
        
        // Standard add...
        } else {
            return super.addNewResult(tl, sr);
        }
    }
    
    /**
     * Determines whether or not this line should be added.
     */
    public int add(TableLine tl, int row) {
        boolean isNotJunk = junkFilter.allow(tl);
        boolean allow = allow(tl);
             
        if(isNotJunk || !SearchSettings.hideJunk()) {
            if (allow) {
                return super.add(tl, row);
            } else {
                HIDDEN.add(tl);
                if(_useMetadata) {
                    METADATA.addNew(tl);
                }
                _numSources += tl.getLocationCount();
            }
        } else {
            _numSources += tl.getLocationCount();
        }
        return -1;
    }
    
    /**
     * Intercepts to clear the hidden map.
     */
    protected void simpleClear() {
        _numSources = 0;
        HIDDEN.clear();
        super.simpleClear();
    }
    
    /**
     * Notification that the filters have changed.
     */
    void filtersChanged() {
        fireTableDataChanged();
    }
	
    /**
     * Sets the Junk Filter. Pass null as argument to disable the filter
     */
    void setJunkFilter(TableLineFilter junkFilter) {
        if (junkFilter != null) {
            this.junkFilter = junkFilter;
        } else {
            this.junkFilter = AllowFilter.instance();
        }
    }
    
    /**
     * Determines whether or not the specified line is allowed by the filter.
     */
    private boolean allow(TableLine line) {
        return FILTER.allow(line);
    }
}
