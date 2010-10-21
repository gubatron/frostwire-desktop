package com.limegroup.gnutella.gui.menu;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.GUIMediator.Tabs;

/**
 * The menu item that actually displays the options for dynamically
 * showing or hiding tabs.
 */
final class ShowHideMenu extends AbstractMenu {
    

    /**
     * Constructs all of the elements of the <tt>ViewMenu</tt>, in particular
     * the check box menu items and listeners for the various tabs displayed
     * in the main window.
     *
     * @param key the key allowing the <tt>AbstractMenu</tt> superclass to
     *  access the appropriate locale-specific string resources
     */
    ShowHideMenu() {
        super(I18n.tr("Sho&w/Hide"));
        initializeMenu();
    }

    private void initializeMenu() {
        for (Tabs tab : Tabs.getOptionalTabs()) {
        	if (tab == Tabs.FROSTCLICK)
        		continue;
        	addToggleMenuItem(tab.getShowTabAction(), tab.isViewEnabled());
        }
        
        MENU.add(new SearchMenu().getMenu());
    }
    
    public void refreshMenu() {
    	MENU.removeAll();
    	initializeMenu();
    }
}
