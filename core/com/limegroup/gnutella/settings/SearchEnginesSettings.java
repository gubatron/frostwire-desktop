/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, 2013, FrostWire(R). All rights reserved.
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

package com.limegroup.gnutella.settings;

import org.limewire.setting.BooleanSetting;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SearchEnginesSettings extends LimeProps {
    // In the near future, we will refactor this code to allow a configurable amount of
    // search providers.

    public static final BooleanSetting CLEARBITS_SEARCH_ENABLED = FACTORY.createBooleanSetting("CLEARBITS_SEARCH2_ENABLED", true);

    public static final BooleanSetting ISOHUNT_SEARCH_ENABLED = FACTORY.createBooleanSetting("ISOHUNT_SEARCH2_ENABLED", true);

    public static final BooleanSetting MININOVA_SEARCH_ENABLED = FACTORY.createBooleanSetting("MININOVA_SEARCH2_ENABLED", true);

    public static final BooleanSetting KAT_SEARCH_ENABLED = FACTORY.createBooleanSetting("KAT_SEARCH2_ENABLED", true);

    public static final BooleanSetting EXTRATORRENT_SEARCH_ENABLED = FACTORY.createBooleanSetting("EXTRATORRENT_SEARCH2_ENABLED", true);

    public static final BooleanSetting VERTOR_SEARCH_ENABLED = FACTORY.createBooleanSetting("VERTOR_SEARCH2_ENABLED", true);

    public static final BooleanSetting TPB_SEARCH_ENABLED = FACTORY.createBooleanSetting("TPB_SEARCH2_ENABLED", true);

    public static final BooleanSetting MONOVA_SEARCH_ENABLED = FACTORY.createBooleanSetting("MONOVA_SEARCH2_ENABLED", true);

    public static final BooleanSetting YOUTUBE_SEARCH_ENABLED = FACTORY.createBooleanSetting("YOUTUBE_SEARCH2_ENABLED", true);

    public static final BooleanSetting SOUNDCLOUD_SEARCH_ENABLED = FACTORY.createBooleanSetting("SOUNDCLOUD_SEARCH2_ENABLED", true);

    public static final BooleanSetting ARCHIVEORG_SEARCH_ENABLED = FACTORY.createBooleanSetting("ARCHIVEORG_SEARCH2_ENABLED", true);

    public static final BooleanSetting FROSTCLICK_SEARCH_ENABLED = FACTORY.createBooleanSetting("FROSTCLICK_SEARCH_ENABLED", true);
    
}
