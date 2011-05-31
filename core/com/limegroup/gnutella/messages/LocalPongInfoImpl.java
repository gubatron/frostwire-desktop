package com.limegroup.gnutella.messages;

import org.gudy.azureus2.plugins.network.ConnectionManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.FileManager;

@Singleton
public class LocalPongInfoImpl implements LocalPongInfo {
    
    private final Provider<FileManager> fileManager;

    @Inject
    public LocalPongInfoImpl(
            Provider<FileManager> fileManager) {
        this.fileManager = fileManager;
    }


    /**
     * @return the number of free non-leaf slots available for limewires.
     */
    public byte getNumFreeLimeWireNonLeafSlots() {
        return 0;//(byte)connectionManager.get().getNumFreeLimeWireNonLeafSlots();
    }

    /**
     * @return the number of free leaf slots available for limewires.
     */
    public byte getNumFreeLimeWireLeafSlots() {
        return 0;//(byte)connectionManager.get().getNumFreeLimeWireLeafSlots();
    }

    public long getNumSharedFiles() {
        return fileManager.get().getNumFiles();
    }

    public int getSharedFileSize() {
        return fileManager.get().getSize();
    }

    public boolean isSupernode() {
        return false;//connectionManager.get().isSupernode();
    }
}
