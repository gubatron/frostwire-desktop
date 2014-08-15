/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.frostwire.gui.Librarian;
import com.limegroup.gnutella.gui.tables.DefaultTableBevelledCellRenderer;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
class FileShareCellRenderer extends DefaultTableBevelledCellRenderer {

    private static final long serialVersionUID = 6392689488563533358L;

    public FileShareCellRenderer() {
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        FileShareCell cell = (FileShareCell) value;
        JLabel component = (JLabel) super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
        component.setIcon(getNextFileShareStateIcon(cell.getPath()));
        component.setHorizontalTextPosition(JLabel.CENTER);
        component.setHorizontalAlignment(SwingConstants.CENTER);
        return component;
    }

    static Icon getNextFileShareStateIcon(String path) {
        int state = Librarian.instance().getFileShareState(path);
        switch (state) {
        case Librarian.FILE_STATE_UNSHARED:
            return LibraryUtils.FILE_UNSHARED_ICON;
        case Librarian.FILE_STATE_SHARING:
            return LibraryUtils.FILE_SHARING_ICON;
        case Librarian.FILE_STATE_SHARED:
            return LibraryUtils.FILE_SHARED_ICON;
        }
        return null;
    }
}