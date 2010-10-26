package com.frostwire.gnutella.gui.chat;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;

public class AndroidMediator implements ThemeObserver {

	/**
	 * Singleton instance of this class.
	 */
	private static final AndroidMediator INSTANCE = new AndroidMediator();
	
	/**
	 * The primary panel that contains all of the library elements.
	 */
	private static final JPanel MAIN_PANEL = new JPanel(new FlowLayout());
    
	/**
	 * @return the <tt>AndroidMediator</tt> instance
	 */
	public static AndroidMediator instance() { return INSTANCE; }
	
	private AndroidMediator() {
    	GUIMediator.setSplashScreenString(I18n.tr("Loading phone and tablets..."));
    	ThemeMediator.addThemeObserver(this);
    }

	@Override
	public void updateTheme() {
	}
	
	/**
	 * Returns the <tt>JComponent</tt> that contains all of the elements of
	 * the Chat.
	 *
	 * @return the <tt>JComponent</tt> that contains all of the elements of
	 * the Chat.
	 */
	public JComponent getComponent() {
		return MAIN_PANEL;
	}

	public static void handleNewDevice(String json) {
		INSTANCE.showMessage(json);
	}

	private void showMessage(String json) {
		MAIN_PANEL.add(new JLabel(json));
	}
}
