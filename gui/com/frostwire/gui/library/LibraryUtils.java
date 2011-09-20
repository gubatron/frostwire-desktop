package com.frostwire.gui.library;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.limewire.util.FileUtils;

import com.frostwire.alexandria.Playlist;
import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.gui.library.LibraryPlaylistTransferable.Item;
import com.frostwire.gui.player.AudioMetaData;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

public class LibraryUtils {

    public static void addPlaylistItem(Playlist playlist, File file) {
        try {
            LibraryMediator.instance().getLibrarySearch().pushStatus(I18n.tr("Importing ") + file.getName());
            AudioMetaData mt = new AudioMetaData(file);
            PlaylistItem item = playlist.newItem(file.getAbsolutePath(), file.getName(), file.length(), FileUtils.getFileExtension(file), mt.getTitle(),
                    mt.getLength(), mt.getArtist(), mt.getAlbum(), "",// TODO: cover art path
                    mt.getBitrate(), mt.getComment(), mt.getGenre(), mt.getTrack(), mt.getYear());
            playlist.getItems().add(item);
            item.save();
        } finally {
            LibraryMediator.instance().getLibrarySearch().revertStatus();
        }
    }
    
    public static String getSecondsInDDHHMMSS(int s) {
    	if (s < 0) {
    		s = 0;
    	}
    	
    	StringBuilder result = new StringBuilder();
    	
    	String DD = "";
    	String HH = "";
    	String MM = "";
    	String SS = "";

    	//math
    	int days=s/86400;
    	int r = s%86400;
    	
    	int hours=r/3600;
    	r = s%3600;
    	int minutes = r/60;
    	int seconds = r%60;

    	//padding
    	DD = String.valueOf(days);
    	HH = (hours < 10) ? "0"+hours : String.valueOf(hours);
    	MM = (minutes < 10) ? "0"+minutes : String.valueOf(minutes);
    	SS = (seconds < 10) ? "0"+seconds : String.valueOf(seconds);
    	
    	//lazy formatting
    	if (days > 0) {
    		result.append(DD);
    		result.append(" day");
    		if (days > 1) {
    			result.append("s");
    		}
    		return result.toString();    		
    	}
    	
    	if (hours > 0) {
    		result.append(HH);
    		result.append(":");
    	}

    	result.append(MM);
    	result.append(":");
    	result.append(SS);
    	
    	return result.toString();   
    }

    public static void createNewPlaylist(final List<? extends AbstractLibraryTableDataLine<?>> lines) {
        String playlistName = (String) JOptionPane.showInputDialog(GUIMediator.getAppFrame(), I18n.tr("Playlist name"), I18n.tr("Playlist name"),
                JOptionPane.PLAIN_MESSAGE, null, null, null);

        if (playlistName != null && playlistName.length() > 0) {
            final Playlist playlist = LibraryMediator.getLibrary().newPlaylist(playlistName, playlistName);

            new Thread(new Runnable() {
                public void run() {
                    addToPlaylist(playlist, lines);
                    playlist.save();
                    GUIMediator.safeInvokeLater(new Runnable() {
                        public void run() {
                            LibraryMediator.instance().getLibraryPlaylists().addPlaylist(playlist);
                        }
                    });
                }
            }).start();
        }
    }

    public static void createNewPlaylist(final File[] files) {
        String playlistName = (String) JOptionPane.showInputDialog(GUIMediator.getAppFrame(), I18n.tr("Playlist name"), I18n.tr("Playlist name"),
                JOptionPane.PLAIN_MESSAGE, null, null, null);

        if (playlistName != null && playlistName.length() > 0) {
            final Playlist playlist = LibraryMediator.getLibrary().newPlaylist(playlistName, playlistName);

            new Thread(new Runnable() {
                public void run() {
                    addToPlaylist(playlist, files);
                    playlist.save();
                    GUIMediator.safeInvokeLater(new Runnable() {
                        public void run() {
                            LibraryMediator.instance().getLibraryPlaylists().addPlaylist(playlist);
                        }
                    });
                }
            }).start();
        }
    }

    public static void createNewPlaylist(final PlaylistItem[] playlistItems) {
        String playlistName = (String) JOptionPane.showInputDialog(GUIMediator.getAppFrame(), I18n.tr("Playlist name"), I18n.tr("Playlist name"),
                JOptionPane.PLAIN_MESSAGE, null, null, null);

        if (playlistName != null && playlistName.length() > 0) {
            final Playlist playlist = LibraryMediator.getLibrary().newPlaylist(playlistName, playlistName);

            new Thread(new Runnable() {
                public void run() {
                    addToPlaylist(playlist, playlistItems);
                    playlist.save();
                    GUIMediator.safeInvokeLater(new Runnable() {
                        public void run() {
                            LibraryMediator.instance().getLibraryPlaylists().addPlaylist(playlist);
                        }
                    });
                }
            }).start();
        }
    }

    public static void asyncAddToPlaylist(final Playlist playlist, final List<? extends AbstractLibraryTableDataLine<?>> lines) {
        new Thread(new Runnable() {
            public void run() {
                addToPlaylist(playlist, lines);
                GUIMediator.safeInvokeLater(new Runnable() {
                    public void run() {
                        LibraryMediator.instance().getLibraryPlaylists().refreshSelection();
                    }
                });
            }
        }).start();
    }

    public static void asyncAddToPlaylist(final Playlist playlist, final File[] files) {
        new Thread(new Runnable() {
            public void run() {
                addToPlaylist(playlist, files);
                GUIMediator.safeInvokeLater(new Runnable() {
                    public void run() {
                        LibraryMediator.instance().getLibraryPlaylists().refreshSelection();
                    }
                });
            }
        }).start();
    }
    
    public static void asyncAddToPlaylist(final Playlist playlist, final PlaylistItem[] playlistItems) {
        new Thread(new Runnable() {
            public void run() {
                addToPlaylist(playlist, playlistItems);
                playlist.save();
                GUIMediator.safeInvokeLater(new Runnable() {
                    public void run() {
                        LibraryMediator.instance().getLibraryPlaylists().refreshSelection();
                    }
                });
            }
        }).start();
    }

    public static List<LibraryPlaylistTransferable.Item> convertToItems(List<PlaylistItem> playlistItems) {
        List<LibraryPlaylistTransferable.Item> items = new ArrayList<LibraryPlaylistTransferable.Item>(playlistItems.size());
        for (PlaylistItem playlistItem : playlistItems) {
            Item item = new LibraryPlaylistTransferable.Item();
            item.id = playlistItem.getId();
            item.filePath = playlistItem.getFilePath();
            item.fileName = playlistItem.getFileName();
            item.fileSize = playlistItem.getFileSize();
            item.fileExtension = playlistItem.getFileExtension();
            item.trackTitle = playlistItem.getTrackTitle();
            item.trackDurationInSecs = playlistItem.getTrackDurationInSecs();
            item.artistName = playlistItem.getArtistName();
            item.albumName = playlistItem.getAlbumName();
            item.coverArtPath = playlistItem.getCoverArtPath();
            item.bitrate = playlistItem.getBitrate();
            item.comment = playlistItem.getComment();
            item.genre = playlistItem.getGenre();
            item.track = playlistItem.getTrack();
            item.year = playlistItem.getYear();
            items.add(item);
        }
        return items;
    }
    
    public static PlaylistItem[] convertToPlaylistItems(LibraryPlaylistTransferable.Item[] items) {
        List<PlaylistItem> playlistItems = new ArrayList<PlaylistItem>(items.length);
        for (LibraryPlaylistTransferable.Item item : items) {
            PlaylistItem playlistItem = new PlaylistItem(null,
                    item.id,
                    item.filePath,
                    item.fileName,
                    item.fileSize,
                    item.fileExtension,
                    item.trackTitle,
                    item.trackDurationInSecs,
                    item.artistName,
                    item.albumName,
                    item.coverArtPath,
                    item.bitrate,
                    item.comment,
                    item.genre,
                    item.track,
                    item.year);
            playlistItems.add(playlistItem);
        }
        return playlistItems.toArray(new PlaylistItem[0]);
    }

    private static void addToPlaylist(Playlist playlist, List<? extends AbstractLibraryTableDataLine<?>> lines) {
        for (int i = 0; i < lines.size(); i++) {
            AbstractLibraryTableDataLine<?> line = lines.get(i);
            LibraryUtils.addPlaylistItem(playlist, line.getFile());
        }
    }

    private static void addToPlaylist(Playlist playlist, File[] files) {
        for (int i = 0; i < files.length; i++) {
            LibraryUtils.addPlaylistItem(playlist, files[i]);
        }
    }

    private static void addToPlaylist(Playlist playlist, PlaylistItem[] playlistItems) {
        for (int i = 0; i < playlistItems.length; i++) {
            playlistItems[i].setPlaylist(playlist);
            playlist.getItems().add(playlistItems[i]);
        }
    }

	public static String getPlaylistDurationInDDHHMMSS(Playlist playlist) {
		List<PlaylistItem> items = playlist.getItems();
		float totalSecs = 0;
		for (PlaylistItem item : items) {
			totalSecs += item.getTrackDurationInSecs();
		}
		
		return getSecondsInDDHHMMSS((int) totalSecs);
	}
}
