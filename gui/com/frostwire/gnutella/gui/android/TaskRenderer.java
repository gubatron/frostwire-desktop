package com.frostwire.gnutella.gui.android;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class TaskRenderer extends JPanel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3202019053091139910L;
	
	private Task _activity;
	
	private JLabel _label;
	
	public TaskRenderer() {
		setupUI();
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

		_activity = (Task) value;
		
		if (isSelected) {
		    setBackground(Color.LIGHT_GRAY);
		    setForeground(Color.BLACK);
		}
		else {
		    setBackground(Color.WHITE);
		    setForeground(Color.BLACK);
		}
		
		if (_activity instanceof BrowseTask) {
			renderBrowseActivity((BrowseTask) _activity);
		} else if (_activity instanceof CopyToDeviceTask) {
			renderCopyToDeviceActivity((CopyToDeviceTask) _activity);
		} else if (_activity instanceof CopyToDesktopTask) {
			renderCopyToDesktopActivity((CopyToDesktopTask) _activity);
		} else {
			_label.setText(_activity.toString());
		}

		return this;
	}
	
	protected void setupUI() {
		_label = new JLabel();
		add(_label);
	}

	private void renderBrowseActivity(BrowseTask activity) {
		String text = "Browsing device for file type " + UITool.getFileTypeAsString(activity.getType());
		
		if (activity.isCanceled()) {
			text += " Canceled";
		} else if (activity.isFailed()) {
			text += " Failed (" + activity.getFailException().getMessage() + ")";
		} else if (activity.getProgress() == 0) {
			text += " Pending";
		} else if (activity.getProgress() == 100) {
			text += "  Done";
		}
		
		_label.setText(text);
	}
	
	private void renderCopyToDeviceActivity(CopyToDeviceTask activity) {
		String text = "Copying " + activity.getProgressMessage();
		
		if (activity.isCanceled()) {
			text += " Canceled";
		} else if (activity.isFailed()) {
			text += " Failed (" + activity.getFailException().getMessage() + ")";
		} else if (activity.getProgress() == 0) {
			text += " Pending";
		} else if (activity.getProgress() == 100) {
			text += "  Done";
		} else {
			text += " " + activity.getProgress() + "%";
		}
		
		_label.setText(text);
	}
	
	private void renderCopyToDesktopActivity(CopyToDesktopTask activity) {
		String text = "Copying " + activity.getProgressMessage();
		
		if (activity.isCanceled()) {
			text += " Canceled";
		} else if (activity.isFailed()) {
			text += " Failed (" + activity.getFailException().getMessage() + ")";
		} else if (activity.getProgress() == 0) {
			text += " Pending";
		} else if (activity.getProgress() == 100) {
			text += "  Done";
		} else {
			text += " " + activity.getProgress() + "%";
		}
		
		_label.setText(text);
	}
}
