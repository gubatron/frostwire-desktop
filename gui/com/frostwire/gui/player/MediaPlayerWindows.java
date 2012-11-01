package com.frostwire.gui.player;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.limegroup.gnutella.util.FrostWireUtils;

public class MediaPlayerWindows extends MediaPlayer {

	protected String getPlayerPath() {
		String playerPath;
		
		boolean isRelease = !FrostWireUtils.getFrostWireJarPath().contains("frostwire-desktop");

		playerPath = (isRelease) ? FrostWireUtils.getFrostWireJarPath() + File.separator + "fwplayer.exe" : "lib/native/fwplayer.exe";
        playerPath = decode(playerPath);

        if (!new File(playerPath).exists()) {
            playerPath = decode("../lib/native/fwplayer.exe");
        }
        
        return playerPath;
	}
	
	protected float getVolumeGainFactor() {
		return 100.0f;
	}
	
    private static String decode(String s) {
        if (s == null) {
            return "";
        }
        try {
            return (URLDecoder.decode(s, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }
}
