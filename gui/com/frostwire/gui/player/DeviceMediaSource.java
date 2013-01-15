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

package com.frostwire.gui.player;

import com.frostwire.core.Constants;
import com.frostwire.core.FileDescriptor;
import com.frostwire.gui.library.Device;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class DeviceMediaSource extends MediaSource {

    private final Device device;
    private final FileDescriptor fd;


    public DeviceMediaSource(String url, Device device, FileDescriptor fd) {
        super(url);
        this.device = device;
        this.fd = fd;
        initializeDisplayText();
    }

    public Device getDevice() {
        return device;
    }

    public FileDescriptor getFileDescriptor() {
        return fd;
    }
    
    public boolean showPlayerWindow() {
        return fd.fileType == Constants.FILE_TYPE_VIDEOS;
    }
}
