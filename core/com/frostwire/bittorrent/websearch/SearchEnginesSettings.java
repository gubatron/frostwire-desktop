package com.frostwire.bittorrent.websearch;

import org.limewire.setting.BooleanSetting;

import com.limegroup.gnutella.settings.LimeProps;

public class SearchEnginesSettings extends LimeProps {
	
	public static final BooleanSetting CLEARBITS_SEARCH_ENABLED =
		FACTORY.createBooleanSetting("CLEARBITS_SEARCH_ENABLED", true);
	
	public static final BooleanSetting ISOHUNT_SEARCH_ENABLED =
		FACTORY.createBooleanSetting("ISOHUNT_SEARCH_ENABLED", true);

	public static final BooleanSetting MININOVA_SEARCH_ENABLED =
		FACTORY.createBooleanSetting("MININOVA_SEARCH_ENABLED", true);

//	public static final BooleanSetting FROSTCLICK_SEARCH_ENABLED =
//		FACTORY.createBooleanSetting("FROSTCLICK_SEARCH_ENABLED", true);
	
}
