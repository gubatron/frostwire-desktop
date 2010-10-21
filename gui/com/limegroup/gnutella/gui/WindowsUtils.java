package com.limegroup.gnutella.gui;

import java.io.File;

import org.limewire.util.CommonUtils;
import org.limewire.util.FileUtils;
import org.limewire.util.OSUtils;

import com.limegroup.gnutella.util.LimeWireUtils;

/**
 * A collection of Windows-related GUI utility methods.
 */
public class WindowsUtils {
    
    private WindowsUtils() {}

    /**
     * Determines if we know how to set the login status.
     */    
    public static boolean isLoginStatusAvailable() {
	System.out.println("****STARTUP DEBUG: Is this a supported windows or Mac? " + OSUtils.isGoodWindows());
        return OSUtils.isGoodWindows();
    }

    /**
     * Sets the login status.  Only available on W2k+.
     */
    public static void setLoginStatus(boolean allow) {
        if(!isLoginStatusAvailable())
            return;
	/*
	 FTA: From Windows XP or Vista now FrostWire uses the installer's auto-generated link to
         let the user run frostwire when windows starts. This setting could be changed from "Windows Boot" in
	 the options Menu or when FrostWire is installed for the 1st time.
	**/

        File homeDir = CommonUtils.getUserHomeDir();
        File src = new File(homeDir, "Start Menu\\Programs\\FrostWire\\FrostWire "+ LimeWireUtils.getLimeWireVersion() +".lnk");
        File startup = new File(homeDir, "Start Menu\\Programs\\Startup");
        File dst = new File(startup, "FrostWire On Startup.lnk");

        if(allow)
            FileUtils.copy(src, dst); // Generates the Startup
        else
            dst.delete(); // Removes Startup
    }
}
