package com.limegroup.gnutella.settings;

import java.io.File;

import org.limewire.setting.FileSetSetting;
import org.limewire.setting.FileSetting;
import org.limewire.util.CommonUtils;

import com.limegroup.gnutella.util.FrostWireUtils;

/**
 * Settings for iTunes
 */
public class LibrarySettings extends LimeProps {

    private LibrarySettings() {
    }

    public static final File LIBRARY_DATABASE = new File(CommonUtils.getUserSettingsDir(), "library_db");

    /**
     * The include directories. 
     */
    public static final FileSetSetting DIRECTORIES_TO_INCLUDE = FACTORY.createFileSetSetting("DIRECTORIES_TO_INCLUDE_FOR_FILES", new File[0]);
    
    public static final FileSetSetting DIRECTORIES_NOT_TO_INCLUDE = FACTORY.createFileSetSetting("DIRECTORIES_NOT_TO_INCLUDE", new File[0]);

    public static final FileSetSetting DIRECTORIES_TO_INCLUDE_FROM_FROSTWIRE4 = FACTORY.createFileSetSetting("DIRECTORIES_TO_INCLUDE_FROM_FROSTWIRE4", new File[0]);

    public static final FileSetting USER_MUSIC_FOLDER = FACTORY.createFileSetting("USER_MUSIC_FOLDER", FrostWireUtils.getUserMusicFolder());
}
