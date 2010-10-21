package com.limegroup.gnutella.settings;

import java.io.File;

import org.limewire.setting.LongSetting;
import org.limewire.setting.SettingsFactory;
import org.limewire.setting.StringSetting;
    
public class PluginsSettings extends LimeWireSettings {

    private static final PluginsSettings INSTANCE =
        new PluginsSettings();
	
	
    private static final SettingsFactory FACTORY =
        INSTANCE.getFactory();	
	
	private PluginsSettings() { 
		super("plugins.props", "FrostWire plugins properties file");
	}
	
    public static PluginsSettings instance() {
        return INSTANCE;
    }
    
    /** Setting to store the default folder where to look for plugins */
    public static final StringSetting PLUGINS_FOLDER =
    	FACTORY.createStringSetting("PLUGINS_FOLDER", "plugins");    
    
    /** The name of the file that will store the 
     * names of the plugins that are currently installed
     */
    public static final StringSetting INSTALLED_PLUGINS_FILE =
    	FACTORY.createStringSetting("INSTALLED_PLUGINS_FILE",
    			PLUGINS_FOLDER.getValue() + 
    			File.separator + 
    			"installed_plugins.dat");


    /** The name of the file that will store the available
        plugins that we checked remotely the last time.
        So that we don't overwhelm plugins.frostwire.com/list
        everytime we open FrostWire.
    */
    public static final StringSetting AVAILABLE_PLUGINS_FILE =
        FACTORY.createStringSetting("AVAILABLE_PLUGINS_FILE",
                                    PLUGINS_FOLDER.getValue() +
                                    File.separator + 
                                    "available_plugins.dat");

    /**
     * Ideas for other properties that'll come in handy:
     *  - Period between each check for plugin updates (every X days?)
     *  - ...
     */
    public static final LongSetting LAST_TIME_PLUGINS_CHECKED = 
        FACTORY.createLongSetting("LAST_TIME_PLUGINS_CHECKED",0);
} //PluginsSettings