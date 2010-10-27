package com.frostwire.gnutella.gui.android;

import java.awt.Dimension;
import java.net.URI;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.frostwire.HttpFetcher;
import com.frostwire.json.JsonEngine;

public class DeviceExplorer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6716798921645948528L;
	
	private JsonEngine _jsonEngine;
	
	private JList _list;
	
	private DefaultListModel _model;
	
	private Device _device;

	public DeviceExplorer() {

		_jsonEngine = new JsonEngine();
		
		_model = new DefaultListModel();
		
		_list = new JList(_model);
		_list.setCellRenderer(new FileDescriptorRenderer());
		_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_list.setLayoutOrientation(JList.VERTICAL);
		_list.setVisibleRowCount(-1);
		
		JScrollPane scrollPane = new JScrollPane(_list);
		
		add(scrollPane);
	}

	public void setDevice(Device device) {
		_device = device;
		_model.clear();
		
		fillModel(1);
	}
	
	private void fillModel(int type) {
		try {
			URI uri = new URI("http://" + _device.getAddress().getHostAddress() + ":" + _device.getPort() + "/browse?type=" + type);
			
			HttpFetcher fetcher = new HttpFetcher(uri);
			
			byte[] jsonBytes = fetcher.fetch();
			
			if (jsonBytes == null) {
				System.out.println("Failed to connnect to " + uri);
				return;
			}
			
			String json = new String(jsonBytes);
			
			FileDescriptorList list = _jsonEngine.toObject(json, FileDescriptorList.class);
			
			for (FileDescriptor fileDescriptor : list.files) {
				_model.addElement(fileDescriptor);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
