package com.frostwire.gui.library.android;

import javax.swing.tree.DefaultMutableTreeNode;


public class DeviceTreeNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 2733848224018434257L;

    private final Device device;

    public DeviceTreeNode(Device device) {
        this.device = device;
    }

    public Device getDevice() {
        return device;
    }

    @Override
    public String toString() {
        return device.getFinger().nickname;
    }
}
