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

import com.frostwire.gui.player.StreamMediaSource;
import com.frostwire.search.SearchResult;
import com.frostwire.search.soundcloud.SoundcloudSearchResult;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.util.PopupUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class SoundcloudUISearchResult extends AbstractUISearchResult {

    private final SoundcloudSearchResult sr;
    private final SearchEngine searchEngine;

    public SoundcloudUISearchResult(SoundcloudSearchResult sr, SearchEngine searchEngine, String query) {
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
        GUIMediator.instance().openSoundcloudTrackUrl(sr.getDetailsUrl(), sr.getDisplayName(), this);
        showDetails(false);
    }

    @Override
    public JPopupMenu createMenu(JPopupMenu popupMenu, SearchResultDataLine[] lines, SearchResultMediator rp) {
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                download(false);
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
    public void play() {
        GUIMediator.instance().launchMedia(new StreamMediaSource(sr.getStreamUrl(), "Soundcloud: " + sr.getDisplayName(), sr.getDetailsUrl(), false));
    }

    public String getThumbnailUrl() {
        return sr.getThumbnailUrl();
    }

    public String getUsername() {
        return sr.getUsername();
    }

    public String getDetailsUrl() {
        return sr.getDetailsUrl();
    }
}
