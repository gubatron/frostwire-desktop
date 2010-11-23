package com.frostwire.gnutella.gui;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class BackgroundImageJPanel extends JPanel {
	private Image _background;

	public BackgroundImageJPanel(Image backgroundImage) {
		_background = backgroundImage;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if (_background != null) {
			g.drawImage(_background,0,0,null);
		}
	}
}
