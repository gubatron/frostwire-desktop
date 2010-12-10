package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
	private MyTaskListener _taskListener;
	
	private JButton _buttonCancelAll;
	private JButton _buttonClearFinished;
	private JList _listTasks;
	private JScrollPane _scrollPaneTasks;
	private JLabel _labelTitle;
	private GraphicPanel _panelTitle;

	public ProgressPanel() {
		
		_model = new TaskListModel();
		_taskListener = new MyTaskListener();
		
		setupUI();
	}
	
	public void addTask(Task task) {
	    
	    if (!(task instanceof CopyToDesktopTask ||
	          task instanceof CopyToDeviceTask)) {
	        return;
	    }
		
		task.addOnChangedListener(_taskListener);
		
		_model.addTask(task);
		
		int lastIndex = _model.getSize() - 1;
		if (lastIndex >= 0) {
			_listTasks.ensureIndexIsVisible(lastIndex);
		}
	}
	
	protected void setupUI() {
		setLayout(new BorderLayout());
		
		_panelTitle = new GraphicPanel();
		_panelTitle.setLayout(new BorderLayout());
		_panelTitle.setGradient(new GradientPaint(0, 0,new Color(0x2c7fb0), 0, 25,  Color.BLACK));
		add(_panelTitle, BorderLayout.PAGE_START);
		
		String fontFamily = GuiFrostWireUtils.getFontFamily("myriad","helvetica","arial", "Dialog","FreeSans");
        Font titleFont = new Font(fontFamily, Font.PLAIN, 20);

        _labelTitle = new JLabel(" " + I18n.tr("File Transfers"));
        _labelTitle.setForeground(Color.white);
        _labelTitle.setFont(titleFont);
		
		_panelTitle.add(_labelTitle, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		_buttonCancelAll = new JButton(I18n.tr("Cancel All"));
		_buttonCancelAll.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				buttonCancelAll_mouseClicked(e);
			}
		});
		bottomPanel.add(_buttonCancelAll);
		
		_buttonClearFinished = new JButton(I18n.tr("Clear Finished"));
		_buttonClearFinished.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                buttonClearFinished_mouseClicked(e);
            }
        });
        bottomPanel.add(_buttonClearFinished);
		
		add(bottomPanel, BorderLayout.PAGE_END);		
		
		_listTasks = new JList(_model);
		_listTasks.setCellRenderer(new TaskRenderer());
		_listTasks.addMouseListener(new RedispatchMouseListener(_listTasks));
		_listTasks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_listTasks.setLayoutOrientation(JList.VERTICAL);
		_listTasks.setVisibleRowCount(-1);

		_scrollPaneTasks = new JScrollPane(_listTasks);
		
		add(_scrollPaneTasks, BorderLayout.CENTER);
		
		setPreferredSize(new Dimension(300, 100));
	}
	
	protected void buttonClearFinished_mouseClicked(MouseEvent e) {
        for (int i = _model.getSize() - 1; i >= 0; i--) {
            Task task = (Task) _model.getElementAt(i);
            if (!task.isRunning()) {
                _model.delete(i);
            }
        }
    }

    protected void buttonCancelAll_mouseClicked(MouseEvent e) {
        Component parent = AndroidMediator.instance().getComponent();
        int showConfirmDialog = JOptionPane.showConfirmDialog(parent, I18n.tr("Should I stop all transfers?"), I18n.tr("Are you sure?"),
                JOptionPane.YES_NO_OPTION);

        if (showConfirmDialog != JOptionPane.YES_OPTION) {
            return;
        }

        for (int i = _model.getSize() - 1; i >= 0; i--) {
            Task task = (Task) _model.getElementAt(i);
            if (task.isRunning()) {
                _model.getElementAt(i).cancel();
                _model.refreshIndex(i);
            }
        }
	}
	
	private Rectangle getRepaintBounds(int index) {
		Point p = _listTasks.indexToLocation(index);
		JPanel renderer = (JPanel) _listTasks.getCellRenderer().getListCellRendererComponent(_listTasks, _listTasks.getModel().getElementAt(index), index, false, false);
		return new Rectangle(p.x, p.y, renderer.getPreferredSize().width, renderer.getPreferredSize().height);
	}
	
	private final class MyTaskListener implements OnChangedListener {
		public void onChanged(final Task activity) {
			
			final int index = _model.indexOf(activity);
			
			if (index == -1) {
				return;
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    try {
				        _listTasks.repaint(getRepaintBounds(index));
				        _model.refreshIndex(index);
				    } catch (Exception e) {
				        // ignored due to concurrent modifications
				    }
				}
			});
		}
	}
}
