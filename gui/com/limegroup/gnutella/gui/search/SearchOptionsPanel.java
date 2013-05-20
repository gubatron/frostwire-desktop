/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.limegroup.gnutella.gui.search;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.limewire.setting.BooleanSetting;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledTextField;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
final class SearchOptionsPanel extends JPanel {

    private final SearchResultMediator resultPanel;

    private final LabeledTextField textFieldKeywords;
    private final LabeledRangeSlider sliderSize;
    private final LabeledRangeSlider sliderSeeds;
    
    //private final GeneralResultFilter 

    public SearchOptionsPanel(SearchResultMediator resultPanel) {
        this.resultPanel = resultPanel;

        setLayout(new MigLayout("insets 0"));

        add(createSearchEnginesFilter(), "wrap");

        this.textFieldKeywords = createNameFilter();
        add(textFieldKeywords, "wrap");

        this.sliderSize = createSizeFilter();
        add(sliderSize, "wrap");

        this.sliderSeeds = createSeedsFilter();
        add(sliderSeeds, "wrap");

        resetFilters();
    }

    public void updateFiltersPanel() {
        GeneralResultFilter filter = new GeneralResultFilter(resultPanel, sliderSeeds, sliderSize, textFieldKeywords);
        resultPanel.filterChanged(filter, 1);
    }

    public void resetFilters() {
        sliderSeeds.setMinimum(0);
        sliderSeeds.setMaximum(100);
        sliderSeeds.setLowerValue(0);
        sliderSeeds.setUpperValue(100);

        sliderSize.setMinimum(0);
        sliderSize.setMaximum(100);
        sliderSize.setLowerValue(0);
        sliderSize.setUpperValue(100);

        sliderSeeds.getMinimumValueLabel().setText(I18n.tr("0"));
        sliderSeeds.getMaximumValueLabel().setText(I18n.tr("Max"));
        sliderSize.getMinimumValueLabel().setText(I18n.tr("0"));
        sliderSize.getMaximumValueLabel().setText(I18n.tr("Max"));

        textFieldKeywords.setText("");
    }

    private JComponent createSearchEnginesFilter() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setAlignmentX(0.0f);
        List<SearchEngine> searchEngines = SearchEngine.getEngines();
        setupCheckboxes(searchEngines, panel);
        return panel;
    }

    private LabeledTextField createNameFilter() {
        LabeledTextField textField = new LabeledTextField(I18n.tr("Name"), 40, -1, 100);

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                keywordFilterChanged(e);
            }
        });

        return textField;
    }

    private LabeledRangeSlider createSizeFilter() {
        LabeledRangeSlider slider = new LabeledRangeSlider(I18n.tr("Size"), null, 0, 1000);
        slider.setPreferredSize(new Dimension(80, (int) slider.getPreferredSize().getHeight()));
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                sliderSize_stateChanged(e);
            }
        });

        return slider;
    }

    private LabeledRangeSlider createSeedsFilter() {
        LabeledRangeSlider slider = new LabeledRangeSlider(I18n.tr("Seeds"), null, 0, 1000);
        slider.setPreferredSize(new Dimension(80, (int) slider.getPreferredSize().getHeight()));
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                sliderSeeds_stateChanged(e);
            }
        });

        return slider;
    }

    private void setupCheckboxes(List<SearchEngine> searchEngines, JPanel parent) {

        final Map<JCheckBox, BooleanSetting> cBoxes = new HashMap<JCheckBox, BooleanSetting>();

        ItemListener listener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean allDeSelected = true;

                for (JCheckBox cBox : cBoxes.keySet()) {
                    if (cBox.isSelected()) {
                        allDeSelected = false;
                        break;
                    }
                }

                if (allDeSelected) {
                    ((JCheckBox) e.getItemSelectable()).setSelected(true);
                }

                for (JCheckBox cBox : cBoxes.keySet()) {
                    cBoxes.get(cBox).setValue(cBox.isSelected());
                }

                if (resultPanel != null) {
                    resultPanel.filterChanged(new SearchEngineFilter(), 0);
                }
            }
        };

        for (SearchEngine se : searchEngines) {
            JCheckBox cBox = new JCheckBox(se.getName());
            cBox.setSelected(se.isEnabled());

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets(0, 10, 2, 10);
            parent.add(cBox, c);

            cBoxes.put(cBox, se.getEnabledSetting());
            cBox.addItemListener(listener);
        }
    }

    private void keywordFilterChanged(KeyEvent e) {
        //        if (_activeFilter != null) {
        //            _activeFilter.updateKeywordFiltering(_keywordFilterTextField.getText());
        //        }
    }

    private void sliderSize_stateChanged(ChangeEvent e) {
        System.out.println("sliderSize_stateChanged");
        //        if (_activeFilter != null) {
        //            _activeFilter.setRangeSize(_rangeSliderSize.getValue(), _rangeSliderSize.getUpperValue());
        //        }
    }

    private void sliderSeeds_stateChanged(ChangeEvent e) {
        System.out.println("sliderSeeds_stateChanged");
        //        if (_activeFilter != null) {
        //            _activeFilter.setRangeSeeds(_rangeSliderSeeds.getValue(), _rangeSliderSeeds.getUpperValue());
        //        }
    }
}
