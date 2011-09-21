package com.limegroup.gnutella.settings;

import org.limewire.setting.BooleanSetting;

/**
 * Settings for Music Player
 */
public class PlayerSettings extends LimeProps {
    
    private PlayerSettings() {}
    
    /**
     * whether or not player should be enabled.
     */
    public static BooleanSetting PLAYER_ENABLED =
        FACTORY.createBooleanSetting("PLAYER_ENABLED", true);
    
    public static BooleanSetting LOOP_PLAYLIST = FACTORY.createBooleanSetting("LOOP_PLAYLIST", true);
    
    public static BooleanSetting SHUFFLE_PLAYLIST = FACTORY.createBooleanSetting("SHUFFLE_PLAYLIST", false);
    
}
