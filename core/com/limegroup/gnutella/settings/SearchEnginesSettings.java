/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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
import org.limewire.setting.IntSetting;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SearchEnginesSettings extends LimeProps {
    // In the near future, we will refactor this code to allow a configurable amount of
    // search providers.

    public static final BooleanSetting CLEARBITS_SEARCH_ENABLED = FACTORY.createBooleanSetting("CLEARBITS_SEARCH_ENABLED", true);

    public static final BooleanSetting ISOHUNT_SEARCH_ENABLED = FACTORY.createBooleanSetting("ISOHUNT_SEARCH_ENABLED", true);

    public static final BooleanSetting MININOVA_SEARCH_ENABLED = FACTORY.createBooleanSetting("MININOVA_SEARCH_ENABLED", true);

    public static final BooleanSetting KAT_SEARCH_ENABLED = FACTORY.createBooleanSetting("KAT_SEARCH_ENABLED", true);

    public static final BooleanSetting EXTRATORRENT_SEARCH_ENABLED = FACTORY.createBooleanSetting("EXTRATORRENT_SEARCH_ENABLED", true);

    public static final BooleanSetting VERTOR_SEARCH_ENABLED = FACTORY.createBooleanSetting("VERTOR_SEARCH_ENABLED", true);

    public static final BooleanSetting TPB_SEARCH_ENABLED = FACTORY.createBooleanSetting("TPB_SEARCH_ENABLED", true);

    public static final BooleanSetting MONOVA_SEARCH_ENABLED = FACTORY.createBooleanSetting("MONOVA_SEARCH_ENABLED", true);

    public static final BooleanSetting YOUTUBE_SEARCH_ENABLED = FACTORY.createBooleanSetting("YOUTUBE_SEARCH_ENABLED", true);
    
    public static final BooleanSetting SOUNDCLOUD_SEARCH_ENABLED = FACTORY.createBooleanSetting("SOUNDCLOUD_SEARCH_ENABLED", true);

    //	public static final BooleanSetting FROSTCLICK_SEARCH_ENABLED =
    //		FACTORY.createBooleanSetting("FROSTCLICK_SEARCH_ENABLED", true);

    public static final IntSetting MONOVA_WEBSEARCHPERFORMER_MAX_RESULTS = FACTORY.createIntSetting("MONOVA_WEBSEARCHPERFORMER_MAX", 10);

    public static final IntSetting KAT_WEBSEARCHPERFORMER_MAX_RESULTS = FACTORY.createIntSetting("KAT_WEBSEARCHPERFORMER_MAX", 10);

    public static final IntSetting TPB_WEBSEARCHPERFORMER_MAX_RESULTS = FACTORY.createIntSetting("TPB_WEBSEARCHPERFORMER_MAX", 20);

    public static final IntSetting YOUTUBE_WEBSEARCHPERFORMER_MAX_RESULTS = FACTORY.createIntSetting("YOUTUBE_WEBSEARCHPERFORMER_MAX_RESULTS", 15);
    
    public static final IntSetting SOUNDCLOUD_WEBSEARCHPERFORMER_MAX_RESULTS = FACTORY.createIntSetting("SOUNDCLOUD_WEBSEARCHPERFORMER_MAX_RESULTS", 20);
}
