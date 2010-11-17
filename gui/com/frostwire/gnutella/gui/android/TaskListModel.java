package com.frostwire.gnutella.gui.android;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

public class TaskListModel extends AbstractListModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8310165387627881837L;
	
	private List<Task> _activities;
	
	public TaskListModel() {
		_activities = new ArrayList<Task>();
	}

	@Override
	public int getSize() {
		return _activities.size();
	}

	@Override
	public Task getElementAt(int index) {
		return _activities.get(index);
	}
	
	public void addActivity(Task activity) {
		int index = _activities.size();
		_activities.add(activity);
		fireIntervalAdded(this, index, index);
	}
	
	public void refreshIndex(int index) {
		fireContentsChanged(this, index, index);
	}
	
	public int indexOf(Task activity) {
		return _activities.indexOf(activity);
	}
}
