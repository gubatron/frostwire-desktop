package com.frostwire.gui.library;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.frostwire.alexandria.PlaylistItem;
import com.limegroup.gnutella.gui.GUIMediator;

public class PlaylistItemStarEditor extends AbstractCellEditor implements TableCellEditor {

    private static final long serialVersionUID = 2484867032644699734L;
    
    private final Icon starOn;
    private final Icon starOff;

    public PlaylistItemStarEditor() {
        starOn = GUIMediator.getThemeImage("star_on");
        starOff = GUIMediator.getThemeImage("star_off");
    }    

    public Object getCellEditorValue() {
        return null;
    }

    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        final LibraryPlaylistsTableDataLine line = ((PlaylistItemStar) value).getLine();
        
        final JLabel component = (JLabel) new PlaylistItemStarRenderer().getTableCellRendererComponent(table, value, isSelected, true, row, column);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                PlaylistItem playlistItem = line.getInitializeObject();
                if (line.getInitializeObject().isStarred()) {
                    playlistItem.setStarred(false);
                    playlistItem.save();
                    component.setIcon(starOff);
                } else {
                    playlistItem.setStarred(true);
                    playlistItem.save();
                    component.setIcon(starOn);
                }
            }
        });
        
        if (line.getInitializeObject().isStarred()) {
            component.setIcon(starOn);
        } else {
            component.setIcon(starOff);
        }

        return component;
    }
}
