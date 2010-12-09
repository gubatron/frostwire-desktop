package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.frostwire.GuiFrostWireUtils;
import com.frostwire.gnutella.gui.GraphicPanel;
import com.frostwire.gnutella.gui.android.Task.OnChangedListener;
import com.limegroup.gnutella.gui.I18n;

public class ProgressPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5916970294500660451L;
	
	private TaskListModel _model;
	private MyActivityListener _taskListener;
	
	private JButton _buttonCancel;
	private JList _listTasks;
	private JScrollPane _scrollPaneTasks;
	private JLabel _labelTitle;
	private GraphicPanel _panelTitle;

	public ProgressPanel() {
		
		_model = new TaskListModel();
		_taskListener = new MyActivityListener();
		
		setupUI();
	}
	
	public void addTask(Task task) {
		
		task.addOnChangedListener(_taskListener);
		
		_model.addActivity(task);
		
		int lastIndex = _model.getSize() - 1;
		if (lastIndex >= 0) {
			_listTasks.ensureIndexIsVisible(lastIndex);
		}
	}
	
	protected void setupUI() {
		setLayout(new BorderLayout());
		
		_panelTitle = new GraphicPanel();
		_panelTitle.setLayout(new BorderLayout());
		_panelTitle.setGradient(new GradientPaint(0, 0, Color.RED, 0, 25, Color.BLUE));
		add(_panelTitle, BorderLayout.PAGE_START);
		
		String fontFamily = GuiFrostWireUtils.getFontFamily("Dialog", "myriad", "garuda", "helvetica", "arial", "FreeSans");
        Font titleFont = new Font(fontFamily, Font.PLAIN, 20);

        _labelTitle = new JLabel(" " + I18n.tr("File Transfers"));
        _labelTitle.setForeground(Color.white);
        _labelTitle.setFont(titleFont);
		
		_panelTitle.add(_labelTitle, BorderLayout.CENTER);
		
		_buttonCancel = new JButton("Cancel");
		_buttonCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				buttonCancel_mouseClicked(e);
			}
		});
		add(_buttonCancel, BorderLayout.PAGE_END);		
		
		_listTasks = new JList(_model);
		_listTasks.setCellRenderer(new TaskRenderer());
		_listTasks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_listTasks.setLayoutOrientation(JList.VERTICAL);
		_listTasks.setVisibleRowCount(-1);

		_scrollPaneTasks = new JScrollPane(_listTasks);
		
		add(_scrollPaneTasks, BorderLayout.CENTER);
		
		setPreferredSize(new Dimension(300, 100));
	}
	
	private void buttonCancel_mouseClicked(MouseEvent e) {
		int index = _listTasks.getSelectedIndex();
		
		if (index != -1) {
			int showConfirmDialog = JOptionPane.showConfirmDialog(null, I18n.tr("Should I stop the File Transfer?"), I18n.tr("Are you sure?"), JOptionPane.YES_NO_OPTION);
			
			if (showConfirmDialog != JOptionPane.YES_OPTION) {
				return;
			}

			_model.getElementAt(index).cancel();
			_model.refreshIndex(index);
		}
	}
	
	private Rectangle getRepaintBounds(int index) {
		Point p = _listTasks.indexToLocation(index);
		JPanel renderer = (JPanel) _listTasks.getCellRenderer().getListCellRendererComponent(_listTasks, _listTasks.getModel().getElementAt(index), index, false, false);
		return new Rectangle(p.x, p.y, renderer.getPreferredSize().width, renderer.getPreferredSize().height);
	}
	
	private final class MyActivityListener implements OnChangedListener {
		public void onChanged(final Task activity) {
			
			final int index = _model.indexOf(activity);
			
			if (index == -1) {
				return;
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_listTasks.repaint(getRepaintBounds(index));
					_model.refreshIndex(index);
				}
			});
		}
	}
}
