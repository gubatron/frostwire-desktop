package com.limegroup.gnutella.settings;

import org.limewire.setting.BooleanSetting;
import org.limewire.setting.FloatSetting;

/**
 * Settings for Music Player
 */
public class PlayerSettings extends LimeProps {

    private PlayerSettings() {
    }
    
    public static BooleanSetting LOOP_PLAYLIST = FACTORY.createBooleanSetting("LOOP_PLAYLIST", true);

    public static BooleanSetting SHUFFLE_PLAYLIST = FACTORY.createBooleanSetting("SHUFFLE_PLAYLIST", false);

    public static FloatSetting PLAYER_VOLUME = FACTORY.createFloatSetting("PLAYER_VOLUME", 0.5f);
}
