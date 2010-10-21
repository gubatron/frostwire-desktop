
package com.limegroup.gnutella.settings;

import org.limewire.setting.BooleanSetting;
import org.limewire.setting.StringSetting;

/**
 * Settings for chat
 */
public class ChatSettings extends LimeProps {
    
    private ChatSettings() {}
    
    /**
	 * Sets whether or not chat should be enabled.
	 */
    public static final BooleanSetting CHAT_ENABLED =
        FACTORY.createBooleanSetting("CHAT_ENABLED", true);

    public static final BooleanSetting CHAT_IRC_ENABLED = 
    	FACTORY.createBooleanSetting("CHAT_IRC_ENABLED", true);
    
    /** Sets the default nick for this user */
    public static final StringSetting CHAT_IRC_NICK =
	FACTORY.createStringSetting("CHAT_IRC_NICK","FW_Guest_"+
                     Integer.toString(new java.util.Random().nextInt()%10000));
    
    /** Sets the default server where FrostWire will connect */
    public static final StringSetting CHAT_SERVER = 
    FACTORY.createStringSetting("CHAT_SERVER","chat.peercommons.net");
    
    /**
	 * Sets whether or not chat should be enabled.
	 */
    public static final BooleanSetting SMILEYS_ENABLED =
        FACTORY.createBooleanSetting("SMILEYS_ENABLED", UISettings.SMILEYS_IN_CHAT.getValue());
}