package com.frostwire.gui.library;

import org.limewire.util.StringUtils;

class PlaylistItemProperty implements Comparable<PlaylistItemProperty> {

    private final String value;
    private final boolean playing;
    private final boolean exists;
    private final int columnIndex;

    public PlaylistItemProperty(String value, boolean playing, boolean exists, int columnIndex) {
        this.value = value;
        this.playing = playing;
        this.exists = exists;
        this.columnIndex = columnIndex;
    }

    public String getValue() {
        return value;
    }

    public boolean isPlaying() {
        return playing;
    }
    
    public boolean exists() {
        return exists;
    }

    public int compareTo(PlaylistItemProperty o) {
    	if ((o == null || o.value == null) && value != null) {
    		return 1;
    	} else if (value == null && o!=null) {
    		return -1;
    	} else if (value == null && o==null) {
    		return 0;
    	}
    	
    	//bitrates come in strings, we only care about the numerical portion
    	//since all of them are in KBPS.
    	if (columnIndex == LibraryPlaylistsTableDataLine.BITRATE_IDX) {
    		return compareByBitrate(o);
    	} else if (columnIndex == LibraryPlaylistsTableDataLine.TRACK_IDX) {
    	    return compareByTrack(o);
    	}
    	
        return value.compareTo(o.value);
    }

    private int compareByBitrate(PlaylistItemProperty o) {
        if (StringUtils.isNullOrEmpty(value) && !StringUtils.isNullOrEmpty(o.value)) {
        	return 1;
        } else if (!StringUtils.isNullOrEmpty(value) && StringUtils.isNullOrEmpty(o.value)) {
        	return -1;
        } else if (StringUtils.isNullOrEmpty(value) && StringUtils.isNullOrEmpty(o.value)) {
        	return 0;
        }
        
        try {
        	return Integer.valueOf(value.toLowerCase().replace("kbps", "").trim()).compareTo(Integer.valueOf(o.value.toLowerCase().replace("kbps", "").trim()));
        } catch (Exception e) {
        	return 0;
        }
    }
    
    private int compareByTrack(PlaylistItemProperty o) {
        if (StringUtils.isNullOrEmpty(value) && !StringUtils.isNullOrEmpty(o.value)) {
            return 1;
        } else if (!StringUtils.isNullOrEmpty(value) && StringUtils.isNullOrEmpty(o.value)) {
            return -1;
        } else if (StringUtils.isNullOrEmpty(value) && StringUtils.isNullOrEmpty(o.value)) {
            return 0;
        }
        
        try {
            return Integer.valueOf(value.toLowerCase().trim().replaceFirst("^0+(?!$)", "")).compareTo(Integer.valueOf(o.value.toLowerCase().trim().replaceFirst("^0+(?!$)", "")));
        } catch (Exception e) {
            return 0;
        }
    }
}
