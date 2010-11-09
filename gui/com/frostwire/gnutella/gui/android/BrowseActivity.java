package com.frostwire.gnutella.gui.android;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

public class BrowseActivity extends Activity {
	
	private Device _device;
	private DefaultListModel _model;
	private int _type;

	public BrowseActivity(Device device, DefaultListModel model, int type) {
		_device = device;
		_model = model;
		_type = type;
	}
	
	public Device getDevice() {
		return _device;
	}
	
	public DefaultListModel getModel() {
		return _model;
	}
	
	public int getType() {
		return _type;
	}

	@Override
	public void run() {
		if (isCanceled()) {
			return;
		}
		
		try {			
			final List<FileDescriptor> result = _device.browse(_type);
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						for (FileDescriptor fileDescriptor : result) {
							_model.addElement(fileDescriptor);
						}
					} catch (Exception e) {
						fail(e);
					}
				}
			});
			
			setProgress(100);
			
		} catch (Exception e) {
			fail(e);
		}
	}
}
