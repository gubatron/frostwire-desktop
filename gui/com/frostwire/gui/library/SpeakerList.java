package com.frostwire.gui.library;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JList;
import javax.swing.ListModel;

import com.frostwire.alexandria.Playlist;
import com.frostwire.gui.library.LibraryFiles.LibraryFilesListCell;
import com.frostwire.gui.library.LibraryPlaylists.LibraryPlaylistsListCell;
import com.frostwire.gui.player.AudioPlayer;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;

public class SpeakerList extends JList {

    private static final long serialVersionUID = 6951236485310381795L;

    private final Image speaker;

    public SpeakerList() {
        speaker = GUIMediator.getThemeImage("speaker").getImage();
    }

    public SpeakerList(ListModel dataModel) {
        super(dataModel);
        speaker = GUIMediator.getThemeImage("speaker").getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        AudioPlayer player = AudioPlayer.instance();
        if (player.getCurrentSong() != null && player.getCurrentPlaylist() == null && player.getPlaylistFilesView() != null) {
            int index = getAudioIndex();
            if (index != -1) {
                paintSpaker(g, index);
            }
        } else if (player.getCurrentSong() != null && player.getCurrentPlaylist() != null && player.getPlaylistFilesView() != null) {
            int index = getPlaylistIndex(player.getCurrentPlaylist());
            if (index != -1) {
                paintSpaker(g, index);
            }
        }
    }

    private void paintSpaker(Graphics g, int index) {
        Rectangle rect = getUI().getCellBounds(this, index, index);
        Dimension lsize = rect.getSize();
        Point llocation = rect.getLocation();
        g.drawImage(speaker, llocation.x + lsize.width - speaker.getWidth(null) - 4, llocation.y + (lsize.height - speaker.getHeight(null)) / 2, null);
    }

    private int getAudioIndex() {
        int n = getModel().getSize();
        for (int i = 0; i < n; i++) {
            Object value = getModel().getElementAt(i);
            if (value instanceof LibraryFilesListCell) {
                DirectoryHolder dh = ((LibraryFilesListCell) value).getDirectoryHolder();
                if (dh instanceof MediaTypeSavedFilesDirectoryHolder
                        && ((MediaTypeSavedFilesDirectoryHolder) dh).getMediaType().equals(MediaType.getAudioMediaType())) {
                    return i;
                }
            }
        }

        return -1;
    }

    private int getPlaylistIndex(Playlist playlist) {
        int n = getModel().getSize();
        for (int i = 0; i < n; i++) {
            Object value = getModel().getElementAt(i);
            if (value instanceof LibraryPlaylistsListCell) {
                Playlist p = ((LibraryPlaylistsListCell) value).getPlaylist();
                if (p != null && p.equals(playlist)) {
                    return i;
                }
            }
        }

        return -1;
    }
}
