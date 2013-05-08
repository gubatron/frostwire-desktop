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
package com.frostwire.gui.library;

import java.io.File;

import javax.swing.Icon;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

public class InternetRadioDirectoryHolder implements DirectoryHolder {

    private final Icon icon;

    public InternetRadioDirectoryHolder() {
        icon = GUIMediator.getThemeImage("radio_small");
    }

    public boolean accept(File pathname) {
        return true;
    }

    public String getName() {
        return I18n.tr("Radio");
    }

    public String getDescription() {
        return I18n.tr("Internet Radio");
    }

    public File getDirectory() {
        return null;
    }

    public File[] getFiles() {
        return null;
    }

    public int size() {
        return 0;
    }

    public Icon getIcon() {
        return icon;
    }

    public boolean isEmpty() {
        return false;
    }
}
