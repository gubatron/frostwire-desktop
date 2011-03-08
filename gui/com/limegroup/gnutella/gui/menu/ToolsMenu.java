package com.limegroup.gnutella.gui.menu;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.limewire.i18n.I18nMarker;

import com.frostwire.actions.ConnectionDoctorAction;
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
	    
	    addMenuItem(new CheckForUpdatesAction());
        addMenuItem(ConnectionDoctorAction.getInstance());
	    
        //if (!OSUtils.isMacOSX()) {
        addMenuItem(new ShowOptionsAction());
        //}
        // MENU.add(new AdvancedMenu().getMenu()); //FTA: Mojito menu disabled for release
    }
	
	
	
	private static class CheckForUpdatesAction extends AbstractAction {
	    /**
         * 
         */
        private static final long serialVersionUID = -4544415000859745587L;
        private final long TIME_BETWEEN_CHECKS = 1000 * 300; //5 minutes
	    private long lastTimeWeChecked = 0;
	    private Thread enabler;
	    
	    public CheckForUpdatesAction() {
	        super(I18n.tr("&Check for updates"));
	        putValue(LONG_DESCRIPTION, 
                     I18nMarker.marktr("Check for new FrostWire updates or automatic configuration updates"));
	    }

	    public void actionPerformed(ActionEvent e) {
	        long now = new java.util.Date().getTime();
	        
	        lastTimeWeChecked = now;
	        com.frostwire.updates.UpdateManager.scheduleUpdateCheckTask(0);


            setEnabled(false);
	        
	        //when this thread is done, it'll renable this action, like a simple timer.
	        enabler = new Thread() {
	            public void run() {
	                long now = new java.util.Date().getTime();
	                int secsLeft = (int) ((TIME_BETWEEN_CHECKS - (now - lastTimeWeChecked))/1000);
	                long endTime = now + TIME_BETWEEN_CHECKS;
	                
	                try {
	                    while (now < endTime) {
	                        now = new java.util.Date().getTime();
	                        putValue(Action.NAME,I18n.tr("&Check for updates") +" ("+secsLeft+" " +I18n.tr("seconds left)"));
	                        secsLeft = (int) ((TIME_BETWEEN_CHECKS - (now - lastTimeWeChecked))/1000);
	                        sleep(500);
	                    }
	                    
	                } catch(Exception e) {}	                
	                putValue(Action.NAME,I18n.tr("&Check for updates"));
	                setEnabled(true);
	            }
	        };
	        enabler.start();
	    }
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
