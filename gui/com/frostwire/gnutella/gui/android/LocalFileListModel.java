package com.frostwire.gnutella.gui.android;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import com.frostwire.gnutella.gui.android.LocalFile.OnOpenListener;

public class LocalFileListModel extends AbstractListModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3669455405023885518L;
	
	private File _root;
	private List<LocalFile> _files;
	
	private OnRootListener _listener;
	private MyOnOpenListener _myOnOpenListener;
	
	public LocalFileListModel() {
		_files = new ArrayList<LocalFile>();
		_myOnOpenListener = new MyOnOpenListener();
	}
	
	public void setRoot(File path) {
		if (!path.isDirectory()) {
			return;
		}
		
		_root = path;
		_files.clear();
		_files.addAll(getChildren(_root));
		fireOnRoot(path);
		fireContentsChanged(this, 0, _files.size() - 1);
	}
	
	public File getRoot() {
		return _root;
	}
	
	public OnRootListener getOnRootListener() {
		return _listener;
	}
	
	public void setOnRootListener(OnRootListener listener) {
		_listener = listener;
	}

	@Override
	public int getSize() {
		return _files.size();
	}

	@Override
	public Object getElementAt(int index) {
	    if (index < _files.size()) {
	        return _files.get(index);
	    } else {
	        return null;
	    }
	}
	
	public void refresh() {
		setRoot(_root);
	}
	
	protected void fireOnRoot(File path) {
		if (_listener != null) {
			_listener.onRoot(this, path);
		}
	}
	
	private List<LocalFile> getChildren(File path) {
		if (path == null || !path.isDirectory()) {
			return new ArrayList<LocalFile>();
		}
		
		ArrayList<LocalFile> result = new ArrayList<LocalFile>();
		
		for (File f : path.listFiles()) {
			if (!f.isHidden()) {
				LocalFile localFile = new LocalFile(f);
				localFile.setOnOpenListener(_myOnOpenListener);
				result.add(localFile);
			}
		}

		return result;
	}
	
	public interface OnRootListener {
		public void onRoot(LocalFileListModel localFileListModel, File path);
	}
	
	private final class MyOnOpenListener implements OnOpenListener {
		public void onOpen(LocalFile localFile) {
			setRoot(localFile.getFile());
		}
	}
}
