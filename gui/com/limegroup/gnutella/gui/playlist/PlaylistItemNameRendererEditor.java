package com.limegroup.gnutella.gui.playlist;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.limegroup.gnutella.gui.mp3.MediaPlayerComponent;
import com.limegroup.gnutella.gui.search.CompositeCellTableRendererAndTableCellEditor;

/**
 *  Creates both a renderer and an editor for cells in the playlist table that display the name
 *  of the file being played. When displaying a preview item from the LWS, buttons are
 *  displayed which enable the user to purchase the song directly from the playlist
 */
public class PlaylistItemNameRendererEditor extends CompositeCellTableRendererAndTableCellEditor {
    
    /**
     * line containing information about the row being painted
     */
    private PlaylistDataLine line;
    
    public PlaylistItemNameRendererEditor(){
        super();
    }
    
    /**
     * Create two buttons in the playlist, a buy and a try button that reference store
     * actions. 
     */
    @Override
    protected AbstractAction[] createActions() {
    //GUB: temp fix to compile, might just get rid of these two actions if needed later.
        return new AbstractAction[] { 
           new AbstractAction("buy") {
            public void actionPerformed(ActionEvent e) {
                //PlaylistMediator.getInstance().buyProduct(line);
            }
        }, new AbstractAction("try") {
            public void actionPerformed(ActionEvent e) {
                //PlaylistMediator.getInstance().infoProduct(line);
            }
        } };
    }

    /**
     * Returns the default filename for this table line
     */
    @Override
    protected String getNameForValue(Object value) { 
        final PlaylistItemName rnh = (PlaylistItemName)value;
        this.line = rnh.getLine();
        return line.getSongName();
    }
     
    /**
     * Determines whether to paint the buttons for purchasing a song
     * This should only be true when the item it a LWS preview
     */
    protected boolean buttonsVisible(){
        return false;//line.isStoreSong();
    }
    
    /**
     * @return true if this PlayListItem is currently playing, false otherwise
     */
    protected boolean isPlaying(){
        return MediaPlayerComponent.getInstance().getCurrentSong() == line.getPlayListItem();
    }
    
    /**
     * Check what font color to use if this song is playing. 
     */
    @Override
    protected Color getFontColor(Color defaultColor) {
        if( line != null && isPlaying() )
            return line.getColor(true);
        else
            return defaultColor;
    }
    
    public String toString() {
        return line.getSongName();
    }
}