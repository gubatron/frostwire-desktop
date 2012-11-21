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

package com.limegroup.gnutella.gui.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.limewire.util.I18NConvert;

import com.frostwire.AzureusStarter;
import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.bittorrent.websearch.soundcloud.SoundcloudTrackSearchResult;
import com.frostwire.gui.filters.SearchFilter;
import com.frostwire.gui.filters.SearchFilterFactory;
import com.frostwire.gui.filters.SearchFilterFactoryImpl;
import com.frostwire.websearch.youtube.YouTubeSearchResult;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.SearchSettings;

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

    static final String LAUNCH_STRING = I18n.tr("Launch Action");

    static final String REPEAT_SEARCH_STRING = I18n.tr("Repeat Search");

    static final String DOWNLOAD_PARTIAL_FILES_STRING = I18n.tr("Download Partial Files");

    static final String TORRENT_DETAILS_STRING = I18n.tr("Torrent Details");

    static final String YOUTUBE_DETAILS_STRING = I18n.tr("View in YouTube");
    
    static final String SOUNDCLOUD_DETAILS_STRING = I18n.tr("View in Soundcloud");

    /**
     * Variable for the component that handles all search input from the user.
     */
    private static SearchInputManager INPUT_MANAGER;

    /**
     * This instance handles the display of all search results.
     * TODO: Changed to package-protected for testing to add special results
     */
    private static SearchResultDisplayer RESULT_DISPLAYER;

    private static SearchFilterFactory SEARCH_FILTER_FACTORY;

    /**
     * Constructs the UI components of the search result display area of the 
     * search tab.
     */
    public SearchMediator() {
        // Set the splash screen text...
        final String splashScreenString = I18n.tr("Loading Search Window...");
        GUIMediator.setSplashScreenString(splashScreenString);
        GUIMediator.addRefreshListener(getSearchResultDisplayer());

        // Link up the tabs of results with the filters of the input screen.
        getSearchResultDisplayer().setSearchListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                SearchResultMediator panel = getSearchResultDisplayer().getSelectedResultPanel();
                if (panel == null)
                    getSearchInputManager().clearFilters();
                else
                    getSearchInputManager().setFiltersFor(panel);
            }
        });
    }

    /**
     * Rebuilds the INPUT_MANAGER's panel.
     */
    public static void rebuildInputPanel() {
        getSearchInputManager().rebuild();
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
    static byte[] repeatSearch(SearchResultMediator rp, SearchInformation info) {
        return repeatSearch(rp, info, true);
    }

    /** 
     * Repeats the given search.
     */
    static byte[] repeatSearch(SearchResultMediator rp, SearchInformation info, boolean clearingResults) {
        if (!validate(info))
            return null;

        // 1. Update panel with new GUID
        byte[] guidBytes = newQueryGUID();
        final GUID newGuid = new GUID(guidBytes);

        rp.setGUID(newGuid);
        if (clearingResults) {
            getSearchInputManager().panelReset(rp);
        }

        GUIMediator.instance().setSearching(true);
        doSearch(guidBytes, info);

        return guidBytes;
    }

    private static byte[] newQueryGUID() {
        return GUID.makeGuid();
    }

    /**
     * Initiates a new search with the specified SearchInformation.
     *
     * Returns the GUID of the search if a search was initiated,
     * otherwise returns null.
     */
    public static byte[] triggerSearch(final SearchInformation info) {
        if (!validate(info))
            return null;

        // generate a guid for the search.
        final byte[] guid = newQueryGUID();
        addResultTab(new GUID(guid), info);

        doSearch(guid, info);

        return guid;
    }

    /**
     * Triggers a search given the text in the search field.  For testing
     * purposes returns the 16-byte GUID of the search or null if the search
     * didn't happen because it was greedy, etc.  
     */
    public static byte[] triggerSearch(String query) {
        return triggerSearch(SearchInformation.createKeywordSearch(query, null, MediaType.getAnyTypeMediaType()));
    }

    /**
     * Validates the given search information.
     */
    private static boolean validate(SearchInformation info) {
        switch (validateInfo(info)) {
        case QUERY_EMPTY:
            return false;
        case QUERY_TOO_SHORT:
            GUIMediator.showMessage(I18n.tr("Your search must be at least three characters to avoid congesting the network."));
            return false;
        case QUERY_TOO_LONG:
            GUIMediator.showMessage(I18n.tr("Your search is too long. Please make your search smaller and try again."));
            return false;
        case QUERY_VALID:
        default:
            /**
            boolean searchingTorrents = info.getMediaType().equals(MediaType.getTorrentMediaType());
            boolean gnutellaStarted = GuiCoreMediator.getLifecycleManager().isStarted();
            boolean azureusStarted = AzureusStarter.isAzureusCoreStarted();

            if ((searchingTorrents && !azureusStarted) || (!searchingTorrents && !gnutellaStarted)) {
                GUIMediator.showMessage(I18n.tr("Please wait, FrostWire must finish loading before a search can be started."));
                return false;
            }
            */

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

        if (query.length() == 0) {
            return QUERY_EMPTY;
        /*} else if (query.length() <= 2 && !(query.length() == 2 && ((Character.isDigit(query.charAt(0)) && Character.isLetter(query.charAt(1))) || (Character.isLetter(query.charAt(0)) && Character.isDigit(query.charAt(1)))))) {
            return QUERY_TOO_SHORT;*/
        } else if (query.length() > SearchSettings.MAX_QUERY_LENGTH.getValue()) {
            return QUERY_TOO_LONG;
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

        List<SearchEngine> searchEngines = SearchEngine.getSearchEngines();

        for (final SearchEngine searchEngine : searchEngines) {
            if (searchEngine.isEnabled()) {
                Thread t = new Thread(new Runnable() {
                    public void run() {

                        final SearchResultMediator rp = getResultPanelForGUID(new GUID(guid));
                        if (rp != null && !rp.isStopped()) {
                            rp.incrementSearchCount();
                            List<WebSearchResult> webResults = searchEngine.getPerformer().search(query);

                            if (webResults.size() > 0) {
                                final List<SearchResult> results = normalizeWebResults(webResults, searchEngine, info);

                                GUIMediator.safeInvokeAndWait(new Runnable() {
                                    public void run() {
                                        try {
                                            SearchFilter filter = getSearchFilterFactory().createFilter();
                                            for (SearchResult sr : results) {
                                                if (filter.allow(sr)) {
                                                    getSearchResultDisplayer().addQueryResult(guid, sr, rp);
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {
                                            SearchResultMediator trp = getResultPanelForGUID(new GUID(guid));
                                            if (trp != null) {
                                                trp.decrementSearchCount();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
                t.setDaemon(true);
                t.start();
            }
        }

        //start local search.
        doLocalSearch(guid, query, info);
    }

    public static void doLocalSearch(final byte[] guid, final String query, final SearchInformation info) {
        Thread t = new Thread(new Runnable() {
            public void run() {

                final SearchResultMediator rp = getResultPanelForGUID(new GUID(guid));
                if (rp != null && !rp.isStopped()) {
                    rp.incrementSearchCount();
                    final List<SmartSearchResult> localResults = LocalSearchEngine.instance().search(query);

                    final SearchFilter filter = getSearchFilterFactory().createFilter();

                    if (localResults.size() > 0) {
                        GUIMediator.safeInvokeLater(new Runnable() {
                            public void run() {
                                try {
                                    for (SearchResult sr : localResults) {
                                        if (filter.allow(sr)) {
                                            getSearchResultDisplayer().addQueryResult(guid, sr, rp);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    SearchResultMediator trp = getResultPanelForGUID(new GUID(guid));
                                    if (trp != null) {
                                        trp.decrementSearchCount();
                                    }
                                }
                            }
                        });
                    }
                }

                LocalSearchEngine.instance().deepSearch(guid, query);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private static List<SearchResult> normalizeWebResults(List<WebSearchResult> webResults, SearchEngine engine, SearchInformation info) {

        List<SearchResult> result = new ArrayList<SearchResult>();

        for (WebSearchResult webResult : webResults) {

            SearchResult sr = null;

            if (webResult instanceof YouTubeSearchResult) {
                sr = new YouTubePackageSearchResult((YouTubeSearchResult) webResult, engine, info.getQuery());
            } else if (webResult instanceof SoundcloudTrackSearchResult) {
                sr = new SoundcloudSearchResult((SoundcloudTrackSearchResult)webResult, engine, info.getQuery());
            } else {
                sr = new SearchEngineSearchResult(webResult, engine, info.getQuery());
            }

            result.add(sr);
        }

        return result;
    }

    /**
     * Adds a single result tab for the specified GUID, type,
     * standard query string, and XML query string.
     */
    private static SearchResultMediator addResultTab(GUID guid, SearchInformation info) {
        return getSearchResultDisplayer().addResultTab(guid, info);
    }

    /**
     * Downloads all the selected table lines from the given result panel.
     */
    public static void downloadFromPanel(SearchResultMediator rp, SearchResultDataLine[] lines) {
        downloadAll(lines, new GUID(rp.getGUID()), rp.getSearchInformation());
        rp.refresh();
    }

    
    public static boolean hasTorrentDownloads(SearchResultDataLine[] lines) {
        for (SearchResultDataLine line : lines) {
            SearchResult initializeObject = line.getInitializeObject();
            if (initializeObject instanceof BittorrentSearchResult) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Downloads the selected files in the currently displayed
     * <tt>ResultPanel</tt> if there is one.
     */
    static void doDownload(final SearchResultMediator rp) {
        final SearchResultDataLine[] lines = rp.getAllSelectedLines();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                
                if (!AzureusStarter.isAzureusCoreStarted() && hasTorrentDownloads(lines)) {
                    GUIMediator.showMessage(I18n.tr("Please try this download in a few seconds, FrostWire is still warming up."));            
                    return;
                }

                SearchMediator.downloadAll(lines, new GUID(rp.getGUID()), rp.getSearchInformation());
                rp.refresh();
            }
        });
    }
   
    /**
     * Downloads all the selected lines.
     */
    private static void downloadAll(SearchResultDataLine[] lines, GUID guid, SearchInformation searchInfo) {
        for (int i = 0; i < lines.length; i++)
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
    private static void downloadLine(SearchResultDataLine line, GUID guid, File saveDir, String fileName, boolean saveAs, SearchInformation searchInfo) {
        if (line == null)
            throw new NullPointerException("Tried to download null line");

        line.getSearchResult().download(false);
    }

    ////////////////////////// Other Controls ///////////////////////////

    /**
     * called by ResultPanel when the views are changed. Used to set the
     * tab to indicate the correct number of TableLines in the current
     * view.
     */
    static void setTabDisplayCount(SearchResultMediator rp) {
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
    static void panelSelected(SearchResultMediator panel) {
        //getSearchInputManager().setFiltersFor(panel);
    }

    /**
     * Notification that a search has been killed.
     */
    static void searchKilled(SearchResultMediator panel) {
        getSearchInputManager().panelRemoved(panel);
        SearchResultMediator rp = getSearchResultDisplayer().getSelectedResultPanel();
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
    static SearchResultMediator getResultPanelForGUID(GUID rguid) {
        return getSearchResultDisplayer().getResultPanelForGUID(rguid);
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

    public static SearchFilterFactory getSearchFilterFactory() {
        if (SEARCH_FILTER_FACTORY == null) {
            SEARCH_FILTER_FACTORY = new SearchFilterFactoryImpl();
        }
        return SEARCH_FILTER_FACTORY;
    }

}
