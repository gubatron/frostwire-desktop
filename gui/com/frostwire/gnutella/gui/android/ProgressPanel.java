package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.frostwire.gnutella.gui.android.Activity.OnChangedListener;

public class ProgressPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5916970294500660451L;
	
	private ActivityListModel _model;
	private MyActivityListener _activityListener;
	
	private JButton _buttonCancel;
	private JList _listActivities;
	private JScrollPane _scrollPaneActivities;

	public ProgressPanel() {
		
		_model = new ActivityListModel();
		_activityListener = new MyActivityListener();
		
		setupUI();
	}
	
	public void addActivity(Activity activity) {
		
		activity.setOnChangedListener(_activityListener);
		
		_model.addActivity(activity);
		
		int lastIndex = _model.getSize() - 1;
		if (lastIndex >= 0) {
			_listActivities.ensureIndexIsVisible(lastIndex);
		}
	}
	
	private void setupUI() {
		setLayout(new BorderLayout());
		
		_buttonCancel = new JButton("Cancel");
		_buttonCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				buttonCancel_mouseClicked(e);
			}
		});
		add(_buttonCancel, BorderLayout.PAGE_START);		
		
		_listActivities = new JList(_model);
		_listActivities.setCellRenderer(new ActivityRenderer());
		_listActivities.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_listActivities.setLayoutOrientation(JList.VERTICAL);
		_listActivities.setVisibleRowCount(-1);

		_scrollPaneActivities = new JScrollPane(_listActivities);
		
		add(_scrollPaneActivities, BorderLayout.CENTER);
		
		setPreferredSize(new Dimension(300, 100));
	}
	
	private void buttonCancel_mouseClicked(MouseEvent e) {
		int index = _listActivities.getSelectedIndex();
		
		_model.getElementAt(index).cancel();
		_model.refreshIndex(index);
	}
	
	private final class MyActivityListener implements OnChangedListener {
		public void onChanged(final Activity activity) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_model.refreshActivity(activity);
				}
			});
		}
	}
}
