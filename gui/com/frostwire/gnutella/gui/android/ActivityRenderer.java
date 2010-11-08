package com.frostwire.gnutella.gui.android;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.limegroup.gnutella.gui.I18n;

public class ActivityRenderer extends JPanel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3202019053091139910L;
	
	private Activity _activity;
	
	private JLabel _label;
	
	public ActivityRenderer() {
		_label = new JLabel();
		add(_label);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

		_activity = (Activity) value;
		
		if (_activity instanceof BrowseActivity) {
			renderBrowseActivity((BrowseActivity) _activity);
		} else if (_activity instanceof CopyToDeviceActivity) {
			renderCopyToDeviceActivity((CopyToDeviceActivity) _activity);
		} else {
			_label.setText(_activity.toString());
		}

		return this;
	}

	private void renderBrowseActivity(BrowseActivity activity) {
		String text = "Browsing device for file type " + getFileTypeAsString(activity.getType());
		
		if (activity.isCanceled()) {
			text += " Canceled";
		} else if (activity.isFailed()) {
			text += " Failed (" + activity.getFailException().getMessage() + ")";
		} else if (activity.getProgress() == 100) {
			text += "  Done";
		}
		
		_label.setText(text);
	}
	
	private void renderCopyToDeviceActivity(CopyToDeviceActivity activity) {
		String text = "Copying " + activity.getProgressMessage();
		
		if (activity.isCanceled()) {
			text += " Canceled";
		} else if (activity.isFailed()) {
			text += " Failed (" + activity.getFailException().getMessage() + ")";
		} else if (activity.getProgress() == 100) {
			text += "  Done";
		} else {
			text += " " + activity.getProgress() + "%";
		}
		
		_label.setText(text);
	}
	
	private String getFileTypeAsString(int type) {

		switch (type) {
		case DeviceConstants.FILE_TYPE_APPLICATIONS:
			return I18n.tr("Applications");
		case DeviceConstants.FILE_TYPE_AUDIO:
			return I18n.tr("Audio");
		case DeviceConstants.FILE_TYPE_DOCUMENTS:
			return I18n.tr("Documents");
		case DeviceConstants.FILE_TYPE_PICTURES:
			return I18n.tr("Pictures");
		case DeviceConstants.FILE_TYPE_RINGTONES:
			return I18n.tr("Ringtones");
		case DeviceConstants.FILE_TYPE_VIDEOS:
			return I18n.tr("Video");
		default:
			return "Unkown file type";
		}
	}
}
