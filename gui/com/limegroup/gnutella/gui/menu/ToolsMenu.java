package com.limegroup.gnutella.gui.menu;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.limewire.i18n.I18nMarker;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.AbstractAction;

/**
 * Contains all of the menu items for the tools menu.
 */
final class ToolsMenu extends AbstractMenu {
    
    /**
	 * Creates a new <tt>ToolsMenu</tt>, using the <tt>key</tt> 
	 * argument for setting the locale-specific title and 
	 * accessibility text.
	 *
	 * @param key the key for locale-specific string resources unique
	 *            to the menu
	 */
	ToolsMenu() {
	    super(I18n.tr("&Tools"));
        addMenuItem(new ShowOptionsAction());
    }
	
	
	
	private static class ShowOptionsAction extends AbstractAction {
	    
	    /**
         * 
         */
        private static final long serialVersionUID = 6187597973189408647L;

        public ShowOptionsAction() {
	        super(I18n.tr("&Options"));
	        putValue(LONG_DESCRIPTION, I18nMarker.marktr("Display the Options Screen"));
        }
	    
	    public void actionPerformed(ActionEvent e) {
	        GUIMediator.instance().setOptionsVisible(true);
	    }
	}
}
