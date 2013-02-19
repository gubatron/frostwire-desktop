package com.frostwire.gui.library.tags;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.limewire.util.FilenameUtils;

class TagsParserFactory {

    private static final List<String> MP4_EXTENSIONS = Arrays.asList("mp4", "m4v", "m4a", "mov", "3gp");
    private static final List<String> JAUDIOTAGGER_EXTENSIONS = Arrays.asList("mp3", "wma", "flac", "ogg", "wav");

    public TagsParser getInstance(File file) {
        String ext = FilenameUtils.getExtension(file.getName());

        if (isMP3(ext)) {
            return new MP3Parser(file);
        } else if (isMP4(ext)) {
            return new MP4Parser(file);
        } else if (isJaudiotagger(ext)) {
            return new JaudiotaggerParser(file);
        } else {
            return new MPlayerParser(file);
        }
    }

    private boolean isMP3(String ext) {
        return ext.equals(ext);
    }

    private boolean isMP4(String ext) {
        return MP4_EXTENSIONS.contains(ext);
    }

    private boolean isJaudiotagger(String ext) {
        return JAUDIOTAGGER_EXTENSIONS.contains(ext);
    }
}
