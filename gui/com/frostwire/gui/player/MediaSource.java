/*
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

package com.frostwire.gui.player;

import java.io.File;

import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.core.FileDescriptor;

/**
 *  A wrapper for the source of an audio file that is currently playing
 *  
 */
public class MediaSource {

    /**
     * current audio source that is loaded in the music player
     */
    private final File file;

    private final String url;

    private final PlaylistItem playlistItem;
    
    private String titleText = "";
    private String toolTipText = "";
	

    public MediaSource(File file) {
        if (file == null) {
            throw new NullPointerException("File cannot be null");
        }

        this.file = file;
        this.url = null;
        this.playlistItem = null;
        initializeDisplayText();
    }

    @Override
    public String toString() {
        String name = null;
        if (getFile() != null) {
            name = getFile().getName();
        } else {
            name = url;
        }
        return "[MediaSource@" + hashCode() + ": " + name + "]";
    }

    public MediaSource(String url) {
        if (url == null) {
            throw new NullPointerException("Url cannot be null");
        }

        this.file = null;
        this.url = url;
        this.playlistItem = null;
        
        //PLEASE FIX: initializeDisplayText() can't be called so early
        //or initializeDisplayText() must be re-implemented.
        //
        //When you code a parent class method (in this case initializeDisplayText()) that makes checks about
        //children classes, that's probably a hint of bad design, because you have to know in advance
        //how future inheritance will work. If you make children classes depend on this method, then you always
        //have to go to the parent class and this will become (as it already is) a source of never ending bugs.
        //
        //For now we have work around that will keep breaking, I suggest instead to make initializeDisplayText()
        //an abstract method, and the constructor of each class should be responsible of implementing it and invoking
        //it only after its member variables have properly been initialized.
        //
        //When children constructors invoke super(), they will invoke initializeDisplayText()
        //and initializeDisplayText() will break left and right with a bunch of fields that
        //have not been populated yet.
        initializeDisplayText();
    }

    public MediaSource(PlaylistItem playlistItem) {
        if (playlistItem == null) {
            throw new NullPointerException("PlaylistItem cannot be null");
        }

        this.file = null;
        this.url = null;
        this.playlistItem = playlistItem;
        initializeDisplayText();
    }

    public File getFile() {
        return file;
    }

    public String getURL() {
        return url;
    }

    public PlaylistItem getPlaylistItem() {
        return playlistItem;
    }

    @Override
    public boolean equals(Object obj) {
        MediaSource o = (MediaSource) obj;
        if (file != null && o.file != null) {
            return file.equals(o.file);
        }
        if (url != null && o.url != null) {
            return url.equals(o.url);
        }
        if (playlistItem != null && o.playlistItem != null) {
            return playlistItem.equals(o.playlistItem);
        }
        return false;
    }
    
    protected void initializeDisplayText() {
    	
    	    PlaylistItem playlistItem = getPlaylistItem();
        
		if (this instanceof DeviceMediaSource) {
            
			FileDescriptor fd = ((DeviceMediaSource) this).getFileDescriptor();
            String artistName = fd.artist;
            String songTitle = fd.title;

            String albumToolTip = fd.album;
            String yearToolTip = fd.year;

            titleText = artistName + " - " + songTitle;
            toolTipText = artistName + " - " + songTitle + albumToolTip + yearToolTip;
        
		} else if ( this instanceof StreamMediaSource) {
            
		    titleText = ((StreamMediaSource) this).getTitle();
            toolTipText = "";
        
		} else if (playlistItem != null) {
            
        	    String artistName = playlistItem.getTrackArtist();
            String songTitle = playlistItem.getTrackTitle();

            String albumToolTip = (playlistItem.getTrackAlbum() != null && playlistItem.getTrackAlbum().length() > 0) ? " - " + playlistItem.getTrackAlbum() : "";
            String yearToolTip = (playlistItem.getTrackYear() != null && playlistItem.getTrackYear().length() > 0) ? " (" + playlistItem.getTrackYear() + ")" : "";

            titleText = artistName + " - " + songTitle;
            toolTipText = artistName + " - " + songTitle + albumToolTip + yearToolTip;

        } else if (getFile() != null) {
        
            titleText = getFile().getName();
            toolTipText = getFile().getAbsolutePath();
        
        } else if (getFile() == null && getURL() != null) {
        
            System.out.println("StreamURL: " + getURL().toString());
            titleText = "internet "; // generic internet stream
            toolTipText = "";
        
        }
    }
    
    public String getTitleText() {
    	    return titleText;
    }
    
    public String getToolTipText() {
    	    return toolTipText;
    }
}
