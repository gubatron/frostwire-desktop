/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml), Erich Pleny (erichpleny)
 * Copyright (c) 2012, FrostWire(R). All rights reserved.
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

package com.frostwire.gui.player;

import java.io.File;

import com.limegroup.gnutella.util.FrostWireUtils;

public class MediaPlayerOSX extends MediaPlayer {

	@Override
	protected String getPlayerPath() {
		
		String playerPath;
		String macOSFolder = new File(FrostWireUtils.getFrostWireJarPath()).getParentFile().getParent() + File.separator + "MacOS";
		boolean isRelease = !FrostWireUtils.getFrostWireJarPath().contains("frostwire-desktop");

        playerPath = (isRelease) ? macOSFolder + File.separator + "fwplayer_osx" : "lib/native/fwplayer_osx";
        
		return playerPath;
	}
	
	@Override
    protected float getVolumeGainFactor() {
    	return 30.0f;
    }
	
}
