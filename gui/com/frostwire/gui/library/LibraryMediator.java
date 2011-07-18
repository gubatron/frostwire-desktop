package com.frostwire.gui.library;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.util.DividerLocationSettingUpdater;
import com.limegroup.gnutella.settings.UISettings;

public class LibraryMediator {
    
    private static final String FILES_TABLE_KEY = "LIBRARY_FILES_TABLE";
    private static final String PLAYLISTS_TABLE_KEY = "LIBRARY_PLAYLISTS_TABLE";

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
    
    private CardLayout _tablesViewLayout = new CardLayout();
    private JPanel _tablesPanel;

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
        JPanel panel = new JPanel(new BorderLayout());
        
        _tablesViewLayout = new CardLayout();
        _tablesPanel = new JPanel(_tablesViewLayout);
        
        _tablesPanel.add(LibraryFilesTableMediator.instance().getScrolledTablePane(), FILES_TABLE_KEY);
        _tablesPanel.add(LibraryPlaylistsTableMediator.instance().getScrolledTablePane(), PLAYLISTS_TABLE_KEY);
        
        panel.add(new LibrarySearch(), BorderLayout.PAGE_START);
        panel.add(_tablesPanel, BorderLayout.CENTER);
        panel.add(new LibraryPlayer(), BorderLayout.PAGE_END);

        return panel;
    }
}
