package com.limegroup.gnutella.gui.search;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.frostwire.gui.components.RangeSlider;
import com.limegroup.gnutella.gui.I18n;

public class FilterPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 346186974515196119L;
    
    private JLabel _labelMinSeeds;
    private JLabel _labelSeeds;
    private JLabel _labelMaxSeeds;
    private RangeSlider _rangeSliderSeeds;
    private JLabel _labelMinSize;
    private JLabel _labelSize;
    private JLabel _labelMaxSize;
    private RangeSlider _rangeSliderSize;

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
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(new TitledBorder(I18n.tr("Filter")));
        
        JPanel panelSeeds = new JPanel(new BorderLayout());

        _labelMinSeeds = new JLabel(I18n.tr("0"));
        panelSeeds.add(_labelMinSeeds, BorderLayout.LINE_START);
        
        _labelSeeds = new JLabel(I18n.tr("Sources"));
        _labelSeeds.setHorizontalAlignment(SwingConstants.CENTER);
        panelSeeds.add(_labelSeeds, BorderLayout.CENTER);
        
        _labelMaxSeeds = new JLabel(I18n.tr("Max"));
        panelSeeds.add(_labelMaxSeeds, BorderLayout.LINE_END);
        
        _rangeSliderSeeds = new RangeSlider(0, 100);
        _rangeSliderSeeds.setPreferredSize(new Dimension(80, (int) _rangeSliderSeeds.getPreferredSize().getHeight()));
        _rangeSliderSeeds.setValue(0);
        _rangeSliderSeeds.setUpperValue(100);
        panelSeeds.add(_rangeSliderSeeds, BorderLayout.PAGE_END);
        
        add(panelSeeds);
        
        JPanel panelSize = new JPanel(new BorderLayout());

        _labelMinSize = new JLabel(I18n.tr("0"));
        panelSize.add(_labelMinSize, BorderLayout.LINE_START);
        
        _labelSize = new JLabel(I18n.tr("Size"));
        _labelSize.setHorizontalAlignment(SwingConstants.CENTER);
        panelSize.add(_labelSize, BorderLayout.CENTER);
        
        _labelMaxSize = new JLabel(I18n.tr("Max"));
        panelSize.add(_labelMaxSize, BorderLayout.LINE_END);
        
        _rangeSliderSize = new RangeSlider(0, 100);
        _rangeSliderSize.setPreferredSize(new Dimension(80, (int) _rangeSliderSize.getPreferredSize().getHeight()));
        _rangeSliderSize.setValue(0);
        _rangeSliderSize.setUpperValue(100);
        panelSize.add(_rangeSliderSize, BorderLayout.PAGE_END);
        
        add(panelSize);
    }
    
    public void setSlidersEnabled(boolean enabled) {
        _rangeSliderSeeds.setEnabled(enabled);
        _rangeSliderSize.setEnabled(enabled);
    }
    
    public void reset() {
        _rangeSliderSeeds.setMinimum(0);
        _rangeSliderSeeds.setMaximum(100);
        _rangeSliderSeeds.setValue(0);
        _rangeSliderSeeds.setUpperValue(100);
        _rangeSliderSize.setMinimum(0);
        _rangeSliderSize.setMaximum(100);
        _rangeSliderSize.setValue(0);
        _rangeSliderSize.setUpperValue(100);
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
