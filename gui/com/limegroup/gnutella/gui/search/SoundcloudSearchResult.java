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

import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.bittorrent.websearch.soundcloud.SoundcloudTrackSearchResult;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.util.PopupUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class SoundcloudSearchResult extends AbstractSearchResult {

    private final SoundcloudTrackSearchResult sr;
    private final SearchEngine searchEngine;

    public SoundcloudSearchResult(SoundcloudTrackSearchResult sr, SearchEngine searchEngine) {
        this.sr = sr;
        this.searchEngine = searchEngine;
    }

    @Override
    public String getFileName() {
        return sr.getFileName();
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
    public String getVendor() {
        return sr.getVendor();
    }

    @Override
    public int getSpeed() {
        return Integer.MAX_VALUE - 2;
    }

    @Override
    public boolean isMeasuredSpeed() {
        return false;
    }

    @Override
    public int getQuality() {
        return 0;
    }

    @Override
    public void takeAction(SearchResultDataLine line, GUID guid, File saveDir, String fileName, boolean saveAs, SearchInformation searchInfo) {
        GUIMediator.instance().openSoundcloudTrackUrl(sr.getTorrentURI(), sr.getDisplayName());
        showDetails(false);
    }

    @Override
    public JPopupMenu createMenu(JPopupMenu popupMenu, SearchResultDataLine[] lines, SearchResultMediator rp) {
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                takeAction(null, null, null, null, false, null);
            }
        }, popupMenu, lines.length > 0, 1);
        PopupUtils.addMenuItem(SearchMediator.SOUNDCLOUD_DETAILS_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDetails(true);
            }
        }, popupMenu, lines.length == 1, 2);

        return popupMenu;
    }

    @Override
    public String getHash() {
        return sr.getHash();
    }

    @Override
    public String getTorrentURI() {
        return sr.getTorrentURI();
    }

    @Override
    public int getSeeds() {
        return sr.getSeeds();
    }

    @Override
    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    @Override
    public WebSearchResult getWebSearchResult() {
        return sr;
    }
}
