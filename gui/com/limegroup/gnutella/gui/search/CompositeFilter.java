package com.limegroup.gnutella.gui.search;

import java.util.ArrayList;
import java.util.List;

import com.frostwire.gui.filters.TableLineFilter;

/**
 * A filter that takes multiple other filters.
 */
class CompositeFilter implements TableLineFilter<TableLine> {
    /**
     * The underlying filters.
     */
    private List<TableLineFilter<TableLine>> delegates;
    
    /**
     * Creates a new CompositeFilter of the specified depth.
     * By default, all the filters are an AllowFilter.
     */
    CompositeFilter(int depth) {
        this.delegates = new ArrayList<TableLineFilter<TableLine>>(depth);
        reset();
    }
    
    /**
     * Resets this filter to all AllowFilters.
     */
    public void reset() {
        for(int i = 0; i < delegates.size(); i++) {
            delegates.set(i, AllowFilter.instance());
        }
    }
    
    /**
     * Determines whether or not the specified TableLine
     * can be displayed.
     */
    public boolean allow(TableLine line) {
        for (int i=0; i<delegates.size(); i++) {
            if (! delegates.get(i).allow(line))
                return false;
        }
        return true;
    }
    
    /**
     * Sets the filter at the specified depth.
     */
    boolean setFilter(int depth, TableLineFilter<TableLine> filter) {
        if (filter == this) {
            throw new IllegalArgumentException("Filter must not be composed of itself");
        }
        if(delegates.get(depth).equals(filter))
            return false;
        else {
            delegates.set(depth, filter);
            return true;
        }
    }
}
