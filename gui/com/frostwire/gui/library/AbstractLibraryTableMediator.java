package com.frostwire.gui.library;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import com.frostwire.gui.player.AudioPlayer;
import com.frostwire.gui.player.AudioSource;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;
import com.limegroup.gnutella.gui.tables.DataLineModel;

public abstract class AbstractLibraryTableMediator<T extends DataLineModel<E, I>, E extends AbstractLibraryTableDataLine<I>, I> extends
        AbstractTableMediator<T, E, I> {

    private Queue<File> lastRandomFiles;

    protected MediaType mediaType;

    protected AbstractLibraryTableMediator(String id) {
        super(id);
        lastRandomFiles = new LinkedList<File>();
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
}
