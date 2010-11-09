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
	private FileDescriptor _fileDescriptor;
	
	public CopyToDesktopActivity(Device device, LocalFile localFile, FileDescriptor fileDescriptor) {
		_device = device;
		_localFile = localFile;
		_fileDescriptor = fileDescriptor;
	}
	
	public Device getDevice() {
		return _device;
	}
	
	public LocalFile getLocalFile() {
		return _localFile;
	}
	
	public FileDescriptor getFileDescriptor() {
		return _fileDescriptor;
	}

	@Override
	public void run() {
		
		if (isCanceled()) {
			return;
		}
		
		setProgress(0);
		
		int totalBytes = (int) _fileDescriptor.fileSize;
		int totalWritten = 0;

		URL url = _device.getDownloadURL(_fileDescriptor.fileType, _fileDescriptor.id);
		
		InputStream is = null;
		FileOutputStream fos = null;
		
		try {
			is = url.openStream();
			
			File file = new File(_localFile.getFile(), _fileDescriptor.fileName);
			
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
		} finally {
			close(fos);
			close(is);
		}
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
