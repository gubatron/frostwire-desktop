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

public final class LibraryInternetRadioTableDataLine extends AbstractLibraryTableDataLine<InternetRadioStation> {

    static final int NAME_IDX = 0;
    private static final LimeTableColumn NAME_COLUMN = new LimeTableColumn(NAME_IDX, "INTERNET_RADIO_TABLE_NAME", I18n.tr("Name"), 80, true, PlayableCell.class);

    static final int DESCRIPTION_IDX = 1;
    private static final LimeTableColumn DESCRIPTION_COLUMN = new LimeTableColumn(DESCRIPTION_IDX, "INTERNET_RADIO_TABLE_DESCRIPTION", I18n.tr("Description"), 80, true, PlayableCell.class);

    /**
     * Total number of columns
     */
    static final int NUMBER_OF_COLUMNS = 2;

    /**
     * Number of columns
     */
    public int getColumnCount() {
        return NUMBER_OF_COLUMNS;
    }

    /**
     * Sets up the dataline for use with the playlist.
     */
    public void initialize(InternetRadioStation item) {
        super.initialize(item);
    }

    /**
     * Returns the value for the specified index.
     */
    public Object getValueAt(int idx) {
        boolean playing = isPlaying();
        switch (idx) {
        case NAME_IDX:
            return new PlayableCell(initializer.getName(), playing);
        case DESCRIPTION_IDX:
            return new PlayableCell(initializer.getDescription(), playing);
        
        }
        return null;
    }

    private boolean isPlaying() {
        if (initializer != null) {
            return false;//AudioPlayer.instance().isThisBeingPlayed(initializer.getURL());
        }

        return false;
    }

    /**
     * Return the table column for this index.
     */
    public LimeTableColumn getColumn(int idx) {
        switch (idx) {
        case NAME_IDX:
            return NAME_COLUMN;
        case DESCRIPTION_IDX:
            return DESCRIPTION_COLUMN;
        }
        return null;
    }

    public boolean isClippable(int idx) {
        return false;
    }
    
    public boolean isDynamic(int idx) {
        return false;
    }

    /**
     * @return the PlayListItem for this table row
     */
    public InternetRadioStation getPlayListItem() {
        return initializer;
    }

    /**
     * Creates a tool tip for each row of the playlist. Tries to grab any information
     * that was extracted from the Meta-Tag or passed in to the PlaylistItem as 
     * a property map
     */
    public String[] getToolTipArray(int col) {
        List<String> list = new ArrayList<String>();
//        if (!StringUtils.isNullOrEmpty(initializer.getTrackTitle(), true)) {
//            list.add(I18n.tr("Title") + ": " + initializer.getTrackTitle());
//        }
//        if (!StringUtils.isNullOrEmpty(initializer.getTrackNumber(), true)) {
//            list.add(I18n.tr("Track") + ": " + initializer.getTrackNumber());
//        }
//        
//        list.add(I18n.tr("Duration") + ": " + LibraryUtils.getSecondsInDDHHMMSS((int) initializer.getTrackDurationInSecs()));
//        
//        if (!StringUtils.isNullOrEmpty(initializer.getTrackArtist(), true)) {
//            list.add(I18n.tr("Artist") + ": " + initializer.getTrackArtist());
//        }
//        if (!StringUtils.isNullOrEmpty(initializer.getTrackAlbum(), true)) {
//            list.add(I18n.tr("Album") + ": " + initializer.getTrackAlbum());
//        }
//        if (!StringUtils.isNullOrEmpty(initializer.getTrackGenre(), true)) {
//            list.add(I18n.tr("Genre") + ": " + initializer.getTrackGenre());
//        }
//        if (!StringUtils.isNullOrEmpty(initializer.getTrackYear(), true)) {
//            list.add(I18n.tr("Year") + ": " + initializer.getTrackYear());
//        }
//        if (!StringUtils.isNullOrEmpty(initializer.getTrackComment(), true)) {
//            list.add(I18n.tr("Comment") + ": " + initializer.getTrackComment());
//        }
//
//        if (list.size() == 1) {
//            if (!StringUtils.isNullOrEmpty(initializer.getFileName(), true)) {
//                list.add(I18n.tr("File") + ": " + initializer.getFileName());
//            }
//            if (!StringUtils.isNullOrEmpty(initializer.getFilePath(), true)) {
//                list.add(I18n.tr("Folder") + ": " + FilenameUtils.getPath(initializer.getFilePath()));
//            }
//            if (!StringUtils.isNullOrEmpty(initializer.getTrackBitrate(), true)) {
//                list.add(I18n.tr("Bitrate") + ": " + initializer.getTrackBitrate());
//            }
//        }

        return list.toArray(new String[0]);
    }

    public File getFile() {
        return null;//new File(initializer.getFilePath());
    }

    @Override
    public int getTypeAheadColumn() {
        return 0;
    }
}
