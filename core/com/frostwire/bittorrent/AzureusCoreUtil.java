package com.frostwire.bittorrent;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfoSet;
import org.gudy.azureus2.core3.download.DownloadManager;

public class AzureusCoreUtil {
    
    public static Set<File> getIncompleteFiles() {
        Set<File> set = new HashSet<File>();
        
        List<?> dms = AzureusStarter.getAzureusCore().getGlobalManager().getDownloadManagers();
        for (Object obj : dms) {
            DownloadManager dm = (DownloadManager) obj;
            
            DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
            for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
                if (getDownloadPercent(fileInfo) < 100) {
                    set.add(fileInfo.getFile(false));
                }
            }
        }
        
        return set;
    }

    public static int getDownloadPercent(DiskManagerFileInfo fileInfo) {
        long length = fileInfo.getLength();
        if (length == 0 || fileInfo.getDownloaded() == length) {
            return 100;
        } else {
            return (int) (fileInfo.getDownloaded() * 100 / length);
        }
    }
}
