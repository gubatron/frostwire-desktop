/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.limegroup.gnutella;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.limewire.util.CommonUtils;

import com.aelitis.azureus.core.AzureusCore;
import com.frostwire.AzureusStarter;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.settings.UpdateSettings;

public class DownloadManagerImpl implements DownloadManager {

    private static final Log LOG = LogFactory.getLog(DownloadManagerImpl.class);


    private final ActivityCallback activityCallback;

    public DownloadManagerImpl(ActivityCallback downloadCallback) {
        this.activityCallback = downloadCallback;
    }

    private void addDownloaderManager(org.gudy.azureus2.core3.download.DownloadManager downloader) {
        synchronized (this) {
            callback(downloader).addDownloadManager(downloader);
        }
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#postGuiInit()
     */
    public void loadSavedDownloadsAndScheduleWriting() {
        loadTorrentDownloads();
    }

    /**
     * This is where torrents are loaded from the last session.
     * If seeding is not enaebled, completed torrents won't be started, they'll be stopped.
     */
    private void loadTorrentDownloads() {
        //this line right here takes a while.
        //System.out.println("DownloadManagerImpl.loadTorrentDownloads() Waiting for azureus core");
        AzureusCore azureusCore = AzureusStarter.getAzureusCore();

        GlobalManager globalManager = azureusCore.getGlobalManager();
        //System.out.println("DownloadManagerImpl.loadTorrentDownloads() Got azureus core");
        List<?> downloadManagers = globalManager.getDownloadManagers();

        List<org.gudy.azureus2.core3.download.DownloadManager> downloads = new ArrayList<org.gudy.azureus2.core3.download.DownloadManager>();
        for (Object obj : downloadManagers) {
            if (obj instanceof org.gudy.azureus2.core3.download.DownloadManager) {
                downloads.add((org.gudy.azureus2.core3.download.DownloadManager) obj);
            }
        }

        for (org.gudy.azureus2.core3.download.DownloadManager obj : downloads) {

            org.gudy.azureus2.core3.download.DownloadManager downloadManager = (org.gudy.azureus2.core3.download.DownloadManager) obj;

            if (downloadManager.getSaveLocation().getParentFile().getAbsolutePath().equals(UpdateSettings.UPDATES_DIR.getAbsolutePath())) {
                LOG.info("Update download: " + downloadManager.getSaveLocation());
                continue;
            }
            
            if (!SharingSettings.SEED_FINISHED_TORRENTS.getValue()) {
                if (downloadManager.getAssumedComplete()) {
                    downloadManager.pause();
                }
            }
            
            if (CommonUtils.isPortable()) {
                updateDownloadManagerPortableSaveLocation(downloadManager);
            }

            addDownloaderManager(downloadManager);
        }
    }

    private void updateDownloadManagerPortableSaveLocation(org.gudy.azureus2.core3.download.DownloadManager downloadManager) {
        boolean hadToPauseIt = false;
        if (downloadManager.getState() != org.gudy.azureus2.core3.download.DownloadManager.STATE_STOPPED) {
            downloadManager.pause();
            hadToPauseIt = true;
        }
        String previousSaveLocation = downloadManager.getSaveLocation().getAbsolutePath();
        String newLocationPrefix = SharingSettings.DEFAULT_TORRENT_DATA_DIR.getAbsolutePath();

        if (!previousSaveLocation.startsWith(newLocationPrefix)) {
            File newSaveLocation = new File(SharingSettings.DEFAULT_TORRENT_DATA_DIR, downloadManager.getSaveLocation().getName());
            if (newSaveLocation.exists()) {
                if (newSaveLocation.isDirectory()) {
                    downloadManager.setDataAlreadyAllocated(false); //absolutely necessary
                    downloadManager.setTorrentSaveDir(newSaveLocation.getAbsolutePath());
                } else if (newSaveLocation.isFile()) {
                    downloadManager.setTorrentSaveDir(SharingSettings.DEFAULT_TORRENT_DATA_DIR.getAbsolutePath());
                }
            }
        }
        
        if (hadToPauseIt) {
            downloadManager.resume();
        }
    }

    private ActivityCallback callback(org.gudy.azureus2.core3.download.DownloadManager dm) {
        return activityCallback;
    }
}
