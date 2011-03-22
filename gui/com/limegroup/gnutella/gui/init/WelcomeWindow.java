package com.limegroup.gnutella.gui.init;

import javax.swing.Icon;

import org.limewire.i18n.I18nMarker;

import com.limegroup.gnutella.gui.GUIMediator;

/**
 * this class displays information welcoming the user to the
 * setup wizard.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
final class WelcomeWindow extends SetupWindow {
    
	/**
     * 
     */
    private static final long serialVersionUID = -5102133230630399469L;

    /**
	 * Creates the window and its components
	 */
	WelcomeWindow(SetupManager manager, boolean partial) {
		super(manager, I18nMarker.marktr("Welcome"), partial ?
		    I18nMarker
                    .marktr("Welcome to the FrostWire setup wizard. FrostWire has recently added new features that require your configuration. FrostWire will guide you through a series of steps to configure these new features.") : I18nMarker
                    .marktr("Welcome to the FrostWire setup wizard. FrostWire will guide you through a series of steps to configure FrostWire for optimum performance."));
	}
	
	public Icon getIcon() {
		return GUIMediator.getThemeImage("logo");
	}
}
