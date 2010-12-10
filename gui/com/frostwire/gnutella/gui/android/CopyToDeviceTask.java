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

    public CopyToDeviceTask(Device device, File[] files, int fileType) {
    	this(device, filesToLocalFiles(files), fileType);
    }

	private static LocalFile[] filesToLocalFiles(File[] files) {
		LocalFile[] localFiles = new LocalFile[files.length];
    	for (int i=0; i < files.length; i++) {
    		if (!files[i].isDirectory()) {
    			localFiles[i] = new LocalFile(files[i]);
    		}
    	}
    	
    	return localFiles;
	}
    
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
							System.out.println(_totalWritten);
							_totalWritten += written;
							setProgress((int) ((_totalWritten * 100) / _totalBytes));
							
							if (_totalWritten > 1024*302) {
								_device.setTokenAuthorized(true);
							}
						}

						public boolean isCanceled() {
							return CopyToDeviceTask.this.isCanceled();
						}
					});
					
					_device.setTokenAuthorized(true);
					
				} catch (Exception e) {
					fail(e);
					return;
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
