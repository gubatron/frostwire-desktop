package com.limegroup.gnutella.gui.search;

public class GeneralResultFilter implements TableLineFilter {
    
    private ResultPanel _rp;
    
    private int _minSeeds;
    private int _maxSeeds;
    private long _minSize;
    private long _maxSize;

    public GeneralResultFilter(ResultPanel rp) {
        _rp = rp;
        _minSeeds = 0;
        _maxSeeds = Integer.MAX_VALUE;
        _minSize = 0;
        _maxSize = Integer.MAX_VALUE;
    }

    public boolean allow(TableLine node) {
        int seeds = node.getSeeds();
        if (seeds < _minSeeds || seeds > _maxSeeds) {
            return false;
        }
//        long size = node.getSize();
//        if (size < _minSize || size > _maxSize) {
//            return false;
//        }
        return true;
    }

    public void setRangeSeeds(int min, int max) {
        _minSeeds = min;
        _maxSeeds = max;
        _rp.filterChanged(this, 1);
    }

    public void setRangeSize(long min, long max) {
        _minSize = min;
        _maxSize = max;
        _rp.filterChanged(this, 1);
    }
}
