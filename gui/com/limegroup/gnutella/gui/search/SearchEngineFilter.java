package com.limegroup.gnutella.gui.search;

import com.frostwire.gui.filters.TableLineFilter;

class SearchEngineFilter implements TableLineFilter<TableLine> {

    public boolean allow(TableLine node) {
        return node.getSearchEngine().isEnabled();
    }
}
