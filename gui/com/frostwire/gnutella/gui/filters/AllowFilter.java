package com.frostwire.gnutella.gui.filters;

import com.limegroup.gnutella.gui.search.SearchResult;

/** 
 * A filter that allows anything.  Use when you don't want to filter
 * traffic. 
 */
public class AllowFilter implements SearchFilter {
    public boolean allow(SearchResult m) {
        return true;
    }
}
