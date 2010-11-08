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
	
	public DeviceButton(Device device) {
		_device = device;
		
		setupUI();
	}

	public Device getDevice() {
		return _device;
	}
	
	protected void deviceButton_mouseClicked(MouseEvent e) {
		AndroidMediator.instance().getDeviceExplorer().setDevice(_device);
	}
	
	private void setupUI() {
		setText(_device.getFinger().nickname);
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				deviceButton_mouseClicked(e);
			}
		});
	}
}
