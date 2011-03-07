package com.frostwire.gnutella.gui.skin;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;

public class SkinCheckBoxMenuItem extends JCheckBoxMenuItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5903015325829909427L;

	public SkinCheckBoxMenuItem(Action a) {
		super(a);
	}
	
	public SkinCheckBoxMenuItem(String text, boolean b) {
		super(text, b);
	}
}
