package com.frostwire.gnutella.gui.android;

import java.io.File;

import com.frostwire.gnutella.gui.android.ProgressFileEntity.ProgressFileEntityListener;

public class CopyToDeviceTask extends Task {
	
	private Device _device;
	private LocalFile[] _localFiles;
	private int _fileType;
    private int _currentIndex;
    
    private long _totalBytes;
    private long _totalWritten;

	public CopyToDeviceTask(Device device, LocalFile[] localFiles, int fileType) {
		_device = device;
		_localFiles = localFiles;
		_fileType = fileType;
		_currentIndex = -1;
	}
	
	public Device getDevice() {
        return _device;
    }
	
	public int getFileType() {
	    return _fileType;
	}
	
	public int getCurrentIndex() {
        return _currentIndex;
    }
    
    public int getTotalItems() {
        return _localFiles.length;
    }

	@Override
	public void run() {
		if (isCanceled()) {
			return;
		}
		
		try {
		    
		    setProgress(0);
		    
		    _totalBytes = getTotalBytes();
            _totalWritten = 0;
		    
			for (int i = 0; i < _localFiles.length; i++) {
				
				if (isCanceled()) {
					return;
				}
				
				_currentIndex = i;
				
				try {			
					File file = _localFiles[i].getFile();
					
					_device.upload(_localFiles[i].getFileType(), file, new ProgressFileEntityListener() {
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
	
	private long getTotalBytes() {
		long total = 0;
		for (LocalFile localFile : _localFiles) {
			total += localFile.getFile().length();
		}
		return total;
	}
}
