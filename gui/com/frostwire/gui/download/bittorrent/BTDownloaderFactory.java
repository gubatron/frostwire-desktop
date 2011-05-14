package com.frostwire.gui.download.bittorrent;

import java.io.File;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.util.TorrentUtils;
import com.limegroup.gnutella.SaveLocationException;
import com.limegroup.gnutella.settings.SharingSettings;

public class BTDownloaderFactory {

    private final GlobalManager _globalManager;
    private final File _file;
    
    public BTDownloaderFactory(GlobalManager globalManager, File file) {
        _globalManager = globalManager;
        _file = file;
    }

    public File getSaveFile() {
        return _file;
    }

    public void setSaveFile(File newFile) {
    }
    
    public BTDownloader createDownloader(boolean overwrite) throws SaveLocationException, TOTorrentException {
        
        File saveDir = SharingSettings.TORRENT_DATA_DIR_SETTING.getValue();
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        TOTorrent torrent = TorrentUtils.readFromFile(_file, false);
        
        DownloadManager manager;
        
        if ((manager = _globalManager.getDownloadManager(torrent)) == null) {         
            manager = _globalManager.addDownloadManager(_file.getAbsolutePath(), saveDir.getAbsolutePath());
        }
        
        return new BTDownloaderImpl(manager);
    }


}
