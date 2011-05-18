package com.frostwire.gnutella.gui.filters;

import com.limegroup.gnutella.gui.search.SearchResult;

public interface SearchFilter {

    public boolean allow(SearchResult result);
}
