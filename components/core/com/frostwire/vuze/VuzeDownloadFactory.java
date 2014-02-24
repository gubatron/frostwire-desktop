/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
 *
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

package com.frostwire.vuze;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerInitialisationAdapter;
import org.gudy.azureus2.core3.download.impl.DownloadManagerAdapter;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.util.HashWrapper;

import com.frostwire.logging.Logger;
import com.frostwire.torrent.TOTorrent;
import com.frostwire.torrent.TOTorrentException;
import com.frostwire.torrent.TorrentUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeDownloadFactory {

    private static final Logger LOG = Logger.getLogger(VuzeDownloadFactory.class);

    private VuzeDownloadFactory() {
    }

    public static VuzeDownloadManager create(String torrent, final Set<String> selection, String saveDir, VuzeDownloadListener listener) throws IOException {
        // this args checking is critical
        if (torrent == null) {
            throw new IllegalArgumentException("Torrent file path can't be null");
        }
        if (saveDir == null) {
            throw new IllegalArgumentException("Torrent data save dir can't be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Download manager listener can't be null");
        }

        GlobalManager gm = VuzeManager.getInstance().getGlobalManager();

        DownloadManager dm = findDM(gm, torrent);
        VuzeDownloadManager vdm = null;

        if (dm == null) { // new download
            dm = gm.addDownloadManager(torrent, null, saveDir, null, DownloadManager.STATE_WAITING, true, false, new DownloadManagerInitialisationAdapter() {
                @Override
                public void initialised(DownloadManager manager, boolean for_seeding) {
                    setupPartialSelection(manager, selection);
                }

                @Override
                public int getActions() {
                    return ACT_NONE;
                }
            });

            vdm = new VuzeDownloadManager(dm);
            setupListener(vdm, listener);

        } else { // modify the existing one
            vdm = VuzeDownloadManager.getVDM(dm);
            vdm.setSkipped(selection, true);

            if (dm.getState() == DownloadManager.STATE_STOPPED) {
                dm.initialize();
            }
        }

        return vdm;
    }

    private static DownloadManager findDM(GlobalManager gm, String torrent) throws IOException {
        InputStream is = null;

        try {
            // using fork api for actual reading

            is = new FileInputStream(torrent);

            TOTorrent t = TorrentUtils.readFromBEncodedInputStream(is);

            return gm.getDownloadManager(new HashWrapper(t.getHash()));

        } catch (TOTorrentException e) {
            throw new IOException("Unable to read the torrent", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private static void setupListener(final VuzeDownloadManager dm, final VuzeDownloadListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Download manager listener can't be null");
        }

        dm.getDM().addListener(new DownloadManagerAdapter() {
            @Override
            public void stateChanged(DownloadManager manager, int state) {
                if (state == DownloadManager.STATE_READY) {
                    manager.startDownload();
                } else {
                    if (listener != null) {
                        try {
                            listener.stateChanged(dm, state);
                        } catch (Throwable e) {
                            LOG.error("Error calling download manager listener", e);
                        }
                    }
                }
            }

            @Override
            public void downloadComplete(DownloadManager manager) {
                if (listener != null) {
                    try {
                        listener.downloadComplete(dm);
                    } catch (Throwable e) {
                        LOG.error("Error calling download manager listener", e);
                    }
                }
            }
        });

        if (dm.getDM().getState() != DownloadManager.STATE_STOPPED) {
            dm.getDM().initialize();
        }
    }

    // this method modify the partial selection by only adding new paths.
    private static void setupPartialSelection(DownloadManager dm, Set<String> paths) {
        DiskManagerFileInfo[] infs = dm.getDiskManagerFileInfoSet().getFiles();

        try {
            dm.getDownloadState().suppressStateSave(true);

            if (paths == null || paths.isEmpty()) {
                for (DiskManagerFileInfo inf : infs) {
                    if (inf.isSkipped()) { // I don't want to trigger any internal logic
                        inf.setSkipped(false);
                    }
                }
            } else {
                for (DiskManagerFileInfo inf : infs) {
                    String path = inf.getFile(false).getPath();
                    if (inf.isSkipped() && paths.contains(path)) { // same reasoning
                        inf.setSkipped(false);
                    }
                }
            }
        } finally {
            dm.getDownloadState().suppressStateSave(false);
        }
    }
}
