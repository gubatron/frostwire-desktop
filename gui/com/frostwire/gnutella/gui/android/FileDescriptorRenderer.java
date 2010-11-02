package com.frostwire.gnutella.gui.android;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
				if (_fileDescriptor != null && _fileDescriptor.device != null) {
					play(_fileDescriptor);
				}
			}
		});
		add(_buttonPlay);
		
		_buttonCopy = new JButton("Copy");
		_buttonCopy.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (_fileDescriptor != null && _fileDescriptor.device != null) {
					copy(_fileDescriptor);
				}
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
	
	private void play(FileDescriptor fileDescriptor) {
		
		URL url = fileDescriptor.device.getDownloadURL(fileDescriptor.fileType, fileDescriptor.id);
		_player.loadSong(new AudioSource(url));
		_player.playSong();
		
	}
	
	private void copy(FileDescriptor fileDescriptor) {
		
		if (AndroidMediator.SELECTED_DESKTOP_FOLDER == null) {
			return;
		}
		
		URL url = fileDescriptor.device.getDownloadURL(fileDescriptor.fileType, fileDescriptor.id);
		
		try {
			InputStream is = url.openStream();
			
			File file = new File(AndroidMediator.SELECTED_DESKTOP_FOLDER.getFile(), fileDescriptor.fileName);
			
			FileOutputStream fos = new FileOutputStream(file);
			
			byte[] buffer = new byte[4 * 1024];
			int n = 0;
			
			while ((n = is.read(buffer, 0, buffer.length)) != -1) {
				fos.write(buffer, 0, n);
			}
			
			fos.close();
			is.close();
			
			AndroidMediator.SELECTED_DESKTOP_FOLDER.refresh();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
