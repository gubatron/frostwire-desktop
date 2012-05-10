/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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

import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.limewire.util.FilenameUtils;

import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.gui.GuiFrostWireUtils;
import com.frostwire.websearch.youtube.YouTubeSearchResult;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.util.PopupUtils;

public final class YouTubePackageItemSearchResult extends AbstractSearchResult {

    private static final String AAC_LOW_QUALITY = "(AAC)";
    static final String AAC_HIGH_QUALITY = "(AAC-High Quality)";
    private final YouTubeSearchResult sr;
    private final FilePackage filePackage;
    private final SearchEngine searchEngine;
    private final SearchInformation info;

    private final String filename;

    public YouTubePackageItemSearchResult(YouTubeSearchResult sr, FilePackage filePackage, SearchEngine searchEngine, SearchInformation info) {
        this.sr = sr;
        this.filePackage = filePackage;
        this.searchEngine = searchEngine;
        this.info = info;

        this.filename = readFilename(filePackage);
    }

    @Override
    public String getFileName() {
        return filename;
    }

    @Override
    public String getFilenameNoExtension() {
        String fname = FilenameUtils.getBaseName(filename);
        if (fname.indexOf(AAC_HIGH_QUALITY)>0) {
            return AAC_HIGH_QUALITY + " " + fname.replace(AAC_HIGH_QUALITY, "");
        } else if (fname.indexOf(AAC_LOW_QUALITY)>0) {
            return AAC_LOW_QUALITY + " " + fname.replace(AAC_LOW_QUALITY, "");
        }
        
        return fname;
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
    public float getSpamRating() {
        return 0;
    }

    @Override
    public void takeAction(SearchResultDataLine line, GUID guid, File saveDir, String fileName, boolean saveAs, SearchInformation searchInfo) {
        GUIMediator.instance().openYouTubeItem(filePackage);
    }

    @Override
    public void initialize(SearchResultDataLine line) {
        line.setAddedOn(getCreationTime());
    }

    @Override
    public JPopupMenu createMenu(JPopupMenu popupMenu, SearchResultDataLine[] lines, SearchResultMediator rp) {
        PopupUtils.addMenuItem(SearchMediator.BUY_NOW_STRING, rp.BUY_LISTENER, popupMenu, lines.length == 1, 0);
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                takeAction(null, null, null, null, false, null);
            }
        }, popupMenu, lines.length > 0, 1);
        PopupUtils.addMenuItem(SearchMediator.YOUTUBE_DETAILS_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showTorrentDetails(-1);
            }
        }, popupMenu, lines.length == 1, 2);

        return popupMenu;
    }

    @Override
    public void showTorrentDetails(long delay) {
        GuiFrostWireUtils.showTorrentDetails(delay, searchEngine.redirectUrl, info.getQuery(), sr.getTorrentDetailsURL(), getFileName());
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

    private String readFilename(FilePackage filePackage) {
        DownloadLink dl = filePackage.getChildren().get(0);
        if (dl.getStringProperty("convertto", "").equals("AUDIOMP3")) {
            return FilenameUtils.getBaseName(dl.getName()) + ".mp3";
        }

        return dl.getName();
    }
}
