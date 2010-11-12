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
	
	public void open() {
		if (_file.isDirectory()) {
			fireOnOpen();
		}
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
