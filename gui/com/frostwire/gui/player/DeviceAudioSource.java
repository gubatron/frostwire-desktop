package com.frostwire.gui.player;

import java.net.URL;

import com.frostwire.gui.library.android.Device;
import com.frostwire.gui.library.android.FileDescriptor;

public class DeviceAudioSource extends AudioSource {

    private final Device device;
    private final FileDescriptor fd;

    public DeviceAudioSource(URL url, Device device, FileDescriptor fd) {
        super(url);
        this.device = device;
        this.fd = fd;
    }
    
    public Device getDevice() {
        return device;
    }

    public FileDescriptor getFileDescriptor() {
        return fd;
    }
}
