package com.frostwire.gnutella.gui.android;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.frostwire.gnutella.gui.android.Device.OnActionFailedListener;

public class DeviceBar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6886611714952957959L;
	
	private Map<Device, DeviceButton> _buttons;
	private OnActionFailedListener _deviceListener;
	
	private Device _selectedDevice;
	
	public DeviceBar() {
		
		_buttons = new HashMap<Device, DeviceButton>();
		_deviceListener = new OnActionFailedListener() {
			public void onActionFailed(Device device, Exception e) {
				//handleDeviceStale(device);
				JOptionPane op = new JOptionPane("Device error: " + device.getFinger().nickname, JOptionPane.OK_OPTION);
				op.setVisible(true);
				if (e != null) {
					e.printStackTrace();
				}
			}
		};
		
		setLayout(new FlowLayout());
		setPreferredSize(new Dimension(300, 100));
	}

	public void handleNewDevice(Device device) {
		
		final DeviceButton button = new DeviceButton(device);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				_selectedDevice = button.getDevice();
			}
		});
		_buttons.put(device, button);
		add(button);
		revalidate();
		
		device.setListener(_deviceListener);
	}

	public void handleDeviceAlive(Device device) {
	}
	
	public void handleDeviceStale(final Device device) {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DeviceButton button = _buttons.remove(device);
				
				if (button != null) {
					remove(button);
					repaint();
					
					if (_buttons.size() == 0) {
						AndroidMediator.instance().getDeviceExplorer().setPanelDevice(false);
					}
				}
			}
		});
	}

	public Device getSelectedDevice() {
		return _selectedDevice;
	}
}
