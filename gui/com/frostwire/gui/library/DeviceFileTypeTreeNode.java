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

import javax.swing.Icon;

import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.search.NamedMediaType;

public class DeviceFileTypeTreeNode extends LibraryNode {

    private static final long serialVersionUID = 1664082200849026954L;

    private static final Icon applications;
    private static final Icon documents;
    private static final Icon pictures;
    private static final Icon videos;
    private static final Icon ringtones;
    private static final Icon audio;

    static {
        applications = NamedMediaType.getFromMediaType(MediaType.getProgramMediaType()).getIcon();
        documents = NamedMediaType.getFromMediaType(MediaType.getDocumentMediaType()).getIcon();
        pictures = NamedMediaType.getFromMediaType(MediaType.getImageMediaType()).getIcon();
        videos = NamedMediaType.getFromMediaType(MediaType.getVideoMediaType()).getIcon();
        audio = NamedMediaType.getFromMediaType(MediaType.getAudioMediaType()).getIcon();
        ringtones = GUIMediator.getThemeImage("speaker");
    }

    private final Device device;
    private final byte fileType;

    public DeviceFileTypeTreeNode(Device device, byte fileType) {
        super(UITool.getFileTypeAsString(fileType));
        this.device = device;
        this.fileType = fileType;
        updateText();
    }

    public Device getDevice() {
        return device;
    }

    public byte getFileType() {
        return fileType;
    }

    public Icon getIcon() {
        switch (fileType) {
        case DeviceConstants.FILE_TYPE_APPLICATIONS:
            return applications;
        case DeviceConstants.FILE_TYPE_DOCUMENTS:
            return documents;
        case DeviceConstants.FILE_TYPE_PICTURES:
            return pictures;
        case DeviceConstants.FILE_TYPE_VIDEOS:
            return videos;
        case DeviceConstants.FILE_TYPE_RINGTONES:
            return ringtones;
        case DeviceConstants.FILE_TYPE_AUDIO:
            return audio;
        default:
            return null;
        }
    }

    public void updateText() {
        String str = UITool.getFileTypeAsString(fileType) + " (" + UITool.getNumSharedFiles(device.getFinger(), fileType) + ")";
        setUserObject(str);
    }
}