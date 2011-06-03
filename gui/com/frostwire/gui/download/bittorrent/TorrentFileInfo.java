package com.frostwire.gui.download.bittorrent;

import org.gudy.azureus2.core3.torrent.TOTorrentFile;

public class TorrentFileInfo {
    public TOTorrentFile torrentFile;
    public boolean selected;

    public TorrentFileInfo(TOTorrentFile torrentFile, boolean selected) {
        this.torrentFile = torrentFile;
        this.selected = selected;
    }
}
