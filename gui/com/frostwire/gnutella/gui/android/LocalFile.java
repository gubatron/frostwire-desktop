package com.frostwire.gnutella.gui.android;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalFile {
	
	private String _name;
	private File _file;
	private LocalFileListModel _model;
	
	public LocalFile(String name, LocalFileListModel model) {
		_name = name;
		_model = model;
	}
	
	public LocalFile(File file, LocalFileListModel model) {
		_file = file;
		_model = model;
	}

	public String getName() {
		return _name != null ? _name : _file.getName();
	}
	
	public File getFile() {
		return _file;
	}
	
	public void setFile(File file) {
		_file = file;
	}
	
	public void open() {
		if (_file == null || !_file.isDirectory()) {
			return;
		}
		
		_model.setRoot(this);
	}

	public List<LocalFile> getChildren() {
		if (_file == null || !_file.isDirectory()) {
			return new ArrayList<LocalFile>();
		}
		
		ArrayList<LocalFile> result = new ArrayList<LocalFile>();
		
		result.addAll(_model.getSpecialFiles());
		
		for (File f : _file.listFiles()) {
			if (!f.isHidden()) {
				result.add(new LocalFile(f, _model));
			}
		}

		return result;
	}

	public void refresh() {
		_model.refresh();
	}
}
