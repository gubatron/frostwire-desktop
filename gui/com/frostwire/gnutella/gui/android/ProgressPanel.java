package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.frostwire.GuiFrostWireUtils;
import com.frostwire.gnutella.gui.android.Task.OnChangedListener;
import com.limegroup.gnutella.gui.I18n;

public class ProgressPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5916970294500660451L;
	
	private TaskListModel _model;
	private MyActivityListener _activityListener;
	
	private JButton _buttonCancel;
	private JList _listActivities;
	private JScrollPane _scrollPaneActivities;

	public ProgressPanel() {
		
		_model = new TaskListModel();
		_activityListener = new MyActivityListener();
		
		setupUI();
	}
	
	public void addActivity(Task activity) {
		
		activity.setOnChangedListener(_activityListener);
		
		_model.addActivity(activity);
		
		int lastIndex = _model.getSize() - 1;
		if (lastIndex >= 0) {
			_listActivities.ensureIndexIsVisible(lastIndex);
		}
	}
	
	private void setupUI() {
		setLayout(new BorderLayout());
		
		JLabel titleLabel = setupTitle();
		
		add(titleLabel, BorderLayout.PAGE_START);
		
		_buttonCancel = new JButton("Cancel");
		_buttonCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				buttonCancel_mouseClicked(e);
			}
		});
		add(_buttonCancel, BorderLayout.PAGE_END);		
		
		_listActivities = new JList(_model);
		_listActivities.setCellRenderer(new TaskRenderer());
		_listActivities.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_listActivities.setLayoutOrientation(JList.VERTICAL);
		_listActivities.setVisibleRowCount(-1);

		_scrollPaneActivities = new JScrollPane(_listActivities);
		
		add(_scrollPaneActivities, BorderLayout.CENTER);
		
		setPreferredSize(new Dimension(300, 100));
	}

	/**
	 * Sets up a title label. It tries to use Helvetica, Arial and Sans-Serif.
	 * If it cannot find any of these font families it'll just use Dialog.
	 * 
	 * The implementation of choosing fonts by a priority should probably be done in a generic manner,
	 * maybe on {@link GuiFrostWireUtils}.getFamilyName(defaultValue, ...); Where ... is an open ended
	 * list of String arguments representing the font families you may want. 
	 * @return
	 */
	private JLabel setupTitle() {
		String[] availableFontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		
		//fetch helvetica, arial or sans-serif.
		String fontFamily = new String();
		String helveticaCandidate = null;
		String arialCandidate = null;
		String sansCandidate = null;
		for (String fontName : availableFontFamilyNames) {
			
			if (fontName.equalsIgnoreCase("helvetica")) {
				helveticaCandidate = fontName;
				continue;
			}
			
			if (fontName.equalsIgnoreCase("arial")) {
				arialCandidate = fontName;
				continue;
			}
			
			if (fontName.equalsIgnoreCase("sansserif")) {
				sansCandidate = fontName;
				continue;
			}
			
			if (helveticaCandidate!=null && arialCandidate!=null & sansCandidate!=null) {
				break;
			}
		}
		
		if (helveticaCandidate!=null) {
			fontFamily=helveticaCandidate;
		} else if (arialCandidate!=null) {
			fontFamily=arialCandidate;
		} else if (sansCandidate!=null) {
			fontFamily=sansCandidate;
		} else {
			fontFamily="Dialog";
		}
		
		Font titleFont = new Font(fontFamily,Font.BOLD,20);
		JLabel titleLabel = new JLabel(" " + I18n.tr("File Transfers"));
		titleLabel.setForeground(Color.white);
		titleLabel.setOpaque(true);
		titleLabel.setBackground(new Color(0x2c7fb0));
		titleLabel.setFont(titleFont);
		return titleLabel;
	}
	
	private void buttonCancel_mouseClicked(MouseEvent e) {
		int index = _listActivities.getSelectedIndex();
		
		if (index != -1) {
			_model.getElementAt(index).cancel();
			_model.refreshIndex(index);
		}
	}
	
	private Rectangle getRepaintBounds(int index) {
		Point p = _listActivities.indexToLocation(index);
		JPanel renderer = (JPanel) _listActivities.getCellRenderer().getListCellRendererComponent(_listActivities, _listActivities.getModel().getElementAt(index), index, false, false);
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
					_listActivities.repaint(getRepaintBounds(index));
					_model.refreshIndex(index);
				}
			});
		}
	}
}
