package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class DesktopExplorer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7362861227107918643L;
	
	private LocalFileListModel _model;

	private JList _list;
	
	public DesktopExplorer() {
		
		_model = new LocalFileListModel();
		
		setupUI();
	}
	
	public LocalFile getSelectedFolder() {
		return _model.getRoot();
	}

	private void setupUI() {
		_list = new JList(_model);
		_list.setCellRenderer(new LocalFileRenderer());
		_list.addMouseListener(new RedispatchMouseListener(_list));
		_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		_list.setDragEnabled(true);
		_list.setTransferHandler(new DesktopListTransferHandler());
		_list.setVisibleRowCount(-1);
		
		JScrollPane scrollPane = new JScrollPane(_list);
		
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		
		_model.setRoot("C:\\Users\\Alden\\Downloads\\FW");
	}
}
