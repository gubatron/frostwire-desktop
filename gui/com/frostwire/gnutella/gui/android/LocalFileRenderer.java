package com.frostwire.gnutella.gui.android;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class LocalFileRenderer extends JPanel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8723371611297478100L;
	
	private JLabel _label;
	
	public LocalFileRenderer() {
		_label = new JLabel();
		add(_label);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		LocalFile file = (LocalFile) value;
		
		_label.setText(file.getName());
		
		return this;
	}
}
