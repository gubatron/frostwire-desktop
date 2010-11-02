package com.frostwire.gnutella.gui.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class LocalFileListModel implements ListModel {
	
	private Set<ListDataListener> _listeners;
	
	private LocalFile _root;
	private List<LocalFile> _files;
	private LocalFile UP;
	private List<LocalFile> _specialFolders;
	
	public LocalFileListModel() {
		_listeners = new HashSet<ListDataListener>();
		_files = new ArrayList<LocalFile>();
		
		UP = new LocalFile("UP", this);
		_specialFolders = Arrays.asList(UP);
	}
	
	public void setRoot(LocalFile root) {
		if (_root != null) {
			UP.setFile(_root.getFile().getParentFile());
		}
		_root = root;
		_files.clear();
		_files = root.getChildren();
		contentsChanched();
		AndroidMediator.SELECTED_DESKTOP_FOLDER = root;
	}
	
	public LocalFile getRoot() {
		return _root;
	}
	
	public void setRoot(String path) {
		setRoot(new LocalFile(new File(path), this));
	}
	
	public List<LocalFile> getSpecialFiles() {
		return _specialFolders;
	}

	@Override
	public int getSize() {
		return _files.size();
	}

	@Override
	public Object getElementAt(int index) {
		return _files.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		_listeners.add(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		_listeners.remove(l);
	}
	
	private void contentsChanched() {
		
		ListDataEvent evt = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
		
		for (ListDataListener l : _listeners) {
			try {
				l.contentsChanged(evt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void refresh() {
		setRoot(_root);
	}
}
