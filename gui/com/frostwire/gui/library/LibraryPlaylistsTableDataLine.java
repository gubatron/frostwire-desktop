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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.limewire.util.FilenameUtils;
import org.limewire.util.StringUtils;

import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.gui.player.AudioPlayer;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.SizeHolder;

public final class LibraryPlaylistsTableDataLine extends AbstractLibraryTableDataLine<PlaylistItem> {

    /**
     * Starred column
     */
    static final int STARRED_IDX = 0;
    private static final LimeTableColumn STARRED_COLUMN = new LimeTableColumn(STARRED_IDX, "PLAYLIST_TABLE_STARRED", I18n.tr("Starred"), 20, true, false, false, PlaylistItemStar.class);

    /**
     * Title column
     */
    static final int TITLE_IDX = 1;
    private static final LimeTableColumn TITLE_COLUMN = new LimeTableColumn(TITLE_IDX, "PLAYLIST_TABLE_TITLE", I18n.tr("Title"), 80, true, PlaylistItemProperty.class);

    /**
     * Artist column
     */
    static final int ARTIST_IDX = 2;
    private static final LimeTableColumn ARTIST_COLUMN = new LimeTableColumn(ARTIST_IDX, "PLAYLIST_TABLE_ARTIST", I18n.tr("Artist"), 80, true, PlaylistItemProperty.class);

    /**
     * Length column (in hour:minutes:seconds format)
     */
    static final int LENGTH_IDX = 3;
    private static final LimeTableColumn LENGTH_COLUMN = new LimeTableColumn(LENGTH_IDX, "PLAYLIST_TABLE_LENGTH", I18n.tr("Length"), 150, true, PlaylistItemProperty.class);

    /**
     * Album column
     */
    static final int ALBUM_IDX = 4;
    private static final LimeTableColumn ALBUM_COLUMN = new LimeTableColumn(ALBUM_IDX, "PLAYLIST_TABLE_ALBUM", I18n.tr("Album"), 120, true, PlaylistItemProperty.class);

    /**
     * Track column
     */
    static final int TRACK_IDX = 5;
    private static final LimeTableColumn TRACK_COLUMN = new LimeTableColumn(TRACK_IDX, "PLAYLIST_TABLE_TRACK", I18n.tr("Track"), 20, false, PlaylistItemProperty.class);

    /**
     * Genre column
     */
    static final int GENRE_IDX = 6;
    private static final LimeTableColumn GENRE_COLUMN = new LimeTableColumn(GENRE_IDX, "PLAYLIST_TABLE_GENRE", I18n.tr("Genre"), 80, true, PlaylistItemProperty.class);

    /**
     * Bitrate column info
     */
    static final int BITRATE_IDX = 7;
    private static final LimeTableColumn BITRATE_COLUMN = new LimeTableColumn(BITRATE_IDX, "PLAYLIST_TABLE_BITRATE", I18n.tr("Bitrate"), 60, true, PlaylistItemProperty.class);

    /**
     * Comment column info
     */
    static final int COMMENT_IDX = 8;
    private static final LimeTableColumn COMMENT_COLUMN = new LimeTableColumn(COMMENT_IDX, "PLAYLIST_TABLE_COMMENT", I18n.tr("Comment"), 20, false, PlaylistItemProperty.class);

    /**
     * Size column (in bytes)
     */
    static final int SIZE_IDX = 9;
    private static final LimeTableColumn SIZE_COLUMN = new LimeTableColumn(SIZE_IDX, "PLAYLIST_TABLE_SIZE", I18n.tr("Size"), 80, false, PlaylistItemProperty.class);

    /**
     * TYPE column
     */
    static final int TYPE_IDX = 10;
    private static final LimeTableColumn TYPE_COLUMN = new LimeTableColumn(TYPE_IDX, "PLAYLIST_TABLE_TYPE", I18n.tr("Type"), 40, true, PlaylistItemProperty.class);

    /**
     * Year column
     */
    static final int YEAR_IDX = 11;
    private static final LimeTableColumn YEAR_COLUMN = new LimeTableColumn(YEAR_IDX, "PLAYLIST_TABLE_YEAR", I18n.tr("Year"), 30, false, PlaylistItemProperty.class);

    /**
     * Total number of columns
     */
    static final int NUMBER_OF_COLUMNS = 12;

    /**
     * Number of columns
     */
    public int getColumnCount() {
        return NUMBER_OF_COLUMNS;
    }

    /**
     *  Coverts the size of the PlayListItem into readable form postfixed with
     *  Kb or Mb
     */
    private SizeHolder sizeHolder;

    private boolean exists;

    /**
     * Sets up the dataline for use with the playlist.
     */
    public void initialize(PlaylistItem item) {
        super.initialize(item);
        sizeHolder = new SizeHolder(item.getFileSize());
        exists = new File(item.getFilePath()).exists();
    }

    /**
     * Returns the value for the specified index.
     */
    public Object getValueAt(int idx) {
        boolean playing = isPlaying();
        switch (idx) {
        case STARRED_IDX:
            return new PlaylistItemStar(this, playing, exists);
        case ALBUM_IDX:
            return new PlaylistItemProperty(initializer.getTrackAlbum(), playing, exists,idx);
        case ARTIST_IDX:
            return new PlaylistItemProperty(initializer.getTrackArtist(), playing, exists,idx);
        case BITRATE_IDX:
            return new PlaylistItemProperty(initializer.getTrackBitrate(), playing, exists,idx);
        case COMMENT_IDX:
            return new PlaylistItemProperty(initializer.getTrackComment(), playing, exists,idx);
        case GENRE_IDX:
            return new PlaylistItemProperty(initializer.getTrackGenre(), playing, exists,idx);
        case LENGTH_IDX:
            return new PlaylistItemProperty(LibraryUtils.getSecondsInDDHHMMSS((int) initializer.getTrackDurationInSecs()), playing, exists,idx);
        case SIZE_IDX:
            return new PlaylistItemProperty(sizeHolder.toString(), playing, exists,idx);
        case TITLE_IDX:
            return new PlaylistItemProperty(initializer.getTrackTitle(), playing, exists,idx);
        case TRACK_IDX:
            return new PlaylistItemProperty(initializer.getTrackNumber(), playing, exists,idx);
        case TYPE_IDX:
            return new PlaylistItemProperty(initializer.getFileExtension(), playing, exists,idx);
        case YEAR_IDX:
            return new PlaylistItemProperty(initializer.getTrackYear(), playing, exists,idx);
        }
        return null;
    }

    private boolean isPlaying() {
        if (initializer != null) {
            return AudioPlayer.instance().isThisBeingPlayed(initializer);
        }

        return false;
    }

    /**
     * Return the table column for this index.
     */
    public LimeTableColumn getColumn(int idx) {
        switch (idx) {
        case STARRED_IDX:
            return STARRED_COLUMN;
        case ALBUM_IDX:
            return ALBUM_COLUMN;
        case ARTIST_IDX:
            return ARTIST_COLUMN;
        case BITRATE_IDX:
            return BITRATE_COLUMN;
        case COMMENT_IDX:
            return COMMENT_COLUMN;
        case GENRE_IDX:
            return GENRE_COLUMN;
        case LENGTH_IDX:
            return LENGTH_COLUMN;
        case SIZE_IDX:
            return SIZE_COLUMN;
        case TITLE_IDX:
            return TITLE_COLUMN;
        case TRACK_IDX:
            return TRACK_COLUMN;
        case TYPE_IDX:
            return TYPE_COLUMN;
        case YEAR_IDX:
            return YEAR_COLUMN;
        }
        return null;
    }

    public boolean isClippable(int idx) {
        return false;
    }

    public int getTypeAheadColumn() {
        return STARRED_IDX;
    }

    public boolean isDynamic(int idx) {
        return false;
    }

    /**
     * @return the PlayListItem for this table row
     */
    public PlaylistItem getPlayListItem() {
        return initializer;
    }

    /**
     * Creates a tool tip for each row of the playlist. Tries to grab any information
     * that was extracted from the Meta-Tag or passed in to the PlaylistItem as 
     * a property map
     */
    public String[] getToolTipArray(int col) {
        List<String> list = new ArrayList<String>();
        if (!StringUtils.isNullOrEmpty(initializer.getTrackTitle(), true)) {
            list.add(I18n.tr("Title") + ": " + initializer.getTrackTitle());
        }
        if (!StringUtils.isNullOrEmpty(initializer.getTrackNumber(), true)) {
            list.add(I18n.tr("Track") + ": " + initializer.getTrackNumber());
        }
        
        list.add(I18n.tr("Duration") + ": " + LibraryUtils.getSecondsInDDHHMMSS((int) initializer.getTrackDurationInSecs()));
        
        if (!StringUtils.isNullOrEmpty(initializer.getTrackArtist(), true)) {
            list.add(I18n.tr("Artist") + ": " + initializer.getTrackArtist());
        }
        if (!StringUtils.isNullOrEmpty(initializer.getTrackAlbum(), true)) {
            list.add(I18n.tr("Album") + ": " + initializer.getTrackAlbum());
        }
        if (!StringUtils.isNullOrEmpty(initializer.getTrackGenre(), true)) {
            list.add(I18n.tr("Genre") + ": " + initializer.getTrackGenre());
        }
        if (!StringUtils.isNullOrEmpty(initializer.getTrackYear(), true)) {
            list.add(I18n.tr("Year") + ": " + initializer.getTrackYear());
        }
        if (!StringUtils.isNullOrEmpty(initializer.getTrackComment(), true)) {
            list.add(I18n.tr("Comment") + ": " + initializer.getTrackComment());
        }

        if (list.size() == 1) {
            if (!StringUtils.isNullOrEmpty(initializer.getFileName(), true)) {
                list.add(I18n.tr("File") + ": " + initializer.getFileName());
            }
            if (!StringUtils.isNullOrEmpty(initializer.getFilePath(), true)) {
                list.add(I18n.tr("Folder") + ": " + FilenameUtils.getPath(initializer.getFilePath()));
            }
            if (!StringUtils.isNullOrEmpty(initializer.getTrackBitrate(), true)) {
                list.add(I18n.tr("Bitrate") + ": " + initializer.getTrackBitrate());
            }
        }

        return list.toArray(new String[0]);
    }

    public File getFile() {
        return new File(initializer.getFilePath());
    }
}
