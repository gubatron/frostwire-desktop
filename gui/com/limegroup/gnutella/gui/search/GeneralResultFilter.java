package com.limegroup.gnutella.gui.search;

import com.frostwire.gui.components.LabeledRangeSlider;
import com.limegroup.gnutella.gui.GUIUtils;

public class GeneralResultFilter implements TableLineFilter {

    private ResultPanel _rp;
    private LabeledRangeSlider _rangeSliderSeeds;
    private LabeledRangeSlider _rangeSliderSize;

    private int _minResultsSeeds;
    private int _maxResultsSeeds;
    private long _minResultsSize;
    private long _maxResultsSize;

    private int _minSeeds;
    private int _maxSeeds;
    private int _minSize;
    private int _maxSize;
	private String _keywords;

    public GeneralResultFilter(ResultPanel rp, LabeledRangeSlider rangeSliderSeeds, LabeledRangeSlider rangeSliderSize) {
        _rp = rp;
        _rangeSliderSeeds = rangeSliderSeeds;
        _rangeSliderSize = rangeSliderSize;
        _minResultsSeeds = Integer.MAX_VALUE;
        _maxResultsSeeds = 0;
        _minResultsSize = Long.MAX_VALUE;
        _maxResultsSize = 0;
        _minSeeds = 0;
        _maxSeeds = Integer.MAX_VALUE;
        _minSize = 0;
        _maxSize = Integer.MAX_VALUE;
        _keywords = "";
    }

    public boolean allow(TableLine node) {
        boolean seedsNeedUpdate = false;
        int seeds = node.getSeeds();
        if (seeds < _minResultsSeeds) {
            _minResultsSeeds = seeds;
            seedsNeedUpdate = true;
        }
        if (seeds > _maxResultsSeeds) {
            _maxResultsSeeds = seeds;
            seedsNeedUpdate = true;
        }
        boolean sizeNeedUpdate = false;
        long size = node.getSize();
        if (size < _minResultsSize) {
            _minResultsSize = size;
            sizeNeedUpdate = true;
        }
        if (size > _maxResultsSize) {
            _maxResultsSize = size;
            sizeNeedUpdate = true;
        }

        if (seedsNeedUpdate) {
            _rangeSliderSeeds.getMinimumValueLabel().setText(String.valueOf(_minResultsSeeds));
            _rangeSliderSeeds.getMaximumValueLabel().setText(String.valueOf(_maxResultsSeeds));
        }
        if (sizeNeedUpdate) {
            _rangeSliderSize.getMinimumValueLabel().setText(GUIUtils.toUnitbytes(_minResultsSize));
            _rangeSliderSize.getMaximumValueLabel().setText(GUIUtils.toUnitbytes(_maxResultsSize));
        }
        
        boolean inSeedRange = false;

        if (_maxResultsSeeds > _minResultsSeeds) {
            int seedNorm = ((seeds - _minResultsSeeds) * 1000) / (_maxResultsSeeds - _minResultsSeeds);
            
            if (_minSeeds == 0 && _maxSeeds == 1000) {
                inSeedRange = true;
            } else if (_minSeeds == 0) {
                inSeedRange = seedNorm <= _maxSeeds;
            } else if (_maxSeeds == 1000) {
                inSeedRange = seedNorm >= _minSeeds;
            } else {
                inSeedRange = seedNorm >= _minSeeds && seedNorm <= _maxSeeds;
            }
        } else {
            inSeedRange = seeds == _maxResultsSeeds;
        }
        
        boolean inSizeRange = false;
        
        if (_maxResultsSize > _minResultsSize) {
            long sizeNorm = ((size - _minResultsSize) * 1000) / (_maxResultsSize - _minResultsSize);

            if (_minSize == 0 && _maxSize == 1000) {
                inSizeRange = true;
            } else if (_minSize == 0) {
                inSizeRange = sizeNorm <= _maxSize;
            } else if (_maxSize == 1000) {
                inSizeRange = sizeNorm >= _minSize;
            } else {
                inSizeRange = sizeNorm >= _minSize && sizeNorm <= _maxSize;
            }
        } else {
            inSizeRange = size == _maxResultsSize;
        }
        
        boolean hasKeywords = hasKeywords(node.getFilenameNoExtension());

        return inSeedRange && inSizeRange && hasKeywords;
    }

    private boolean hasKeywords(String filename) {

    	if (_keywords == null || _keywords.trim().length()==0) {
    		return true;
    	}
    	
    	//if it's just one keyword.
    	String[] keywords = _keywords.split(" ");
    	
    	if (keywords.length == 1) {
    		return filename.contains(_keywords);
    	} else {
    		String fname = filename.toLowerCase();
    		//all keywords must be in the file name.
    		for (String k : keywords) {
    			if (!fname.contains(k.toLowerCase())) {
    				return false;
    			}
    		}
    	}
    	
    	return true;
	}

	public int getMinResultsSeeds() {
        return _minResultsSeeds;
    }

    public int getMaxResultsSeeds() {
        return _maxResultsSeeds;
    }

    public long getMinResultsSize() {
        return _minResultsSize;
    }

    public long getMaxResultsSize() {
        return _maxResultsSize;
    }

    public int getMinSeeds() {
        return _minSeeds;
    }

    public int getMaxSeeds() {
        return _maxSeeds;
    }

    public int getMinSize() {
        return _minSize;
    }

    public int getMaxSize() {
        return _maxSize;
    }

    public void setRangeSeeds(int min, int max) {
        _minSeeds = min;
        _maxSeeds = max;
        _rp.filterChanged(this, 1);
    }

    public void setRangeSize(int min, int max) {
        _minSize = min;
        _maxSize = max;
        _rp.filterChanged(this, 1);
    }

	public void updateKeywordFiltering(String text) {
		_keywords = text;
		_rp.filterChanged(this,1);
	}
}
