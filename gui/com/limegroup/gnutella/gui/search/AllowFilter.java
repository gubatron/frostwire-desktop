package com.limegroup.gnutella.gui.search;

import com.frostwire.gui.filters.TableLineFilter;

/**
 * Filter denoting that anything is allowed.
 */
class AllowFilter implements TableLineFilter<SearchResultDataLine> {
    /**
     * The sole instance that can be returned, for convenience.
     */
	private static AllowFilter INSTANCE = new AllowFilter();
    
    /**
     * Returns a reusable instance of AllowFilter.
     */
    public static AllowFilter instance() {
        return INSTANCE;
    }

    /**
     * Returns true.
     */
    public boolean allow(SearchResultDataLine line) {
        return true;
    }
    
    public boolean equals(Object o) {
        return (o instanceof AllowFilter);
    }    
}