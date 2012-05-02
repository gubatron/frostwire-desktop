package com.limegroup.gnutella.settings;

import org.limewire.setting.BooleanSetting;
import org.limewire.setting.IntSetting;

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

    //	public static final BooleanSetting FROSTCLICK_SEARCH_ENABLED =
    //		FACTORY.createBooleanSetting("FROSTCLICK_SEARCH_ENABLED", true);

    public static final IntSetting MONOVA_WEBSEARCHPERFORMER_MAX_RESULTS = FACTORY.createIntSetting("MONOVA_WEBSEARCHPERFORMER_MAX", 10);

    public static final IntSetting KAT_WEBSEARCHPERFORMER_MAX_RESULTS = FACTORY.createIntSetting("KAT_WEBSEARCHPERFORMER_MAX", 10);

    public static final IntSetting TPB_WEBSEARCHPERFORMER_MAX_RESULTS = FACTORY.createIntSetting("TPB_WEBSEARCHPERFORMER_MAX", 20);

    public static final IntSetting YOUTUBE_WEBSEARCHPERFORMER_MAX_RESULTS = FACTORY.createIntSetting("YOUTUBE_WEBSEARCHPERFORMER_MAX_RESULTS", 5);
}
