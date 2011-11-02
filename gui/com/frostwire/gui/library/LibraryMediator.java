package com.frostwire.gui.library;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.frostwire.alexandria.InternetRadioStation;
import com.frostwire.alexandria.Library;
import com.frostwire.alexandria.Playlist;
import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.alexandria.db.LibraryDatabase;
import com.frostwire.gui.library.LibraryInternetRadioTableMediator.AddRadioStationAction;
import com.frostwire.gui.player.AudioPlayer;
import com.frostwire.gui.player.AudioSource;
import com.frostwire.gui.player.InternetRadioAudioSource;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconButton;
import com.limegroup.gnutella.gui.options.ConfigureOptionsAction;
import com.limegroup.gnutella.gui.options.OptionsConstructor;
import com.limegroup.gnutella.gui.util.DividerLocationSettingUpdater;
import com.limegroup.gnutella.settings.LibrarySettings;
import com.limegroup.gnutella.settings.UISettings;

public class LibraryMediator {

    private static final String FILES_TABLE_KEY = "LIBRARY_FILES_TABLE";
    private static final String PLAYLISTS_TABLE_KEY = "LIBRARY_PLAYLISTS_TABLE";
    private static final String INTERNET_RADIO_TREE_KEY = "LIBRARY_INTERNET_RADIO_TREE";

    private static JPanel MAIN_PANEL;

    private LibraryPlaylists LIBRARY_PLAYLISTS;

    /**
     * Singleton instance of this class.
     */
    private static LibraryMediator INSTANCE;

    private LibraryFiles libraryFiles;
    private LibraryLeftPanel libraryLeftPanel;
    private LibrarySearch librarySearch;
    private LibraryCoverArt libraryCoverArt;

    private static Library LIBRARY;

    private CardLayout _tablesViewLayout = new CardLayout();
    private JPanel _tablesPanel;
    private JSplitPane splitPane;
    
    private Map<Object,Integer> scrollbarValues;
	private Object lastSelectedKey;
	private AbstractLibraryTableMediator<?, ?, ?> lastSelectedMediator;
	
	private Set<Integer> idScanned;
	private AddRadioStationAction addStationAction;

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
        GUIMediator.setSplashScreenString(I18n.tr("Loading Library Window..."));
        
        idScanned = new HashSet<Integer>();

        getComponent(); // creates MAIN_PANEL
        
        scrollbarValues =  new HashMap<Object, Integer>();

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getLibraryLeftPanel(), getLibraryRightPanel());
        splitPane.setContinuousLayout(true);
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
    
    protected Object getSelectedKey() {
		if (getSelectedPlaylist() != null) {
			return getSelectedPlaylist();
		} else {
			return getLibraryFiles().getSelectedDirectoryHolder();
		}
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

    /**
     * Returns null if none is selected.
     * @return
     */
    public Playlist getSelectedPlaylist() {
        return getLibraryPlaylists().getSelectedPlaylist();
    }

    public LibrarySearch getLibrarySearch() {
        if (librarySearch == null) {
            librarySearch = new LibrarySearch();
        }
        return librarySearch;
    }

    public LibraryCoverArt getLibraryCoverArt() {
        if (libraryCoverArt == null) {
            libraryCoverArt = new LibraryCoverArt();
        }
        return libraryCoverArt;
    }

    public JComponent getComponent() {
        if (MAIN_PANEL == null) {
            MAIN_PANEL = new JPanel(new BorderLayout());
        }
        return MAIN_PANEL;
    }

    public void showView(String key) {
        rememberScrollbarsOnMediators(key);
        _tablesViewLayout.show(_tablesPanel, key);        
    }
    
    private void rememberScrollbarsOnMediators(String key) {
        AbstractLibraryTableMediator<?, ?, ?> tableMediator = null;
        AbstractLibraryListPanel listPanel = null;

        if (key.equals(FILES_TABLE_KEY)) {
            tableMediator = LibraryFilesTableMediator.instance();
            listPanel = getLibraryFiles();
        } else if (key.equals(PLAYLISTS_TABLE_KEY)) {
            tableMediator = LibraryPlaylistsTableMediator.instance();
            listPanel = getLibraryPlaylists();
        }

        if (tableMediator == null || listPanel == null) {
            //nice antipattern here.
            return;
        }

        if (lastSelectedMediator != null && lastSelectedKey != null) {
            scrollbarValues.put(lastSelectedKey, lastSelectedMediator.getScrollbarValue());
        }

        lastSelectedMediator = tableMediator;
        lastSelectedKey = getSelectedKey();

        if (listPanel.getPendingRunnables().size() == 0) {
            int lastScrollValue = scrollbarValues.containsKey(lastSelectedKey) ? scrollbarValues.get(lastSelectedKey) : 0;

            tableMediator.scrollTo(lastScrollValue);
        }
    }

    public void updateTableFiles(DirectoryHolder dirHolder) {
        clearLibraryTable();
        showView(FILES_TABLE_KEY);
        LibraryFilesTableMediator.instance().updateTableFiles(dirHolder);
    }

    public void updateTableItems(Playlist playlist) {
        clearLibraryTable();
        showView(PLAYLISTS_TABLE_KEY);
        LibraryPlaylistsTableMediator.instance().updateTableItems(playlist);
    }
    
    public void showInternetRadioStations(List<InternetRadioStation> internetRadioStations) {
        clearLibraryTable();
        showView(INTERNET_RADIO_TREE_KEY);
        LibraryInternetRadioTableMediator.instance().updateTableItems(internetRadioStations);
    }

    public void clearLibraryTable() {
        LibraryFilesTableMediator.instance().clearTable();
        LibraryPlaylistsTableMediator.instance().clearTable();
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
    
    public void addInternetRadioStationsToLibraryTable(List<InternetRadioStation> items) {
        for (InternetRadioStation item : items) {
            LibraryInternetRadioTableMediator.instance().add(item);
        }
        getLibrarySearch().addResults(items.size());
    }

    private JComponent getLibraryLeftPanel() {
        if (libraryLeftPanel == null) {
            libraryLeftPanel = new LibraryLeftPanel(getLibraryFiles(), getLibraryPlaylists(), getLibraryCoverArt());
        }
        return libraryLeftPanel;
    }

    private JComponent getLibraryRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        _tablesViewLayout = new CardLayout();
        _tablesPanel = new JPanel(_tablesViewLayout);

        _tablesPanel.add(LibraryFilesTableMediator.instance().getScrolledTablePane(), FILES_TABLE_KEY);
        _tablesPanel.add(LibraryPlaylistsTableMediator.instance().getScrolledTablePane(), PLAYLISTS_TABLE_KEY);
        _tablesPanel.add(LibraryInternetRadioTableMediator.instance().getScrolledTablePane(), INTERNET_RADIO_TREE_KEY);

        panel.add(getLibrarySearch(), BorderLayout.PAGE_START);
        panel.add(_tablesPanel, BorderLayout.CENTER);

        //BOTTOM PART - Actions to the left and Player to the right
        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new BoxLayout(panelBottom,BoxLayout.LINE_AXIS));
        //actions
        panelBottom.add(
                new IconButton(new ConfigureOptionsAction(OptionsConstructor.SHARED_KEY, I18n.tr("Options"), I18n
                        .tr("You can configure the folders you share in FrostWire\'s Options."))));

        addStationAction = new LibraryInternetRadioTableMediator.AddRadioStationAction();
        panelBottom.add(new IconButton(addStationAction));
        
        //empty space
        panelBottom.add(Box.createHorizontalGlue());
        
        //player
        panelBottom.add(new LibraryPlayer());
        
        panel.add(panelBottom, BorderLayout.PAGE_END);
        return panel;
    }
    
    public void setStationActionEnabled(boolean state) {
    	addStationAction.setEnabled(state);
    }

    public void setSelectedFile(File file) {
        getLibraryFiles().selectFinishedDownloads();
        LibraryFilesTableMediator.instance().setFileSelected(file);
    }

    public void selectCurrentSong() {
        //Select current playlist.
        Playlist currentPlaylist = AudioPlayer.instance().getCurrentPlaylist();
        final AudioSource currentSong = AudioPlayer.instance().getCurrentSong();

        //If the current song is being played from a playlist.
        if (currentPlaylist != null && currentSong != null && currentSong.getPlaylistItem() != null) {
            if (currentPlaylist.getId() != LibraryDatabase.STARRED_PLAYLIST_ID) {

                //select the song once it's available on the right hand side
                getLibraryPlaylists().enqueueRunnable(new Runnable() {
                    public void run() {
                        GUIMediator.safeInvokeLater(new Runnable() {
                            public void run() {
                                LibraryPlaylistsTableMediator.instance().setItemSelected(currentSong.getPlaylistItem());
                            }
                        });
                    }
                });

                //select the playlist
                getLibraryPlaylists().selectPlaylist(currentPlaylist);
            } else {
                LibraryFiles libraryFiles = getLibraryFiles();

                //select the song once it's available on the right hand side
                libraryFiles.enqueueRunnable(new Runnable() {
                    public void run() {
                        GUIMediator.safeInvokeLater(new Runnable() {
                            public void run() {
                                LibraryPlaylistsTableMediator.instance().setItemSelected(currentSong.getPlaylistItem());
                            }
                        });
                    }
                });

                libraryFiles.selectStarred();
            }

        } else if (currentSong != null && currentSong.getFile() != null) {
            //selects the audio node at the top
            LibraryFiles libraryFiles = getLibraryFiles();

            //select the song once it's available on the right hand side
            libraryFiles.enqueueRunnable(new Runnable() {
                public void run() {
                    GUIMediator.safeInvokeLater(new Runnable() {
                        public void run() {
                            LibraryFilesTableMediator.instance().setFileSelected(currentSong.getFile());
                        }
                    });
                }
            });

            libraryFiles.selectAudio();
        } else if (currentSong instanceof InternetRadioAudioSource) {
        	//selects the audio node at the top
            LibraryFiles libraryFiles = getLibraryFiles();

            //select the song once it's available on the right hand side
            libraryFiles.enqueueRunnable(new Runnable() {
                public void run() {
                    GUIMediator.safeInvokeLater(new Runnable() {
                        public void run() {
                            LibraryInternetRadioTableMediator.instance().setItemSelected(((InternetRadioAudioSource) currentSong).getInternetRadioStation());
                        }
                    });
                }
            });

            libraryFiles.selectRadio();
        }

        //Scroll to current song.
    }

    public boolean isScanned(int id) {
        return idScanned.contains(id);
    }

    public void scan(int hashCode, File location) {
        idScanned.add(hashCode);

        if (location.isDirectory()) {
            for (File file : location.listFiles()) {
                scan(hashCode, file);
            }
        } else {
            List<MediaTypeSavedFilesDirectoryHolder> holders = getLibraryFiles().getMediaTypeSavedFilesDirectoryHolders();
            for (MediaTypeSavedFilesDirectoryHolder holder : holders) {
                Set<File> cache = holder.getCache();
                if (holder.accept(location) && !cache.isEmpty() && !cache.contains(location)) {
                    cache.add(location);
                }
            }
        }
    }
}
