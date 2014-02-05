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

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.plugins.PluginManager;
import org.gudy.azureus2.plugins.PluginManagerDefaults;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.frostwire.util.OSUtils;

/**
 * Class to initialize the azureus core.
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeManager {

    private final AzureusCore core;

    private VuzeManager() {
        this.core = AzureusCoreFactory.create();
    }

    private static class Loader {
        static VuzeManager INSTANCE = new VuzeManager();
    }

    public static VuzeManager getInstance() {
        return Loader.INSTANCE;
    }

    public AzureusCore getCore() {
        return core;
    }

    GlobalManager getGlobalManager() {
        return core.getGlobalManager();
    }

    public static void setupConfiguration() {
        disableDefaultPlugins();

        if (OSUtils.isAndroid()) {
            SystemTime.TIME_GRANULARITY_MILLIS = 300;

            COConfigurationManager.setParameter("network.max.simultaneous.connect.attempts", 1);
        }
    }

    private static void disableDefaultPlugins() {
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
}
