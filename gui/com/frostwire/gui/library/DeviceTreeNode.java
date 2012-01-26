package com.frostwire.gui.library;



public class DeviceTreeNode extends LibraryNode {

    private static final long serialVersionUID = 2733848224018434257L;

    private final Device device;

    public DeviceTreeNode(Device device) {
        super(device.getFinger().nickname);
        this.device = device;
    }

    public Device getDevice() {
        return device;
    }
}
