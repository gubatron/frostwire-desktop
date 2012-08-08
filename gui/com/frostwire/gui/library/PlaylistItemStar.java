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

/**
 * Wraps the current dataline to be displayed in the table to pass it to the
 * {@link PlaylistItemNameRenderer}
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
class PlaylistItemStar implements Comparable<PlaylistItemStar> {

    /**
     * The current line to display in the table
     */
    private final LibraryPlaylistsTableDataLine line;
    private boolean isPlaying;
    private boolean exists;

    public PlaylistItemStar(LibraryPlaylistsTableDataLine line, boolean isPlaying, boolean exists) {
        this.line = line;
        this.isPlaying = isPlaying;
        this.exists = exists;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean exists() {
        return exists;
    }

    /**
     * @return the current dataline
     */
    public LibraryPlaylistsTableDataLine getLine() {
        return line;
    }

    public int compareTo(PlaylistItemStar o) {
        return Boolean.valueOf(line.getInitializeObject().isStarred()).compareTo(o.line.getInitializeObject().isStarred());
    }
}
