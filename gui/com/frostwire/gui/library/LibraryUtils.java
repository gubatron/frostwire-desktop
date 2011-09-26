package com.frostwire.gui.library;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.limewire.util.FileUtils;
import org.limewire.util.FilenameUtils;

import com.frostwire.alexandria.Playlist;
import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.gui.library.LibraryPlaylistTransferable.Item;
import com.frostwire.gui.player.AudioPlayer;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

public class LibraryUtils {

    private static void addPlaylistItem(Playlist playlist, File file) {
        try {
            LibraryMediator.instance().getLibrarySearch().pushStatus(I18n.tr("Importing ") + file.getName());
            AudioMetaData mt = new AudioMetaData(file);
            PlaylistItem item = playlist.newItem(file.getAbsolutePath(), file.getName(), file.length(), FileUtils.getFileExtension(file), mt.getTitle(), mt.getDurationInSecs(), mt.getArtist(), mt.getAlbum(), "",// TODO: cover art path
                    mt.getBitrate(), mt.getComment(), mt.getGenre(), mt.getTrack(), mt.getYear(), false);
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
        int days = s / 86400;
        int r = s % 86400;

        int hours = r / 3600;
        r = s % 3600;
        int minutes = r / 60;
        int seconds = r % 60;

        //padding
        DD = String.valueOf(days);
        HH = (hours < 10) ? "0" + hours : String.valueOf(hours);
        MM = (minutes < 10) ? "0" + minutes : String.valueOf(minutes);
        SS = (seconds < 10) ? "0" + seconds : String.valueOf(seconds);

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
        String playlistName = (String) JOptionPane.showInputDialog(GUIMediator.getAppFrame(), I18n.tr("Playlist name"), I18n.tr("Playlist name"), JOptionPane.PLAIN_MESSAGE, null, null, calculateName(lines));

        if (playlistName != null && playlistName.length() > 0) {
            final Playlist playlist = LibraryMediator.getLibrary().newPlaylist(playlistName, playlistName);
            playlist.save();
            LibraryMediator.instance().getLibraryPlaylists().addPlaylist(playlist);
            LibraryMediator.instance().getLibraryPlaylists().markBeginImport(playlist);
            new Thread(new Runnable() {
                public void run() {
                    addToPlaylist(playlist, lines);
                    playlist.save();
                    GUIMediator.safeInvokeLater(new Runnable() {
                        public void run() {
                            LibraryMediator.instance().getLibraryPlaylists().markEndImport(playlist);
                            LibraryMediator.instance().getLibraryPlaylists().refreshSelection();
                        }
                    });
                }
            }).start();
        }
    }

    public static void createNewPlaylist(final File[] files) {
        String playlistName = (String) JOptionPane.showInputDialog(GUIMediator.getAppFrame(), I18n.tr("Playlist name"), I18n.tr("Playlist name"), JOptionPane.PLAIN_MESSAGE, null, null, calculateName(files));

        if (playlistName != null && playlistName.length() > 0) {
            final Playlist playlist = LibraryMediator.getLibrary().newPlaylist(playlistName, playlistName);
            playlist.save();
            LibraryMediator.instance().getLibraryPlaylists().addPlaylist(playlist);
            LibraryMediator.instance().getLibraryPlaylists().markBeginImport(playlist);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        addToPlaylist(playlist, files);
                        playlist.save();
                    } finally {
                        GUIMediator.safeInvokeLater(new Runnable() {
                            public void run() {
                                LibraryMediator.instance().getLibraryPlaylists().markEndImport(playlist);
                                LibraryMediator.instance().getLibraryPlaylists().refreshSelection();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    public static void createNewPlaylist(final PlaylistItem[] playlistItems) {
        String playlistName = (String) JOptionPane.showInputDialog(GUIMediator.getAppFrame(), I18n.tr("Playlist name"), I18n.tr("Playlist name"), JOptionPane.PLAIN_MESSAGE, null, null, calculateName(playlistItems));

        if (playlistName != null && playlistName.length() > 0) {
            final Playlist playlist = LibraryMediator.getLibrary().newPlaylist(playlistName, playlistName);

            new Thread(new Runnable() {
                public void run() {
                    playlist.save();
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

    public static void createNewPlaylist(File m3uFile) {
        try {
            List<File> files = M3UPlaylist.load(m3uFile.getAbsolutePath());
            createNewPlaylist(files.toArray(new File[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void asyncAddToPlaylist(final Playlist playlist, final List<? extends AbstractLibraryTableDataLine<?>> lines) {
        LibraryMediator.instance().getLibraryPlaylists().markBeginImport(playlist);
        new Thread(new Runnable() {
            public void run() {
                try {
                    addToPlaylist(playlist, lines);
                } finally {
                    GUIMediator.safeInvokeLater(new Runnable() {
                        public void run() {
                            LibraryMediator.instance().getLibraryPlaylists().markEndImport(playlist);
                            LibraryMediator.instance().getLibraryPlaylists().refreshSelection();
                        }
                    });
                }
            }
        }).start();
    }

    public static void asyncAddToPlaylist(final Playlist playlist, final File[] files) {
        LibraryMediator.instance().getLibraryPlaylists().markBeginImport(playlist);
        new Thread(new Runnable() {
            public void run() {
                try {
                    addToPlaylist(playlist, files);
                } finally {
                    GUIMediator.safeInvokeLater(new Runnable() {
                        public void run() {
                            LibraryMediator.instance().getLibraryPlaylists().markEndImport(playlist);
                            LibraryMediator.instance().getLibraryPlaylists().refreshSelection();
                        }
                    });
                }
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

    public static void asyncAddToPlaylist(Playlist playlist, File m3uFile) {
        try {
            List<File> files = M3UPlaylist.load(m3uFile.getAbsolutePath());
            asyncAddToPlaylist(playlist, files.toArray(new File[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            item.trackArtist = playlistItem.getTrackArtist();
            item.trackAlbum = playlistItem.getTrackAlbum();
            item.coverArtPath = playlistItem.getCoverArtPath();
            item.trackBitrate = playlistItem.getTrackBitrate();
            item.trackComment = playlistItem.getTrackComment();
            item.trackGenre = playlistItem.getTrackGenre();
            item.trackNumber = playlistItem.getTrackNumber();
            item.trackYear = playlistItem.getTrackYear();
            item.starred = playlistItem.isStarred();
            items.add(item);
        }
        return items;
    }

    public static PlaylistItem[] convertToPlaylistItems(LibraryPlaylistTransferable.Item[] items) {
        List<PlaylistItem> playlistItems = new ArrayList<PlaylistItem>(items.length);
        for (LibraryPlaylistTransferable.Item item : items) {
            PlaylistItem playlistItem = new PlaylistItem(null, item.id, item.filePath, item.fileName, item.fileSize, item.fileExtension, item.trackTitle, item.trackDurationInSecs, item.trackArtist, item.trackAlbum, item.coverArtPath, item.trackBitrate, item.trackComment, item.trackGenre,
                    item.trackNumber, item.trackYear, item.starred);
            playlistItems.add(playlistItem);
        }
        return playlistItems.toArray(new PlaylistItem[0]);
    }

    private static void addToPlaylist(Playlist playlist, List<? extends AbstractLibraryTableDataLine<?>> lines) {
        for (int i = 0; i < lines.size() && !playlist.isDeleted(); i++) {
            AbstractLibraryTableDataLine<?> line = lines.get(i);
            if (AudioPlayer.isPlayableFile(line.getFile())) {
                LibraryUtils.addPlaylistItem(playlist, line.getFile());
            }
        }
    }

    private static void addToPlaylist(Playlist playlist, File[] files) {
        for (int i = 0; i < files.length && !playlist.isDeleted(); i++) {
            if (AudioPlayer.isPlayableFile(files[i])) {
                LibraryUtils.addPlaylistItem(playlist, files[i]);
            } else if (files[i].isDirectory()) {
                addToPlaylist(playlist, files[i].listFiles());
            }
        }
    }

    private static void addToPlaylist(Playlist playlist, PlaylistItem[] playlistItems) {
        for (int i = 0; i < playlistItems.length && !playlist.isDeleted(); i++) {
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

    public static boolean directoryContainsAudio(File directory) {
        return directoryContainsAudio(directory, 4);
    }

    private static boolean directoryContainsAudio(File directory, int deep) {
        if (directory == null || !directory.isDirectory()) {
            return false;
        }

        for (File childFile : directory.listFiles()) {
            if (!childFile.isDirectory()) {
                if (AudioPlayer.isPlayableFile(childFile)) {
                    return true;
                }
            } else {
                if (deep > 0) {
                    if (directoryContainsAudio(childFile, deep - 1)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static String calculateName(File[] files) {
        List<String> names = new ArrayList<String>(150);
        findNames(names, files);
        return new NameCalculator(names).getName();
    }

    private static String calculateName(List<? extends AbstractLibraryTableDataLine<?>> lines) {
        File[] files = new File[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            files[i] = lines.get(i).getFile();
        }
        return calculateName(files);
    }

    private static String calculateName(PlaylistItem[] playlistItems) {
        File[] files = new File[playlistItems.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(playlistItems[i].getFilePath());
        }
        return calculateName(files);
    }

    private static void findNames(List<String> names, File[] files) {
        if (names.size() > 100) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                String fullPathNoEndSeparator = FilenameUtils.getFullPathNoEndSeparator(file.getAbsolutePath());
                String baseName = FilenameUtils.getBaseName(fullPathNoEndSeparator);
                names.add(baseName);
                findNames(names, file.listFiles());
            } else if (AudioPlayer.isPlayableFile(file)) {
                String baseName = FilenameUtils.getBaseName(file.getAbsolutePath());
                names.add(baseName);
            }
        }
    }
}
