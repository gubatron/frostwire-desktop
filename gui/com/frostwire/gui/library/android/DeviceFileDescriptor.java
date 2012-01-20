package com.frostwire.gui.library.android;


public class DeviceFileDescriptor {

    private final Device device;
    private final FileDescriptor fd;

    public DeviceFileDescriptor(Device device, FileDescriptor fd) {
        this.device = device;
        this.fd = fd;
    }

    public Device getDevice() {
        return device;
    }

    public FileDescriptor getFD() {
        return fd;
    }
}
