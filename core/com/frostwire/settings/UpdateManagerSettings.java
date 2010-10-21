package com.frostwire.settings;

import org.limewire.setting.BooleanSetting;

import com.limegroup.gnutella.settings.LimeProps;

public class UpdateManagerSettings extends LimeProps {
	private UpdateManagerSettings() {

	}
	
	/** Wether or not to show promotion overlays */
	public static BooleanSetting SHOW_PROMOTION_OVERLAYS = (BooleanSetting) FACTORY.createBooleanSetting("SHOW_PROMOTION_OVERLAYS", true).setAlwaysSave(true); 
}
