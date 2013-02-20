package com.frostwire.gui.library.tags;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.limewire.util.FilenameUtils;

class TagsParserFactory {

    private static final List<String> MP3_EXTENSIONS = Arrays.asList("mp3");
    private static final List<String> M4A_EXTENSIONS = Arrays.asList("m4a");
    private static final List<String> MP4_EXTENSIONS = Arrays.asList("mp4", "m4v", "mov", "3gp");
    private static final List<String> OGG_EXTENSIONS = Arrays.asList("ogg");
    private static final List<String> FLAC_EXTENSIONS = Arrays.asList("flac");
    private static final List<String> JAUDIOTAGGER_EXTENSIONS = Arrays.asList("wma", "wav");

    public TagsParser getInstance(File file) {
        String ext = FilenameUtils.getExtension(file.getName());

        if (isMP3(ext)) {
            return new MP3Parser(file);
        } else if (isM4A(ext)) {
            return new M4AParser(file);
        } else if (isMP4(ext)) {
            return new MP4Parser(file);
        } else if (isOgg(ext)) {
            return new OggParser(file);
        } else if (isFlac(ext)) {
            return new FlacParser(file);
        } else if (isJaudiotagger(ext)) {
            return new JaudiotaggerParser(file);
        } else {
            return new MPlayerParser(file);
        }
    }

    private boolean isMP3(String ext) {
        return MP3_EXTENSIONS.contains(ext);
    }

    private boolean isM4A(String ext) {
        return M4A_EXTENSIONS.contains(ext);
    }

    private boolean isMP4(String ext) {
        return MP4_EXTENSIONS.contains(ext);
    }

    private boolean isOgg(String ext) {
        return OGG_EXTENSIONS.contains(ext);
    }

    private boolean isFlac(String ext) {
        return FLAC_EXTENSIONS.contains(ext);
    }

    private boolean isJaudiotagger(String ext) {
        return JAUDIOTAGGER_EXTENSIONS.contains(ext);
    }
}
