package com.limegroup.gnutella.settings;

import java.io.File;

import org.limewire.setting.BooleanSetting;
import org.limewire.setting.FileSetting;
import org.limewire.setting.IntSetting;
import org.limewire.setting.StringArraySetting;
import org.limewire.setting.StringSetting;
import org.limewire.util.CommonUtils;

import com.limegroup.gnutella.util.FrostWireUtils;


/**
 * Settings for sharing
 */
public class SharingSettings extends LimeProps {
    
    private static final File PORTABLE_ROOT_FOLDER = CommonUtils.getPortableRootFolder();
    
    private SharingSettings() {}
    
    public static final File DEFAULT_TORRENTS_DIR = new File((PORTABLE_ROOT_FOLDER == null) ? FrostWireUtils.getFrostWireRootFolder() : PORTABLE_ROOT_FOLDER ,"Torrents");
            
    public static final FileSetting TORRENTS_DIR_SETTING =
            FACTORY.createFileSetting("TORRENTS_DIR_SETTING", DEFAULT_TORRENTS_DIR).setAlwaysSave(true);
    
    /**
     * The default folder where Torrent Data will be saved. This folder CANNOT BE SHARED
     * to avoid sharing inconsistencies. 
     * 
     * In the case of FrostWire Portable, we'll name the default torrent data folder "Downloads"
     * In regular frostwire it's "Torrent Data"
     */
    public static final File DEFAULT_TORRENT_DATA_DIR = (PORTABLE_ROOT_FOLDER == null) ? 
            new File(FrostWireUtils.getFrostWireRootFolder(), "Torrent Data") :
            new File(PORTABLE_ROOT_FOLDER, "Downloads");

    /**
     * The folder value where Torrent Data will be saved. This folder CANNOT BE SHARED
     * to avoid sharing inconsistencies. 
     */
    public static final FileSetting TORRENT_DATA_DIR_SETTING = 
    	FACTORY.createFileSetting("DEFAULT_TORRENT_DATA_DIR_SETTING", 
    			DEFAULT_TORRENT_DATA_DIR).setAlwaysSave(true);
    
    public static final BooleanSetting SEED_FINISHED_TORRENTS =
    	FACTORY.createBooleanSetting("SEED_FINISHED_TORRENTS", true);
    
    public static final BooleanSetting SEED_HANDPICKED_TORRENT_FILES =
            FACTORY.createBooleanSetting("SEED_HANDPICKED_TORRENT_FILES", false);        
    
    public static final File IMAGE_CACHE_DIR = 
        new File(CommonUtils.getUserSettingsDir(), "image_cache");
    
    /**
     * Specifies whether or not completed downloads
     * should automatically be cleared from the download window.
     */    
    public static final BooleanSetting CLEAR_DOWNLOAD =
        FACTORY.createBooleanSetting("CLEAR_DOWNLOAD", false);
    
    public static final File getImageCacheDirectory() {
        if (!IMAGE_CACHE_DIR.exists()) {
            IMAGE_CACHE_DIR.mkdirs();
        }
        return IMAGE_CACHE_DIR;
    }
    
    /**
     * Default file extensions.
     */
    private static final String DEFAULT_EXTENSIONS_TO_SHARE =
		"asx;html;htm;xml;txt;pdf;ps;rtf;doc;tex;mp3;mp4;wav;wax;aif;aiff;"+
		"ra;ram;wm;wmv;mp2v;mlv;mpa;mpv2;mid;midi;rmi;aifc;snd;flac;fla;"+
		"mpg;mpeg;asf;qt;mov;avi;mpe;swf;dcr;gif;jpg;jpeg;jpe;png;tif;tiff;"+
		"exe;zip;gz;gzip;hqx;tar;tgz;z;rmj;lqt;rar;ace;sit;smi;img;ogg;rm;"+
		"bin;dmg;jve;nsv;med;mod;7z;iso;fwtp;lwtp;pmf;m4a;bz2;sea;pf;arc;arj;"+
		"bz;tbz;mime;taz;ua;toast;lit;rpm;deb;pkg;sxw;l6t;srt;sub;idx;mkv;"+
		"ogm;shn;dvi;rmvp;kar;cdg;ccd;cue;c;h;m;java;jar;pl;py;pyc;"+
		"pyo;pyz";
    
    /**
     * Default disabled extensions.
     */
    private static final String DEFAULT_EXTENSIONS_TO_DISABLE =
        "au;doc;pdf;xls;rtf;bak;csv;dat;docx;xlsx;xlam;xltx;xltm;xlsm;xlsb;dotm;docm;dotx;dot;qdf;qtx;qph;qel;qdb;qsd;qif;mbf;mny;wma";
        
    
    public static void initTorrentDataDirSetting() {
        if (CommonUtils.isPortable()) {
            SharingSettings.TORRENT_DATA_DIR_SETTING.setValue(SharingSettings.DEFAULT_TORRENT_DATA_DIR);
        }
    }

    public static void initTorrentsDirSetting() {
        //in case we changed locations, always reset.
        if (CommonUtils.isPortable()) {
            SharingSettings.TORRENTS_DIR_SETTING.setValue(SharingSettings.DEFAULT_TORRENTS_DIR);
        }
        
        //in case it's first time
        if (!SharingSettings.TORRENTS_DIR_SETTING.getValue().exists()) {
            SharingSettings.TORRENTS_DIR_SETTING.getValue().mkdirs();
        }
    }
    
    /**
     * The list of extensions shared by default
     */
    public static final String[] getDefaultExtensions() {
        return StringArraySetting.decode(DEFAULT_EXTENSIONS_TO_SHARE); 
    }
    
    /**
     * The list of extensions shared by default
     */
    public static final String getDefaultExtensionsAsString() {
        return DEFAULT_EXTENSIONS_TO_SHARE; 
    }
    
    /**
     * The list of extensions disabled by default in the file types sharing screen
     */
    public static final String[] getDefaultDisabledExtensions() {
        return StringArraySetting.decode(DEFAULT_EXTENSIONS_TO_DISABLE); 
    }
    
    /**
     * The list of extensions disabled by default in the file types sharing screen
     */
    public static final String getDefaultDisabledExtensionsAsString() {
        return DEFAULT_EXTENSIONS_TO_DISABLE; 
    }
        
    /**
     * Whether or not to auto-share .torrent files.
     */
    public static final BooleanSetting SHARE_TORRENT_META_FILES =
        FACTORY.createBooleanSetting("SHARE_TORRENT_META_FILES", true);
    
    /**
     * Whether or not to show .torrent directory in Library.
     */
    public static final BooleanSetting SHOW_TORRENT_META_FILES =
        FACTORY.createBooleanSetting("SHOW_TORRENT_META_FILES", true); 
    
    /**
	 * File extensions that are shared.
	 */
    public static final StringSetting EXTENSIONS_TO_SHARE =
        FACTORY.createStringSetting("EXTENSIONS_TO_SEARCH_FOR", DEFAULT_EXTENSIONS_TO_SHARE);
    
    // New Settings for extension management

    /**
     * Used to flag the first use of the new database type to migrate the 
     *  extensions database across into the new settings 
     */
    public static final BooleanSetting EXTENSIONS_MIGRATE = 
        FACTORY.createBooleanSetting("EXTENSIONS_MIGRATE", true);
    
    /**
     * List of Extra file extensions.
     */
    public static final StringSetting EXTENSIONS_LIST_CUSTOM =
         FACTORY.createStringSetting("EXTENSIONS_LIST_CUSTOM", "");
    
    /**
     * File extensions that are not shared.
     */
    public static final StringSetting EXTENSIONS_LIST_UNSHARED =
         FACTORY.createStringSetting("EXTENSIONS_LIST_UNSHARED", "");
    
    
    
    /**
     * If to not force disable sensitive extensions.
     */
    public static final BooleanSetting DISABLE_SENSITIVE =
        FACTORY.createBooleanSetting("DISABLE_SENSITIVE_EXTS", true);
    
    /**
     * Sets the probability (expressed as a percentage) that an incoming
     * freeloader will be accepted.   For example, if allowed==50, an incoming
     * connection has a 50-50 chance being accepted.  If allowed==100, all
     * incoming connections are accepted.
     */                                                        
    public static final IntSetting FREELOADER_ALLOWED =
        FACTORY.createIntSetting("FREELOADER_ALLOWED", 100);
    
    /**
     * Minimum the number of files a host must share to not be considered
     * a freeloader.  For example, if files==0, no host is considered a
     * freeloader.
     */
    public static final IntSetting FREELOADER_FILES =
        FACTORY.createIntSetting("FREELOADER_FILES", 1);
    
    /**
	 * The timeout value for persistent HTTP connections in milliseconds.
	 */
    public static final IntSetting PERSISTENT_HTTP_CONNECTION_TIMEOUT =
        FACTORY.createIntSetting("PERSISTENT_HTTP_CONNECTION_TIMEOUT", 15000);
    
    /**
     * Specifies whether or not completed uploads
     * should automatically be cleared from the upload window.
     */
    public static final BooleanSetting CLEAR_UPLOAD =
        FACTORY.createBooleanSetting("CLEAR_UPLOAD", true);
    
    /**
	 * Whether or not browsers should be allowed to perform uploads.
	 */
    public static final BooleanSetting ALLOW_BROWSER =
        FACTORY.createBooleanSetting("ALLOW_BROWSER", false);

    /**
     * Whether to throttle hashing of shared files.
     */
    public static final BooleanSetting FRIENDLY_HASHING =
        FACTORY.createBooleanSetting("FRIENDLY_HASHING", true);	
    
    /** 
     * Setting for the threshold of when to warn the user that a lot of 
     *  files are being shared
     */
    public static final IntSetting FILES_FOR_WARNING =
        FACTORY.createIntSetting("FILES_FOR_WARNING", 1000);

    /** 
     * Setting for the threshold of when to warn the user that a lot of 
     *  files are being shared
     */
    public static final IntSetting DEPTH_FOR_WARNING =
        FACTORY.createIntSetting("DEPTH_FOR_WARNING", 4);
    	
    /**
     * Setting for whether or not to allow partial files to be shared.
     */
    public static final BooleanSetting ALLOW_PARTIAL_SHARING =
        FACTORY.createBooleanSetting("ALLOW_PARTIAL_SHARING", false);
    
    /**
     * Remote switch to turn off partial results.
     */
    public static final BooleanSetting ALLOW_PARTIAL_RESPONSES = 
        FACTORY.createRemoteBooleanSetting("ALLOW_PARTIAL_RESPONSES", true, "SharingSettings.allowPartialResponses");
    
    /**
     * Maximum size in bytes for the encoding of available ranges per Response object
     */
    public static final IntSetting MAX_PARTIAL_ENCODING_SIZE =
        FACTORY.createRemoteIntSetting("MAX_PARTIAL_ENCODING_SIZE", 20, 
                "SharingSettings.maxPartialEncodingSize", 10, 40);
    
    /**
     * Whether to publish keywords from partial files in the qrp.
     */
    public static final BooleanSetting PUBLISH_PARTIAL_QRP = 
        FACTORY.createRemoteBooleanSetting("PUBLISH_PARTIAL_QRP", true, "SharingSettings.publishPartialQRP");
    
    /**
     * Whether to load keywords from incomplete files in the trie
     */
    public static final BooleanSetting LOAD_PARTIAL_KEYWORDS = 
        FACTORY.createRemoteBooleanSetting("LOAD_PARTIAL_KEYWORDS", true, "SharingSettings.loadPartialKeywords");
}
