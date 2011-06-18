package com.limegroup.gnutella.gui.search;

class SearchEngineFilter implements TableLineFilter {

    public boolean allow(TableLine node) {
        return node.getSearchEngine().isEnabled();
    }
}
