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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.limewire.setting.BooleanSetting;

import com.frostwire.gui.theme.ThemeMediator;
import com.limegroup.gnutella.gui.I18n;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
final class SearchOptionsPanel extends JPanel {

    private final SearchResultMediator resultPanel;

    public SearchOptionsPanel(SearchResultMediator resultPanel) {
        this.resultPanel = resultPanel;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JPanel controls = new JPanel();
        controls.setLayout(new GridBagLayout());
        controls.setAlignmentX(0.0f);
        List<SearchEngine> searchEngines = SearchEngine.getEngines();
        setupCheckboxes(searchEngines, controls);
        add(controls);

        add(Box.createVerticalStrut(15));

        SearchFilterPanel filterPanel = new SearchFilterPanel();
        filterPanel.setBorder(ThemeMediator.createTitledBorder(I18n.tr("Filter")));
        filterPanel.setAlignmentX(0.0f);
        add(filterPanel);
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
}
