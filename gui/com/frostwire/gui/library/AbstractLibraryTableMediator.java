package com.frostwire.gui.library;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.frostwire.alexandria.Library;
import com.frostwire.alexandria.Playlist;
import com.frostwire.gui.player.AudioPlayer;
import com.frostwire.gui.player.AudioSource;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;
import com.limegroup.gnutella.gui.tables.DataLineModel;
import com.limegroup.gnutella.gui.themes.SkinMenu;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;

abstract class AbstractLibraryTableMediator<T extends DataLineModel<E, I>, E extends AbstractLibraryTableDataLine<I>, I> extends
        AbstractTableMediator<T, E, I> {

    private Queue<File> lastRandomFiles;

    private MediaType mediaType;

    protected AbstractLibraryTableMediator(String id) {
        super(id);
        lastRandomFiles = new LinkedList<File>();
    }

    public AbstractLibraryTableDataLine<?>[] getSelectedLines() {
        int[] selected = TABLE.getSelectedRows();
        AbstractLibraryTableDataLine<?>[] lines = new AbstractLibraryTableDataLine[selected.length];
        for (int i = 0; i < selected.length; i++)
            lines[i] = DATA_MODEL.get(selected[i]);
        return lines;
    }
    
    public MediaType getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public AudioSource getNextRandomSong(AudioSource currentSong) {
        if (!mediaType.equals(MediaType.getAudioMediaType())) {
            return null;
        }

        File songFile;
        int count = 4;
        while ((songFile = findRandomSongFile(currentSong.getFile())) == null && count-- > 0)
            ;

        if (count > 0) {
            lastRandomFiles.add(songFile);
            if (lastRandomFiles.size() > 3) {
                lastRandomFiles.poll();
            }
        } else {
            songFile = currentSong.getFile();
            lastRandomFiles.clear();
            lastRandomFiles.add(songFile);
        }

        return new AudioSource(songFile);
    }

    public AudioSource getNextContinuousSong(AudioSource currentSong) {
        if (!mediaType.equals(MediaType.getAudioMediaType())) {
            return null;
        }

        int n = DATA_MODEL.getRowCount();
        for (int i = 0; i < n; i++) {
            try {
                E line = DATA_MODEL.get(i);
                if (currentSong.getFile().equals(line.getFile())) {
                    for (int j = 1; j < n; j++) {
                        File file = DATA_MODEL.get((j + i) % n).getFile();
                        if (AudioPlayer.isPlayableFile(file)) {
                            return new AudioSource(file);
                        }
                    }
                }
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    public AudioSource getNextSong(AudioSource currentSong) {
        if (!mediaType.equals(MediaType.getAudioMediaType())) {
            return null;
        }

        int n = DATA_MODEL.getRowCount();
        for (int i = 0; i < n; i++) {
            try {
                E line = DATA_MODEL.get(i);
                if (currentSong.getFile().equals(line.getFile())) {
                    for (int j = i + 1; j < n; j++) {
                        File file = DATA_MODEL.get(j).getFile();
                        if (AudioPlayer.isPlayableFile(file)) {
                            return new AudioSource(file);
                        }
                    }
                }
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    protected SkinMenu createAddToPlaylistSubMenu() {
        SkinMenu menu = new SkinMenu(I18n.tr("Add to playlist"));

        menu.add(new SkinMenuItem(new CreateNewPlaylistAction()));

        Library library = LibraryMediator.getLibrary();
        List<Playlist> playlists = library.getPlaylists();
        Playlist currentPlaylist = LibraryMediator.instance().getCurrentPlaylist();

        if (playlists.size() > 0) {
            menu.addSeparator();

            for (Playlist playlist : library.getPlaylists()) {

                if (currentPlaylist != null && currentPlaylist.equals(playlist)) {
                    continue;
                }

                menu.add(new SkinMenuItem(new AddToPlaylistAction(playlist)));
            }
        }

        return menu;
    }

    private File findRandomSongFile(File excludeFile) {
        int n = DATA_MODEL.getRowCount();
        int index = new Random(System.currentTimeMillis()).nextInt(n);

        for (int i = index; i < n; i++) {
            try {
                File file = DATA_MODEL.get(i).getFile();

                if (!lastRandomFiles.contains(file) && !file.equals(excludeFile) && AudioPlayer.isPlayableFile(file)) {
                    return file;
                }
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    private class CreateNewPlaylistAction extends AbstractAction {

        private static final long serialVersionUID = 3460908036485828909L;

        public CreateNewPlaylistAction() {
            super(I18n.tr("Create New Playlist"));
            putValue(Action.LONG_DESCRIPTION, I18n.tr("Create and add to a new playlist"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            PlaylistUtils.createNewPlaylist(getSelectedLines());
        }
    }

    private final class AddToPlaylistAction extends AbstractAction {

        private static final long serialVersionUID = 4658698262279334616L;

        private Playlist playlist;

        public AddToPlaylistAction(Playlist playlist) {
            super(playlist.getName());
            putValue(Action.LONG_DESCRIPTION, I18n.tr("Add to playlist ") + "\"" + playlist.getName() + "\"");
            this.playlist = playlist;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            PlaylistUtils.addToPlaylist(playlist, getSelectedLines());
        }
    }
}
