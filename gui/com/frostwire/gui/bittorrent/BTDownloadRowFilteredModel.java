package com.frostwire.gui.bittorrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    
 
    
    @Override
    public int getSortedPosition(BTDownloadDataLine dl) {
    	
    	if (_list == null || _list.size() == 0) {
    		return 0;
    	}
    	
    	@SuppressWarnings("unchecked")
		int sortedPosition = Collections.binarySearch(_list, dl,  new Comparator<Object>() {
			@Override
			public int compare(Object a, Object b) {
				BTDownloadDataLine aDataLine = (BTDownloadDataLine) a;
				BTDownloadDataLine bDataLine = (BTDownloadDataLine) b;
				
				if (isSorted()) {
					Comparable<Object> aComparable = (Comparable<Object>) aDataLine.getValueAt(_activeColumn);
					Comparable<Object> bComparable = (Comparable<Object>) bDataLine.getValueAt(_activeColumn);
					return aComparable.compareTo(bComparable);
				} else {
					BTDownload torrentA = aDataLine.getInitializeObject();
					BTDownload torrentB = bDataLine.getInitializeObject();
					return new Integer(torrentA.getState()).compareTo(new Integer(torrentB.getState()));
				}
				
			}
		} );
    	
    	if (sortedPosition < 0) sortedPosition = -(sortedPosition + 1);
    	
    	
    	return sortedPosition;
    }
    
    /**
     * Rebuilds the internal map to denote a new filter.
     */
    private void rebuild(){
        List<BTDownloadDataLine> existing = new ArrayList<BTDownloadDataLine>(_list);
        List<BTDownloadDataLine> hidden = new ArrayList<BTDownloadDataLine>(HIDDEN);

        clear();    
        
        // For stuff in _list, we can just re-add the DataLines as-is.
        for(int i = 0; i < existing.size(); i++) {
        	//if (isSorted()) {
        		//see override of getSortedPosition.
        		//rebuild only seems to happen when we first build the table
        		//in which case addSorted takes care of business by invoking getSortedPosition.
        		addSorted(existing.get(i));
        	//} else {
        	//	add(existing.get(i));
        //	}
        }
        
        // Merge the hidden TableLines
        for(int i = 0; i < hidden.size(); i++) {
        	BTDownloadDataLine tl = hidden.get(i);
            
            //if(isSorted()) {
                addSorted(tl);
            //} else {
            //    add(tl);
           // }
        }
  
    }
}