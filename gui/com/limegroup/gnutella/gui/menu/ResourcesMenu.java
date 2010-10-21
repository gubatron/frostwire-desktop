package com.limegroup.gnutella.gui.menu;

import com.limegroup.gnutella.gui.I18n;
//import com.limegroup.gnutella.gui.actions.OpenLinkAction; //old
//import com.limegroup.gnutella.gui.actions.TestDrAction;

/**
 * Contains all of the menu items for the resources menu.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
final class ResourcesMenu extends AbstractMenu {

	/**
	 * Creates a new <tt>ResourcesMenu</tt>, using the <tt>key</tt> 
	 * argument for setting the locale-specific title and 
	 * accessibility text.
	 *
	 * @param key the key for locale-specific string resources unique
	 *            to the menu
	 */
	ResourcesMenu() {
	    super(I18n.tr("&Resources"));
		//addMenuItem(new TestDrAction("http://www.frostwire.com", 
		//        I18n.tr("Try connection Fix"),
		//        I18n.tr("Test menu for connection fix")));
	}
}
