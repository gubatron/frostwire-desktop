package com.frostwire.gnutella.gui.android;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class CopyToDesktopActivity extends Activity {
	
	private Device _device;
	private LocalFile _localFile;
	private FileDescriptor[] _fileDescriptors;

	private String _progressMessage;
	
	public CopyToDesktopActivity(Device device, LocalFile localFile, FileDescriptor[] fileDescriptors) {
		_device = device;
		_localFile = localFile;
		_fileDescriptors = fileDescriptors;
		
		_progressMessage = "";
	}
	
	public Device getDevice() {
		return _device;
	}
	
	public LocalFile getLocalFile() {
		return _localFile;
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
		
		int totalBytes = getTotalBytes();
		int totalWritten = 0;
		
		for (int i = 0; i < _fileDescriptors.length; i++) {
			
			if (isCanceled()) {
				return;
			}
			
			FileDescriptor fileDescriptor = _fileDescriptors[i];

			URL url = _device.getDownloadURL(fileDescriptor.fileType, fileDescriptor.id);
			
			InputStream is = null;
			FileOutputStream fos = null;
			
			try {
				is = url.openStream();
				
				File file = new File(_localFile.getFile(), fileDescriptor.fileName);
				
				_progressMessage = file.getName() + ((_fileDescriptors.length > 1) ? (" " + (i + 1) + "/" + _fileDescriptors.length) : "");
				
				fos = new FileOutputStream(file);
				
				byte[] buffer = new byte[4 * 1024];
				int n = 0;
				
				while ((n = is.read(buffer, 0, buffer.length)) != -1) {
					
					if (isCanceled()) {
						return;
					}
					
					fos.write(buffer, 0, n);
					totalWritten += n;
					setProgress((int) ((totalWritten * 100) / totalBytes));
				}
				
				_localFile.refresh();
				
				setProgress(100);
				
			} catch (IOException e) {
				fail(e);
				break;
			} finally {
				close(fos);
				close(is);
			}
		}
	}
	
	private int getTotalBytes() {
		int total = 0;
		for (FileDescriptor fileDescriptor : _fileDescriptors) {
			total += fileDescriptor.fileSize;
		}
		return total;
	}
	
	private void close(Closeable closable) {
		if (closable != null) {
			try {
				closable.close();
			} catch (IOException e) {
			}
		}
	}
}
