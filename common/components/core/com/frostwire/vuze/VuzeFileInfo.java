/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
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

package com.frostwire.vuze;

import java.io.File;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeFileInfo {

    private final DiskManagerFileInfo info;
    private final File file;
    private final String filename;
    private final long length;

    public VuzeFileInfo(DiskManagerFileInfo info) {
        this.info = info;

        this.file = info.getFile(false);
        this.filename = file.getName();
        this.length = info.getLength();
    }

    public File getFile() {
        return file;
    }

    public String getFilename() {
        return filename;
    }

    public long getLength() {
        return length;
    }

    public long getDownloaded() {
        return info.getDownloaded();
    }
}
