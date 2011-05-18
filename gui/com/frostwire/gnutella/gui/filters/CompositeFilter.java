package com.frostwire.gnutella.gui.filters;

import com.limegroup.gnutella.gui.search.SearchResult;

public class CompositeFilter implements SearchFilter {

    private SearchFilter[] _delegates;

    public CompositeFilter(SearchFilter[] filters) {
        _delegates = filters;
    }

    public boolean allow(SearchResult m) {
        for (int i = 0; i < _delegates.length; i++) {
            if (!_delegates[i].allow(m))
                return false;
        }
        return true;
    }
}
