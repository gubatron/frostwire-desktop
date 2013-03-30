/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;

import org.apache.commons.io.FilenameUtils;

import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.gui.player.StreamMediaSource;
import com.frostwire.search.SearchResult;
import com.frostwire.search.youtube2.YouTubeCrawledSearchResult;
import com.frostwire.search.youtube2.YouTubeSearchResult;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.util.PopupUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class YouTubePackageSearchResult extends AbstractSearchResult {

    private final YouTubeCrawledSearchResult sr;
    private final SearchEngine searchEngine;

    public YouTubePackageSearchResult(YouTubeCrawledSearchResult sr, SearchEngine searchEngine, String query) {
        super(query);
        this.sr = sr;
        this.searchEngine = searchEngine;
    }

    @Override
    public String getFilename() {
        return sr.getFilename();
    }

    @Override
    public String getDisplayName() {
        return sr.getDisplayName();
    }

    @Override
    public long getSize() {
        return sr.getSize();
    }

    @Override
    public long getCreationTime() {
        return sr.getCreationTime();
    }

    @Override
    public String getSource() {
        return sr.getSource();
    }

    @Override
    public void download(boolean partial) {
        GUIMediator.instance().openYouTubeVideoUrl(sr.getDetailsUrl());
        showDetails(false);
    }

    @Override
    public JPopupMenu createMenu(JPopupMenu popupMenu, SearchResultDataLine[] lines, SearchResultMediator rp) {
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                download(false);
            }
        }, popupMenu, lines.length > 0, 1);
        PopupUtils.addMenuItem(SearchMediator.YOUTUBE_DETAILS_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDetails(true);
            }
        }, popupMenu, lines.length == 1, 2);

        return popupMenu;
    }

    @Override
    public String getHash() {
        return null;
    }

    @Override
    public String getTorrentURI() {
        return null;
    }

    @Override
    public int getSeeds() {
        return -1;
    }

    @Override
    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    @Override
    public SearchResult getSearchResult() {
        return sr;
    }

    @Override
    public boolean allowDeepSearch() {
        return true;
    }

    @Override
    public void play() {
        String streamUrl = sr.getStreamUrl();
        MediaType mediaType = MediaType.getMediaTypeForExtension(FilenameUtils.getExtension(getFilename()));
        boolean showPlayerWindow = mediaType.equals(MediaType.getVideoMediaType());
        GUIMediator.instance().launchMedia(new StreamMediaSource(streamUrl, "YouTube: " + sr.getDisplayName(), sr.getDetailsUrl(), showPlayerWindow));
    }
}
