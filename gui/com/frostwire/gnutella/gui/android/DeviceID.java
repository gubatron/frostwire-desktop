package com.frostwire.gnutella.gui.android;

public final class DeviceID {

	public static boolean isNexusOne(Device device) {
		Finger finger = device.getFinger();
		return finger.deviceManufacturer.equals("HTC") && finger.deviceDevice.equals("passion");
	}
}
