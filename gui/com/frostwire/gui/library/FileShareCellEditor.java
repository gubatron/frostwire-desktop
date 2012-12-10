/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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

package com.frostwire.gui.library;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.frostwire.gui.Librarian;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class FileShareCellEditor extends AbstractCellEditor implements TableCellEditor {

    private static final long serialVersionUID = -1754077059065368054L;

    public FileShareCellEditor() {
    }

    public Object getCellEditorValue() {
        return null;
    }

    public Component getTableCellEditorComponent(final JTable table, final Object value, boolean isSelected, int row, int column) {
        final FileShareCell cell = (FileShareCell) value;

        final JLabel component = (JLabel) new FileShareCellRenderer().getTableCellRendererComponent(table, value, isSelected, true, row, column);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String path = cell.getPath();
                int state = Librarian.instance().getFileShareState(path);
                switch (state) {
                case Librarian.FILE_STATE_UNSHARED:
                    cell.getDataLine().setShared(true);
                    Librarian.instance().shareFile(path, true);
                    component.setIcon(LibraryUtils.FILE_SHARING_ICON);
                    break;
                case Librarian.FILE_STATE_SHARING: //nothing to do for now
                    break;
                case Librarian.FILE_STATE_SHARED:
                    cell.getDataLine().setShared(false);
                    Librarian.instance().shareFile(path, false);
                    component.setIcon(LibraryUtils.FILE_UNSHARED_ICON);
                }
                
                if (table.isEditing()) {
                    TableCellEditor editor = table.getCellEditor();
                    editor.cancelCellEditing();
                }
            }
        });
        component.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (table.isEditing()) {
                    TableCellEditor editor = table.getCellEditor();
                    editor.cancelCellEditing();
                }
            }
        });

        component.setIcon(FileShareCellRenderer.getNextFileShareStateIcon(cell.getPath()));

        return component;
    }
}