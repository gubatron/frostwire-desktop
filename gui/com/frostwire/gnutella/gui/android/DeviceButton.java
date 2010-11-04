package com.frostwire.gnutella.gui.android;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

public class DeviceButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4608372510091566914L;
	
	private Device _device;
	
	private DeviceExplorer _deviceExplorer;

	public DeviceButton(Device device, DeviceExplorer deviceExplorer) {
		_device = device;
		_deviceExplorer = deviceExplorer;
		setText(device.getFinger().nickname);
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				DeviceButton.this.mouseClicked(e);
			}
		});
	}
	
	public Device getDevice() {
		return _device;
	}
	
	protected void mouseClicked(MouseEvent e) {
		AndroidMediator.SELECTED_DEVICE = _device;
		_deviceExplorer.setDevice(_device);
	}
}
