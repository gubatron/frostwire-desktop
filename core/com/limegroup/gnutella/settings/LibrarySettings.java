/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
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

package com.limegroup.gnutella.settings;

import java.io.File;

import org.limewire.setting.BooleanSetting;
import org.limewire.setting.FileSetSetting;
import org.limewire.setting.FileSetting;
import org.limewire.util.CommonUtils;

import com.frostwire.AzureusStarter;
import com.limegroup.gnutella.util.FrostWireUtils;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public class LibrarySettings extends LimeProps {

    private LibrarySettings() {
    }
    
    private static final File PORTABLE_ROOT_FOLDER = CommonUtils.getPortableRootFolder();

    public static final File LIBRARY_DATABASE = new File(CommonUtils.getUserSettingsDir(), "library_db");

    public static final File DEFAULT_LIBRARY_FROM_DEVICE_DATA_DIR = new File((PORTABLE_ROOT_FOLDER == null) ? FrostWireUtils.getFrostWireRootFolder() : PORTABLE_ROOT_FOLDER, "From Device");

    /**
     * The include directories. 
     */
    public static final FileSetSetting DIRECTORIES_TO_INCLUDE = FACTORY.createFileSetSetting("DIRECTORIES_TO_INCLUDE_FOR_FILES", new File[0]);

    public static final FileSetSetting DIRECTORIES_NOT_TO_INCLUDE = FACTORY.createFileSetSetting("DIRECTORIES_NOT_TO_INCLUDE", new File[0]);

    public static final FileSetSetting DIRECTORIES_TO_INCLUDE_FROM_FROSTWIRE4 = FACTORY.createFileSetSetting("DIRECTORIES_TO_INCLUDE_FROM_FROSTWIRE4", new File[0]);

    public static final FileSetting USER_MUSIC_FOLDER = FACTORY.createFileSetting("USER_MUSIC_FOLDER", FrostWireUtils.getUserMusicFolder());
    
    public static final FileSetting USER_VIDEO_FOLDER = FACTORY.createFileSetting("USER_VIDEO_FOLDER", FrostWireUtils.getUserVideoFolder());

    public static final FileSetting LIBRARY_FROM_DEVICE_DATA_DIR_SETTING = FACTORY.createFileSetting("LIBRARY_FROM_DEVICE_DATA_DIR_SETTING", DEFAULT_LIBRARY_FROM_DEVICE_DATA_DIR).setAlwaysSave(true);

    public static final BooleanSetting LIBRARY_WIFI_SHARING_ENABLED = FACTORY.createBooleanSetting("LIBRARY_WIFI_SHARING_ENABLED", true);

    
    public static void setupInitialLibraryFolders() {
        SharingSettings.initTorrentDataDirSetting();
        
        LibrarySettings.DIRECTORIES_TO_INCLUDE.add(SharingSettings.TORRENT_DATA_DIR_SETTING.getValue());
        
        for (File f : FrostWireUtils.getFrostWire4SaveDirectories()) {
            LibrarySettings.DIRECTORIES_TO_INCLUDE.add(f);
            LibrarySettings.DIRECTORIES_TO_INCLUDE_FROM_FROSTWIRE4.add(f);
        }

        if (PORTABLE_ROOT_FOLDER == null) {
            if (LibrarySettings.USER_MUSIC_FOLDER.getValue().exists()) {
                LibrarySettings.DIRECTORIES_TO_INCLUDE.add(LibrarySettings.USER_MUSIC_FOLDER.getValue());
            }
            
            if (LibrarySettings.USER_VIDEO_FOLDER.getValue().exists()) {
                LibrarySettings.DIRECTORIES_TO_INCLUDE.add(LibrarySettings.USER_VIDEO_FOLDER.getValue());
            }
        }
        
        File fromDeviceFolder = LibrarySettings.LIBRARY_FROM_DEVICE_DATA_DIR_SETTING.getValue();
        if (!fromDeviceFolder.exists()) {
            fromDeviceFolder.mkdir();
        }
        
        LibrarySettings.DIRECTORIES_TO_INCLUDE.add(fromDeviceFolder);
        
        File azureusUserPath = new File(CommonUtils.getUserSettingsDir() + File.separator + "azureus" + File.separator);
        if (!azureusUserPath.exists()) {
            System.setProperty("azureus.config.path", azureusUserPath.getAbsolutePath());
            System.setProperty("azureus.install.path", azureusUserPath.getAbsolutePath());
            AzureusStarter.revertToDefaultConfiguration();
        }
    }


    public static void resetLibraryFoldersIfPortable() {
        if (CommonUtils.isPortable()) {
            LibrarySettings.DIRECTORIES_TO_INCLUDE.removeAll();
            LibrarySettings.LIBRARY_FROM_DEVICE_DATA_DIR_SETTING.setValue(DEFAULT_LIBRARY_FROM_DEVICE_DATA_DIR);
            setupInitialLibraryFolders();
        }
    }
}