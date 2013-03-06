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

public class PlaylistItemBitRateProperty extends PlaylistItemIntProperty {

    public PlaylistItemBitRateProperty(LibraryPlaylistsTableDataLine line, String stringValue, boolean playing, boolean exists) {
        // using Integer.MAX_VALUE to put entries with no bitrate at the bottom of the list
        super(line, stringValue, stringValue.toLowerCase().replace("kbps", "").trim().length() > 0 ? 
                           Integer.valueOf( stringValue.toLowerCase().replace("kbps", "").trim() ) :
                           Integer.MAX_VALUE, playing, exists);
    }
}
