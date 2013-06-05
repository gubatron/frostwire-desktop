/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, 2013, FrostWire(R). All rights reserved.
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

package com.limegroup.gnutella.gui.tables;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.limegroup.gnutella.gui.GUIMediator;

/**
 * Renders the column in the search window that displays an icon for
 * whether or not the host returning the result is chattable.
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class ChatRenderer extends DefaultTableCellRenderer {

    /**
     * Constant <tt>Icon</tt> for chatability.
     */
    private static Icon _chatIcon = GUIMediator.getThemeImage("chat");

    /**
     * The constructor sets this <tt>JLabel</tt> to be opaque and sets the
     * border.
     */
    public ChatRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    // inherit doc comment
    public void updateTheme() {
        _chatIcon = GUIMediator.getThemeImage("chat");
    }

    /**
     * Returns the <tt>Component</tt> that displays the stars based
     * on the number of stars in the <tt>QualityHolder</tt> object.
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if (value != null && value.equals(Boolean.TRUE))
            setIcon(_chatIcon);
        else
            setIcon(null);
        return super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
    }
}
