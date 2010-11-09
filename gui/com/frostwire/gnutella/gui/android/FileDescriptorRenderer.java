package com.frostwire.gnutella.gui.android;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.limegroup.gnutella.gui.mp3.AudioSource;
import com.limegroup.gnutella.gui.mp3.LimeWirePlayer;

public class FileDescriptorRenderer extends JPanel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8978172363548823491L;
	
	private FileDescriptor _fileDescriptor;
	
	private JLabel _label;
	private JButton _buttonPlay;
	private JButton _buttonCopy;
	
	private LimeWirePlayer _player;
	
	public FileDescriptorRenderer() {
		
		_player = new LimeWirePlayer();
		
		_label = new JLabel();
		add(_label);
		
		_buttonPlay = new JButton("Play");
		_buttonPlay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				buttonPlay_mouseClicked(e);
			}
		});
		add(_buttonPlay);
		
		_buttonCopy = new JButton("Copy");
		_buttonCopy.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				buttonCopy_mouseClicked(e);
			}
		});
		add(_buttonCopy);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		setComponentOrientation(list.getComponentOrientation());
		
		_fileDescriptor = (FileDescriptor) value;
		
		if (isSelected) {
		    setBackground(Color.BLUE);
		    setForeground(Color.WHITE);
		}
		else {
		    setBackground(Color.WHITE);
		    setForeground(Color.BLACK);
		}
		
		_label.setText(_fileDescriptor.title);
		
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
	
	protected void buttonPlay_mouseClicked(MouseEvent e) {
		Device device = AndroidMediator.instance().getDeviceBar().getSelectedDevice();
		
		if (device != null && _fileDescriptor != null) {
			play(device, _fileDescriptor);
		}
	}
	
	protected void buttonCopy_mouseClicked(MouseEvent e) {
		
		Device device = AndroidMediator.instance().getDeviceBar().getSelectedDevice();
		LocalFile localFile = AndroidMediator.instance().getDesktopExplorer().getSelectedFolder();
		if (device != null && localFile != null && _fileDescriptor != null) {
			AndroidMediator.addActivity(new CopyToDesktopActivity(device, localFile, new FileDescriptor[] { _fileDescriptor }));
		}
	}
	
	private void play(Device device, FileDescriptor fileDescriptor) {
		URL url = device.getDownloadURL(fileDescriptor.fileType, fileDescriptor.id);
		_player.loadSong(new AudioSource(url));
		_player.playSong();
	}
}
