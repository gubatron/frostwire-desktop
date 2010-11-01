package com.frostwire.gnutella.gui.android;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.frostwire.gnutella.gui.android.Device.DeviceListener;

public class DeviceBar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6886611714952957959L;
	
	private static final long STALE_DEVICE_TIMEOUT = 21000;
	
	private DeviceExplorer _deviceExplorer;
	
	private Map<Device, Long> _devices;
	private Map<Device, DeviceButton> _buttons;
	private DeviceListener _deviceListener;
	
	public DeviceBar(DeviceExplorer deviceExplorer) {
		
		_deviceExplorer = deviceExplorer;
		
		_devices = new HashMap<Device, Long>();
		_buttons = new HashMap<Device, DeviceButton>();
		_deviceListener = new DeviceListener() {
			public void onActionFailed(Device button, Exception e) {
				cleanDevice(button);
				if (e != null) {
					e.printStackTrace();
				}
			}
		};
		new Thread(new CleanStaleDevices()).start();
		
		setLayout(new FlowLayout());
		setPreferredSize(new Dimension(300, 100));
	}

	public void handleNewDevice(Device device) {
		
		handleDeviceAlive(device);
		
		DeviceButton button = new DeviceButton(device, _deviceExplorer);
		_buttons.put(device, button);
		add(button);
		revalidate();
		
		device.setListener(_deviceListener);
	}

	public void handleDeviceAlive(Device device) {
		_devices.put(device, System.currentTimeMillis());
	}
	
	private void cleanDevice(final Device device) {
		_devices.remove(device);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DeviceButton button = _buttons.remove(device);
				remove(button);
				repaint();
			}
		});
	}
	
	private final class CleanStaleDevices implements Runnable {

		@Override
		public void run() {
			
			while (true) {
				
				long now = System.currentTimeMillis();
				
				for (Entry<Device, Long> entry :_devices.entrySet()){
					if (entry.getValue() + STALE_DEVICE_TIMEOUT < now) {
						cleanDevice(entry.getKey());
					}
				}
				
				for (int i = 0; i < STALE_DEVICE_TIMEOUT; i+= 1000) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
}
