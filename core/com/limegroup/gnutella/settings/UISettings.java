package com.limegroup.gnutella.settings;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.limewire.setting.BooleanSetting;
import org.limewire.setting.IntSetting;
import org.limewire.setting.StringSetting;

/**
 * Settings to deal with UI.
 */ 
public final class UISettings extends LimeProps {

    private UISettings() {}

    /**
     * Setting for autocompletion
     */
    public static final BooleanSetting AUTOCOMPLETE_ENABLED =
		FACTORY.createBooleanSetting("AUTOCOMPLETE_ENABLED", true);
		
    /**
     * Setting for search-result filters.
     */
    public static final BooleanSetting SEARCH_RESULT_FILTERS =
        FACTORY.createBooleanSetting("SEARCH_RESULT_FILTERS", true);
                                     
    /**
     * Setting for using small icons.
     */
    public static final BooleanSetting SMALL_ICONS =
        FACTORY.createBooleanSetting("UI_SMALL_ICONS", isResolutionLow());
        
    /**
     * Setting for displaying text under icons.
     */
    public static final BooleanSetting TEXT_WITH_ICONS =
        FACTORY.createBooleanSetting("UI_TEXT_WITH_ICONS", true);

    /**
     * Setting for displaying smileys in chat window.
     */
    public static final BooleanSetting SMILEYS_IN_CHAT =
        FACTORY.createBooleanSetting("UI_SMILEYS_IN_CHAT", true);        
                       
    /**
     * Setting for not grouping search results in GUI
     */
    public static final BooleanSetting UI_GROUP_RESULTS =
        FACTORY.createBooleanSetting("UI_GROUP_RESULTS", true);
        
    /**
     * Setting to allow ignoring of alt-locs in replies.
     */
    public static final BooleanSetting UI_ADD_REPLY_ALT_LOCS =
        FACTORY.createBooleanSetting("UI_ADD_REPLY_ALT_LOCS", true);
        
    /**
     * For people with bad eyes.
     */
    private static boolean isResolutionLow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return screenSize.width <= 800 || screenSize.height <= 600;
    }

    /**
     * Setting to persist monitor check box state.
     */
    public static final BooleanSetting UI_MONITOR_SHOW_INCOMING_SEARCHES =
        FACTORY.createBooleanSetting("UI_MONITOR_SHOW_INCOMING_SEARCHES", false);
	
	/**
	 * Setting for the divider location between library tree and table.
	 */
	public static final IntSetting UI_LIBRARY_TREE_DIVIDER_LOCATION =
		FACTORY.createIntSetting("UI_LIBRARY_TREE_DIVIDER_LOCATION", -1);
	
	public static final IntSetting UI_LIBRARY_MAIN_DIVIDER_LOCATION =
	        FACTORY.createIntSetting("UI_LIBRARY_MAIN_DIVIDER_LOCATION", -1);
	
	public static final IntSetting UI_LIBRARY_EXPLORER_DIVIDER_POSITION =
	        FACTORY.createIntSetting("UI_LIBRARY_EXPLORER_DIVIDER_POSITION", 230);
	
	/**
	 * Setting for the divider location between incoming query monitors and
	 * upload panel.
	 */
	public static final IntSetting UI_TRANSFERS_DIVIDER_LOCATION =
		FACTORY.createIntSetting("UI_TRANSFERS_DIVIDER_LOCATION", Integer.MAX_VALUE);
    
    /** Setting for if native icons should be preloaded. */
    public static final BooleanSetting PRELOAD_NATIVE_ICONS =
        FACTORY.createBooleanSetting("PRELOAD_NATIVE_ICONS", true);
    
    /**
     * Setting to persist the width of the options dialog if the dialog
     * was resized by the user.
     */
    public static final IntSetting UI_OPTIONS_DIALOG_WIDTH = 
        FACTORY.createIntSetting("UI_OPTIONS_DIALOG_WIDTH", 844);
    
    /**
     * Setting to persist the height of the options dialog if the dialog
     * was resized by the user.
     */
    public static final IntSetting UI_OPTIONS_DIALOG_HEIGHT= 
        FACTORY.createIntSetting("UI_OPTIONS_DIALOG_HEIGHT", 600);
    
    /**
     * Setting that globally enables or disables notifications.
     */
    public static final BooleanSetting SHOW_NOTIFICATIONS = 
        FACTORY.createBooleanSetting("SHOW_NOTIFICATIONS", true);
    
    /** Whether or not to use network-based images, or just always use built-in ones. */
    private static final BooleanSetting USE_NETWORK_IMAGES = FACTORY.createRemoteBooleanSetting("USE_NETWORK_IMAGES",
            true, "UI.useNetworkImages"); // WE DO NOT USE REMOTE IMAGES, IT SHOULD BE FALSE
    
    /** Collection of info for the 'Getting Started' image. */
    public static ImageInfo INTRO_IMAGE_INFO = new ImageInfoImpl(true);

    /** Holds the home remote url */
    public static final StringSetting HOME_URL = null;

    /** Collection of info for the 'After Search' image. */
    public static ImageInfo AFTER_SEARCH_IMAGE_INFO = new ImageInfoImpl(false);
    
    public static interface ImageInfo {     
        /** The URL to pull the image from. */
        public String getImageUrl();
        /** Whether or not this pic can have an outgoing link. */
        public boolean canLink();
        /** The outgoing link if triggered from the backup image. */
        public String getLocalLinkUrl();
        /** The outgoing link if triggered from the network image. */
        public String getNetworkLinkUrl();
        /** True if network images should be used. */
        public boolean useNetworkImage();
        /** True if this is the 'Into' pic. */
        boolean isIntro();
        /** Returns the URL of a torrent related to this ImageInfo if available, otherwise returns null */
        public String getTorrentUrl();
    }
    
    public static class ImageInfoImpl implements ImageInfo {
        private final boolean intro;

        //private static final BooleanSetting USE_NETWORK_IMAGES = 
        //	FACTORY.createRemoteBooleanSetting("USE_NETWORK_IMAGES",true, "UI.useNetworkImages");

        //SETTINGS FOR INTRO IMAGE
        
        private static final StringSetting INTRO_URL = 
        	FACTORY.createRemoteStringSetting("INTRO_URL", 
        			"http://www.frostwire.com/fwclient_welcome_image.php", "UI.introUrl");
        
        private static final BooleanSetting INTRO_PRO_SHOW = 
        	FACTORY.createRemoteBooleanSetting("INTRO_PRO_SHOW",false,"UI.introProShow");
        
        private static final BooleanSetting INTRO_HAS_LINK = 
        	FACTORY.createRemoteBooleanSetting("INTRO_HAS_LINK", true, "UI.introCanLink");
        
        private static final StringSetting INTRO_LOCAL_LINK = 
        	FACTORY.createRemoteStringSetting("INTRO_LOCAL_LINK","http://www.frostwire.com/?", "UI.introClickLinkLocal");
        
        private static final StringSetting INTRO_NETWORK_LINK = 
        	FACTORY.createRemoteStringSetting("INTRO_NETWORK_LINK","http://www.frostwire.com/?", "UI.introClickLink");
        
        private static final StringSetting INTRO_TORRENT_LINK =
        	FACTORY.createRemoteStringSetting("INTRO_TORRENT_LINK","","UI.introTorrentLink");
        
        //SETTINGS FOR AFTER SEARCH IMAGE
        private static final StringSetting AFTER_SEARCH_URL = 
        	FACTORY.createRemoteStringSetting("AFTER_SEARCH_URL", "http://static.frostwire.com/images/overlays/default.png", "UI.afterSearchUrl");
        
        private static final BooleanSetting AFTER_SEARCH_PRO_SHOW = 
        	FACTORY.createRemoteBooleanSetting("AFTER_SEARCH_PRO_SHOW",false,"UI.afterSearchProShow");
        
        private static final BooleanSetting AFTER_SEARCH_HAS_LINK = 
        	FACTORY.createRemoteBooleanSetting("AFTER_SEARCH_HAS_LINK", true, "UI.afterSearchCanLink");
        
        private static final StringSetting AFTER_SEARCH_LOCAL_LINK = 
        	FACTORY.createRemoteStringSetting("AFTER_SEARCH_LOCAL_LINK","http://www.frostwire.com/?", "UI.afterSearchClickLinkLocal");
        
        private static final StringSetting AFTER_SEARCH_NETWORK_LINK = 
        	FACTORY.createRemoteStringSetting("AFTER_SEARCH_NETWORK_LINK","http://www.frostwire.com/?", "UI.afterSearchClickLink");

        private static final StringSetting AFTER_SEARCH_TORRENT_LINK =
        	FACTORY.createRemoteStringSetting("AFTER_SEARCH_TORRENT_LINK","","UI.afterSearchTorrentLink");
        
        ImageInfoImpl(boolean intro) {
        	this.intro = intro;
        }
        
        /**
         * Pass intro False, to edit the properties of the afterSearch Image.
         * @param intro
         * @param imgUrl
         * @param canLink (In case you don't want this image to link anywhere, just display a message)
         * @param linkUrl
         * @param torrentUrl
         */
        public ImageInfoImpl(boolean intro, 
        		             String imgUrl,
        		             boolean canLink,
        		             String linkUrl,
        		             String torrentUrl) {
        	this.intro = intro;
        	INTRO_PRO_SHOW.setValue(false);
        	AFTER_SEARCH_PRO_SHOW.setValue(false);
        	
        	if (torrentUrl == null) {
        		torrentUrl = "";
        	}

        	if (linkUrl == null) {
        		linkUrl = "";
        	}
        	
        	if (intro) {
	        	INTRO_URL.setValue(imgUrl);
	        	INTRO_HAS_LINK.setValue(linkUrl != null);
	        	INTRO_LOCAL_LINK.setValue(linkUrl);
	        	INTRO_NETWORK_LINK.setValue(linkUrl);
	        	INTRO_TORRENT_LINK.setValue(torrentUrl);
        	} else {
        		AFTER_SEARCH_PRO_SHOW.setValue(false);
        		AFTER_SEARCH_URL.setValue(imgUrl);
        		AFTER_SEARCH_HAS_LINK.setValue(linkUrl != null);
        		AFTER_SEARCH_LOCAL_LINK.setValue(linkUrl);
        		AFTER_SEARCH_NETWORK_LINK.setValue(linkUrl);
        		AFTER_SEARCH_TORRENT_LINK.setValue(torrentUrl);
        	}
        }

        public boolean canLink() {
            return intro ? INTRO_HAS_LINK.getValue() : AFTER_SEARCH_HAS_LINK.getValue();
        }

        public String getImageUrl() {
            return intro ? INTRO_URL.getValue() : AFTER_SEARCH_URL.getValue();
        }

        public String getLocalLinkUrl() {
            return intro ? INTRO_LOCAL_LINK.getValue() : AFTER_SEARCH_LOCAL_LINK.getValue();
        }

        public String getNetworkLinkUrl() {
            return intro ? INTRO_NETWORK_LINK.getValue() : AFTER_SEARCH_NETWORK_LINK.getValue();
        }
        
        public boolean useNetworkImage() {
            return USE_NETWORK_IMAGES.getValue();
        }
        
        public boolean isIntro() {
            return intro;
        }
        
        public String getTorrentUrl() {
        	String result = intro ? INTRO_TORRENT_LINK.getValue() : AFTER_SEARCH_TORRENT_LINK.getValue();
        	if (result.equals(""))
        		return null;
        	return result;
        }
    }
    


}
