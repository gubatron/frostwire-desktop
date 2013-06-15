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

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SearchResultActionsEditor extends AbstractCellEditor implements TableCellEditor {

    private static final long serialVersionUID = -1173782952710148468L;

    private final SearchResultActionsRenderer renderer;
    private Object value;

    public SearchResultActionsEditor() {
        renderer = new SearchResultActionsRenderer();
    }

    public Object getCellEditorValue() {
        return value;
    }

    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        this.value = value;
        SearchResultActionsRenderer actionsComponent = (SearchResultActionsRenderer) renderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
        return actionsComponent; 
    }
}