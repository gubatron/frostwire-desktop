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
import com.frostwire.search.SearchResult;
import com.frostwire.search.torrent.TorrentSearchResult;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.util.PopupUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class TorrentUISearchResult extends AbstractUISearchResult {

    private TorrentSearchResult sr;
    private SearchEngine _searchEngine;
    private final String extension;

    public TorrentUISearchResult(TorrentSearchResult sr, SearchEngine searchEngine, String query) {
        super(query);
        this.sr = sr;
        _searchEngine = searchEngine;
        this.extension = FilenameUtils.getExtension(sr.getFilename());
    }

    @Override
    public long getCreationTime() {
        return sr.getCreationTime();
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public String getFilename() {
        return sr.getFilename();
    }

    public String getHash() {
        return sr.getHash();
    }

    @Override
    public long getSize() {
        return sr.getSize();
    }

    @Override
    public String getSource() {
        return sr.getSource();
    }

    @Override
    public void download(boolean partial) {
        GUIMediator.instance().openTorrentSearchResult(sr, partial);
        showDetails(false);
    }

    @Override
    public JPopupMenu createMenu(JPopupMenu popupMenu, SearchResultDataLine[] lines, SearchResultMediator resultPanel) {
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                download(false);
            }
        }, popupMenu, lines.length > 0, 1);
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_PARTIAL_FILES_STRING, resultPanel.DOWNLOAD_PARTIAL_FILES_LISTENER, popupMenu, lines.length == 1, 2);
        PopupUtils.addMenuItem(SearchMediator.TORRENT_DETAILS_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDetails(true);
            }
        }, popupMenu, lines.length == 1, 3);

        return popupMenu;
    }

    public int getSeeds() {
        return sr.getSeeds();
    }

    public SearchEngine getSearchEngine() {
        return _searchEngine;
    }
    
    @Override
    public String getDisplayName() {
        return sr.getDisplayName();
    }

    @Override
    public SearchResult getSearchResult() {
        return sr;
    }

    @Override
    public void play() {
        // TODO Auto-generated method stub
        
    }
}
