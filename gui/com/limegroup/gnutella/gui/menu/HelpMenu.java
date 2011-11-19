package com.limegroup.gnutella.gui.menu;

import java.awt.event.ActionEvent;

import org.limewire.util.OSUtils;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.TipOfTheDayMediator;
import com.limegroup.gnutella.gui.actions.AbstractAction;
import com.limegroup.gnutella.gui.actions.OpenLinkAction;

/**
 * Handles all of the contents of the help menu in the menu bar.  This 
 * includes such items as the link to the "Using LimeWire" page of the
 * web site as well as links to the forum, faq, "tell a friend", etc.
 */
final class HelpMenu extends AbstractMenu {

    /**
     * Creates a new <tt>HelpMenu</tt>, using the <tt>key</tt> 
     * argument for setting the locale-specific title and 
     * accessibility text.
     *
     * @param key the key for locale-specific string resources unique
     *            to the menu
     */
    HelpMenu() {
        super(I18n.tr("Sharing &Help"));

        addMenuItem(new OpenLinkAction("http://www.frostwire.com/?id=faq", I18n.tr("&FAQ"), I18n.tr("Frequently Asked Questions for FrostWire")));

        addMenuItem(new OpenLinkAction("http://www.frostwire.com/?id=faq#fil7", I18n.tr("What is \"Seeding\"?"), I18n.tr("Learn about BitTorrent Seeding")));

        addSeparator();
        
        addMenuItem(new OpenLinkAction("http://www.frostwire.com/forum/viewforum.php?f=1", I18n.tr("Foru&m"), I18n.tr("Access the FrostWire Users\' Forum")));

        addMenuItem(new OpenLinkAction("http://www.frostclick.com/wp/?from=frostwire" + com.limegroup.gnutella.util.FrostWireUtils.getFrostWireVersion(),
                I18n.tr("FrostClick.com"), I18n.tr("Free Legal Downloads")));

        addMenuItem(new OpenLinkAction("http://www.frostwire.com/twitter", I18n.tr("Follow us on Twitter"), I18n.tr("Follow us on Twitter")));

        addSeparator();
        
        addMenuItem(new OpenLinkAction("http://frostwire.wordpress.com/2007/03/19/dont-get-scammed-frostwire-is-free/", I18n.tr("&Did you pay for FrostWire?"),
                I18n.tr("Did you pay for FrostWire? FrostWire is Free as in Free Beer. Avoid Scams.")));

        addMenuItem(new ShowTipOfTheDayAction());

        if (!OSUtils.isMacOSX()) {
            addSeparator();
            addMenuItem(new ShowAboutDialogAction());
        }
    }

    /**
     * Displays the TOTD window.
     */
    private static class ShowTipOfTheDayAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = -4964160055694967725L;

        public ShowTipOfTheDayAction() {
            super(I18n.tr("Tip of the &Day"));
            putValue(LONG_DESCRIPTION, I18n.tr("Show the Tip of the Day Window"));
        }

        public void actionPerformed(ActionEvent e) {
            TipOfTheDayMediator.instance().displayTipWindow();
        }
    }

    /**
     * Shows the about window with more information about the application.
     */
    private static class ShowAboutDialogAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = 2425666944873627828L;

        public ShowAboutDialogAction() {
            super(I18n.tr("&About FrostWire"));
            putValue(LONG_DESCRIPTION, I18n.tr("Information about FrostWire"));
        }

        public void actionPerformed(ActionEvent e) {
            GUIMediator.showAboutWindow();
        }
    }
}
