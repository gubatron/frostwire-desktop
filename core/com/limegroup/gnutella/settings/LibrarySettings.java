package com.limegroup.gnutella.settings;

import java.io.File;

import org.limewire.setting.FileSetSetting;
import org.limewire.setting.FileSetting;
import org.limewire.setting.IntSetting;
import org.limewire.util.CommonUtils;

import com.limegroup.gnutella.util.FrostWireUtils;

/**
 * 
 */
public class LibrarySettings extends LimeProps {

    private LibrarySettings() {
    }

    public static final File LIBRARY_DATABASE = new File(CommonUtils.getUserSettingsDir(), "library_db");

    public static final File DEFAULT_LIBRARY_FROM_DEVICE_DATA_DIR = new File(FrostWireUtils.getFrostWireRootFolder(), "From Device");

    /**
     * The include directories. 
     */
    public static final FileSetSetting DIRECTORIES_TO_INCLUDE = FACTORY.createFileSetSetting("DIRECTORIES_TO_INCLUDE_FOR_FILES", new File[0]);

    public static final FileSetSetting DIRECTORIES_NOT_TO_INCLUDE = FACTORY.createFileSetSetting("DIRECTORIES_NOT_TO_INCLUDE", new File[0]);

    public static final FileSetSetting DIRECTORIES_TO_INCLUDE_FROM_FROSTWIRE4 = FACTORY.createFileSetSetting("DIRECTORIES_TO_INCLUDE_FROM_FROSTWIRE4", new File[0]);

    public static final FileSetting USER_MUSIC_FOLDER = FACTORY.createFileSetting("USER_MUSIC_FOLDER", FrostWireUtils.getUserMusicFolder());

    public static final FileSetting LIBRARY_FROM_DEVICE_DATA_DIR_SETTING = FACTORY.createFileSetting("LIBRARY_FROM_DEVICE_DATA_DIR_SETTING", DEFAULT_LIBRARY_FROM_DEVICE_DATA_DIR).setAlwaysSave(true);

    public static final IntSetting EXPLORER_SPLIT_PANE_LAST_POSITION = FACTORY.createIntSetting("EXPLORER_SPLIT_PANE_LAST_POSITION", 250);
}
