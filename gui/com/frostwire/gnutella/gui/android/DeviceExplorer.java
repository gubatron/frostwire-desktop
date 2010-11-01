package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class DeviceExplorer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6716798921645948528L;
	
	private JList _list;
	
	private DefaultListModel _model;
	
	private Device _device;

	public DeviceExplorer() {
		
		_model = new DefaultListModel();
		
		_list = new JList(_model);
		_list.setCellRenderer(new FileDescriptorRenderer());
		_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_list.setLayoutOrientation(JList.VERTICAL);
		_list.setVisibleRowCount(-1);
		
		JScrollPane scrollPane = new JScrollPane(_list);
		
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}

	public void setDevice(Device device) {
		_device = device;
		_model.clear();
		
		fillModel(1);
	}
	
	private void fillModel(int type) {
		try {
			
			for (FileDescriptor fileDescriptor : _device.browse(type)) {
				_model.addElement(fileDescriptor);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
