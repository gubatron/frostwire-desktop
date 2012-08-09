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

import javax.swing.JPopupMenu;

import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.SpeedConstants;

/**
 * A single SearchResult. These are returned in the {@link SearchInputPanel} and
 * are used to create {@link SearchResultDataLine}s to show search results. *
 */
public interface SearchResult {

    /**
     * @return the file name
     */
    String getFileName();
    
    /**
     * Gets the size of this SearchResult.
     */
    long getSize();
    
    /**
     * @return milliseconds since January 01, 1970 the artifact of t
     */
    long getCreationTime();
    
    /**
     * @return the name of vendor who created this artifact
     */
    String getVendor();
    
    /**
     * @return the connection speed of this result or
     *         {@link SpeedConstants#THIRD_PARTY_SPEED_INT} for a
     *         {@link ThirdPartySearchResult}
     */
    int getSpeed();
    
    /**
     * @return <code>true</code> if this speed is messaured.
     */
    boolean isMeasuredSpeed();
    
    /**
     * @return the quality of the search result as one of
     * <ul>
     *  <li>{@link QualityRenderer#SPAM_FILE_QUALITY}</li>   
     *  <li>{@link QualityRenderer#SAVED_FILE_QUALITY}</li>
     *  <li>{@link QualityRenderer#DOWNLOADING_FILE_QUALITY}</li>
     *  <li>{@link QualityRenderer#INCOMPLETE_FILE_QUALITY}</li>   
     *  <li>{@link QualityRenderer#SECURE_QUALITY}</li>   
     *  <li>{@link QualityRenderer#THIRD_PARTY_RESULT_QUALITY}</li>   
     *  <li>{@link QualityRenderer#MULTICAST_QUALITY}</li>   
     *  <li>{@link QualityRenderer#EXCELLENT_QUALITY}</li>   
     *  <li>{@link QualityRenderer#GOOD_QUALITY}</li>   
     *  <li>{@link QualityRenderer#FAIR_QUALITY}</li>   
     *  <li>{@link QualityRenderer#POOR_QUALITY}</li>
     *  <li>{@link QualityRenderer#THIRD_PARTY_RESULT_QUALITY}</li>
     * </ul>
     */
    int getQuality();
    
    /**
     * Returns the extension of this result.
     */
    String getExtension();

    /**
     * This method is called when a {@link SearchResultDataLine} is clicked on in the
     * {@link SearchResultMediator}, and the user wants to take some action, such as
     * downloading or displaying the result in a browser.
     * 
     * @param line the line on which was clicked; this is needed for the
     *        {@link GnutellaSearchResult} so it can pass the line back to
     *        {@link SearchMediator} to do the download
     * @param guid the GUID of the result; used for the reason above
     * @param saveDir if we were to download the result, it would go here; used
     *        for the reason above
     * @param fileName name of the file in which we would save the result on a
     *        download; used for the reason above
     * @param saveAs used for the reason above
     * @param searchInfo the info used for the original search
     */
    void takeAction(SearchResultDataLine line, GUID guid, File saveDir, String fileName,
            boolean saveAs, SearchInformation searchInfo);

    JPopupMenu createMenu(JPopupMenu popupMenu, SearchResultDataLine[] lines, SearchResultMediator rp);
    
    public String getHash();

    public String getTorrentURI();

    public int getSeeds();
    
    public SearchEngine getSearchEngine();
    
    public WebSearchResult getWebSearchResult();
    
    public void showDetails(boolean now);
    
    public String getDisplayName();
    
    public boolean allowDeepSearch();
    
    public String getStreamUrl();
}