package com.limegroup.gnutella.gui.menu;

import com.limegroup.gnutella.gui.GUIMediator.Tabs;
import com.limegroup.gnutella.gui.I18n;

/**
 * Contains all of the menu items for the navigation menu.
 */
final class NavMenu extends AbstractMenu {
    
    /**
	 * Creates a new <tt>NavMenu</tt>, using the <tt>key</tt> 
	 * argument for setting the locale-specific title and 
	 * accessibility text.
	 *
	 * @param key the key for locale-specific string resources unique
	 *            to the menu
	 */
	NavMenu() {
	    super(I18n.tr("&Navigation"));
	
		for (Tabs tab : Tabs.values()) {
		    addMenuItem(tab.getNavigationAction());
        }
    }
}
