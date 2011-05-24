package com.limegroup.gnutella.metadata;

import java.io.File;
import java.io.IOException;

/**
 * Factory for returning readers and writers of metadata. 
 */
public interface MetaDataFactory {
    /**
     * Reads the meta data from the file if the file type is supported
     * or return null if reading the file meta data if not supprted
     */
    public MetaReader parse(File f) throws IOException;
}
