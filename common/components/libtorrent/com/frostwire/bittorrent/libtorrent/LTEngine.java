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
import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.Session;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.logging.Logger;

import java.io.File;

/**
 * @author gubatron
 * @author aldenml
 */
public final class LTEngine implements BTEngine {

    private static final Logger LOG = Logger.getLogger(LTEngine.class);

    private final Session session;

    private File home;

    public LTEngine() {
        this.session = new Session();

        addEngineListener();
    }

    private static class Loader {
        static LTEngine INSTANCE = new LTEngine();
    }

    public static LTEngine getInstance() {
        return Loader.INSTANCE;
    }

    @Override
    public File getHome() {
        return home;
    }

    @Override
    public void setHome(File home) {
        this.home = home;
    }

    Session getSession() {
        return session;
    }

    @Override
    public BTDownload download(File torrent, File saveDir) {
        LTEngine e = LTEngine.getInstance();

        Session s = e.getSession();
        TorrentHandle th = s.addTorrent(torrent, saveDir);
        LTDownload dl = new LTDownload(e, th, torrent);

        s.addListener(new LTDownloadListener(dl));

        return dl;
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
            }
        });
    }
}
