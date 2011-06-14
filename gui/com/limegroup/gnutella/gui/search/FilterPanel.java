package com.limegroup.gnutella.gui.search;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.frostwire.gui.components.LabeledRangeSlider;
import com.limegroup.gnutella.gui.I18n;

public class FilterPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 346186974515196119L;
    
    private LabeledRangeSlider _rangeSliderSeeds;
    private LabeledRangeSlider _rangeSliderSize;

    private int _minSeeds;
    private int _maxSeeds;
    private int _minSize;
    private int _maxSize;
    
    private GeneralResultFilter _activeFilter;
    
    private final Map<ResultPanel, GeneralResultFilter> ACTIVE_FILTERS =
        new HashMap<ResultPanel, GeneralResultFilter>();

    public FilterPanel() {
        setupUI();
        setSlidersEnabled(false);
    }

    protected void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        
        setBorder(new TitledBorder(I18n.tr("Filter Results")));

        _rangeSliderSeeds = new LabeledRangeSlider("Download Sources", null, 0, 50000);
        _rangeSliderSeeds.setPreferredSize(new Dimension(80, (int) _rangeSliderSeeds.getPreferredSize().getHeight()));
        _rangeSliderSeeds.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                rangeSliderSeeds_stateChanged(e);
            }
        });
        add(_rangeSliderSeeds,c);

        _rangeSliderSize = new LabeledRangeSlider("File Size",null,0,1000);
        _rangeSliderSize.setPreferredSize(new Dimension(80, (int) _rangeSliderSize.getPreferredSize().getHeight()));
        _rangeSliderSize.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                rangeSliderSize_stateChanged(e);
            }
        });
        add(_rangeSliderSize,c);
    }
    
    protected void rangeSliderSeeds_stateChanged(ChangeEvent e) {
        if (_activeFilter != null) {
            _activeFilter.setRangeSeeds(_rangeSliderSeeds.getValue(), _rangeSliderSeeds.getUpperValue());
        }
    }

    protected void rangeSliderSize_stateChanged(ChangeEvent e) {
        if (_activeFilter != null) {
            _activeFilter.setRangeSize(_rangeSliderSize.getValue(), _rangeSliderSize.getUpperValue());
        }
    }

    public void setSlidersEnabled(boolean enabled) {
        _rangeSliderSeeds.setEnabled(enabled);
        _rangeSliderSize.setEnabled(enabled);
    }
    
    public void reset() {
        _rangeSliderSeeds.setMinimum(0);
        _rangeSliderSeeds.setMaximum(1000);
        _rangeSliderSeeds.setValue(0);
        _rangeSliderSeeds.setUpperValue(1000);
        _rangeSliderSize.setMinimum(0);
        _rangeSliderSize.setMaximum(1000);
        _rangeSliderSize.setValue(0);
        _rangeSliderSize.setUpperValue(1000);
    }

    public void clearFilters() {
        ACTIVE_FILTERS.clear();
        setSlidersEnabled(false);
        reset();
    }

    public void setFilterFor(ResultPanel rp) {
        GeneralResultFilter filter = ACTIVE_FILTERS.get(rp);
        if(filter == null) {
            filter = new GeneralResultFilter(rp);
            ACTIVE_FILTERS.put(rp, filter);
        }
        setActiveFilter(filter);
    }
    
    private void setActiveFilter(GeneralResultFilter filter) {
        _activeFilter = filter;
        setSlidersEnabled(true);
    }

    public void panelReset(ResultPanel rp) {
        GeneralResultFilter filter = ACTIVE_FILTERS.get(rp);
        if(filter != null) {
            ACTIVE_FILTERS.remove(rp);
            setFilterFor(rp);
        }
    }

    public boolean panelRemoved(ResultPanel rp) {
        GeneralResultFilter filter = ACTIVE_FILTERS.get(rp);
        if(filter != null) {
            ACTIVE_FILTERS.remove(rp);
        }
        return ACTIVE_FILTERS.isEmpty();
    }
}
