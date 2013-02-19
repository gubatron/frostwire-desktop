package com.frostwire.gui.library.tags;

import java.io.File;

import org.limewire.util.FilenameUtils;

abstract class AbstractTagParser implements TagsParser {

    protected final File file;

    public AbstractTagParser(File file) {
        this.file = file;
    }

    protected TagsData sanitize(int duration, String bitrate, String title, String artist, String album, String comment, String genre, String track, String year) {
        if (title == null || title.length() == 0) {
            title = FilenameUtils.getBaseName(file.getAbsolutePath());
        }

        if (duration < 0) {
            duration = 0;
        }
        if (artist == null) {
            artist = "";
        }
        if (album == null) {
            album = "";
        }
        if (bitrate == null) {
            bitrate = "";
        }
        if (comment == null) {
            comment = "";
        }

        if (genre == null) {
            genre = "";
        }

        if (track == null) {
            track = "";
        } else {
            int index = -1;
            index = track.indexOf('/');
            if (index != -1) {
                track = track.substring(0, index);
            }
        }

        if (year == null) {
            year = "";
        }

        return new TagsData(duration, bitrate, title, artist, album, comment, genre, track, year);
    }
}
