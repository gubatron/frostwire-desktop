package com.frostwire.gui.player;

import java.io.File;

import com.limegroup.gnutella.util.FrostWireUtils;

public class MediaPlayerOSX extends MediaPlayer {

	protected String getPlayerPath() {

		String playerPath;
		String macOSFolder = new File(FrostWireUtils.getFrostWireJarPath()).getParentFile().getParent() + File.separator + "MacOS";
		boolean isRelease = !FrostWireUtils.getFrostWireJarPath().contains("frostwire-desktop");

        playerPath = (isRelease) ? macOSFolder + File.separator + "fwplayer" : "lib/native/fwplayer";
		
        //playerPath = "/opt/local/bin/mplayer";
        
		return playerPath;
	}
	
}
