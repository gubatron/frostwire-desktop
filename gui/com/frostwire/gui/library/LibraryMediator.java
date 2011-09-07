package com.frostwire.gui.library;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.frostwire.alexandria.Library;
import com.frostwire.alexandria.PlaylistItem;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.util.DividerLocationSettingUpdater;
import com.limegroup.gnutella.settings.LibrarySettings;
import com.limegroup.gnutella.settings.UISettings;

public class LibraryMediator {
    
    public static final String FILES_TABLE_KEY = "LIBRARY_FILES_TABLE";
    public static final String PLAYLISTS_TABLE_KEY = "LIBRARY_PLAYLISTS_TABLE";
    
    private static JPanel MAIN_PANEL;
    
    private LibraryPlaylists LIBRARY_PLAYLISTS;

    /**
     * Singleton instance of this class.
     */
    private static LibraryMediator INSTANCE;
    
    private LibraryFiles libraryFiles;
    private LibraryLeftPanel libraryLeftPanel;
    private LibrarySearch librarySearch;
    
    private static Library LIBRARY;

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
        
        getComponent(); // creates MAIN_PANEL

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getLibraryLeftPanel(), getLibraryRightPanel());
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.5);
        splitPane.addPropertyChangeListener(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                JSplitPane splitPane = (JSplitPane) evt.getSource();
                int current = splitPane.getDividerLocation();
                if (current > LibraryLeftPanel.MAX_WIDTH) {
                    splitPane.setDividerLocation(LibraryLeftPanel.MAX_WIDTH);
                } else if (current < LibraryLeftPanel.MIN_WIDTH) {
                    splitPane.setDividerLocation(LibraryLeftPanel.MIN_WIDTH);
                }
                
            }
        });
        
        DividerLocationSettingUpdater.install(splitPane, UISettings.UI_LIBRARY_MAIN_DIVIDER_LOCATION);

        MAIN_PANEL.add(splitPane);
    }
    
    public static Library getLibrary() {
        if (LIBRARY == null) {
            LIBRARY = new Library(LibrarySettings.LIBRARY_DATABASE);
        }
        return LIBRARY;
    }
    
    public LibraryFiles getLibraryFiles() {
        if (libraryFiles == null) {
            libraryFiles = new LibraryFiles();
        }
        return libraryFiles;
    }
    
    public LibraryPlaylists getLibraryPlaylists() {
        if (LIBRARY_PLAYLISTS == null) {
            LIBRARY_PLAYLISTS = new LibraryPlaylists();
        }
        return LIBRARY_PLAYLISTS;
    }
    
    public LibrarySearch getLibrarySearch() {
        if (librarySearch == null) {
            librarySearch = new LibrarySearch();
        }
        return librarySearch;
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
    
    public void updateTableItems(List<PlaylistItem> items) {
        LibraryPlaylistsTableMediator.instance().updateTableItems(items);
        showView(PLAYLISTS_TABLE_KEY);
    }
    
    public void clearLibraryTable() {
        LibraryFilesTableMediator.instance().clearTable();
        getLibrarySearch().clear();
    }
    
    public void addFilesToLibraryTable(List<File> files) {
        for (File file : files) {
            LibraryFilesTableMediator.instance().add(file);
        }
        getLibrarySearch().addResults(files.size());
    }
    
    public void addItemsToLibraryTable(List<PlaylistItem> items) {
        for (PlaylistItem item : items) {
            LibraryPlaylistsTableMediator.instance().add(item);
        }
        getLibrarySearch().addResults(items.size());
    }

    private JComponent getLibraryLeftPanel() {
        if (libraryLeftPanel == null) {
            libraryLeftPanel = new LibraryLeftPanel(getLibraryFiles(), getLibraryPlaylists(), new LibraryCoverArt());
        }
        return libraryLeftPanel;
    }

    private JComponent getLibraryRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        _tablesViewLayout = new CardLayout();
        _tablesPanel = new JPanel(_tablesViewLayout);
        
        _tablesPanel.add(LibraryFilesTableMediator.instance().getScrolledTablePane(), FILES_TABLE_KEY);
        _tablesPanel.add(LibraryPlaylistsTableMediator.instance().getScrolledTablePane(), PLAYLISTS_TABLE_KEY);
        
        panel.add(getLibrarySearch(), BorderLayout.PAGE_START);
        panel.add(_tablesPanel, BorderLayout.CENTER);
        panel.add(new LibraryPlayer(), BorderLayout.PAGE_END);
        

        return panel;
    }
}
