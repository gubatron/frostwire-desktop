package com.limegroup.gnutella.gui;

import java.io.File;

import org.limewire.util.FileUtils;
import org.limewire.util.OSUtils;

import com.limegroup.gnutella.util.FrostWireUtils;

/**
 * A collection of Windows-related GUI utility methods.
 */
public class WindowsUtils {

	private WindowsUtils() {
	}

	/**
	 * Determines if we know how to set the login status.
	 */
	public static boolean isLoginStatusAvailable() {
		return OSUtils.isModernWindows();
	}

	/**
	 * Sets the login status. Only available on W2k+.
	 */
	public static void setLoginStatus(boolean allow) {
		if (!isLoginStatusAvailable()) {
			return;
		}

		File startMenu = getUserStartMenu();
		if (startMenu == null || !startMenu.exists()) {
			return;
		}

		File src = new File(startMenu, "Programs\\FrostWire 5\\FrostWire " + FrostWireUtils.getFrostWireVersion() + ".lnk");
		File dst = new File(startMenu, "Programs\\Startup\\FrostWire On Startup.lnk");

		if (allow) {
			FileUtils.copy(src, dst); // Generates the Startup
		} else {
			dst.delete(); // Removes Startup
		}
	}

	public static File getUserStartMenu() {
		if (OSUtils.isModernWindows()) {
			return new File(System.getenv("appdata"), "Microsoft\\Windows\\Start Menu");
		} else {
			return null;
		}
	}
}
