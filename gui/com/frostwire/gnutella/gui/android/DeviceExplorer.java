package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import com.frostwire.gnutella.gui.ImagePanel;

public class DeviceExplorer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6716798921645948528L;
	
	private static final String DEVICE = "device";
	private static final String NO_DEVICE = "no-device";
	
	private FileDescriptorListModel _model;
	private Device _device;
	
	private JPanel _panelDevice;
	private JPanel _panelNoDevice;
	private JList _list;
	
	private ImageRadioButton _buttonApplications;
	private ImageRadioButton _buttonDocuments;
	private ImageRadioButton _buttonPictures;
	private ImageRadioButton _buttonVideos;
	private ImageRadioButton _buttonRingtones;
	private ImageRadioButton _buttonAudio;

	public DeviceExplorer() {
		_model = new FileDescriptorListModel();
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
	
	public FileDescriptorListModel getModel() {
	    return _model;
	}
	
	protected void setupUI() {
        setLayout(new CardLayout());
        
        _panelDevice = setupPanelDevice();
        _panelNoDevice = setupPanelNoDevice();
        
        add(_panelDevice, DEVICE);
        add(_panelNoDevice, NO_DEVICE);
    }
	
	private JPanel setupPanelDevice() {
		JPanel panel = new JPanel(new BorderLayout());
		
		ImagePanel header = new ImagePanel(loadImageIcon("device_explorer_background.jpg").getImage());
		header.setSize(400, 100);
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
		_list.setPrototypeCellValue(new FileDescriptor(0, DeviceConstants.FILE_TYPE_AUDIO, "", "", "", "", "", 0));
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
	
	private ImageRadioButton setupButtonType(JPanel container, final int type) {
	    ImageRadioButton button = new ImageRadioButton();
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
		
		Font font = button.getFont();
		button.setFont(new Font(font.getName(), font.getStyle() | Font.BOLD, font.getSize() + 4));
		
		container.add(button);
		
		return button;
	}
	
	private ImageIcon loadImageIcon(String name) {
		String path = "images" + File.separator + name + ".png";
		
		if (name.endsWith(".jpg")) {
			path = "images" + File.separator + name;
		}
		
	    URL url = getClass().getResource(path);
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
