package com.frostwire.gui.player;

import java.net.URL;

import com.frostwire.gui.library.android.DeviceFileDescriptor;

public class DeviceAudioSource extends AudioSource {

    private final DeviceFileDescriptor dfd;

    public DeviceAudioSource(URL url, DeviceFileDescriptor dfd) {
        super(url);
        this.dfd = dfd;
    }

    public DeviceFileDescriptor getDeviceFileDescriptor() {
        return dfd;
    }
}
