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