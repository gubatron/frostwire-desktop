/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import com.frostwire.gui.LocaleLabel;
import com.frostwire.gui.theme.ThemeMediator;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class SearchResultNameRenderer extends JPanel implements TableCellRenderer {

    private LocaleLabel labelText;

    public SearchResultNameRenderer() {
        setupUI();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setData((SearchResultNameHolder) value, table, row);
        this.setOpaque(true);
        this.setEnabled(table.isEnabled());

        if (isSelected) {
            this.setBackground(ThemeMediator.TABLE_SELECTED_BACKGROUND_ROW_COLOR);
        } else {
            this.setBackground(row % 2 == 1 ? ThemeMediator.TABLE_ALTERNATE_ROW_COLOR : Color.WHITE);
        }

        return this;
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints c;
        labelText = new LocaleLabel();
        labelText.setHorizontalTextPosition(SwingConstants.LEFT);
        c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(labelText, c);
    }

    private void setData(SearchResultNameHolder value, JTable table, int row) {
        labelText.setText(value.getLocaleString());
        labelText.setFont(table.getFont());
        syncFont(table, labelText);
    }

    private void syncFont(JTable table, JComponent c) {
        Font tableFont = table.getFont();
        if (tableFont != null && !tableFont.equals(c.getFont())) {
            c.setFont(tableFont);
        }
    }
}