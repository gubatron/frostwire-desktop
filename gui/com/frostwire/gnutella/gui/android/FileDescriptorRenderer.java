package com.frostwire.gnutella.gui.android;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ListCellRenderer;

import com.frostwire.gnutella.gui.ImagePanel;
import com.limegroup.gnutella.gui.mp3.AudioSource;
import com.limegroup.gnutella.gui.mp3.LimeWirePlayer;
import com.limegroup.gnutella.gui.mp3.PlayerState;

public class FileDescriptorRenderer extends JPanel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8978172363548823491L;
	
	private static final Color COLOR_LIGHTER_BLUE = new Color(0xc4, 0xee, 0xfe);
	private static String[] BYTE_UNITS = new String[] { "b", "KB", "Mb", "Gb", "Tb" };
	
	private static LimeWirePlayer PLAYER = new LimeWirePlayer();
	private static FileDescriptor LAST_PLAY;
    private static Map<Integer, Image> IMAGE_TYPES = new HashMap<Integer, Image>();
    private static Image IMAGE_COPY;
    private static Image IMAGE_PLAY;
    private static Image IMAGE_STOP;
	
	private ImagePanel _imagePanel;
	private JLabel _labelTitle;
	private JLabel _labelSize;
	private JLabel _labelExtra;
	private JRadioButton _buttonPlay;
	private JRadioButton _buttonCopy;
	
	private FileDescriptor _fileDescriptor;
	
	public FileDescriptorRenderer() {
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
		
		_imagePanel.setImage(loadImageType(_fileDescriptor.fileType));
		_labelTitle.setText(_fileDescriptor.title);
		_labelSize.setText(getBytesInHuman(_fileDescriptor.fileSize));
		
		if (_fileDescriptor.fileType == DeviceConstants.FILE_TYPE_AUDIO ||
		    _fileDescriptor.fileType == DeviceConstants.FILE_TYPE_APPLICATIONS) {
		    _labelExtra.setText(_fileDescriptor.artist);
		}
		
		if (_fileDescriptor.fileType == DeviceConstants.FILE_TYPE_AUDIO) {
			_buttonPlay.setVisible(true);
			_buttonPlay.setIcon(new ImageIcon(_fileDescriptor.equals(LAST_PLAY) ? loadImageStop() : loadImagePlay()));
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
	    Dimension size = new Dimension(24, 24);
	    _imagePanel.setPreferredSize(size);
	    _imagePanel.setMinimumSize(size);
	    _imagePanel.setMaximumSize(size);
	    _imagePanel.setSize(size);
	    c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5, 5, 0, 5);
        add(_imagePanel, c);
        
        _labelTitle = new JLabel();
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(5, 0, 0, 0);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 3;
        add(_labelTitle, c);
        
        _labelSize = new JLabel();
        _labelSize.setForeground(new Color(0xe9, 0xa5, 0x4c));
        _labelSize.setAlignmentY(0.5f);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 3, 5);
        add(_labelSize, c);
        
        _labelExtra = new JLabel();
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 3, 0);
        add(_labelExtra, c);
        
        _buttonCopy = new JRadioButton();
        _buttonCopy.setBorder(null);
        _buttonCopy.setBackground(null);
        _buttonCopy.setFocusable(false);
        _buttonCopy.setFocusPainted(false);
        _buttonCopy.setContentAreaFilled(false);
        _buttonCopy.setIcon(new ImageIcon(loadImageCopy()));
        _buttonCopy.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                buttonCopy_mouseReleased(e);
            }
        });
        c = new GridBagConstraints();
        c.gridx = 4;
        c.gridy = 0;
        c.gridheight = 2;
        c.insets = new Insets(0, 0, 0, 5);
        add(_buttonCopy, c);
        
        _buttonPlay = new JRadioButton();
        _buttonPlay.setBorder(null);
        _buttonPlay.setBackground(null);
        _buttonPlay.setFocusable(false);
        _buttonPlay.setFocusPainted(false);
        _buttonPlay.setContentAreaFilled(false);
        _buttonPlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                buttonPlay_mouseReleased(e);
            }
        });
        c = new GridBagConstraints();
        c.gridx = 5;
        c.gridy = 0;
        c.gridheight = 2;
        c.insets = new Insets(0, 0, 0, 5);
        add(_buttonPlay, c);
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
		List<FileDescriptor> fileDescriptors = AndroidMediator.instance().getDeviceExplorer().getSelectedFileDescriptors();
		if (device != null && path != null && _fileDescriptor != null) {
			AndroidMediator.addActivity(new CopyToDesktopTask(device, path, fileDescriptors.toArray(new FileDescriptor[0])));
		}
	}
	
	private static void play(Device device, FileDescriptor fileDescriptor) {
	    
	    if (PLAYER.getStatus() == PlayerState.PLAYING && fileDescriptor.equals(LAST_PLAY)) {
	        LAST_PLAY = null;
	        AndroidMediator.instance().getDeviceExplorer().getModel().update(fileDescriptor);
	        PLAYER.stop();
	    } else {
	        AndroidMediator.instance().getDeviceExplorer().getModel().update(fileDescriptor);
    		URL url = device.getDownloadURL(fileDescriptor.fileType, fileDescriptor.id);
    		LAST_PLAY = fileDescriptor;
    		PLAYER.loadSong(new AudioSource(url));
    		PLAYER.playSong();
	    }
	}
	
	private Image loadImageType(int type) {
	    
	    if (IMAGE_TYPES.containsKey(type)) {
	        return IMAGE_TYPES.get(type);
	    }
	        
        Image image = loadImage(getImageName(_fileDescriptor.fileType)).getScaledInstance(24, 24, Image.SCALE_SMOOTH);
         IMAGE_TYPES.put(type, image);
         
        return image;
    }
	
	private Image loadImageCopy() {
        if (IMAGE_COPY != null) {
            return IMAGE_COPY;
        }
        
        return IMAGE_COPY = loadImage("copy_device").getScaledInstance(32, 32, Image.SCALE_SMOOTH);
    }
	
	private Image loadImagePlay() {
	    if (IMAGE_PLAY != null) {
	        return IMAGE_PLAY;
	    }
	    
	    return IMAGE_PLAY = loadImage("play").getScaledInstance(32, 32, Image.SCALE_SMOOTH);
	}
	
	private Image loadImageStop() {
        if (IMAGE_STOP != null) {
            return IMAGE_STOP;
        }
        
        return IMAGE_STOP = loadImage("stop").getScaledInstance(32, 32, Image.SCALE_SMOOTH);
    }
	
	private Image loadImage(String name) {
        String path = "images" + File.separator + name + ".png";
        
        if (name.endsWith(".jpg")) {
            path = "images" + File.separator + name;
        }
        
        URL url = getClass().getResource(path);
        try {
            return ImageIO.read(url);
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
