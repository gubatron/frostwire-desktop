package com.frostwire.gui.bittorrent;

import java.util.ArrayList;
import java.util.List;

import com.frostwire.gui.filters.TableLineFilter;

/**
 * Filters out certain rows from the data model.
 *
 * @author Sumeet Thadani, Sam Berlin, Gubatron
 * 
 */
public class BTDownloadRowFilteredModel extends BTDownloadModel {
    
	private static final long serialVersionUID = -581151930850193368L;


	/**
     * The filter to use in this row filter.
     */
    private final TableLineFilter<BTDownloadDataLine> FILTER;
    
    
    /**
     * A list of all filtered results.
     */
    protected final List<BTDownloadDataLine> HIDDEN;
    

    /**
     * Constructs a TableRowFilter with the specified TableLineFilter.
     */
    public BTDownloadRowFilteredModel(TableLineFilter<BTDownloadDataLine> f) {

        if(f == null)
            throw new NullPointerException("null filter");

        FILTER = f;
        HIDDEN = new ArrayList<BTDownloadDataLine>();
    }
    
    
    /**
     * Determines whether or not this line should be added.
     */
    public int add(BTDownloadDataLine tl, int row) {

    	if (!allow(tl)) {
        	HIDDEN.add(tl);
        	return -1;
        } else {
        	return super.add(tl,row);
        }

    }
    
    @Override
    public void clear() {
    	super.clear();
    	HIDDEN.clear();
    }

    /**
     * Notification that the filters have changed.
     */
    void filtersChanged() {
        rebuild();
        fireTableDataChanged();
    }
	
    /**
     * Determines whether or not the specified line is allowed by the filter.
     */
    private boolean allow(BTDownloadDataLine tl) {
        return FILTER.allow(tl);
    }
    
    /**
     * Rebuilds the internal map to denote a new filter.
     */
    private void rebuild(){
        List<BTDownloadDataLine> existing = new ArrayList<BTDownloadDataLine>(_list);
        List<BTDownloadDataLine> hidden = new ArrayList<BTDownloadDataLine>(HIDDEN);
        clear();        
        
        // For stuff in _list, we can just re-add the DataLines as-is.
        if(isSorted()) {
            for(int i = 0; i < existing.size(); i++) {
                addSorted(existing.get(i));
            }
        } else {
            for(int i = 0; i < existing.size(); i++) {
                add(existing.get(i));
            }
        }
        
        // Merge the hidden TableLines

        for(int i = 0; i < hidden.size(); i++) {
        	BTDownloadDataLine tl = hidden.get(i);
            
            if(isSorted()) {
                addSorted(tl);
            } else {
                add(tl);
            }
        }
  
    }
}