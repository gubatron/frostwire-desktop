package com.frostwire.gnutella.gui.tabs;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIMediator.Tabs;

/**
 * This class is put as a mouse adapter on the tabs so that the users
 * can quickly show and hide all the FrostWire Tabs with the exception
 * of the Search Tab.
 * 
 * To save processing and memory, this adapter and it's popup menu is built
 * only once. On click, all the visibilities are checked and the popup menu
 * is refreshed accordingly.
 * @author FTA, gubatron
 *
 */
public class TabRightClickAdapter extends MouseAdapter {
	private JPopupMenu _menu;
	private static TabRightClickAdapter INSTANCE = new TabRightClickAdapter();
	
	private TabRightClickAdapter() {
		initializeMenu();
	}

	private final void initializeMenu() {
		_menu = new JPopupMenu();
		
		for (Tabs tab : Tabs.getOptionalTabs() ) {
			JCheckBoxMenuItem menuItem = 
				new JCheckBoxMenuItem(tab.getShowTabAction());
			menuItem.setState(tab.isViewEnabled());
			_menu.add(menuItem);
		}
	}
	
	
	/*
	 * Iterate through all the elements of the menu and refresh if they should
	 * be checked or not. 
	 * 
	 * NOTE: Uses _menu_items hashmap to get the references of the
	 * JPopupMenu JCheckBoxMenuItems. Couldn't find a way to do
	 * popupMenu.get(key) -> JMenuItem. Only a numeric index, and I already had
	 * mapped the actual tabs to names, so we used the same keys for both the tabs
	 * and the menu items. 
	 */
	private final void refreshMenu() {
		_menu.removeAll();
		initializeMenu();
	}
	
	public final static TabRightClickAdapter getInstance() {
		return INSTANCE;
	}
	
	public void mouseClicked(MouseEvent me) {
		if (me.getButton() != MouseEvent.BUTTON1) {
			refreshMenu();
            _menu.pack();
            _menu.show(me.getComponent(), me.getX(), me.getY());
        }
	}
}
