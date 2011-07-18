package com.frostwire.gui.library;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.util.DividerLocationSettingUpdater;
import com.limegroup.gnutella.settings.UISettings;

public class LibraryMediator {

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

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getLibraryLeftPanel(), getLibraryRightPanel());
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        DividerLocationSettingUpdater.install(splitPane, UISettings.UI_LIBRARY_MAIN_DIVIDER_LOCATION);

        MAIN_PANEL.add(splitPane);
    }

    public JComponent getComponent() {
        if (MAIN_PANEL == null) {
            MAIN_PANEL = new JPanel(new BorderLayout());
        }
        return MAIN_PANEL;
    }

    private JComponent getLibraryLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(new LibraryFiles(), BorderLayout.PAGE_START);
        panel.add(new LibraryPlaylists(), BorderLayout.CENTER);
        panel.add(new LibraryCoverArt(), BorderLayout.PAGE_END);

        return panel;
    }

    private JComponent getLibraryRightPanel() {
        JPanel panel = new JPanel();

        return panel;
    }
}
