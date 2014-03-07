package com.frostwire.torrent;

import org.gudy.azureus2.core3.download.impl.DownloadManagerImpl;
import org.gudy.azureus2.core3.download.impl.DownloadManagerStateImpl;
import org.gudy.azureus2.core3.util.TorrentUtils.ExtendedTorrent;

public class FWDownloadManagerStateImpl extends DownloadManagerStateImpl {

    protected FWDownloadManagerStateImpl(DownloadManagerImpl _download_manager, ExtendedTorrent _torrent) {
        super(_download_manager, _torrent);
        
    }
        
}
