package com.frostwire.gui.library.tags;

import java.io.File;

abstract class AbstractTagParser implements TagsParser {

    protected final File file;

    public AbstractTagParser(File file) {
        this.file = file;
    }
}
