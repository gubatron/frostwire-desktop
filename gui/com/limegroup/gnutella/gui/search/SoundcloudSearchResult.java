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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JPopupMenu;

import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.bittorrent.websearch.soundcloud.SoundcloudTrackSearchResult;
import com.frostwire.gui.player.StreamAudioSource;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.util.PopupUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class SoundcloudSearchResult extends AbstractSearchResult implements StreamableSearchResult {

    private static final Log LOG = LogFactory.getLog(SoundcloudSearchResult.class);

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final SoundcloudTrackSearchResult sr;
    private final SearchEngine searchEngine;

    private String streamUrl;
    private boolean streamUrlCrawled;

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
    public String getSource() {
        return sr.getSource();
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
    public void download(boolean partial) {
        GUIMediator.instance().openSoundcloudTrackUrl(sr.getTorrentURI(), sr.getDisplayName());
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

    @Override
    public void play() {
        executor.execute(new PlayTask());
    }

    private String crawlStreamUrl(String detailsUrl) {
        String url = null;
        try {
            Browser br = new Browser();
            br.getPage(detailsUrl);
            if (br.containsHTML("Oops, looks like we can\\'t find that page"))
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            String filename = br.getRegex("<em>(.*?)</em>").getMatch(0);
            br.setFollowRedirects(true);
            if (filename == null)
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            String username = br.getRegex("\"username\":\"(.*?)\"").getMatch(0);
            filename = Encoding.htmlDecode(filename.trim());
            String type = br.getRegex("title=\"Uploaded format\">(.*?)<").getMatch(0);
            if (type == null) {
                type = br.getRegex("class=\"file\\-type\">(.*?)</span>").getMatch(0);
                if (type == null)
                    type = "mp3";
            }
            username = username.trim();
            if (username != null && !filename.contains(username))
                filename += " - " + username;
            filename += "." + type;
            if (!br.containsHTML("class=\"download pl\\-button\"")) {
                String[] data = br.getRegex("\"uid\":\"(.*?)\".*?\"token\":\"(.*?)\"").getRow(0);
                url = "http://media.soundcloud.com/stream/" + data[0] + "?stream_token=" + data[1];
                URLConnectionAdapter con = br.openGetConnection(url);
                if (!con.getContentType().contains("html")) {
                    //parameter.setDownloadSize(con.getLongContentLength());
                } else {
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                }
                con.disconnect();
                //parameter.getLinkStatus().setStatusText(JDL.L("plugins.hoster.SoundCloudCom.status.previewavailable", "Preview is downloadable"));
            } else {
                String filesize = br.getRegex("The file you're about to download has a size of (.*?)\"").getMatch(0);
                if (filesize != null) {
                    //parameter.setDownloadSize(SizeFormatter.getSize(filesize));
                }
                url = detailsUrl + "/download";
                //parameter.getLinkStatus().setStatusText(JDL.L("plugins.hoster.SoundCloudCom.status.downloadavailable", "Original file is downloadable"));
            }
            //parameter.setFinalFileName(filename);
        } catch (Throwable e) {
            LOG.error("Error crawling soundcloud stream url from: " + detailsUrl, e);
        }
        return url;
    }

    private final class PlayTask implements Runnable {

        public PlayTask() {
        }

        @Override
        public void run() {
            if (!streamUrlCrawled) {
                streamUrl = crawlStreamUrl(sr.getDetailsUrl());
                streamUrlCrawled = true;
            }
            if (streamUrl != null) {
                crawlStreamUrl(sr.getDetailsUrl());
                GUIMediator.instance().launchAudio(new StreamAudioSource(streamUrl, "Soundcloud: " + sr.getDisplayName()));
            }
        }
    }
}
