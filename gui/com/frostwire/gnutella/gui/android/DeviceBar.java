package com.frostwire.gnutella.gui.android;

import java.awt.FlowLayout;

import javax.swing.JPanel;

public class DeviceBar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6886611714952957959L;
	
	private DeviceExplorer _deviceExplorer;
	
	public DeviceBar(DeviceExplorer deviceExplorer) {
		_deviceExplorer = deviceExplorer;
		setLayout(new FlowLayout());
	}

	public void handleNewDevice(Device device) {
		DeviceButton button = new DeviceButton(device, _deviceExplorer);
		add(button);
		invalidate();
	}

	public void handleDeviceAlive(Device device) {
	}
}
