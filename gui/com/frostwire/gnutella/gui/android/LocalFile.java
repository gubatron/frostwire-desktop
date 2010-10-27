package com.frostwire.gnutella.gui.android;

import java.io.File;
import java.util.ArrayList;

public class LocalFile {
	
	private File _file;
	private LocalFileListModel _model;
	
	public LocalFile(File file, LocalFileListModel model) {
		_file = file;
		_model = model;
	}

	public String getName() {
		return _file.getName();
	}

	public ArrayList<LocalFile> getChildren() {
		
		ArrayList<LocalFile> result = new ArrayList<LocalFile>();
		
		result.addAll(_model.getSpecialFiles());
		
		for (File f : _file.listFiles()) {
			result.add(new LocalFile(f, _model));
		}

		return result;
	}
}
