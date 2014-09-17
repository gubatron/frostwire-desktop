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

package com.frostwire.bittorrent.libtorrent;

import com.frostwire.bittorrent.BTDownload;
import com.frostwire.bittorrent.BTEngine;
import com.frostwire.bittorrent.BTEngineListener;
import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.Session;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.TorrentAlert;
import com.frostwire.jlibtorrent.swig.torrent_added_alert;
import com.frostwire.logging.Logger;
import com.limegroup.gnutella.settings.SharingSettings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.limewire.util.CommonUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * @author gubatron
 * @author aldenml
 */
public final class LTEngine implements BTEngine {

    private static final Logger LOG = Logger.getLogger(LTEngine.class);

    private final Session session;
    private final File home;

    private BTEngineListener listener;

    public LTEngine() {
        this.session = new Session();
        this.home = buildHome();

        addEngineListener();
    }

    private static class Loader {
        static LTEngine INSTANCE = new LTEngine();
    }

    public static LTEngine getInstance() {
        return Loader.INSTANCE;
    }

    Session getSession() {
        return session;
    }

    @Override
    public BTEngineListener getListener() {
        return listener;
    }

    @Override
    public void setListener(BTEngineListener listener) {
        this.listener = listener;
    }

    @Override
    public BTDownload download(File torrent, File saveDir) throws IOException {
        LTEngine e = LTEngine.getInstance();

        Session s = e.getSession();
        TorrentHandle th = s.addTorrent(torrent, saveDir);
        FileUtils.copyFile(torrent, new File(home, th.getInfoHash() + ".torrent"));
        LTDownload dl = new LTDownload(th);

        s.addListener(new LTDownloadListener(dl));

        return dl;
    }

    @Override
    public void restoreDownloads() {
        File[] torrents = home.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return FilenameUtils.getExtension(name).equals("torrent");
            }
        });

        File saveDir = SharingSettings.TORRENT_DATA_DIR_SETTING.getValue();

        for (File t : torrents) {
            try {
                session.asyncAddTorrent(t, saveDir);
            } catch (Throwable e) {
                LOG.error("Error restoring torrent download", e);
            }
        }
    }

    private void addEngineListener() {
        session.addListener(new AlertListener() {
            @Override
            public boolean accept(Alert<?> alert) {
                return true;
            }

            @Override
            public void onAlert(Alert<?> alert) {
                //LOG.info(a.message());
                int type = alert.getType();
                if (type == torrent_added_alert.alert_type) {
                    if (listener != null) {
                        listener.downloadAdded(new LTDownload(((TorrentAlert<?>) alert).getTorrentHandle()));
                    }
                }
            }
        });
    }

    private File buildHome() {
        File path = new File(CommonUtils.getUserSettingsDir() + File.separator + "libtorrent" + File.separator);
        if (!path.exists()) {
            path.mkdirs();
        }
        return path;
    }
}
