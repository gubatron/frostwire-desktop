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

package com.frostwire.gui.library;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.frostwire.alexandria.InternetRadioStation;
import com.limegroup.gnutella.gui.GUIMediator;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class InternetRadioBookmarkEditor extends AbstractCellEditor implements TableCellEditor {

	private static final long serialVersionUID = 8617767980030779166L;
	private static final Icon bookmarkedOn;
    private static final Icon bookmarkedOff;
   
    static {
    	bookmarkedOn = GUIMediator.getThemeImage("radio_bookmarked_on");
    	bookmarkedOff = GUIMediator.getThemeImage("radio_bookmarked_off");
    }

    public InternetRadioBookmarkEditor() {
    }

    public Object getCellEditorValue() {
        return null;
    }

    public Component getTableCellEditorComponent(final JTable table, final Object value, boolean isSelected, int row, int column) {
        final LibraryInternetRadioTableDataLine line = ((InternetRadioBookmark) value).getLine();

        final JLabel component = (JLabel) new InternetRadioBookmarkRenderer().getTableCellRendererComponent(table, value, isSelected, true, row, column);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                InternetRadioStation radioStation = line.getInitializeObject();
                if (line.getInitializeObject().isBookmarked()) {
                    radioStation.setBookmarked(false);
                    radioStation.save();
                    component.setIcon(bookmarkedOff);
                } else {
                    radioStation.setBookmarked(true);
                    radioStation.save();
                    component.setIcon(bookmarkedOn);
                }
            }
        });

        component.setIcon((line.getInitializeObject().isBookmarked()) ? bookmarkedOn : bookmarkedOff);

        return component;
    }
}