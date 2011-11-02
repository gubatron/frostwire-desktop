package com.limegroup.gnutella.gui.search;

import com.frostwire.gui.filters.TableLineFilter;

class SearchEngineFilter implements TableLineFilter<SearchResultDataLine> {

    public boolean allow(SearchResultDataLine node) {
        return node.getSearchEngine().isEnabled();
    }
}
