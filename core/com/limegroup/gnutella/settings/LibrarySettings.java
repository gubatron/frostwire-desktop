package com.limegroup.gnutella.settings;

import java.io.File;

import org.limewire.util.CommonUtils;

/**
 * Settings for iTunes
 */
public class LibrarySettings extends LimeProps {

    private LibrarySettings() {
    }

    public static final File LIBRARY_DATABASE = 
            new File(CommonUtils.getUserSettingsDir(), "library_db");
}
