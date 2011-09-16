package com.frostwire.gui.library;

import java.io.File;

import javax.swing.JOptionPane;

import org.limewire.util.FileUtils;

import com.frostwire.alexandria.Playlist;
import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.gui.player.AudioMetaData;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

class LibraryUtils {

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

    public static void createNewPlaylist(final AbstractLibraryTableDataLine<?>[] lines) {
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

    public static void asyncAddToPlaylist(final Playlist playlist, final AbstractLibraryTableDataLine<?>[] lines) {
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

    private static void addToPlaylist(Playlist playlist, AbstractLibraryTableDataLine<?>[] lines) {
        for (int i = 0; i < lines.length; i++) {
            AbstractLibraryTableDataLine<?> line = lines[i];
            LibraryUtils.addPlaylistItem(playlist, line.getFile());
        }
    }

    private static void addToPlaylist(Playlist playlist, File[] files) {
        for (int i = 0; i < files.length; i++) {
            LibraryUtils.addPlaylistItem(playlist, files[i]);
        }
    }
}
