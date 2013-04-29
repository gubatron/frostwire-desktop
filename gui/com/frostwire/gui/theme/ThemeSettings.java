/*
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

package com.frostwire.gui.theme;

import java.awt.Color;
import java.io.File;

import org.limewire.setting.ColorSetting;
import org.limewire.setting.FileSetting;
import org.limewire.util.CommonUtils;

import com.limegroup.gnutella.settings.LimeProps;

/**
 * Class for handling all LimeWire settings that are stored to disk.  To
 * add a new setting, simply add a new public static member to the list
 * of settings.  Construct settings using the <tt>FACTORY</tt> instance
 * from the <tt>AbstractSettings</tt> superclass.  Each setting factory
 * constructor takes the name of the key and the default value, and all
 * settings are typed.  Choose the correct <tt>Setting</tt> factory constructor
 * for your setting type.  It is also important to choose a unique string key
 * for your setting name -- otherwise there will be conflicts, and a runtime
 * exception will be thrown.
 */
public final class ThemeSettings extends LimeProps {
    
    private ThemeSettings() {}
            
    /**
     * The extension for theme packs to allow people to search for them --
     * stands for "LimeWire Theme Pack".
     */
    public static final String EXTENSION = "fwtp";
    
    public static final File SKINS_FILE = new File(CommonUtils.getUserSettingsDir(), "skins.dat");
    
    public static final File THEME_DIR_FILE =
		new File(CommonUtils.getUserSettingsDir(), "themes");
    
    /**
     * The default name of the theme file name for OS X.
     */
    public static final String PINSTRIPES_OSX_THEME_NAME =
		"pinstripes_theme_osx."+EXTENSION;
		
    /**
     * The metal theme name.
     */
    public static final String BRUSHED_METAL_OSX_THEME_NAME =
        "brushed_metal_theme_osx."+EXTENSION;
    
    /**
     * The default name of the windows laf theme file name.
     */
    public static final String WINDOWS_LAF_THEME_NAME =
        "windows_theme."+EXTENSION;
        
    /**
     * The default name of the gtk laf theme file name.
     */
    public static final String GTK_LAF_THEME_NAME =
        "GTK_theme." + EXTENSION;
        
    /**
     * The default name of the theme file name for non-OS X pro users.
     */
    public static final String PRO_THEME_NAME =
        "frostwirePro_theme."+EXTENSION;
        
    /**
     * The name for the unknown theme file.
     */
    public static final String OTHER_THEME_NAME =
        "other_theme." + EXTENSION;
		
    /**
     * The full path to the metal theme file on OS X.
     */
    static final File BRUSHED_METAL_OSX_THEME_FILE =
		new File(THEME_DIR_FILE, BRUSHED_METAL_OSX_THEME_NAME);		
		
    /** 
     * The full path to the windows theme file for the windows LAF
     */
    static final File WINDOWS_LAF_THEME_FILE =
        new File(THEME_DIR_FILE, WINDOWS_LAF_THEME_NAME);
        
    /**
     * The full path to the GTK theme file for the GTK LAF
     */
    static final File GTK_LAF_THEME_FILE =
        new File(THEME_DIR_FILE, GTK_LAF_THEME_NAME);
        
    /**
     * The full path to the pro only theme.
     */
    static final File PRO_THEME_FILE =
        new File(THEME_DIR_FILE, PRO_THEME_NAME);
        
    /**
     * The path for the 'other' theme name.
     */
    static final File OTHER_THEME_FILE =
        new File(THEME_DIR_FILE, OTHER_THEME_NAME);
    
    public static ColorSetting PLAYING_DATA_LINE_COLOR = FACTORY.createColorSetting("PLAYING_DATA_LINE_COLOR", new Color(7, 170, 0));
    
    public static ColorSetting FILE_NO_EXISTS_DATA_LINE_COLOR = FACTORY.createColorSetting("FILE_NO_EXISTS_DATA_LINE_COLOR", Color.RED);
    
    
    /**
     * Determines whether or not the current theme file is the default theme
     * file.
     *
     * @return <tt>true</tt> if the current theme file is the default,
     *  otherwise <tt>false</tt>
     */
    public static boolean isDefaultTheme() {
        return THEME_FILE.getValue().equals(THEME_DEFAULT);
    }
    
    /**
     * Determines if the current theme is the GTK theme.
     */
    public static boolean isGTKTheme() {
        return false;//THEME_FILE.getValue().equals(GTK_LAF_THEME_FILE);
    }
    
    /** 
     * Determines whether or not the current theme is the windows theme,
     * designed to be used for the windows laf.
     * @return <tt>true</tt> if the current theme is the windows theme,
     *  otherwise <tt>false</tt>
     */
    public static boolean isWindowsTheme() {
        return THEME_FILE.getValue().equals(WINDOWS_LAF_THEME_FILE);
    }
    
    /**
     * Determines if the theme is the brushed metal theme.
     */
    public static boolean isBrushedMetalTheme() {
        return THEME_FILE.getValue().equals(BRUSHED_METAL_OSX_THEME_FILE);
    }
    
    /**
     * Determines if the current theme is the native OSX theme.
     */
    public static boolean isNativeOSXTheme() {
        return false;//OSUtils.isMacOSX() &&
              //(isPinstripesTheme() || isBrushedMetalTheme());
    }
    
    /**
     * Determines if the current theme is the native theme.
     */
    public static boolean isNativeTheme() {
        return isNativeOSXTheme() || isWindowsTheme() || isGTKTheme();
    }
    
    /**
     * Determines if the current theme is the 'other' theme.
     */
    public static boolean isOtherTheme() {
        return THEME_FILE.getValue().equals(OTHER_THEME_FILE);
    }       
    
    /**
     * Setting for the default theme file to use for FrostWire display.
     */
    public static final File THEME_DEFAULT;
    public static final File THEME_DEFAULT_DIR;
    static {
        THEME_DEFAULT = GTK_LAF_THEME_FILE;
        THEME_DEFAULT_DIR = new File(THEME_DIR_FILE, "GTK_theme");
    }
	
	/**
	 * Setting for the file name of the theme file.
	 */
	public static final FileSetting THEME_FILE =
		FACTORY.createFileSetting("THEME_FILE", THEME_DEFAULT);
	
	/**
	 * Setting for the file name of the theme directory.
	 */
	public static final FileSetting THEME_DIR =
		FACTORY.createFileSetting("THEME_DIR", THEME_DEFAULT_DIR);
    
    
    public static final ColorSetting DEFAULT_TABLE_EVEN_ROW_COLOR = FACTORY.createColorSetting("DEFAULT_TABLE_EVEN_ROW_COLOR", new Color(255, 255, 255));
    public static final ColorSetting DEFAULT_TABLE_ODD_ROW_COLOR = FACTORY.createColorSetting("DEFAULT_TABLE_ODD_ROW_COLOR", new Color(0xf2fafe));
    public static final ColorSetting DEFAULT_TIP_OF_THE_DAY_PANEL_COLOR = FACTORY.createColorSetting("DEFAULT_TIP_OF_THE_DAY_PANEL_COLOR", new Color(248, 248, 255));
}	