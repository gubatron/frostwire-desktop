package com.frostwire.gnutella.gui.android;

import java.io.File;
import java.io.Serializable;

public class LocalFile implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2596345342420014651L;
	
	private File _file;
	
	private transient OnOpenListener _listener;
	
	public LocalFile() {
		
	}
	
	public LocalFile(File file) {
		_file = file;
	}

	public String getName() {
		return _file.getName();
	}
	
	public File getFile() {
		return _file;
	}
	
	public void setFile(File file) {
		_file = file;
	}
	
	public OnOpenListener getOnOpenListener() {
		return _listener;
	}
	
	public void setOnOpenListener(OnOpenListener listener) {
		_listener = listener;
	}
	
	public String getExt() {
	    String name = _file.getName();
        int index = name.lastIndexOf(".");
        String ext = index != -1 ? name.substring(index + 1, name.length()) : null;
        
        return ext;
	}
	
	public int getFileType() {
	    String ext = getExt();
	    if (ext != null) {
	        return getFileTypeByExt(ext);
	    } else {
	        return DeviceConstants.FILE_TYPE_DOCUMENTS;
	    }
	}

    public void open() {
		if (_file.isDirectory()) {
			fireOnOpen();
		}
	}
    
    public void rename(String name) {
        File dest = new File(_file.getParentFile(), name);
        _file.renameTo(dest);
        _file = dest;
    }
	
	@Override
	public String toString() {
		return _file.toString();
	}
	
	protected void fireOnOpen() {
		if (_listener != null) {
			_listener.onOpen(this);
		}
	}
	
	private int getFileTypeByExt(String ext) {
        if (ext.equalsIgnoreCase("mp3")) {
            return DeviceConstants.FILE_TYPE_AUDIO;
        } else {
            return DeviceConstants.FILE_TYPE_DOCUMENTS;
        }
    }
	
	public interface OnOpenListener {	
		public void onOpen(LocalFile localFile);
	}
}
