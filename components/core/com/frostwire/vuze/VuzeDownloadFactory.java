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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerInitialisationAdapter;
import org.gudy.azureus2.core3.download.impl.DownloadManagerAdapter;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.util.HashWrapper;

import com.frostwire.torrent.TorrentUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class VuzeDownloadFactory {

    private VuzeDownloadFactory() {
    }

    public static VuzeDownloadManager create(String torrent, final Set<String> fileSelection, String saveDir, VuzeDownloadListener listener) throws IOException {
        GlobalManager gm = VuzeManager.getInstance().getGlobalManager();
        DownloadManager dm = null;
        VuzeDownloadManager vdm = null;
        boolean initialize = false;

        dm = findDM(gm, torrent);

        if (dm == null) { // new download
            dm = gm.addDownloadManager(torrent, null, saveDir, null, DownloadManager.STATE_WAITING, true, false, new DownloadManagerInitialisationAdapter() {
                @Override
                public void initialised(DownloadManager manager, boolean for_seeding) {
                    setupPartialSelection(manager, fileSelection);
                }

                @Override
                public int getActions() {
                    return 0;
                }
            });

            initialize = true;
        } else { // download already there
            setupPartialSelection(dm, union(fileSelection, VuzeUtils.getSkippedPaths(dm)));

            initialize = false;
        }

        vdm = new VuzeDownloadManager(dm);
        setup(vdm, listener, initialize);

        return vdm;
    }

    private static void setupPartialSelection(DownloadManager dm, Set<String> fileSelection) {
        DiskManagerFileInfo[] fileInfos = dm.getDiskManagerFileInfoSet().getFiles();

        try {
            dm.getDownloadState().suppressStateSave(true);

            if (fileSelection == null || fileSelection.isEmpty()) {
                for (DiskManagerFileInfo fileInfo : fileInfos) {
                    fileInfo.setSkipped(false);
                }
            } else {
                for (DiskManagerFileInfo fileInfo : fileInfos) {
                    File f = fileInfo.getFile(true);
                    if (!fileSelection.contains(f)) {
                        fileInfo.setSkipped(true);
                    }
                }
            }
        } finally {
            dm.getDownloadState().suppressStateSave(false);
        }
    }

    private static void setup(final VuzeDownloadManager vdm, final VuzeDownloadListener listener, boolean initialize) {
        DownloadManager dm = vdm.getDM();
        dm.addListener(new DownloadManagerAdapter() {

            private AtomicBoolean finished = new AtomicBoolean(false);

            @Override
            public void stateChanged(DownloadManager manager, int state) {
                if (state == DownloadManager.STATE_READY) {
                    manager.startDownload();
                }

                if (manager.getAssumedComplete() && finished.compareAndSet(false, true)) {
                    listener.downloadComplete(vdm);
                }
            }
        });

        if (initialize && dm.getState() != DownloadManager.STATE_STOPPED) {
            dm.initialize();
        }
    }

    private static DownloadManager findDM(GlobalManager gm, String torrent) throws IOException {
        DownloadManager dm;
        InputStream is = null;

        try {
            is = new FileInputStream(torrent);
            // using FrostWire fork api for actual reading
            TOTorrent t = TorrentUtils.readFromBEncodedInputStream(is);
            dm = gm.getDownloadManager(new HashWrapper(t.getHash()));
        } finally {
            IOUtils.closeQuietly(is);
        }

        return dm;
    }

    private static Set<String> union(Set<String> s1, Set<String> s2) {
        Set<String> s = new HashSet<String>(s1); // I don't want to modify original sets
        s.addAll(s2);
        return s;
    }
}
