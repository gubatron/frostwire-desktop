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
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SearchResultNameEditor extends AbstractCellEditor implements TableCellEditor {

    private static final long serialVersionUID = -1173782952710148468L;

    private final SearchResultNameRenderer renderer;

    public SearchResultNameEditor() {
        renderer = new SearchResultNameRenderer();
    }

    public Object getCellEditorValue() {
        return null;
    }

    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        final Component component = renderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
        if (component.getMouseListeners() == null || component.getMouseListeners().length == 0) {
            component.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (!e.getSource().equals(component)) {
                            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(component, e.getID(), e.getWhen(), e.getModifiers(), component.getX() + e.getX(), component.getY() + e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()));
                        }
                        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(table, e.getID(), e.getWhen(), e.getModifiers(), component.getX() + e.getX(), component.getY() + e.getY(), e.getClickCount(), false, e.getButton()));
                    } else {
                        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(table, e.getID(), e.getWhen(), e.getModifiers(), component.getX() + e.getX(), component.getY() + e.getY(), e.getClickCount(), true, e.getButton()));
                    }
                    e.consume();
                    component.invalidate();
                }
            });
        }

        return component;
    }
}
