package com.limegroup.gnutella.gui.playlist;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

import com.limegroup.gnutella.gui.mp3.MediaPlayerComponent;

/**
 *  Creates both a renderer and an editor for cells in the playlist table that display the name
 *  of the file being played. When displaying a preview item from the LWS, buttons are
 *  displayed which enable the user to purchase the song directly from the playlist
 */
public class PlaylistItemNameRendererEditor extends SubstanceDefaultTableCellRenderer {
    
    /**
     * 
     */
    private static final long serialVersionUID = -6309702261794142462L;
    /**
     * line containing information about the row being painted
     */
    private PlaylistDataLine line;
    
    public PlaylistItemNameRendererEditor(){
        super();
    }

    /**
     * Returns the default filename for this table line
     */
//    @Override
//    protected String getNameForValue(Object value) { 
//        final PlaylistItemName rnh = (PlaylistItemName)value;
//        this.line = rnh.getLine();
//        return line.getSongName();
//    }
     
    
    /**
     * @return true if this PlayListItem is currently playing, false otherwise
     */
    protected boolean isPlaying(){
        return MediaPlayerComponent.getInstance().getCurrentSong() == line.getPlayListItem();
    }
    
    /**
     * Check what font color to use if this song is playing. 
     */
//    @Override
//    protected Color getFontColor(Color defaultColor) {
//        if( line != null && isPlaying() )
//            return line.getColor(true);
//        else
//            return defaultColor;
//    }
    
    public String toString() {
        return line.getSongName();
    }
}