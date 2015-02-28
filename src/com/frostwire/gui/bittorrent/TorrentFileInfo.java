package com.frostwire.gui.bittorrent;

import com.frostwire.jlibtorrent.FileEntry;

public class TorrentFileInfo {
    public FileEntry fileEntry;
    public boolean selected;

    public TorrentFileInfo(FileEntry torrentFile, boolean selected) {
        this.fileEntry = torrentFile;
        this.selected = selected;
    }
}
