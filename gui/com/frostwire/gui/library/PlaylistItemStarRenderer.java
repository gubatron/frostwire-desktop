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
        final LibraryPlaylistsTableDataLine line = ((PlaylistItemStar) value).getLine();
        final PlaylistItemStar cell = (PlaylistItemStar) value;
        
        setIcon(cell.isPlaying(), line.getPlayListItem().isStarred());
        
        final JLabel component = (JLabel) super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setIcon(false, line.getPlayListItem().isStarred());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                setIcon(cell.isPlaying(), line.getPlayListItem().isStarred());
            }
        });
        
        return component;
    }
    
    private void setIcon(boolean playing, boolean starred) {
        if (playing) {
            setIcon(speaker);
        } else if (starred) {
            setIcon(starOn);
        } else {
            setIcon(starOff);
        }
    }
}