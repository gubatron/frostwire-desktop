package com.frostwire.gui.library.android;

import javax.swing.tree.DefaultMutableTreeNode;

import com.frostwire.gui.android.Device;
import com.frostwire.gui.android.DeviceConstants;
import com.frostwire.gui.android.UITool;

public class DeviceFileTypeTreeNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1664082200849026954L;

    private final Device device;
    private final byte fileType;

    public DeviceFileTypeTreeNode(Device device, byte fileType) {
        this.device = device;
        this.fileType = fileType;
    }

    public Device getDevice() {
        return device;
    }

    public byte getFileType() {
        return fileType;
    }

    @Override
    public String toString() {
        return UITool.getNumSharedFiles(device.getFinger(), fileType) + " - " + UITool.getFileTypeAsString(fileType);
    }
}
