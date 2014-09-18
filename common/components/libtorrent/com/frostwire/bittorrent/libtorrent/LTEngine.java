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
import com.frostwire.jlibtorrent.LibTorrent;
import com.frostwire.jlibtorrent.Session;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.SaveResumeDataAlert;
import com.frostwire.jlibtorrent.alerts.TorrentAlert;
import com.frostwire.jlibtorrent.swig.block_finished_alert;
import com.frostwire.jlibtorrent.swig.entry;
import com.frostwire.jlibtorrent.swig.save_resume_data_alert;
import com.frostwire.jlibtorrent.swig.torrent_added_alert;
import com.frostwire.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

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

    private File home;
    private BTEngineListener listener;

    private long lastTimeSavedResumeData;

    public LTEngine() {
        this.session = new Session();


        this.home = new File(".").getAbsoluteFile();

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
    public File getHome() {
        return home;
    }

    @Override
    public void setHome(File home) {
        this.home = home;
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

        return dl;
    }

    @Override
    public void restoreDownloads(File saveDir, boolean paused) {
        File[] torrents = home.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return FilenameUtils.getExtension(name).equals("torrent");
            }
        });

        for (File t : torrents) {
            try {
                session.asyncAddTorrent(t, saveDir, paused);
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
                if (listener == null) {
                    return;
                }

                int type = alert.getType();

                if (type == torrent_added_alert.alert_type) {
                    listener.downloadAdded(new LTDownload(((TorrentAlert<?>) alert).getTorrentHandle()));
                }

                if (type == save_resume_data_alert.alert_type) {
                    saveResumeData((SaveResumeDataAlert) alert);
                }

                if (type == block_finished_alert.alert_type) {
                    doResumeData((TorrentAlert<?>) alert);
                }
            }
        });
    }

    public void saveResumeData(SaveResumeDataAlert alert) {
        try {
            TorrentHandle th = alert.getTorrentHandle();
            entry d = alert.getResumeData();
            byte[] arr = LibTorrent.entry2bytes(d);
            FileUtils.writeByteArrayToFile(new File(home, th.getInfoHash() + ".resume"), arr);
        } catch (IOException e) {
            LOG.warn("Error saving resume data", e);
        }
    }

    public void doResumeData(TorrentAlert<?> alert) {
        TorrentHandle th = alert.getTorrentHandle();
        if (th.needSaveResumeData()) {
            th.saveResumeData();
        }
    }
}
