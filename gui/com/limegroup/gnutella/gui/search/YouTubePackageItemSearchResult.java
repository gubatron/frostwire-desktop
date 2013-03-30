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

import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.limewire.util.FilenameUtils;

import com.frostwire.gui.player.StreamMediaSource;
import com.frostwire.search.SearchResult;
import com.frostwire.search.youtube2.YouTubeSearchResult;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.util.PopupUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class YouTubePackageItemSearchResult extends AbstractSearchResult {

    private static final String AAC_LOW_QUALITY = "(AAC)";
    static final String AAC_HIGH_QUALITY = "(AAC-High Quality)";

    private final YouTubeSearchResult sr;
    private final FilePackage filePackage;
    private final SearchEngine searchEngine;

    private final String filename;
    private final long size;

    public YouTubePackageItemSearchResult(YouTubeSearchResult sr, FilePackage filePackage, SearchEngine searchEngine, String query) {
        super(query);
        this.sr = sr;
        this.filePackage = filePackage;
        this.searchEngine = searchEngine;

        this.filename = readFilename(filePackage);
        this.size = filePackage.getChildren().get(0).getLongProperty("size", -1);
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String getDisplayName() {
        String fname = FilenameUtils.getBaseName(filename);
        if (fname.indexOf(AAC_HIGH_QUALITY) > 0) {
            return AAC_HIGH_QUALITY + " " + fname.replace(AAC_HIGH_QUALITY, "");
        } else if (fname.indexOf(AAC_LOW_QUALITY) > 0) {
            return AAC_LOW_QUALITY + " " + fname.replace(AAC_LOW_QUALITY, "");
        }

        return fname;
    }

    @Override
    public long getSize() {
        return size;
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
        GUIMediator.instance().openYouTubeItem(filePackage);
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
    public void play() {
        String streamUrl = filePackage.getChildren().get(0).getDownloadURL();
        MediaType mediaType = MediaType.getMediaTypeForExtension(FilenameUtils.getExtension(filename));
        boolean showPlayerWindow = mediaType.equals(MediaType.getVideoMediaType());
        GUIMediator.instance().launchMedia(new StreamMediaSource(streamUrl, "YouTube: " + sr.getDisplayName(), sr.getDetailsUrl(), showPlayerWindow));
    }

    private String readFilename(FilePackage filePackage) {
        DownloadLink dl = filePackage.getChildren().get(0);
        if (dl.getStringProperty("convertto", "").equals("AUDIOMP3")) {
            return FilenameUtils.getBaseName(dl.getName()) + ".mp3";
        }

        return dl.getName();
    }
}
