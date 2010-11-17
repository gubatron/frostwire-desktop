package com.frostwire.gnutella.gui.android;

import java.io.File;

import com.frostwire.gnutella.gui.android.ProgressFileEntity.ProgressFileEntityListener;

public class CopyToDeviceTask extends Task {
	
	private Device _device;
	private LocalFile[] _localFiles;
	
	private int _totalBytes;
	private int _totalWritten;
	private String _progressMessage;

	public CopyToDeviceTask(Device device, LocalFile[] localFiles) {
		_device = device;
		_localFiles = localFiles;
		
		_totalBytes = getTotalBytes();
		_totalWritten = 0;
		_progressMessage = "";
	}
	
	public String getProgressMessage() {
		return _progressMessage;
	}

	@Override
	public void run() {
		if (isCanceled()) {
			return;
		}
		
		setProgress(0);
		
		try {
			for (int i = 0; i < _localFiles.length; i++) {
				
				if (isCanceled()) {
					return;
				}
				
				try {			
					File file = _localFiles[i].getFile();
					
					_progressMessage = file.getName() + ((_localFiles.length > 1) ? (" " + (i + 1) + "/" + _localFiles.length) : "");
					
					_device.upload(getFileType(file), file, new ProgressFileEntityListener() {
						public void onWrite(ProgressFileEntity progressFileEntity, int written) {
							_totalWritten += written;
							setProgress((int) ((_totalWritten * 100) / _totalBytes));
							
							if (getProgress() >= 2) {
								_device.setTokenAuthorized(true);
							}
						}

						public boolean isCanceled() {
							return CopyToDeviceTask.this.isCanceled();
						}
					});
					
				} catch (Exception e) {
					fail(e);
					break;
				}
			}
			
			setProgress(100);
			
		} catch (Exception e) {
			fail(e);
		}
	}
	
	private int getTotalBytes() {
		int total = 0;
		for (LocalFile localFile : _localFiles) {
			total += localFile.getFile().length();
		}
		return total;
	}
	
	private int getFileType(File file) {
		String name = file.getName();
		
		if (name.endsWith(".mp3")) {
			return DeviceConstants.FILE_TYPE_AUDIO;
		} else {
			return DeviceConstants.FILE_TYPE_DOCUMENTS;
		}
	}
}
