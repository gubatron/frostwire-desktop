package com.frostwire.gui.library;

import javax.swing.tree.DefaultMutableTreeNode;



public class DeviceFileTypeTreeNode extends LibraryNode {

    private static final long serialVersionUID = 1664082200849026954L;

    private final Device device;
    private final byte fileType;

    public DeviceFileTypeTreeNode(Device device, byte fileType) {
        super(UITool.getNumSharedFiles(device.getFinger(), fileType) + " - " + UITool.getFileTypeAsString(fileType));
        this.device = device;
        this.fileType = fileType;
    }

    public Device getDevice() {
        return device;
    }

    public byte getFileType() {
        return fileType;
    }
}
