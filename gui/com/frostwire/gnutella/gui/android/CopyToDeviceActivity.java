package com.frostwire.gnutella.gui.android;

import java.io.File;

import com.frostwire.gnutella.gui.android.ProgressFileEntity.OnWriteListener;

public class CopyToDeviceActivity extends Activity {
	
	private Device _device;
	private LocalFile[] _localFiles;
	
	private int _totalBytes;
	private int _totalRead;
	private String _progressMessage;

	public CopyToDeviceActivity(Device device, LocalFile[] localFiles) {
		_device = device;
		_localFiles = localFiles;
		
		_totalBytes = getTotalBytes();
		_totalRead = 0;
		_progressMessage = "";
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
					_progressMessage = file.getName() + " " + (i + 1) + "/" + _localFiles.length;
					_device.upload(getFileType(file), file, new OnWriteListener() {
						public void onWrite(ProgressFileEntity progressFileEntity, int read) {
							_totalRead += read;
							setProgress((int) ((_totalRead * 100) / _totalBytes));
						}
					});
				} catch (Exception e) {
					fail(e);
				}
			}
			
			setProgress(100);
			
		} catch (Exception e) {
			fail(e);
		}
	}
	
	public String getProgressMessage() {
		return _progressMessage;
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
