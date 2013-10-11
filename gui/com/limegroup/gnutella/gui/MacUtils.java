/*
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

package com.limegroup.gnutella.gui;

import java.awt.FileDialog;

import net.roydesign.ui.FolderDialog;

/**
 * A collection of OSX GUI utilities.
 *
 * This is in a separate class so that we won't have classloading errors if
 * OSX jars aren't included with other installations.
 */
public final class MacUtils {
    
    private MacUtils() {}
    
    /**
     * Returns the OSX Folder Dialog.
     */
    public static FileDialog getFolderDialog() {
        // net.roydesign.ui.FolderDialog:
        // This class takes advantage of a little know trick in 
        // Apple's VMs to show a real folder dialog, with a 
        // Choose button and all.
        return new FolderDialog(GUIMediator.getAppFrame(), "");
    }
}