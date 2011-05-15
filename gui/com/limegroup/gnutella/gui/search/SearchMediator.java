package com.limegroup.gnutella.gui.search;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.limewire.io.Connectable;
import org.limewire.io.ConnectableImpl;
import org.limewire.io.IpPort;
import org.limewire.io.IpPortSet;
import org.limewire.rudp.RUDPUtils;
import org.limewire.setting.FileSetting;
import org.limewire.setting.evt.SettingEvent;
import org.limewire.setting.evt.SettingListener;
import org.limewire.util.I18NConvert;
import org.limewire.util.StringUtils;

import com.frostwire.bittorrent.AzureusStarter;
import com.frostwire.bittorrent.websearch.WebTorrentSearch;
import com.frostwire.bittorrent.websearch.clearbits.ClearBitsItem;
import com.frostwire.bittorrent.websearch.clearbits.ClearBitsResponse;
import com.frostwire.bittorrent.websearch.isohunt.ISOHuntItem;
import com.frostwire.bittorrent.websearch.isohunt.ISOHuntResponse;
import com.frostwire.bittorrent.websearch.mininova.MininovaVuzeItem;
import com.frostwire.bittorrent.websearch.mininova.MininovaVuzeResponse;
import com.limegroup.gnutella.BrowseHostHandler;
import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.PushEndpoint;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.gui.DialogOption;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.banner.Ad;
import com.limegroup.gnutella.gui.banner.Banner;
import com.limegroup.gnutella.gui.download.DownloaderUtils;
import com.limegroup.gnutella.gui.download.GuiDownloaderFactory;
import com.limegroup.gnutella.gui.download.SearchResultDownloaderFactory;
import com.limegroup.gnutella.search.HostData;
import com.limegroup.gnutella.settings.QuestionsHandler;
import com.limegroup.gnutella.settings.SearchSettings;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.util.FrostWireUtils;
import com.limegroup.gnutella.xml.LimeXMLDocument;
import com.limegroup.gnutella.xml.LimeXMLProperties;

/**
 * This class acts as a mediator between the various search components --
 * the hub that all traffic passes through.  This allows the decoupling of
 * the various search packages and simplfies the responsibilities of the
 * underlying classes.
 */
public final class SearchMediator {

	/**
	 * Query text is valid.
	 */
	public static final int QUERY_VALID = 0;
	/**
	 * Query text is empty.
	 */
	public static final int QUERY_EMPTY = 1;
	/**
	 * Query text is too short.
	 */
	public static final int QUERY_TOO_SHORT = 2;
	/**
	 * Query text is too long.
	 */
	public static final int QUERY_TOO_LONG = 3;
	/**
	 * Query xml is too long.
	 */
	public static final int QUERY_XML_TOO_LONG = 4;
    /**
     * Query contains invalid characters.
     */
    public static final int QUERY_INVALID_CHARACTERS = 5;
	
	static final String DOWNLOAD_STRING = I18n.tr("Download");

    static final String KILL_STRING = I18n.tr("Close Search");

    static final String STOP_STRING = I18n.tr("Stop Search");

    static final String LAUNCH_STRING = I18n.tr("Launch Action");

    static final String BROWSE_STRING = I18n.tr("Browse Host");

    static final String CHAT_STRING = I18n.tr("Chat With Host");

    static final String REPEAT_SEARCH_STRING = I18n.tr("Repeat Search");

    static final String BROWSE_HOST_STRING = I18n.tr("Browse Host");

    static final String BITZI_LOOKUP_STRING = I18n.tr("Lookup File with Bitzi");

    static final String BLOCK_STRING = I18n.tr("Block Hosts");

    static final String MARK_AS_STRING = I18n.tr("Mark As");

    static final String SPAM_STRING = I18n.tr("Junk");

    static final String NOT_SPAM_STRING = I18n.tr("Not Junk");

    static final String REPEAT_SEARCH_NO_CLEAR_STRING = I18n.tr("Get More Results");

    /** A name of attribute, which holds a query in state of downloaded file. */
    public static final String SEARCH_INFORMATION_KEY = "searchInformationMap";

    /**
     * Variable for the component that handles all search input from the user.
     */
    private static SearchInputManager INPUT_MANAGER;

    /**
     * This instance handles the display of all search results.
     * TODO: Changed to package-protected for testing to add special results
     */
    private static SearchResultDisplayer RESULT_DISPLAYER;
    
    /** Banner that shows a message in the search result panel. */
    private static volatile Banner banner;

    /**
     * Constructs the UI components of the search result display area of the 
     * search tab.
     */
    public SearchMediator() {
        // Set the splash screen text...
        final String splashScreenString =
            I18n.tr("Loading Search Window...");
        GUIMediator.setSplashScreenString(splashScreenString);
        GUIMediator.addRefreshListener(getSearchResultDisplayer());
        
        // Link up the tabs of results with the filters of the input screen.
//        getSearchResultDisplayer().setSearchListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent e) {
//                ResultPanel panel = getSearchResultDisplayer().getSelectedResultPanel();
//                if(panel == null)
//                    getSearchInputManager().clearFilters();
//                else
//                    getSearchInputManager().setFiltersFor(panel);
//            }
//        });
        initBanner();
    }
    
    /**
     * initializes a banner and registers a listener for its change.
     */
    private static void initBanner() {
        reloadBanner();
        SearchSettings.SEARCH_WARNING.addSettingListener(new SettingListener() {
            public void settingChanged(SettingEvent evt) {
                if (evt.getEventType() == SettingEvent.EventType.VALUE_CHANGED)
                    reloadBanner();
            }
        });
    }
    
    /**
     * reloads a banner with the current value from the settings.
     */
    private static void reloadBanner() {
        Banner newBanner = banner;
        try {
            newBanner = new Banner(SearchSettings.SEARCH_WARNING.getValue());
        } catch (IllegalArgumentException badSimpp) {}
        if (newBanner == null)
            newBanner = Banner.getDefaultBanner();
        banner = newBanner;
    }
    
    /**
     * Rebuilds the INPUT_MANAGER's panel.
     */
    public static void rebuildInputPanel() {
        getSearchInputManager().rebuild();
    }
    
    /**
     * Notification that the address has changed -- pass it along.
     */
    public static void addressChanged() {
        getSearchInputManager().addressChanged();
    }
    
    /**
     * Informs the INPUT_MANAGER that we want to display the searching
     * window.
     */
    public static void showSearchInput() {
        getSearchInputManager().goToSearch();
    }
    
    /**
     * Requests the search focus in the INPUT_MANAGER.
     */
    public static void requestSearchFocus() {
        getSearchInputManager().requestSearchFocus();
    }
    
    /**
     * Updates all current results.
     */
    public static void updateResults() {
        getSearchResultDisplayer().updateResults();
    }

    /**
     * Placehold for repeatSearch
     */
    static byte[] repeatSearch(ResultPanel rp, SearchInformation info) {
      return repeatSearch (rp,info,true);
    }
    
    /** 
     * Repeats the given search.
     */
    static byte[] repeatSearch(ResultPanel rp, SearchInformation info, boolean clearingResults) {
        if(!validate(info))
            return null;

        // 1. Update panel with new GUID
        byte [] guidBytes = GuiCoreMediator.getSearchServices().newQueryGUID();
        final GUID newGuid = new GUID(guidBytes);

        GuiCoreMediator.getSearchServices().stopQuery(new GUID(rp.getGUID()));
        rp.setGUID(newGuid);
        if ( clearingResults ) {
            getSearchInputManager().panelReset(rp);
        }
        
        if(info.isBrowseHostSearch()) {
            IpPort ipport = info.getIpPort();
            String host = ipport.getAddress();
            int port = ipport.getPort();
            if(host != null && port != 0) {
                GUIMediator.instance().setSearching(true);
                reBrowseHost(new ConnectableImpl(ipport, false), rp);
            }
        } else {
            GUIMediator.instance().setSearching(true);
            doSearch(guidBytes, info);
        }

        return guidBytes;
    }

    /**
     * Browses the first selected host. Fails silently if couldn't browse.
     */
    static void doBrowseHost(ResultPanel rp) {
        TableLine line = rp.getSelectedLine();
        if(line == null)
            return;
            
        // Get the browse-host RFD from the line.
        RemoteFileDesc rfd = line.getBrowseHostEnabledRFD();
        if(rfd == null)
            return;
        
        // See if it is firewalled
        byte[] serventIDBytes = rfd.getClientGUID();
        // if the reply is to a multicast query, don't use any
        // push proxies so we definitely will send a UDP push request
        Set<? extends IpPort> proxies = rfd.isReplyToMulticast() ? 
            IpPort.EMPTY_SET : rfd.getPushProxies();
        GUID serventID = new GUID(serventIDBytes);        
        doBrowseHost2(rfd, serventID, proxies, rfd.supportsFWTransfer());
     }

    /**
     * Allows for browsing of a host from outside of the search package.
     */
    public static void doBrowseHost(final RemoteFileDesc rfd) {
        doBrowseHost2(rfd,
                      new GUID(rfd.getClientGUID()), rfd.getPushProxies(),
                      rfd.supportsFWTransfer());
    }


    /**
     * Allows for browsing of a host from outside of the search package
     * without an rfd.
     */
    public static void doBrowseHost(Connectable connectable, GUID guid) {
        if (guid == null)
            doBrowseHost2(connectable, null, null, false);
        else
            doBrowseHost2(connectable, new GUID(guid.bytes()), null, false);
    }

    /**
     * Re-browses the host.  Fails silently if browse failed...
     * TODO: WILL NOT WORK FOR RE-BROWSES THAT REQUIRES A PUSH!!!
     */
    private static void reBrowseHost(Connectable host, ResultPanel in) {
        // Update the GUID
        final GUID guid = new GUID(GUID.makeGuid());
        in.setGUID(guid);
        BrowseHostHandler bhh =
            GuiCoreMediator.getSearchServices().doAsynchronousBrowseHost(host, guid, 
                                                   new GUID(GUID.makeGuid()), 
                                                   null, false);
                                         
        getSearchInputManager().panelReset(in);
    }
    

    /**
     * Performs a browse host on a push end point.  
     */
    public static void doBrowseHost(PushEndpoint pushEndpoint) {
        InetSocketAddress inetSocketAddress = pushEndpoint.getInetSocketAddress();
        Connectable host = inetSocketAddress != null ? new ConnectableImpl(inetSocketAddress, false) : null;
        doBrowseHost2(host, new GUID(pushEndpoint.getClientGUID()), pushEndpoint.getProxies(), pushEndpoint.supportsFWTVersion() >= RUDPUtils.VERSION);
    }

    /**
     * Browses the passed host at the passed port.
     * Fails silently if couldn't browse.
     * @param host The host to browse, can be null for firewalled endpoints
     * @param port The port at which to browse
     */
    static private void doBrowseHost2(Connectable host,
                                      GUID serventID, 
                                      Set<? extends IpPort> proxies, boolean canDoFWTransfer) {
        // Update the GUI
        GUID guid = new GUID(GUID.makeGuid());
        String title = host != null ? host.getAddress() + ":" + host.getPort() : I18n.tr("Firewalled Host"); 
        ResultPanel rp =  addBrowseHostTab(guid, title);
        // Do the actual browse host
        BrowseHostHandler bhh = GuiCoreMediator.getSearchServices().doAsynchronousBrowseHost(
                                    host, guid, serventID, proxies,
                                    canDoFWTransfer);
        

    }

    /**
     * Call this when a Browse Host fails.
     * @param guid The guid associated with this Browse. 
     */
    public static void browseHostFailed(GUID guid) {
        getSearchResultDisplayer().browseHostFailed(guid);
    }
    
    /**
     * Initiates a new search with the specified SearchInformation.
     *
     * Returns the GUID of the search if a search was initiated,
     * otherwise returns null.
     */
    public static byte[] triggerSearch(final SearchInformation info) {
        if(!validate(info))
            return null;
            
        // generate a guid for the search.
        final byte[] guid = GuiCoreMediator.getSearchServices().newQueryGUID();
        // only add tab if this isn't a browse-host search.
        if(!info.isBrowseHostSearch()) {
            addResultTab(new GUID(guid), info);
        }
        
        doSearch(guid, info);
        
        return guid;
    }

    
    /**
     * Triggers a search given the text in the search field.  For testing
     * purposes returns the 16-byte GUID of the search or null if the search
     * didn't happen because it was greedy, etc.  
     */
    public static byte[] triggerSearch(String query) {
        return triggerSearch(
            SearchInformation.createKeywordSearch(query, null, 
                                  MediaType.getAnyTypeMediaType())
        );
    }
    
    /** Shows a search result tab with your files, with the given title. */
    public static void showMyFiles(String title) {
        addMyFilesResultTab(title);
    }
    
    /**
     * Validates the given search information.
     */
    private static boolean validate(SearchInformation info) {
    
		switch (validateInfo(info)) {
		case QUERY_EMPTY:
			return false;
		case QUERY_TOO_SHORT:
			GUIMediator.showError(I18n.tr("Your search must be at least three characters to avoid congesting the network."));
			return false;
		case QUERY_TOO_LONG:
			String xml = info.getXML();
			if (xml == null || xml.length() == 0) {
				GUIMediator.showError(I18n.tr("Your search is too long. Please make your search smaller and try again."));
			}
			else {
				GUIMediator.showError(I18n.tr("Your search is too specific. Please make your search smaller and try again."));
			}
			return false;
		case QUERY_XML_TOO_LONG:
            GUIMediator.showError(I18n.tr("Your search is too specific. Please make your search smaller and try again."));
			return false;
		case QUERY_VALID:
		default:
			boolean searchingTorrents = info.getMediaType().equals(MediaType.TYPE_TORRENTS);
			boolean gnutellaStarted = GuiCoreMediator.getLifecycleManager().isStarted();
			boolean azureusStarted = AzureusStarter.getAzureusCore().isStarted();			
			
            if((searchingTorrents && !azureusStarted) ||
            	(!searchingTorrents && !gnutellaStarted)) {
                GUIMediator.showMessage(I18n.tr("Please wait, FrostWire must finish loading before a search can be started."));
                return false;
            }
            
	        // only show search messages if not doing browse host.
	        if(!info.isBrowseHostSearch() && !searchingTorrents) {
	            if(!GuiCoreMediator.getConnectionServices().isConnected()) {
	                if(!GuiCoreMediator.getConnectionServices().isConnecting()) {
	                    // if not connected or connecting, show one message.
	                    GUIMediator.showMessage(I18n.tr("You are not connected to the network. To connect, select \"Connect\" from the \"File\" menu. Your search may not return any results until you connect."), QuestionsHandler.NO_NOT_CONNECTED);
	                } else { 
	                    // if attempting to connect, show another.
	                    GUIMediator.showMessage(I18n.tr("FrostWire is currently connecting to the network. Your search may not return many results until you are fully connected to the network."), QuestionsHandler.NO_STILL_CONNECTING);
	                }
	            }
	        }
			return true;
		}
    }
	
	
	/**
	 * Validates the a search info and returns {@link #QUERY_VALID} if it is
	 * valid.
	 * @param info
	 * @return one of the static <code>QUERY*</code> fields
	 */
	public static int validateInfo(SearchInformation info) {
		
	    String query = I18NConvert.instance().getNorm(info.getQuery());
	    String xml = info.getXML();
        
		if (query.length() == 0) {
			return QUERY_EMPTY;
		} else if (query.length() <= 2 && !(query.length() == 2 && 
				((Character.isDigit(query.charAt(0)) && 
						Character.isLetter(query.charAt(1)))   ||
						(Character.isLetter(query.charAt(0)) && 
								Character.isDigit(query.charAt(1)))))) {
			return QUERY_TOO_SHORT;
		} else if (query.length() > SearchSettings.MAX_QUERY_LENGTH.getValue()) {
			return QUERY_TOO_LONG;
		} else if (xml != null &&  xml.length() > SearchSettings.MAX_XML_QUERY_LENGTH.getValue()) {
			return QUERY_XML_TOO_LONG;
		} else if (StringUtils.containsCharacters(query,SearchSettings.ILLEGAL_CHARS.getValue())) {
            return QUERY_INVALID_CHARACTERS;
		} else {
		    return QUERY_VALID;
		}
	}
    
    /**
     * Does the actual search.
     * 
     * 
     * 
     */
    private static void doSearch(final byte[] guid, final SearchInformation info) {
        final String query = info.getQuery();
        String xml = info.getXML();
        MediaType media = info.getMediaType();

        //If type is torrent, we'll try to fetch torrent search results from engine
        if (media.equals(MediaType.TYPE_TORRENTS)) {
        	//ClearBits (formerly known as LegalTorrents.com) Search Thread.
        	new Thread(new Runnable() {
    			public void run() {
    			    
    				final ResultPanel rp = getResultPanelForGUID(new GUID(guid));
			        if(rp != null) {
			        	
			        	ClearBitsResponse response = WebTorrentSearch.searchClearBits(query);
			        	
			        	if (response != null) {
				        	final List<SearchResult> results = normalizeClearBitsResponse(response, info);

				        	SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
						        	for (final SearchResult sr : results) {
						        	    getSearchResultDisplayer().addQueryResult(guid, sr, rp);
						        	}
								}});
			        	}
			        }
    			}
    		}).start();        	

        	
        	//ISOHunt Search Thread.
        	new Thread(new Runnable() {
    			public void run() {
    			    
    				final ResultPanel rp = getResultPanelForGUID(new GUID(guid));
			        if(rp != null) {
			        	
			        	ISOHuntResponse response = WebTorrentSearch.searchISOHunt(query);
			        	
			        	if (response != null) {
				        	final List<SearchResult> results = normalizeISOHuntResponse(response,info);

				        	SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
						        	for (final SearchResult sr : results) {
						        		RESULT_DISPLAYER.addQueryResult(guid, sr, rp);
						        	}
								}});
			        	}
			        }
    			}
    		}).start();
        	
        	//MininovaVuze Search Thread.
        	new Thread(new Runnable() {
    			public void run() {
    			    
    				final ResultPanel rp = getResultPanelForGUID(new GUID(guid));
			        if(rp != null) {
			        	
			        	MininovaVuzeResponse response = WebTorrentSearch.searchMininovaVuze(query);
			        	
			        	if (response != null) {
				        	final List<SearchResult> results = normalizeMininovaVuzeResponse(response, info);

				        	SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
						        	for (final SearchResult sr : results) {
						        		RESULT_DISPLAYER.addQueryResult(guid, sr, rp);
						        	}
								}});
			        	}
			        }
    			}
    		}).start();        	
        }
        
//        if(info.isXMLSearch()) {
//            GuiCoreMediator.getSearchServices().query(guid, query, xml, media);
//        } else if(info.isKeywordSearch()) {
//            GuiCoreMediator.getSearchServices().query(guid, query, media);
//        } else if(info.isWhatsNewSearch()) {
//            GuiCoreMediator.getSearchServices().queryWhatIsNew(guid, media);
//        } else if(info.isBrowseHostSearch()) {
//            IpPort ipport = info.getIpPort();
//            doBrowseHost(new ConnectableImpl(ipport, false), null);
//        }
    }
    
    private static List<SearchResult> normalizeClearBitsResponse(ClearBitsResponse response, SearchInformation info) {
    	
    	List<SearchResult> result = new ArrayList<SearchResult>();
    	
    	if (response.results != null)
    		for (ClearBitsItem bucket : response.results) {
    			
    			SearchResult sr = new ClearBitsSearchResult(bucket, info);
    			
    			result.add(sr);
    		}
    	
    	return result;
    }

    
    private static List<SearchResult> normalizeISOHuntResponse(ISOHuntResponse response, SearchInformation info) {
    	
    	List<SearchResult> result = new ArrayList<SearchResult>();
    	
    	if (response.items != null && response.items.list != null)
    		for (ISOHuntItem item : response.items.list) {
    			
    			SearchResult sr = new ISOHuntSearchResult(item, info);
    			
    			result.add(sr);
    		}
    	
    	return result;
    }

    private static List<SearchResult> normalizeMininovaVuzeResponse(MininovaVuzeResponse response, SearchInformation info) {
    	
    	List<SearchResult> result = new ArrayList<SearchResult>();
    	
    	if (response.results != null)
    		for (MininovaVuzeItem item : response.results) {
    			
    			SearchResult sr = new MininovaVuzeSearchResult(item, info);
    			
    			result.add(sr);
    		}
    	
    	return result;
    }
    
    
    /**
     * Adds a single result tab for the specified GUID, type,
     * standard query string, and XML query string.
     */
    private static ResultPanel addResultTab(GUID guid,
                                            SearchInformation info) {
        return getSearchResultDisplayer().addResultTab(guid, info);
    }
    
    /** Adds a tab to search results that displays your files. */
    private static ResultPanel addMyFilesResultTab(String title) {
        return getSearchResultDisplayer().addMyFilesResultTab(title);
    }

    /**
     * Adds a browse host tab with the given description.
     */
    private static ResultPanel addBrowseHostTab(GUID guid, String desc) {
        return getSearchResultDisplayer().addResultTab(guid, 
            SearchInformation.createBrowseHostSearch(desc)
        );
    }

    //private static HashMap<String, Character> BLACKLISTED_URNS = null;
    
    /**
     * If i rp is no longer the i'th panel of this, returns silently.
     * Otherwise adds line to rp under the given group.  Updates the count
     * on the tab in this and restarts the spinning lime.
     * @requires this is called from Swing thread
     * @modifies this
     */
//    public static void handleQueryResult(RemoteFileDesc rfd,
//                                         HostData data,
//                                         Set<IpPort> alts) {
//        byte[] replyGUID = data.getMessageGUID();
//        ResultPanel rp = getResultPanelForGUID(new GUID(replyGUID));
//        if(rp != null) {
//            SearchResult sr = new GnutellaSearchResult(rfd, data, alts);
//            getSearchResultDisplayer().addQueryResult(replyGUID, sr, rp);
//        }
//    }
    
    /**
     * Downloads all the selected table lines from the given result panel.
     */
    public static void downloadFromPanel(ResultPanel rp, TableLine[] lines) {
        downloadAll(lines, new GUID(rp.getGUID()), rp.getSearchInformation());
        rp.refresh();
    }

    /**
     * Downloads the selected files in the currently displayed
     * <tt>ResultPanel</tt> if there is one.
     */
    static void doDownload(final ResultPanel rp) {
        final TableLine[] lines = rp.getAllSelectedLines();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SearchMediator.downloadAll(lines, new GUID(rp.getGUID()),
                                           rp.getSearchInformation());
                rp.refresh();
            }
        });
    }
	
	/**
	 * Opens a dialog where you can specify the download directory and final
	 * filename for the selected file.
	 * @param panel
	 * @throws IllegalStateException when there is more than one file selected
	 * for download or there is no file selected.
	 */
	static void doDownloadAs(final ResultPanel panel) {
		final TableLine[] lines = panel.getAllSelectedLines();
		if (lines.length != 1) {
			throw new IllegalStateException("There should only be one search result selected: " + lines.length);
		}
		downloadLine(lines[0], new GUID(panel.getGUID()), null, null, true,
                     panel.getSearchInformation());
	}

    /**
     * Downloads all the selected lines.
     */
    private static void downloadAll(TableLine[] lines, GUID guid, 
                                    SearchInformation searchInfo) 
    {
        for(int i = 0; i < lines.length; i++)
            downloadLine(lines[i], guid, null, null, false, searchInfo);
    }
    
    /**
     * Downloads the given TableLine.
     * @param line
     * @param guid
     * @param saveDir optionally the directory where the final file should be
     * saved to, can be <code>null</code>
     * @param fileName the optional filename of the final file, can be
     * <code>null</code>
     * @param searchInfo The query used to find the file being downloaded.
     */
    private static void downloadLine(TableLine line, GUID guid, File saveDir,
            String fileName, boolean saveAs, SearchInformation searchInfo) 
    {
        if (line == null)
            throw new NullPointerException("Tried to download null line");
        
        line.takeAction(line, guid, saveDir, fileName, saveAs, searchInfo);
    }    
    
    public static void downloadGnutellaLine(TableLine line, GUID guid, File saveDir,
            String fileName, boolean saveAs, SearchInformation searchInfo) 
    {
        if (line == null)
            throw new NullPointerException("Tried to download null line");
        
		//  do not download if no license and user does not acknowledge
		//if ((!line.isLicenseAvailable() && !line.isSecure()) && !GUIMediator.showFirstDownloadDialog())
		//	return;
		
        RemoteFileDesc[] rfds;
        Set<IpPort> alts = new IpPortSet();
        List<RemoteFileDesc> otherRFDs = new LinkedList<RemoteFileDesc>();
        
        rfds = line.getAllRemoteFileDescs();
        alts.addAll(line.getAlts());
        
        
        // Iterate through RFDs and remove matching alts.
        // Also store the first SHA1 capable RFD for collecting alts.
        RemoteFileDesc sha1RFD = null;
        for(int i = 0; i < rfds.length; i++) {
            RemoteFileDesc next = rfds[i];
			// this has been moved down until the download is actually started
            // next.setDownloading(true);
            next.setRetryAfter(0);
            if(next.getSHA1Urn() != null)
                sha1RFD = next;
            alts.remove(next);
        }

        // If no SHA1 rfd, just use the first.
        if(sha1RFD == null)
            sha1RFD = rfds[0];
        
        // Now iterate through alts & add more rfds.
        for(IpPort next : alts) {
            otherRFDs.add(GuiCoreMediator.getRemoteFileDescFactory().createRemoteFileDesc(sha1RFD, next));
        }
		
		// determine per mediatype directory if saveLocation == null
		// and only pass it through if directory is different from default
		// save directory == !isDefault()
		if (saveDir == null && line.getNamedMediaType() != null) {
			FileSetting fs = SharingSettings.getFileSettingForMediaType
			(line.getNamedMediaType().getMediaType());
			if (!fs.isDefault()) {
				saveDir = fs.getValue();
			}
		}

        downloadWithOverwritePrompt(rfds, otherRFDs, guid, saveDir, fileName, 
                                    saveAs, searchInfo);
    }

    /**
     * Downloads the given files, prompting the user if the file already exists.
     * @param queryGUID the guid of the query you ar downloading rfds for.
     * @param searchInfo The query used to find the file being downloaded.
     */
    private static void downloadWithOverwritePrompt(RemoteFileDesc[] rfds,
                                                    List<? extends RemoteFileDesc> alts, GUID queryGUID,
                                                    File saveDir, String fileName,
                                                    boolean saveAs, 
                                                    SearchInformation searchInfo) 
    {
        if (rfds.length < 1)
            return;
        if (containsExe(rfds)) {
            if (!userWantsExeDownload())
                return;
        }

        // Before proceeding...check if there is an rfd withpure metadata
        // ie no file
        int actLine = 0;
        boolean pureFound = false;
        for (; actLine < rfds.length; actLine++) {
            if (rfds[actLine].getIndex() ==
               LimeXMLProperties.DEFAULT_NONFILE_INDEX) {
                // we have our line
                pureFound = true;
                break;
            }
        }
        
        if (pureFound) {
            LimeXMLDocument doc = rfds[actLine].getXMLDocument();
            if(doc != null) {
                String action = doc.getAction();
                if (action != null && !action.equals("")) { // valid action
                    if (doc.actionDetailRequested())
                        action = FrostWireUtils.addLWInfoToUrl(action, GuiCoreMediator.getApplicationServices().getMyGUID());
                    if (action.length() > 255) // trim to make sure its not too long
                        action = action.substring(0, 255);
                    GUIMediator.openURL(action);
                    return; // goodbye
                }
            }
        }
        // No pure metadata lines found...continue as usual...
        GuiDownloaderFactory factory = new SearchResultDownloaderFactory
        	(rfds, alts, queryGUID, saveDir, fileName); 
		Downloader dl = saveAs ? DownloaderUtils.createDownloaderAs(factory) 
				: DownloaderUtils.createDownloader(factory);
		if (dl != null) {
			setAsDownloading(rfds);
            if (validateInfo(searchInfo) == QUERY_VALID)
                dl.setAttribute(SEARCH_INFORMATION_KEY, searchInfo.toMap(), false);
		}
    }

	private static void setAsDownloading(RemoteFileDesc[] rfds) {
		for (int i = 0; i < rfds.length; i++) {
			rfds[i].setDownloading(true);
		}
	}
	
    /**
     * Returns true if any of the entries of rfd contains a .exe file.
     */
    private static boolean containsExe(RemoteFileDesc[] rfd) {
        for (int i = 0; i < rfd.length; i++) {
            if (rfd[i].getFileName().toLowerCase(Locale.US).endsWith("exe"))
                return true;
        }
        return false;
    }

    /**
     * Prompts the user if they want to download an .exe file.
     * Returns true s/he said yes.
     */
    private static boolean userWantsExeDownload() {        
        DialogOption response = GUIMediator.showYesNoMessage(I18n.tr("One of the selected files is an executable program and could contain a virus. Are you sure you want to download it?"),
                                            QuestionsHandler.PROMPT_FOR_EXE, DialogOption.NO);
        return response == DialogOption.YES;
    }

    ////////////////////////// Other Controls ///////////////////////////

    /**
     * called by ResultPanel when the views are changed. Used to set the
     * tab to indicate the correct number of TableLines in the current
     * view.
     */
    static void setTabDisplayCount(ResultPanel rp) {
        getSearchResultDisplayer().setTabDisplayCount(rp);
    }

    /**
     * @modifies tabbed pane, entries
     * @effects removes the currently selected result window (if any)
     *  from this
     */
    static void killSearch() {
        getSearchResultDisplayer().killSearch();
    }
    
    /**
     * Notification that a given ResultPanel has been selected
     */
    static void panelSelected(ResultPanel panel) {
        //getSearchInputManager().setFiltersFor(panel);
    }
    
    /**
     * Notification that a search has been killed.
     */
    static void searchKilled(ResultPanel panel) {
        getSearchInputManager().panelRemoved(panel);
        ResultPanel rp = getSearchResultDisplayer().getSelectedResultPanel();
        if (rp != null) {
           // getSearchInputManager().setFiltersFor(rp);
        }

        panel.cleanup();
    }
    
    /**
     * Checks to see if the spinning lime should be stopped.
     */
    static void checkToStopLime() {
        getSearchResultDisplayer().checkToStopLime();
    }
    
    /**
     * Returns the <tt>ResultPanel</tt> for the specified GUID.
     * 
     * @param rguid the guid to search for
     * @return the <tt>ResultPanel</tt> that matches the GUID, or null
     *  if none match.
     */
    static ResultPanel getResultPanelForGUID(GUID rguid) {
        return getSearchResultDisplayer().getResultPanelForGUID(rguid);
    }

    /** @returns true if the user is still using the query results for the input
     *  guid, else false.
     */
    public static boolean queryIsAlive(GUID guid) {
        return (getResultPanelForGUID(guid) != null);
    }

    /**
     * Returns the search input panel component.
     *
     * @return the search input panel component
     */
    public static JComponent getSearchComponent() {
        return getSearchInputManager().getComponent();
    }

    /**
     * Returns the <tt>JComponent</tt> instance containing all of the
     * search result UI components.
     *
     * @return the <tt>JComponent</tt> instance containing all of the
     *  search result UI components
     */
    public static JComponent getResultComponent() {
        return getSearchResultDisplayer().getComponent();
    }

    /**
     * @return an Ad message that should be displayed on top 
     * of the search pannel.
     */
	public synchronized static Ad getAd() {
        if (banner == null)
            initBanner();
	    return banner.getAd();   
    }

	private static SearchInputManager getSearchInputManager() {
	    if (INPUT_MANAGER == null) {
	        INPUT_MANAGER = new SearchInputManager();
	    }
	    return INPUT_MANAGER;
	}
	
	public static SearchResultDisplayer getSearchResultDisplayer() {
	    if (RESULT_DISPLAYER == null) {
	        RESULT_DISPLAYER = new SearchResultDisplayer();
	    }
	    return RESULT_DISPLAYER;
	}
}

