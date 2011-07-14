package com.limegroup.gnutella.gui.search;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.ImageIcon;


public class IconSearchField extends SearchField {

	private static final long serialVersionUID = -2691587080740060305L;
	private Icon _icon;
	
	private int _iconWidth;
	private int _iconHeight;
	
	private Dimension _outerDimensions;

	public IconSearchField() {
		super();
	}
	
	public IconSearchField(int columns, Icon icon) {
		super(columns);
		setIcon(icon);
		
		SearchField dummy = new SearchField(columns);
		_outerDimensions = dummy.getSize();
		
	}
	
	public void setIcon(Icon icon) {
		_icon = icon;
		_iconWidth = _icon.getIconWidth();
		_iconHeight = _icon.getIconHeight();
	}
	
	public Icon getIcon() {
		return _icon;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int rightTextMargin = 2;
		
		if (_outerDimensions.width == 0) {
			_outerDimensions.width = this.getWidth();
			_outerDimensions.height = this.getHeight();
		}
		
		if (_icon != null) {
			int icon_x = _outerDimensions.width - (_iconWidth) + 25;
			int icon_y = (getHeight() - _iconHeight)/2;

			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			        RenderingHints.VALUE_ANTIALIAS_ON);

			    g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
			        RenderingHints.VALUE_RENDER_QUALITY);
			
			//calculate size so that it won't be bigger than the text field.
			int proportion = _iconHeight/_iconWidth;
			int icon_height = getHeight() - (Math.abs(getHeight()-_iconHeight));
			int icon_width = icon_height/proportion;

			//draw it.
			g2d.drawImage(((ImageIcon)_icon).getImage(), icon_x+3, icon_y+3, icon_width-3, icon_height-3, this);
			
			rightTextMargin += _iconWidth+5;
		}
		
		setMargin(new Insets(2, 2, 2, rightTextMargin));
	}
}
