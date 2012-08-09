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
import java.io.File;

import javax.swing.JPopupMenu;

import org.gudy.azureus2.core3.torrent.TOTorrentFile;

import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.util.PopupUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class DeepSearchResult extends AbstractSearchResult implements BittorrentSearchResult {

    private WebSearchResult _item;
    private SearchEngine _searchEngine;
    private TOTorrentFile _torrentFile;

    public DeepSearchResult(TOTorrentFile torrentFile, WebSearchResult item, SearchEngine searchEngine) {
        _item = item;
        _searchEngine = searchEngine;
        _torrentFile = torrentFile;
    }

    @Override
    public long getCreationTime() {
        return _item.getCreationTime();
    }

    @Override
    public String getExtension() {
        return _torrentFile.getRelativePath().substring(_torrentFile.getRelativePath().lastIndexOf(".") + 1);
    }

    @Override
    public String getFileName() {
        String fName = new File(_torrentFile.getRelativePath()).getName();
        if (fName.startsWith("/")) {
            return fName.substring(1);
        }
        return fName;
    }

    @Override
    public String getDisplayName() {
        if (_torrentFile.getRelativePath().indexOf("/") != -1) {
            String fileName = _torrentFile.getRelativePath().substring(_torrentFile.getRelativePath().lastIndexOf("/"));

            if (fileName.startsWith("/")) {
                fileName = fileName.substring(1);
            }

            return fileName.substring(0, fileName.lastIndexOf("."));
        }

        return _torrentFile.getRelativePath().substring(0, _torrentFile.getRelativePath().lastIndexOf("."));
    }

    @Override
    public int getQuality() {
        //TODO: Delete this method
        return 0;
    }

    public String getHash() {
        return _item.getHash();
    }

    public String getTorrentURI() {
        return _item.getTorrentURI();
    }

    @Override
    public long getSize() {
        return _torrentFile.getLength();
    }

    @Override
    public int getSpeed() {
        return Integer.MAX_VALUE - 2;
    }

    @Override
    public String getVendor() {
        return _item.getVendor();
    }

    @Override
    public boolean isMeasuredSpeed() {
        return false;
    }

    @Override
    public void takeAction(SearchResultDataLine line, GUID guid, File saveDir, String fileName, boolean saveAs, SearchInformation searchInfo) {
        GUIMediator.instance().openTorrentSearchResult(_item, _torrentFile.getRelativePath());
        showDetails(false);
    }

    @Override
    public JPopupMenu createMenu(JPopupMenu popupMenu, SearchResultDataLine[] lines, SearchResultMediator resultPanel) {

        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                takeAction(null, null, null, null, false, null);
            }
        }, popupMenu, lines.length > 0, 1);

        PopupUtils.addMenuItem(SearchMediator.TORRENT_DETAILS_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDetails(true);
            }
        }, popupMenu, lines.length == 1, 2);

        return popupMenu;
    }

    public int getSeeds() {
        return _item.getSeeds();
    }

    public SearchEngine getSearchEngine() {
        return _searchEngine;
    }

    public WebSearchResult getWebSearchResult() {
        return _item;
    }
}
