/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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
