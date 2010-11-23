package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

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
	
	private Device _device;
	
	private BrowseFilesButton _buttonApplications;
	private BrowseFilesButton _buttonDocuments;
	private BrowseFilesButton _buttonPictures;
	private BrowseFilesButton _buttonVideos;
	private BrowseFilesButton _buttonRingtones;
	private BrowseFilesButton _buttonAudio;

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
		
		if (device) {
    		Finger finger = _device.getFinger();
    		_buttonApplications.setText(String.valueOf(finger.numSharedApplicationFiles));
    		_buttonDocuments.setText(String.valueOf(finger.numSharedDocumentFiles));
    		_buttonPictures.setText(String.valueOf(finger.numSharedPictureFiles));
    		_buttonVideos.setText(String.valueOf(finger.numSharedVideoFiles));
    		_buttonRingtones.setText(String.valueOf(finger.numSharedRingtoneFiles));
    		_buttonAudio.setText(String.valueOf(finger.numSharedAudioFiles));
		}
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
		
		_buttonApplications = setupButtonType(header, DeviceConstants.FILE_TYPE_APPLICATIONS);
		_buttonDocuments = setupButtonType(header, DeviceConstants.FILE_TYPE_DOCUMENTS);
		_buttonPictures = setupButtonType(header, DeviceConstants.FILE_TYPE_PICTURES);
		_buttonVideos = setupButtonType(header, DeviceConstants.FILE_TYPE_VIDEOS);
		_buttonRingtones = setupButtonType(header, DeviceConstants.FILE_TYPE_RINGTONES);
		_buttonAudio = setupButtonType(header, DeviceConstants.FILE_TYPE_AUDIO);
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
	
	private BrowseFilesButton setupButtonType(JPanel container, final int type) {
	    BrowseFilesButton button = new BrowseFilesButton();
		button.setIcon(loadImageIcon(getImageName(type)));
		button.setPressedIcon(loadImageIcon(getImageName(type) + "_checked"));
		button.setSize(100, 100);
		button.setPreferredSize(button.getSize());
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				_model.clear();
				AndroidMediator.addActivity(new BrowseTask(_device, _model, type));
			}
		});
		container.add(button);
		
		return button;
	}
	
	private ImageIcon loadImageIcon(String name) {
	    URL url = getClass().getResource("images/" + name + ".png");
	    try {
            return new ImageIcon(ImageIO.read(url));
        } catch (IOException e) {
            return null;
        }
	}
	
	private String getImageName(int type) {
	    switch(type) {
	    case DeviceConstants.FILE_TYPE_APPLICATIONS: return "application";
	    case DeviceConstants.FILE_TYPE_DOCUMENTS: return "document";
	    case DeviceConstants.FILE_TYPE_PICTURES: return "picture";
	    case DeviceConstants.FILE_TYPE_VIDEOS: return "video";
	    case DeviceConstants.FILE_TYPE_RINGTONES: return "ringtone";
	    case DeviceConstants.FILE_TYPE_AUDIO: return "audio";
	    default: return "";
	    }
	}
}
