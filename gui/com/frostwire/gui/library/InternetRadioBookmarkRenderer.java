package com.frostwire.gui.library;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

import com.limegroup.gnutella.gui.GUIMediator;

/**
 *  Creates both a renderer and an editor for cells in the playlist table that display the name
 *  of the file being played.
 */
class InternetRadioBookmarkRenderer extends SubstanceDefaultTableCellRenderer {

    private static final long serialVersionUID = 6800146830099830381L;

    private static final Icon bookmarkedOn;
    private static final Icon bookmarkedOff;
    private static final Icon speaker;
   

    static {
    	bookmarkedOn = GUIMediator.getThemeImage("radio_bookmarked_on");
    	bookmarkedOff = GUIMediator.getThemeImage("radio_bookmarked_off");
        speaker = GUIMediator.getThemeImage("speaker");
    }

    public InternetRadioBookmarkRenderer() {
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        final LibraryInternetRadioTableDataLine line = ((InternetRadioBookmark) value).getLine();
        final InternetRadioBookmark cell = (InternetRadioBookmark) value;

        setIcon(cell.isPlaying(), line.getInitializeObject().isBookmarked());

        final JLabel component = (JLabel) super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setIcon(false, line.getInitializeObject().isBookmarked());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setIcon(cell.isPlaying(), line.getInitializeObject().isBookmarked());
            }
        });

        return component;
    }

    private void setIcon(boolean playing, boolean bookmarked) {
    	if (playing) {
            setIcon(speaker);
        } else {
            setIcon((bookmarked) ? bookmarkedOn : bookmarkedOff);
        }
    }
}