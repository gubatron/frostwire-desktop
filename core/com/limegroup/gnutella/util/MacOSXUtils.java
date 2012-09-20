package com.limegroup.gnutella.util;

import org.limewire.service.ErrorService;
import org.limewire.util.CommonUtils;

/**
 * A collection of utility methods for OSX.
 * These methods should only be called if run from OSX,
 * otherwise ClassNotFoundErrors may occur.
 *
 * To determine if the Cocoa Foundation classes are present,
 * use the method CommonUtils.isCocoaFoundationAvailable().
 */
public class MacOSXUtils {
    
    static {
        	try {
        		System.loadLibrary("MacOSXUtilsLeopard");
        	} catch (UnsatisfiedLinkError err) {
            	ErrorService.error(err);
            }
    }
    
    private MacOSXUtils() {}
    
    /**
     * The name of the app that launches.
     */
    private static final String APP_NAME = "FrostWire.app";

    /**
     * Modifies mac OSX environment to run this application on startup
     */
    public static void setLoginStatus(boolean allow) {
    	
    	String rawDir = CommonUtils.getExecutableDirectory();
    	String path = rawDir.substring(0, rawDir.indexOf(APP_NAME) + APP_NAME.length());
    	
        SetLoginStatusNative(allow, path );
    }
    
    /**
     * Gets the full user's name.
     */
    public static String getUserName() {
        return GetCurrentFullUserName();
    }
    
    /**
     * Gets the full user's name.
     */
    private static final native String GetCurrentFullUserName();
    
    /**
     * [Un]registers FrostWire from the startup items list.
     */
    private static final native void SetLoginStatusNative(boolean allow, String appPath);
}