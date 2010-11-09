package com.frostwire.gnutella.gui.android;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

public class ActivityListModel extends AbstractListModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8310165387627881837L;
	
	private List<Activity> _activities;
	
	public ActivityListModel() {
		_activities = new ArrayList<Activity>();
	}

	@Override
	public int getSize() {
		return _activities.size();
	}

	@Override
	public Activity getElementAt(int index) {
		return _activities.get(index);
	}
	
	public void addActivity(Activity activity) {
		int index = _activities.size();
		_activities.add(activity);
		fireIntervalAdded(this, index, index);
	}
	
	public void refreshIndex(int index) {
		fireContentsChanged(this, index, index);
	}
	
	public int indexOf(Activity activity) {
		return _activities.indexOf(activity);
	}
}
