package com.limegroup.gnutella.gui.search;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.frostwire.gui.components.RangeSlider;
import com.limegroup.gnutella.gui.GUIUtils;
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

    public FilterPanel() {
        setupUI();
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
        
        _rangeSliderSeeds = new RangeSlider(0, Integer.MAX_VALUE);
        _rangeSliderSeeds.setPreferredSize(new Dimension(80, (int) _rangeSliderSeeds.getPreferredSize().getHeight()));
        _rangeSliderSeeds.setValue(0);
        _rangeSliderSeeds.setUpperValue(Integer.MAX_VALUE);
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
        
        _rangeSliderSize = new RangeSlider(0, Integer.MAX_VALUE);
        _rangeSliderSize.setPreferredSize(new Dimension(80, (int) _rangeSliderSize.getPreferredSize().getHeight()));
        _rangeSliderSize.setValue(0);
        _rangeSliderSize.setUpperValue(Integer.MAX_VALUE);
        panelSize.add(_rangeSliderSize, BorderLayout.PAGE_END);
        
        add(panelSize);
    }
}
