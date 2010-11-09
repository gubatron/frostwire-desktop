package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class DeviceExplorer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6716798921645948528L;
	
	private static final String DEVICE = "device";
	private static final String NO_DEVICE = "no-device";
	
	private DefaultListModel _model;
	
	private JPanel _panelDevice;
	private JPanel _panelNoDevice;
	private JList _list;
	private Map<Integer, JButton> _buttonTypes;
	
	private Device _device;

	public DeviceExplorer() {
		
		_model = new DefaultListModel();
		
		setupUI();
		
		setPanelDevice(false);
	}
	
	public Device getDevice() {
		return _device;
	}

	public void setDevice(Device device) {
		_device = device;
		_model.clear();		
		setPanelDevice(true);
	}
	
	public void setPanelDevice(boolean device) {
		CardLayout cl = (CardLayout) getLayout();
		cl.show(this, device ? DEVICE : NO_DEVICE);
	}
	
	private void setupUI() {
		setLayout(new CardLayout());
		
		_panelDevice = setupPanelDevice();
		_panelNoDevice = setupPanelNoDevice();
		
		add(_panelDevice, DEVICE);
		add(_panelNoDevice, NO_DEVICE);
	}
	
	private JPanel setupPanelDevice() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.LINE_AXIS));
		_buttonTypes = new HashMap<Integer, JButton>(6);
		setupButtonType(header, DeviceConstants.FILE_TYPE_APPLICATIONS);
		setupButtonType(header, DeviceConstants.FILE_TYPE_DOCUMENTS);
		setupButtonType(header, DeviceConstants.FILE_TYPE_PICTURES);
		setupButtonType(header, DeviceConstants.FILE_TYPE_VIDEOS);
		setupButtonType(header, DeviceConstants.FILE_TYPE_RINGTONES);
		setupButtonType(header, DeviceConstants.FILE_TYPE_AUDIO);
		panel.add(header, BorderLayout.PAGE_START);
		
		_list = new JList(_model);
		_list.setCellRenderer(new FileDescriptorRenderer());
		_list.addMouseListener(new RedispatchMouseListener(_list));
		_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_list.setLayoutOrientation(JList.VERTICAL);
		_list.setDragEnabled(true);
		_list.setTransferHandler(new DeviceListTransferHandler());
		_list.setVisibleRowCount(-1);
		
		JScrollPane scrollPane = new JScrollPane(_list);
		
		panel.add(scrollPane, BorderLayout.CENTER);
		
		return panel;
	}
	
	private JPanel setupPanelNoDevice() {
		JLabel l = new JLabel("hello");
		JPanel p = new JPanel(new BorderLayout());
		p.add(l);
		return p;
	}
	
	private void setupButtonType(JPanel container, final int type) {
		JButton button = new JButton();
		button.setSize(100, 100);
		button.setPreferredSize(button.getSize());
		button.setText("Type:" + type);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				_model.clear();
				AndroidMediator.addActivity(new BrowseActivity(_device, _model, type));
			}
		});
		_buttonTypes.put(type, button);
		container.add(button);
	}
}
