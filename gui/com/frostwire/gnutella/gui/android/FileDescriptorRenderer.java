package com.frostwire.gnutella.gui.android;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class FileDescriptorRenderer extends JPanel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8978172363548823491L;
	
	private JLabel _label;
	
	public FileDescriptorRenderer() {
		_label = new JLabel();
		add(_label);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		FileDescriptor fileDescriptor = (FileDescriptor) value;
		
		if (isSelected) {
		    setBackground(Color.BLUE);
		    setForeground(Color.WHITE);
		}
		else {
		    setBackground(Color.WHITE);
		    setForeground(Color.BLACK);
		}
		
		_label.setText(fileDescriptor.title);
		
		return this;
	}
}
