package com.frostwire.gui.library.tags;

import java.io.File;

import org.jaudiotagger.audio.flac.FlacFileReader;

class FlacParser extends JaudiotaggerParser {

    public FlacParser(File file) {
        super(file, new FlacFileReader());
    }
}
