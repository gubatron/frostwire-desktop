package com.frostwire.gui.tabs;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.frostwire.gui.ChatMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.ApplicationSettings;
/**
 * This class contains access to the chat tab properties.
 */
public final class ChatTab extends AbstractTab {

	/**
	 * Constant for the <tt>Component</tt> instance containing the 
	 * elements of this tab.
	 */
	private static JComponent COMPONENT;
	private static JPanel PANEL = new JPanel(new BorderLayout());
	
	/**
	 * Construcs the connections tab.
	 *
	 * @param CHAT_MEDIATOR the <tt>ChatMediator</tt> instance
	 */
	public ChatTab(final ChatMediator CHAT_MEDIATOR) {
	//public ChatTab(final ChatMediator cm) {
		super(I18n.tr("Chat"),
		        I18n.tr("Show our community chat"), "chat_tab");
		//lime style
		COMPONENT = CHAT_MEDIATOR.getComponent();
		PANEL.add(COMPONENT);
		//CHAT_MEDIATOR = cm;		
		//PANEL.add(cm.getComponent());
	}

	public JComponent getComponent() {
		return PANEL;
	}
}
