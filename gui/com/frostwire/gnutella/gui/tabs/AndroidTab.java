package com.frostwire.gnutella.gui.tabs;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.frostwire.gnutella.gui.android.AndroidMediator;
import com.frostwire.gnutella.gui.android.PeerDiscoveryClerk;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.tabs.AbstractTab;
import com.limegroup.gnutella.settings.ApplicationSettings;

public final class AndroidTab extends AbstractTab {
	
	/**
	 * Constant for the <tt>Component</tt> instance containing the 
	 * elements of this tab.
	 */
	private static JComponent COMPONENT;
	private static JPanel PANEL = new JPanel(new BorderLayout());

	public AndroidTab(AndroidMediator androidMediator) {
		super(I18n.tr("Phone and Tablets"), I18n.tr("Show the phone and tablets"), "chat_tab");
	
		COMPONENT = androidMediator.getComponent();
		PANEL.add(COMPONENT);
		
		PeerDiscoveryClerk clerk = new PeerDiscoveryClerk();
		clerk.start();
	}
	
	public void storeState(boolean visible) {
    	ApplicationSettings.ANDROID_VIEW_ENABLED.setValue(visible);
	}
	
	public JComponent getComponent() {
		return PANEL;
	}
}
