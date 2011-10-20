package com.limegroup.gnutella.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.limewire.setting.SettingsFactory;
import org.limewire.util.CommonUtils;
import org.limewire.util.OSUtils;
import org.limewire.util.SystemUtils;
import org.limewire.util.SystemUtils.SpecialLocations;
import org.limewire.util.VersionUtils;

import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.settings.ApplicationSettings;


/**
 * This class handles common utility functions that many classes
 * may want to access.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
public final class FrostWireUtils {

	/** 
	 * Constant for the current version of FrostWire.
	 */
	private static final String FROSTWIRE_VERSION = "5.2.3";
    
    /** True if this is a beta. */
    private static final boolean betaVersion = true;
    
    /** True if this is an alpha */
    private static final boolean alphaVersion = false;

    /**
     * The cached value of the major revision number.
     */
    private static final int _majorVersionNumber = 
        getMajorVersionNumberInternal(FROSTWIRE_VERSION);

    /**
     * The cached value of the minor revision number.
     */
    private static final int _minorVersionNumber = 
        getMinorVersionNumberInternal(FROSTWIRE_VERSION);
        
    /**
     * The cached value of the really minor version number.
     */
    private static final int _serviceVersionNumber =
        getServiceVersionNumberInternal(FROSTWIRE_VERSION);

    /**
     * The cached value of the GUESS major revision number.
     */
    private static final int _guessMajorVersionNumber = 0;

    /**
     * The cached value of the GUESS minor revision number.
     */
    private static final int _guessMinorVersionNumber = 1;

    /**
     * The cached value of the Ultrapeer major revision number.
     */
    private static final int _upMajorVersionNumber = 0;

    /**
     * The cached value of the Ultrapeer minor revision number.
     */
    private static final int _upMinorVersionNumber = 1;
    
    /**
     * The vendor code for QHD.  WARNING: to avoid character
     * encoding problems, this is hard-coded in QueryReply as well.  So if you
     * change this, you must change QueryReply.
     */
    public static final String QHD_VENDOR_NAME = "LIME";
     
	/**
	 * Cached constant for the HTTP Server: header value.
	 */
	private static final String HTTP_SERVER;

    /** Whether or not a temporary directory is in use. */
    private static boolean temporaryDirectoryInUse;

	/**
	 * Make sure the constructor can never be called.
	 */
	private FrostWireUtils() {}
    
	/**
	 * Initialize the settings statically. 
	 */
	static {
		if(!FROSTWIRE_VERSION.endsWith("Pro")) {
			HTTP_SERVER = "LimeWire/" + FROSTWIRE_VERSION;
		}
		else {
			HTTP_SERVER = ("LimeWire/"+FROSTWIRE_VERSION.
                           substring(0, FROSTWIRE_VERSION.length()-4)+" (Pro)");
		}
	}
    
    /** Returns true if we're a beta. */
    public static boolean isBetaRelease() {
        return betaVersion;
    }
    
    public static boolean isAlphaRelease() {
        return alphaVersion;
    }
	
	/** Gets the major version of GUESS supported.
     */
    public static int getGUESSMajorVersionNumber() {    
        return _guessMajorVersionNumber;
    }
    
    /** Gets the minor version of GUESS supported.
     */
    public static int getGUESSMinorVersionNumber() {
        return _guessMinorVersionNumber;
    }

    /** Gets the major version of Ultrapeer Protocol supported.
     */
    public static int getUPMajorVersionNumber() {    
        return _upMajorVersionNumber;
    }
    
    /** Gets the minor version of Ultrapeer Protocol supported.
     */
    public static int getUPMinorVersionNumber() {
        return _upMinorVersionNumber;
    }

	/**
	 * Returns the current version number of LimeWire as
     * a string, e.g., "1.4".
	 */
	public static String getFrostWireVersion() {
        return FROSTWIRE_VERSION;
	}

    /** Gets the major version of LimeWire.
     */
    public static int getMajorVersionNumber() {    
        return _majorVersionNumber;
    }
    
    /** Gets the minor version of LimeWire.
     */
    public static int getMinorVersionNumber() {
        return _minorVersionNumber;
    }
    
    /** Gets the minor minor version of LimeWire.
     */
   public static int getServiceVersionNumber() {
        return _serviceVersionNumber;
   }
    

    static int getMajorVersionNumberInternal(String version) {
        if (!version.equals("@" + "version" + "@")) {
            try {
                int firstDot = version.indexOf(".");
                String majorStr = version.substring(0, firstDot);
                return new Integer(majorStr).intValue();
            }
            catch (NumberFormatException nfe) {
            }
        }
        // in case this is a mainline version or NFE was caught (strange)
        return 2;
    }
    
    /**
     * Accessor for whether or not this is a testing version
     * (@version@) of LimeWire.
     *
     * @return <tt>true</tt> if the version is @version@,
     *  otherwise <tt>false</tt>
     */
    public static boolean isTestingVersion() {
        return FROSTWIRE_VERSION.equals("@" + "version" + "@");
    }
    
    static int getMinorVersionNumberInternal(String version) {
        if (!version.equals("@" + "version" + "@")) {
            try {
                int firstDot = version.indexOf(".");
                String minusMajor = version.substring(firstDot+1);
                int secondDot = minusMajor.indexOf(".");
                String minorStr = minusMajor.substring(0, secondDot);
                return new Integer(minorStr).intValue();
            }
            catch (NumberFormatException nfe) {
            }
        }
        // in case this is a mainline version or NFE was caught (strange)
        return 7;
    }
    
    static int getServiceVersionNumberInternal(String version) {
        if (!version.equals("@" + "version" + "@")) {
            try {
                int firstDot = version.indexOf(".");
                int secondDot = version.indexOf(".", firstDot+1);
                
                int p = secondDot+1;
                int q = p;
                
                while(q < version.length() && 
                            Character.isDigit(version.charAt(q))) {
                    q++;
                }
                
                if (p != q) {
                    String service = version.substring(p, q);
                    return new Integer(service).intValue();
                }
            }
            catch (NumberFormatException nfe) {
            }
        }
        // in case this is a mainline version or NFE was caught (strange)
        return 0;
    }    

	/**
	 * Returns a version number appropriate for upload headers.
     * Same as '"LimeWire "+getLimeWireVersion'.
	 */
	public static String getVendor() {
		return "LimeWire " + FROSTWIRE_VERSION;
	}    

	/**
	 * Returns the string for the server that should be reported in the HTTP
	 * "Server: " tag.
	 * 
	 * @return the HTTP "Server: " header value
	 */
	public static String getHttpServer() {
		return HTTP_SERVER;
	}
    
    /**
     * Updates a URL to contain common information about the LW installation.
     */
    public static String addLWInfoToUrl(String url, byte[] myClientGUID) {
        if(url.indexOf('?') == -1)
            url += "?";
        else
            url += "&";
        url += "guid=" + EncodingUtils.encode(new GUID(myClientGUID).toHexString())+ 
            "&lang=" + EncodingUtils.encode(ApplicationSettings.getLanguage()) +
            "&lv="   + EncodingUtils.encode(FrostWireUtils.getFrostWireVersion()) +
            "&jv="   + EncodingUtils.encode(VersionUtils.getJavaVersion()) +
            "&os="   + EncodingUtils.encode(OSUtils.getOS()) +
            "&osv="  + EncodingUtils.encode(OSUtils.getOSVersion());
               
        return url;
    }

    /** Returns whether or not a temporary directory is in use. */
    public static boolean isTemporaryDirectoryInUse() {
        return temporaryDirectoryInUse;
    }
    
    /** Returns whether or not failures were encountered in load/save settings on startup. */
    public static boolean hasSettingsLoadSaveFailures() {
        return SettingsFactory.hasLoadSaveFailure();
    }


    /** Sets whether or not a temporary directory is in use. */
    public static void setTemporaryDirectoryInUse(boolean inUse) {
        temporaryDirectoryInUse = inUse;
    }
    
    public static void resetSettingsLoadSaveFailures() {
        SettingsFactory.resetLoadSaveFailure();
    }
    
    /**
     * Returns the path of the FrostWire.jar executable.
     * For a windows binary distribution this is the same path as FrostWire.exe since those files live together.
     * @return
     */
    public static String getFrostWireJarPath() {
    	return new File(FrostWireUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
    }
    
    /**
     * Returns the root folder from which all Saved/Shared/etc..
     * folders should be placed.
     */
    public static File getLimeWireRootFolder() {
        String root = null;
        
        if (OSUtils.isWindowsVista() || OSUtils.isWindows7()) {
        	root = SystemUtils.getSpecialPath(SpecialLocations.DOWNLOADS);
        } else if(OSUtils.isWindows()) {
            root = SystemUtils.getSpecialPath(SpecialLocations.DOCUMENTS);
        }
        
        if(root == null || "".equals(root))
            root = CommonUtils.getUserHomeDir().getPath();
        
        return new File(root, "FrostWire");
    }
    
    public static Tagged<String> getArg(Map<String, String> args, String name, String action) {
        String res = args.get(name);
        if (res == null || res.equals("")) {
            //String detail = "Invalid '" + name + "' while " + action;                                                                       
            return new Tagged<String>("missing.callback.parameter", false);
        }
        String result = res;
        try {
            result = URLDecoder.decode(res);
        } catch (IOException e) {
            // no the end of the world                                                                                                        
        }
        return new Tagged<String>(result, true);
    }
    
    public static interface IndexedMapFunction<T> {
    	public void map(int i, T obj);
    }
    
	/**
	 * Make sure your List implements RandomAccess.
	 * 
	 * @param <T>
	 * @param list
	 * @param mapFunction
	 */
	public static <T> void map(List<T> list, IndexedMapFunction<T> mapFunction) {
		int size = list.size();
		for (int i = 0; i < size; i++) {
			mapFunction.map(i, list.get(i));
		}
	}


}