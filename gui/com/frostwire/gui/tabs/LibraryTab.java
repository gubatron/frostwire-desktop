package com.frostwire.gui.tabs;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.frostwire.gui.library.LibraryMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.ApplicationSettings;

/**
 * This class handles access to the tab that contains the library
 * as well as the playlist to the user.
 */
public final class LibraryTab extends AbstractTab {

    /**
     * Constant for the <tt>Component</tt> instance containing the 
     * elements of this tab.
     */
    private static JComponent COMPONENT;
    private static JPanel PANEL;

    private static LibraryMediator LIBRARY_MEDIATOR;
    
    /**
     * Constructs the elements of the tab.
     *
     * @param LIBRARY_MEDIATOR the <tt>LibraryMediator</tt> instance 
     * @param PLAYLIST_MEDIATOR the <tt>PlayListMediator</tt> instance 
     */
    public LibraryTab(LibraryMediator lm) {
        super(I18n.tr("Library"), I18n.tr("View Repository of Saved Files"), "library_tab");
        LIBRARY_MEDIATOR = lm;
    }

    public JComponent getComponent() {
        return getPanel();
    }

    private static JPanel getPanel() {
        if (PANEL == null) {
            PANEL = createPanel();
        }
        return PANEL;
    }

    private static JPanel createPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        COMPONENT = LIBRARY_MEDIATOR.getComponent();

        panel.add(COMPONENT, BorderLayout.CENTER);

        panel.invalidate();
        panel.validate();

        return panel;
    }
}