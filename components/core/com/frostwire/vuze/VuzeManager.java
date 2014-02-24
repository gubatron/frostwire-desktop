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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.internat.IntegratedResourceBundle;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.util.DisplayFormatters;
import org.gudy.azureus2.core3.util.HashWrapper;
import org.gudy.azureus2.core3.util.SystemProperties;
import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.plugins.PluginManager;
import org.gudy.azureus2.plugins.PluginManagerDefaults;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreRunningListener;
import com.frostwire.logging.Logger;
import com.frostwire.util.OSUtils;

/**
 * Class to initialize the azureus core.
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeManager {

    private static final Logger LOG = Logger.getLogger(VuzeManager.class);

    private static VuzeConfiguration conf = null;

    private final AzureusCore core;

    private VuzeManager() {
        if (conf == null) {
            throw new IllegalStateException("no config set");
        }

        setupConfiguration();

        this.core = AzureusCoreFactory.create();
        this.core.start();
    }

    private static class Loader {
        static VuzeManager INSTANCE = new VuzeManager();
    }

    public static VuzeManager getInstance() {
        return Loader.INSTANCE;
    }

    AzureusCore getCore() {
        return core;
    }

    GlobalManager getGlobalManager() {
        return core.getGlobalManager();
    }

    public VuzeDownloadManager find(byte[] hash) {
        GlobalManager gm = getGlobalManager();
        DownloadManager dm = gm.getDownloadManager(new HashWrapper(hash));
        if (dm != null) {
            return (VuzeDownloadManager) dm.getUserData(VuzeDownloadManager.VUZE_DOWNLOAD_MANAGER_OBJECT_KEY);
        } else {
            return null;
        }
    }

    public void loadTorrents(final boolean stop, final LoadTorrentsListener listener) {
        AzureusCoreFactory.addCoreRunningListener(new AzureusCoreRunningListener() {

            @Override
            public void azureusCoreRunning(AzureusCore core) {
                List<VuzeDownloadManager> dms = new ArrayList<VuzeDownloadManager>();

                GlobalManager gm = core.getGlobalManager();

                for (DownloadManager dm : gm.getDownloadManagers()) {
                    VuzeDownloadManager vdm = new VuzeDownloadManager(dm);

                    if (stop && vdm.isComplete()) {
                        vdm.stop();
                    }

                    dms.add(vdm);
                }

                listener.onLoad(dms);
            }
        });
    }

    public long getDataReceiveRate() {
        return core.getGlobalManager().getStats().getDataReceiveRate() / 1000;
    }

    public long getDataSendRate() {
        return core.getGlobalManager().getStats().getDataSendRate() / 1000;
    }

    public void pause() {
        core.getGlobalManager().pauseDownloads();
    }

    public void resume() {
        core.getGlobalManager().resumeDownloads();
    }

    public void setParameter(String key, long value) {
        COConfigurationManager.setParameter(key, value);
        COConfigurationManager.save();
    }

    public void revertToDefaultConfiguration() {
        COConfigurationManager.resetToDefaults();
        autoAdjustBittorrentSpeed();
    }

    public static void setConfiguration(VuzeConfiguration conf) {
        VuzeManager.conf = conf;

        // before anything else
        setApplicationPath(conf.getConfigPath());
        SystemProperties.setUserPath(conf.getConfigPath());
    }

    private void setMessages(Map<String, String> msgs) {
        IntegratedResourceBundle res = new IntegratedResourceBundle(new EmptyResourceBundle(), new HashMap<String, ClassLoader>());

        for (Entry<String, String> kv : msgs.entrySet()) {
            res.addString(kv.getKey(), kv.getValue());
        }

        try {
            Field f = MessageText.class.getDeclaredField("DEFAULT_BUNDLE");
            f.setAccessible(true);
            f.set(null, res);
        } catch (Throwable e) {
            LOG.error("Unable to set vuze messages", e);
        }
        DisplayFormatters.loadMessages();
    }

    private void setupConfiguration() {
        System.setProperty("azureus.loadplugins", "0"); // disable third party azureus plugins

        SystemProperties.APPLICATION_NAME = "azureus";

        COConfigurationManager.setParameter("Auto Adjust Transfer Defaults", false);
        COConfigurationManager.setParameter("General_sDefaultTorrent_Directory", conf.getTorrentsPath());

        disableDefaultPlugins();

        if (OSUtils.isAndroid()) {
            SystemTime.TIME_GRANULARITY_MILLIS = 300;

            COConfigurationManager.setParameter("network.tcp.write.select.time", 1000);
            COConfigurationManager.setParameter("network.tcp.write.select.min.time", 1000);
            COConfigurationManager.setParameter("network.tcp.read.select.time", 1000);
            COConfigurationManager.setParameter("network.tcp.read.select.min.time", 1000);
            COConfigurationManager.setParameter("network.control.write.idle.time", 1000);
            COConfigurationManager.setParameter("network.control.read.idle.time", 1000);

            COConfigurationManager.setParameter("network.max.simultaneous.connect.attempts", 1);
        }

        if (conf.getMessages() != null) {
            setMessages(conf.getMessages());
        }
    }

    private void disableDefaultPlugins() {
        PluginManagerDefaults pmd = PluginManager.getDefaults();

        pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_START_STOP_RULES, false);
        pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_REMOVE_RULES, false);
        pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_SHARE_HOSTER, false);
        pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_DEFAULT_TRACKER_WEB, false);

        pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_PLUGIN_UPDATE_CHECKER, false);
        pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_CORE_UPDATE_CHECKER, false);
        pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_CORE_PATCH_CHECKER, false);
        pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_PLATFORM_CHECKER, false);

        pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_BUDDY, false);
        pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_RSS, false);

        if (OSUtils.isAndroid()) {
            pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_DHT, false);
            pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_DHT_TRACKER, false);
            pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_MAGNET, false);
            pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_EXTERNAL_SEED, false);
            pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_LOCAL_TRACKER, false);
            pmd.setDefaultPluginEnabled(PluginManagerDefaults.PID_TRACKER_PEER_AUTH, false);
        }
    }

    private void autoAdjustBittorrentSpeed() {
        if (COConfigurationManager.getBooleanParameter("Auto Adjust Transfer Defaults")) {

            int up_limit_bytes_per_sec = 0;//getEstimatedUploadCapacityBytesPerSec().getBytesPerSec();
            //int down_limit_bytes_per_sec    = 0;//getEstimatedDownloadCapacityBytesPerSec().getBytesPerSec();

            int up_kbs = up_limit_bytes_per_sec / 1024;

            final int[][] settings = {

            { 56, 2, 20, 40 }, // 56 k/bit
                    { 96, 3, 30, 60 }, { 128, 3, 40, 80 }, { 192, 4, 50, 100 }, // currently we don't go lower than this
                    { 256, 4, 60, 200 }, { 512, 5, 70, 300 }, { 1024, 6, 80, 400 }, // 1Mbit
                    { 2 * 1024, 8, 90, 500 }, { 5 * 1024, 10, 100, 600 }, { 10 * 1024, 20, 110, 750 }, // 10Mbit
                    { 20 * 1024, 30, 120, 900 }, { 50 * 1024, 40, 130, 1100 }, { 100 * 1024, 50, 140, 1300 }, { -1, 60, 150, 1500 }, };

            int[] selected = settings[settings.length - 1];

            // note, we start from 3 to avoid over-restricting things when we don't have
            // a reasonable speed estimate

            for (int i = 3; i < settings.length; i++) {

                int[] setting = settings[i];

                int line_kilobit_sec = setting[0];

                // convert to upload kbyte/sec assuming 80% achieved

                int limit = (line_kilobit_sec / 8) * 4 / 5;

                if (up_kbs <= limit) {

                    selected = setting;

                    break;
                }
            }

            int upload_slots = selected[1];
            int connections_torrent = selected[2];
            int connections_global = selected[3];

            if (upload_slots != COConfigurationManager.getIntParameter("Max Uploads")) {
                COConfigurationManager.setParameter("Max Uploads", upload_slots);
                COConfigurationManager.setParameter("Max Uploads Seeding", upload_slots);
            }

            if (connections_torrent != COConfigurationManager.getIntParameter("Max.Peer.Connections.Per.Torrent")) {
                COConfigurationManager.setParameter("Max.Peer.Connections.Per.Torrent", connections_torrent);
                COConfigurationManager.setParameter("Max.Peer.Connections.Per.Torrent.When.Seeding", connections_torrent / 2);
            }

            if (connections_global != COConfigurationManager.getIntParameter("Max.Peer.Connections.Total")) {
                COConfigurationManager.setParameter("Max.Peer.Connections.Total", connections_global);
            }

            COConfigurationManager.save();
        }
    }

    private static void setApplicationPath(String path) {
        try {
            Field f = SystemProperties.class.getDeclaredField("app_path");
            f.setAccessible(true);
            f.set(null, path);
        } catch (Throwable e) {
            throw new RuntimeException("Unable to set vuze application path", e);
        }
    }

    public static interface LoadTorrentsListener {

        public void onLoad(List<VuzeDownloadManager> dms);
    }
}
