package com.frostwire.gui.library.tags;

import java.io.File;

import org.jaudiotagger.audio.mp4.Mp4FileReader;

class M4AParser extends JaudiotaggerParser {

    public M4AParser(File file) {
        super(file, new Mp4FileReader());
    }
}
