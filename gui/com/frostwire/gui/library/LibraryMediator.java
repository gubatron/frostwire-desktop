package com.frostwire.gui.library;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.frostwire.alexandria.Library;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.util.DividerLocationSettingUpdater;
import com.limegroup.gnutella.settings.LibrarySettings;
import com.limegroup.gnutella.settings.UISettings;

public class LibraryMediator {
    
    public static final String FILES_TABLE_KEY = "LIBRARY_FILES_TABLE";
    public static final String PLAYLISTS_TABLE_KEY = "LIBRARY_PLAYLISTS_TABLE";

    private static JPanel MAIN_PANEL;

    /**
     * Singleton instance of this class.
     */
    private static LibraryMediator INSTANCE;
    
    private LibraryFiles _libraryFiles;
    
    private Library _library;

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
        GUIMediator.setSplashScreenString(I18n.tr("Loading Library Window..."));
        
        _library = new Library(LibrarySettings.LIBRARY_DATABASE);
        
        getComponent(); // creates MAIN_PANEL

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getLibraryLeftPanel(), getLibraryRightPanel());
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);

        splitPane.setDividerSize(2);
        
        DividerLocationSettingUpdater.install(splitPane, UISettings.UI_LIBRARY_MAIN_DIVIDER_LOCATION);

        MAIN_PANEL.add(splitPane);
        
        _libraryFiles.setInitialSelection();
    }

    public JComponent getComponent() {
        if (MAIN_PANEL == null) {
            MAIN_PANEL = new JPanel(new BorderLayout());
        }
        return MAIN_PANEL;
    }
    
    public void showView(String key) {
        _tablesViewLayout.show(_tablesPanel, key);
    }
    
    public void updateTableFiles(DirectoryHolder dirHolder) {
        LibraryFilesTableMediator.instance().updateTableFiles(dirHolder);
        showView(FILES_TABLE_KEY);
    }
    
    public void clearLibraryTable() {
        LibraryFilesTableMediator.instance().clearTable();
    }
    
    public void addFilesToLibraryTable(List<File> files) {
        for (File file : files) {
            LibraryFilesTableMediator.instance().add(file);
        }
    }

    private JComponent getLibraryLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        _libraryFiles = new LibraryFiles();

        panel.add(_libraryFiles, BorderLayout.PAGE_START);
        panel.add(new LibraryPlaylists(), BorderLayout.CENTER);
        panel.add(new LibraryCoverArt(), BorderLayout.PAGE_END);
        
        
        Dimension size = panel.getSize();
        panel.setMinimumSize(new Dimension(120,size.height));
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
        
        Dimension size = panel.getSize();
        panel.setMinimumSize(new Dimension(GUIMediator.getAppSize().width-300,size.height));

        return panel;
    }
}
