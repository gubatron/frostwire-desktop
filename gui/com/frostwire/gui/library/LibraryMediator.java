package com.frostwire.gui.library;

import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;

public class LibraryMediator implements ThemeObserver {

    private static JPanel MAIN_PANEL;

    /**
     * Singleton instance of this class.
     */
    private static LibraryMediator INSTANCE;

    /**
     * @return the <tt>LibraryMediator</tt> instance
     */
    public static LibraryMediator instance() {
        if (INSTANCE == null) {
            INSTANCE = new LibraryMediator();
        }
        return INSTANCE;
    }

    public LibraryMediator() {
        getComponent(); // creates MAIN_PANEL
        GUIMediator.setSplashScreenString(I18n.tr("Loading Library Window..."));
        ThemeMediator.addThemeObserver(this);
    }

    public JComponent getComponent() {
        if (MAIN_PANEL == null) {
            MAIN_PANEL = new JPanel(new GridBagLayout());
        }
        return MAIN_PANEL;
    }

    public void updateTheme() {
    }
}
