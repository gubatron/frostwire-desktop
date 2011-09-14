package com.frostwire.gui.library;

import java.io.File;

import javax.swing.JOptionPane;

import org.limewire.util.FileUtils;

import com.frostwire.alexandria.Playlist;
import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.gui.player.AudioMetaData;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

class PlaylistUtils {

    public static void addPlaylistItem(Playlist playlist, File file) {
        AudioMetaData mt = new AudioMetaData(file);
        PlaylistItem item = playlist.newItem(file.getAbsolutePath(), file.getName(), file.length(), FileUtils.getFileExtension(file), mt.getTitle(),
                mt.getLength(), mt.getArtist(), mt.getAlbum(), "",// TODO: cover art path
                mt.getBitrate(), mt.getComment(), mt.getGenre(), mt.getTrack(), mt.getYear());
        playlist.getItems().add(item);
        item.save();
    }

    public static void createNewPlaylist(AbstractLibraryTableDataLine<?>[] lines) {
        String playlistName = (String) JOptionPane.showInputDialog(GUIMediator.getAppFrame(), I18n.tr("Playlist name"), I18n.tr("Playlist name"),
                JOptionPane.PLAIN_MESSAGE, null, null, null);

        if (playlistName != null && playlistName.length() > 0) {
            Playlist playlist = LibraryMediator.getLibrary().newPlaylist(playlistName, playlistName);

            for (int i = 0; i < lines.length; i++) {
                AbstractLibraryTableDataLine<?> line = lines[i];
                PlaylistUtils.addPlaylistItem(playlist, line.getFile());
            }

            playlist.save();
            LibraryMediator.instance().getLibraryPlaylists().addPlaylist(playlist);
        }
    }

    public static void createNewPlaylist(File[] files) {
        String playlistName = (String) JOptionPane.showInputDialog(GUIMediator.getAppFrame(), I18n.tr("Playlist name"), I18n.tr("Playlist name"),
                JOptionPane.PLAIN_MESSAGE, null, null, null);

        if (playlistName != null && playlistName.length() > 0) {
            Playlist playlist = LibraryMediator.getLibrary().newPlaylist(playlistName, playlistName);

            for (int i = 0; i < files.length; i++) {
                PlaylistUtils.addPlaylistItem(playlist, files[i]);
            }

            playlist.save();
            LibraryMediator.instance().getLibraryPlaylists().addPlaylist(playlist);
        }
    }
}
