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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.limegroup.gnutella.gui.GUIMediator;

/**
 *  Creates both a renderer and an editor for cells in the playlist table that display the name
 *  of the file being played.
 */
class InternetRadioBookmarkRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 6800146830099830381L;

    private static final Icon bookmarkedOn;
    private static final Icon bookmarkedOff;

    static {
        bookmarkedOn = GUIMediator.getThemeImage("radio_bookmarked_on");
        bookmarkedOff = GUIMediator.getThemeImage("radio_bookmarked_off");
    }

    public InternetRadioBookmarkRenderer() {
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        final LibraryInternetRadioTableDataLine line = ((InternetRadioBookmark) value).getLine();

        setIcon(line.getInitializeObject().isBookmarked());

        final JLabel component = (JLabel) super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setIcon(line.getInitializeObject().isBookmarked());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setIcon(line.getInitializeObject().isBookmarked());
            }
        });

        return component;
    }

    private void setIcon(boolean bookmarked) {
        setIcon((bookmarked) ? bookmarkedOn : bookmarkedOff);
    }
}