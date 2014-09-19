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

import com.frostwire.bittorrent.BTDownload;
import com.frostwire.bittorrent.BTEngine;
import com.frostwire.bittorrent.BTEngineFactory;
import com.frostwire.bittorrent.BTEngineListener;
import com.frostwire.logging.Logger;
import com.limegroup.gnutella.settings.SharingSettings;

public class DownloadManagerImpl implements DownloadManager {

    private static final Logger LOG = Logger.getLogger(DownloadManagerImpl.class);

    private final ActivityCallback activityCallback;

    public DownloadManagerImpl(ActivityCallback downloadCallback) {
        this.activityCallback = downloadCallback;
    }

    private void addDownload(BTDownload dl) {
        synchronized (this) {
            activityCallback.addDownload(dl);
        }
    }

    public void loadSavedDownloadsAndScheduleWriting() {

        BTEngine engine = BTEngineFactory.getInstance();

        engine.setListener(new BTEngineListener() {
            @Override
            public void downloadAdded(BTDownload dl) {

                //TODO:BITTORRENT
//                if (downloadManager.getSaveLocation().getParentFile().getAbsolutePath().equals(UpdateSettings.UPDATES_DIR.getAbsolutePath())) {
//                    LOG.info("Update download: " + downloadManager.getSaveLocation());
//                    continue;
//                }

//                if (CommonUtils.isPortable()) {
//                    updateDownloadManagerPortableSaveLocation(downloadManager);
//                }

                addDownload(dl);
            }
        });

        engine.restoreDownloads(SharingSettings.TORRENT_DATA_DIR_SETTING.getValue());
    }

    /*
    TODO:BITTORRENT
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
    }*/
}
