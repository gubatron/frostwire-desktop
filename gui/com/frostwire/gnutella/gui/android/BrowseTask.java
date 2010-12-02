package com.frostwire.gnutella.gui.android;

import java.util.List;

import javax.swing.SwingUtilities;

public class BrowseTask extends Task {
	
	private Device _device;
	private FileDescriptorListModel _model;
	private int _type;

	public BrowseTask(Device device, FileDescriptorListModel model, int type) {
		_device = device;
		_model = model;
		_type = type;
	}
	
	public Device getDevice() {
		return _device;
	}
	
	public FileDescriptorListModel getModel() {
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
					    if (AndroidMediator.instance().getDeviceExplorer().getSelectedFileType() == _type) {
					        _model.clear(); // avoid bad effects in instant browse button switch
	                        _model.addAll(result);
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
	
	@Override
	public boolean enqueue() {
	    return false;
	}
}
