package com.frostwire.gui.library.tags;

import java.io.File;

import org.jaudiotagger.audio.ogg.OggFileReader;

class OggParser extends JaudiotaggerParser {

    public OggParser(File file) {
        super(file, new OggFileReader());
    }
}
