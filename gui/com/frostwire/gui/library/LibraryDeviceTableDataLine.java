package com.frostwire.gui.library;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.limewire.util.FilenameUtils;
import org.limewire.util.StringUtils;

import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.gui.library.android.Device;
import com.frostwire.gui.library.android.DeviceFileDescriptor;
import com.frostwire.gui.library.android.FileDescriptor;
import com.frostwire.gui.player.AudioPlayer;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconManager;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.SizeHolder;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;

public final class LibraryDeviceTableDataLine extends AbstractLibraryTableDataLine<DeviceFileDescriptor> {

    /**
     * Icon column
     */
    static final int ICON_IDX = 0;
    private static final LimeTableColumn ICON_COLUMN = new LimeTableColumn(ICON_IDX, "DEVICE_TABLE_TABLE_ICON", I18n.tr("Icon"), 20, true, false, false, PlayableIconCell.class);
    
    /**
     * Title column
     */
    static final int TITLE_IDX = 1;
    private static final LimeTableColumn TITLE_COLUMN = new LimeTableColumn(TITLE_IDX, "DEVICE_TABLE_TITLE", I18n.tr("Title"), 80, true, PlayableCell.class);

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
     *  Coverts the size of the PlayListItem into readable form postfixed with
     *  Kb or Mb
     */
    private SizeHolder sizeHolder;

    private boolean exists;
    
    /**
     * The model this is being displayed on
     */
    private final LibraryDeviceTableModel model;
    
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
    }

    /**
     * Sets up the dataline for use with the playlist.
     */
    public void initialize(DeviceFileDescriptor item) {
        super.initialize(item);
        sizeHolder = new SizeHolder(item.getFD().fileSize);
        //exists = new File(item.getFilePath()).exists();
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
            return new PlayableCell(this, initializer.getFD().filePath, playing, idx);
        }
        return null;
    }

    private boolean isPlaying() {
        if (initializer != null) {
            String url = initializer.getDevice().getDownloadURL(initializer.getFD());
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
        if (!StringUtils.isNullOrEmpty(initializer.getFD().filePath, true)) {
            list.add(I18n.tr("Title") + ": " + initializer.getFD().filePath);
        }

        return list.toArray(new String[0]);
    }

    @Override
    public File getFile() {
        return null;
    }
    
    private Icon getIcon() {
        final File file = new File(initializer.getFD().filePath);
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
