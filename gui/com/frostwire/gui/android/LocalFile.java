package com.frostwire.gui.android;

import java.io.File;
import java.io.Serializable;

import com.frostwire.gui.GuiFrostWireUtils;

class LocalFile implements Serializable {
	
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
        String ext = index != -1 ? name.substring(index + 1, name.length()).toLowerCase() : null;
        
        return ext;
	}
	
	public int getFileType() {
	    String ext = getExt();
	    return new UITool().getFileTypeByExt(ext);
	}

    public void open() {
		if (_file.isDirectory()) {
			fireOnOpen();
		} else {
			GuiFrostWireUtils.launchFile(_file);
		}
	}
    
    public void rename(String name) {
        File dest = new File(_file.getParentFile(), name);
        _file.renameTo(dest);
        _file.delete();
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
	
	public interface OnOpenListener {	
		public void onOpen(LocalFile localFile);
	}
}
