package com.frostwire.gui.android;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

public class TaskListModel extends AbstractListModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8310165387627881837L;
	
	private List<Task> _tasks;
	
	public TaskListModel() {
		_tasks = new ArrayList<Task>();
	}

	@Override
	public int getSize() {
		return _tasks.size();
	}

	@Override
	public Task getElementAt(int index) {
		return _tasks.get(index);
	}
	
	public void addTask(Task task) {
		int index = _tasks.size();
		_tasks.add(task);
		fireIntervalAdded(this, index, index);
	}
	
	public void refreshIndex(int index) {
		fireContentsChanged(this, index, index);
	}
	
	public int indexOf(Task activity) {
		return _tasks.indexOf(activity);
	}
	
	public void delete(int index) {
	    try {
	        _tasks.remove(index);
	        fireIntervalRemoved(this, index, index);
	    } catch (Exception e) {
	        // ignore
	    }
	}
}
