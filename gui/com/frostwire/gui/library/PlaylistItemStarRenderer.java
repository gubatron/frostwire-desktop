package com.frostwire.gui.library;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

import com.limegroup.gnutella.gui.GUIMediator;

/**
 *  Creates both a renderer and an editor for cells in the playlist table that display the name
 *  of the file being played.
 */
class PlaylistItemStarRenderer extends SubstanceDefaultTableCellRenderer {

    private static final long serialVersionUID = 6800146830099830381L;
    
    private final Icon starOn;
    private final Icon starOff;
    private final Icon speaker;

    public PlaylistItemStarRenderer() {
        starOn = GUIMediator.getThemeImage("star_on");
        starOff = GUIMediator.getThemeImage("star_off");
        speaker = GUIMediator.getThemeImage("speaker");
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        LibraryPlaylistsTableDataLine line = ((PlaylistItemStar) value).getLine();
        PlaylistItemStar cell = (PlaylistItemStar) value;
        
        if (cell.isPlaying()) {
            setIcon(speaker);
        } else if (line.getPlayListItem().isStarred()) {
            setIcon(starOn);
        } else {
            setIcon(starOff);
        }
        
        return super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
    }
}