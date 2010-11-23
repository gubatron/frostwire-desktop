package com.frostwire.gnutella.gui.android;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.frostwire.gnutella.gui.ImagePanel;
import com.limegroup.gnutella.gui.mp3.AudioSource;
import com.limegroup.gnutella.gui.mp3.LimeWirePlayer;

public class FileDescriptorRenderer extends JPanel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8978172363548823491L;
	
	private static final Color COLOR_LIGHTER_BLUE = new Color(0xc4, 0xee, 0xfe);
	private static String[] BYTE_UNITS = new String[] { "b", "KB", "Mb", "Gb", "Tb" };
	
	private ImagePanel _imagePanel;
	private JLabel _labelTitle;
	private JLabel _labelSize;
	private JLabel _labelExtra;
	private JButton _buttonPlay;
	private JButton _buttonCopy;
	
	private FileDescriptor _fileDescriptor;
	private LimeWirePlayer _player;
	private Image _image;
	
	public FileDescriptorRenderer() {
		_player = new LimeWirePlayer();
		setupUI();
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		setComponentOrientation(list.getComponentOrientation());
		
		_fileDescriptor = (FileDescriptor) value;
		
		if (isSelected) {
		    setBackground(COLOR_LIGHTER_BLUE);
		}
		else {
		    setBackground(Color.WHITE);
		}
		
		_imagePanel.setImage(loadImage());
		_labelTitle.setText(_fileDescriptor.title);
		_labelSize.setText(getBytesInHuman(_fileDescriptor.fileSize));
		
		if (_fileDescriptor.fileType == DeviceConstants.FILE_TYPE_AUDIO ||
		    _fileDescriptor.fileType == DeviceConstants.FILE_TYPE_APPLICATIONS) {
		    _labelExtra.setText(_fileDescriptor.artist);
		}
		
		if (_fileDescriptor.fileType == DeviceConstants.FILE_TYPE_AUDIO) {
			_buttonPlay.setVisible(true);
		} else {
			_buttonPlay.setVisible(false);
		}
		
		return this;
	}
	
	public Component getComponentAt(int x, int y) {
		for (int i = 0; i < getComponentCount(); i++)
            if (getComponent(i).getBounds().contains(x, y))
                return getComponent(i);
        
        return null;
    }
	
	protected void setupUI() {
	    setLayout(new GridBagLayout());
	    setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
	    
	    GridBagConstraints c;
	    
	    _imagePanel = new ImagePanel();
	    Dimension size = new Dimension(32, 32);
	    _imagePanel.setPreferredSize(size);
	    _imagePanel.setMinimumSize(size);
	    _imagePanel.setMaximumSize(size);
	    _imagePanel.setSize(size);
	    c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        add(_imagePanel, c);
        
        _labelTitle = new JLabel();
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridwidth = 3;
        add(_labelTitle, c);
        
        _labelSize = new JLabel();
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        add(_labelSize, c);
        
        _labelExtra = new JLabel();
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        add(_labelExtra, c);
        
        _buttonPlay = new JButton("Play");
        _buttonPlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                buttonPlay_mouseReleased(e);
            }
        });
        c = new GridBagConstraints();
        c.gridx = 4;
        c.gridy = 0;
        c.gridheight = 2;
        add(_buttonPlay, c);
        
        _buttonCopy = new JButton("Copy");
        _buttonCopy.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                buttonCopy_mouseReleased(e);
            }
        });
        c = new GridBagConstraints();
        c.gridx = 5;
        c.gridy = 0;
        c.gridheight = 2;
        add(_buttonCopy, c);
    }
	
	protected void buttonPlay_mouseReleased(MouseEvent e) {
		Device device = AndroidMediator.instance().getDeviceBar().getSelectedDevice();
		
		if (device != null && _fileDescriptor != null) {
			play(device, _fileDescriptor);
		}
	}
	
	protected void buttonCopy_mouseReleased(MouseEvent e) {
		
		Device device = AndroidMediator.instance().getDeviceBar().getSelectedDevice();
		File path = AndroidMediator.instance().getDesktopExplorer().getSelectedFolder();
		if (device != null && path != null && _fileDescriptor != null) {
			AndroidMediator.addActivity(new CopyToDesktopTask(device, path, new FileDescriptor[] { _fileDescriptor }));
		}
	}
	
	private void play(Device device, FileDescriptor fileDescriptor) {
		URL url = device.getDownloadURL(fileDescriptor.fileType, fileDescriptor.id);
		_player.loadSong(new AudioSource(url));
		_player.playSong();
	}
	
	private Image loadImage() {
	    
	    if (_image != null) {
	        return _image;
	    }
	        
        String path = "images" + File.separator + getImageName(_fileDescriptor.fileType) + ".png";
        
        URL url = getClass().getResource(path);
        try {
            return _image = ImageIO.read(url).getScaledInstance(24, 24, Image.SCALE_SMOOTH);
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
	
	private static String getBytesInHuman(long size) {

        int i = 0;
        float fSize = (float) size;

        for (i = 0; size > 1024; i++) {
            size /= 1024;
            fSize = fSize / 1024f;
        }

        return String.format("%.2f ", fSize) + BYTE_UNITS[i];
    }
}
