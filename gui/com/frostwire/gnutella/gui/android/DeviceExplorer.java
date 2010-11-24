package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

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
	private ButtonGroup _buttonGroup;
	
	private JTextField _textFilter;

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
	
	public List<FileDescriptor> getSelectedFileDescriptors() {
        Object[] selectedValues = _list.getSelectedValues();
        
        if (selectedValues == null) {
            return new ArrayList<FileDescriptor>();
        }
        
        ArrayList<FileDescriptor> selectedFileDescriptors = new ArrayList<FileDescriptor>(selectedValues.length);
        for (int i = 0; i < selectedValues.length; i++) {
            selectedFileDescriptors.add((FileDescriptor) selectedValues[i]);
        }
        
        return selectedFileDescriptors;
    }
	
	protected void setupUI() {
        setLayout(new CardLayout());
        
        _panelDevice = setupPanelDevice();
        _panelNoDevice = setupPanelNoDevice();
        
        add(_panelDevice, DEVICE);
        add(_panelNoDevice, NO_DEVICE);
    }
	
	protected void textFilter_keyPressed(KeyEvent e) {
	    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
           _model.filter(_textFilter.getText());
        }
    }
	
	private JPanel setupPanelDevice() {
		JPanel panel = new JPanel(new BorderLayout());
		
		ImagePanel header = new ImagePanel(new ImageTool().load("device_explorer_background.jpg"));
		header.setLayout(new GridBagLayout());
		
		GridBagConstraints c;
		
		_buttonApplications = setupButtonType(DeviceConstants.FILE_TYPE_APPLICATIONS);
		c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5, 5, 0, 5);
        header.add(_buttonApplications, c);
        
		_buttonDocuments = setupButtonType(DeviceConstants.FILE_TYPE_DOCUMENTS);
		c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(5, 0, 0, 5);
        header.add(_buttonDocuments, c);
        
		_buttonPictures = setupButtonType(DeviceConstants.FILE_TYPE_PICTURES);
		c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.insets = new Insets(5, 0, 0, 5);
        header.add(_buttonPictures, c);
        
		_buttonVideos = setupButtonType(DeviceConstants.FILE_TYPE_VIDEOS);
		c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 0;
        c.insets = new Insets(5, 0, 0, 5);
        header.add(_buttonVideos, c);
        
		_buttonRingtones = setupButtonType(DeviceConstants.FILE_TYPE_RINGTONES);
		c = new GridBagConstraints();
        c.gridx = 4;
        c.gridy = 0;
        c.insets = new Insets(5, 0, 0, 5);
        header.add(_buttonRingtones, c);
        
		_buttonAudio = setupButtonType(DeviceConstants.FILE_TYPE_AUDIO);
		c = new GridBagConstraints();
        c.gridx = 5;
        c.gridy = 0;
        c.insets = new Insets(5, 0, 0, 5);
        header.add(_buttonAudio, c);
        
        _buttonGroup = new ButtonGroup();
        _buttonGroup.add(_buttonApplications);
        _buttonGroup.add(_buttonDocuments);
        _buttonGroup.add(_buttonPictures);
        _buttonGroup.add(_buttonVideos);
        _buttonGroup.add(_buttonRingtones);
        _buttonGroup.add(_buttonAudio);
        
        _textFilter = new JTextField();
        Dimension textFilterSize = new Dimension(100, 25);
        _textFilter.setPreferredSize(textFilterSize);
        _textFilter.setMinimumSize(textFilterSize);
        _textFilter.setMaximumSize(textFilterSize);
        _textFilter.setSize(textFilterSize);
        _textFilter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                textFilter_keyPressed(e);
            }
        });
        c = new GridBagConstraints();
        c.gridx = 6;
        c.gridy = 0;
        c.insets = new Insets(5, 0, 0, 5);
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 1.0;
        header.add(_textFilter, c);
        
		panel.add(header, BorderLayout.PAGE_START);
		
		_list = new JList(_model);
		_list.setCellRenderer(new FileDescriptorRenderer());
		_list.addMouseListener(new RedispatchMouseListener(_list));
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
	
	private ImageRadioButton setupButtonType(final int type) {	    
	    ImageTool imageTool = new ImageTool();
	    ImageRadioButton button = new ImageRadioButton();
		button.setIcon(new ImageIcon(imageTool.load(imageTool.getImageNameByFileType(type))));
		button.setPressedIcon(new ImageIcon(imageTool.load(imageTool.getImageNameByFileType(type) + "_checked")));
		button.setSelectedIcon(new ImageIcon(imageTool.load(imageTool.getImageNameByFileType(type) + "_checked")));
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				_model.clear();
				AndroidMediator.addActivity(new BrowseTask(_device, _model, type));
			}
		});
		
		Font font = button.getFont();
		button.setFont(new Font(font.getName(), font.getStyle() | Font.BOLD, font.getSize() + 4));
		
		return button;
	}
}
