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
import java.util.Date;
import java.util.List;

import javax.swing.Icon;

import org.limewire.util.StringUtils;

import com.frostwire.gui.player.AudioPlayer;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconManager;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.SizeHolder;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;

public final class LibraryDeviceTableDataLine extends AbstractLibraryTableDataLine<FileDescriptor> {
    
    /**
     * Icon column
     */
    static final int ICON_IDX = 0;
    private static final LimeTableColumn ICON_COLUMN = new LimeTableColumn(ICON_IDX, "DEVICE_TABLE_ICON", I18n.tr("Icon"), 20, true, false, false, PlayableIconCell.class);
    
    /**
     * Title column
     */
    static final int TITLE_IDX = 1;
    private static final LimeTableColumn TITLE_COLUMN = new LimeTableColumn(TITLE_IDX, "DEVICE_TABLE_TITLE", I18n.tr("Title"), 80, true, PlayableCell.class);
    
    /**
     * Artist column
     */
    static final int ARTIST_IDX = 2;
    private static final LimeTableColumn ARTIST_COLUMN = new LimeTableColumn(ARTIST_IDX, "DEVICE_TABLE_ARTIST", I18n.tr("Artist"), 80, true, PlayableCell.class);

    /**
     * Album column
     */
    static final int ALBUM_IDX = 3;
    private static final LimeTableColumn ALBUM_COLUMN = new LimeTableColumn(ALBUM_IDX, "DEVICE_TABLE_ALBUM", I18n.tr("Album"), 120, true, PlayableCell.class);

    /**
     * Year column
     */
    static final int YEAR_IDX = 4;
    private static final LimeTableColumn YEAR_COLUMN = new LimeTableColumn(YEAR_IDX, "DEVICE_TABLE_YEAR", I18n.tr("Year"), 30, false, PlayableCell.class);

    /**
     * Size column (in bytes)
     */
    static final int SIZE_IDX = 5;
    private static final LimeTableColumn SIZE_COLUMN = new LimeTableColumn(SIZE_IDX, "DEVICE_TABLE_SIZE", I18n.tr("Size"), 80, false, PlayableCell.class);

    static final int DATE_ADDED_IDX = 6;
    private static final LimeTableColumn DATE_ADDED_COLUMN = new LimeTableColumn(DATE_ADDED_IDX,"DEVICE_TABLE_DATE_ADDED",I18n.tr("Date Added"),80,true,PlayableCell.class);

    static final int DATE_MODIFIED_IDX = 7;
    private static final LimeTableColumn DATE_MODIFIED_COLUMN = new LimeTableColumn(DATE_MODIFIED_IDX, "DEVICE_TABLE_DATE_MODIFIED",I18n.tr("Date Modified"),80, false, PlayableCell.class);
    
    /**
     * Total number of columns
     */
    static final int NUMBER_OF_COLUMNS = 8;

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
    
    /**
     * The model this is being displayed on
     */
    private final LibraryDeviceTableModel model;
    
    private final Device device;
    
    /**
     * Whether or not the icon has been loaded.
     */
    private boolean _iconLoaded = false;
    
    /**
     * Whether or not the icon has been scheduled to load.
     */
    private boolean _iconScheduledForLoad = false;
    
    public LibraryDeviceTableDataLine(LibraryDeviceTableModel ltm) {
        this.model = ltm;
        this.device = ltm.getDevice();
    }

    /**
     * Sets up the dataline for use with the playlist.
     */
    public void initialize(FileDescriptor item) {
        super.initialize(item);
        sizeHolder = new SizeHolder(item.fileSize);
    }

    /**
     * Returns the value for the specified index.
     */
    public Object getValueAt(int idx) {
        boolean playing = isPlaying();
        switch (idx) {
        case ICON_IDX:
            return new PlayableIconCell(getIcon(), playing);
        case TITLE_IDX:
            return new PlayableCell(this, initializer.title, playing, idx);
        case ARTIST_IDX:
            return new PlayableCell(this, filterUnknown(initializer.artist), playing, idx);
        case ALBUM_IDX:
            return new PlayableCell(this, filterUnknown(initializer.album), playing, idx);
        case YEAR_IDX:
            return new PlayableCell(this, filterUnknown(initializer.year), playing, idx);
        case SIZE_IDX:
            return new PlayableCell(this, sizeHolder.toString(), playing, idx);
        case DATE_ADDED_IDX:
            return new PlayableCell(this, new Date(initializer.dateAdded*1000), playing, idx);
        case DATE_MODIFIED_IDX:
            return new PlayableCell(this, new Date(initializer.dateModified*1000), playing, idx);
        }
        return null;
    }

    private boolean isPlaying() {
        if (initializer != null) {
            String url = device.getDownloadURL(initializer);
           return AudioPlayer.instance().isThisBeingPlayed(url);
        }

        return false;
    }

    /**
     * Return the table column for this index.
     */
    public LimeTableColumn getColumn(int idx) {
        switch (idx) {
        case ICON_IDX:
            return ICON_COLUMN;
        case TITLE_IDX:
            return TITLE_COLUMN;
        case ARTIST_IDX:
            return ARTIST_COLUMN;
        case ALBUM_IDX:
            return ALBUM_COLUMN;
        case YEAR_IDX:
            return YEAR_COLUMN;
        case SIZE_IDX:
            return SIZE_COLUMN;
        case DATE_ADDED_IDX:
            return DATE_ADDED_COLUMN;
        case DATE_MODIFIED_IDX:
            return DATE_MODIFIED_COLUMN;
        }
        return null;
    }

    public boolean isClippable(int idx) {
        switch(idx) {
        case ICON_IDX:
            return false;
        default:
            return true;
        }
    }

    public int getTypeAheadColumn() {
        return ICON_IDX;
    }

    public boolean isDynamic(int idx) {
        return false;
    }

    /**
     * Creates a tool tip for each row of the playlist. Tries to grab any information
     * that was extracted from the Meta-Tag or passed in to the PlaylistItem as 
     * a property map
     */
    public String[] getToolTipArray(int col) {
        List<String> list = new ArrayList<String>();
        if (!StringUtils.isNullOrEmpty(initializer.title, true)) {
            list.add(I18n.tr("Title") + ": " + initializer.title);
        }
        
        if (!StringUtils.isNullOrEmpty(filterUnknown(initializer.artist), true)) {
            list.add(I18n.tr("Artist") + ": " + initializer.artist);
        }
        if (!StringUtils.isNullOrEmpty(filterUnknown(initializer.album), true)) {
            list.add(I18n.tr("Album") + ": " + initializer.album);
        }
        if (!StringUtils.isNullOrEmpty(filterUnknown(initializer.year), true)) {
            list.add(I18n.tr("Year") + ": " + initializer.year);
        }

        return list.toArray(new String[0]);
    }

    @Override
    public File getFile() {
        return null;
    }
    
    private String filterUnknown(String str) {
        return str != null && str.contains("<unknown>") ? "" : str;
    }
    
    private Icon getIcon() {
        final File file = new File(initializer.filePath);
        boolean iconAvailable = IconManager.instance().isIconForFileAvailable(file);
        if(!iconAvailable && !_iconScheduledForLoad) {
            _iconScheduledForLoad = true;
            BackgroundExecutorService.schedule(new Runnable() {
                public void run() {
                    GUIMediator.safeInvokeAndWait(new Runnable() {
                        public void run() {
                            IconManager.instance().getIconForFile(file);
                            _iconLoaded = true;
                            model.refresh();
                        }
                    });
                }
            });
            return null;
        } else if(_iconLoaded || iconAvailable) {
            return IconManager.instance().getIconForFile(file);
        } else {
            return null;
        }
    }
}
