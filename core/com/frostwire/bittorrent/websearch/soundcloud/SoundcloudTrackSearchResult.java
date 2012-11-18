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

package com.frostwire.bittorrent.websearch.soundcloud;

import com.frostwire.bittorrent.websearch.WebSearchResult;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SoundcloudTrackSearchResult implements WebSearchResult {

    private final SoundcloudItem item;
    private final String trackUrl;

    public SoundcloudTrackSearchResult(SoundcloudItem item) {
        this.item = item;
        trackUrl = "http://soundcloud.com" + item.uri;
    }

    @Override
    public String getFileName() {
        return item.name + ".mp3";
    }

    @Override
    public long getSize() {
        return Math.round((item.duration * 128f) / 8f);
    }

    @Override
    public long getCreationTime() {
        return item.date;
    }

    @Override
    public String getSource() {
        if (item.user != null && item.user.username != null) {
            return "Soundcloud - " + item.user.username;
        } else {
            return "Soundcloud";
        }
    }

    @Override
    public String getHash() {
        return null;
    }

    @Override
    public String getTorrentURI() {
        return trackUrl;
    }

    @Override
    public int getSeeds() {
        return -1;
    }

    @Override
    public String getDetailsUrl() {
        return trackUrl;
    }

    @Override
    public String getDisplayName() {
        return item.title;
    }

    public String getStreamUrl() {
        return item.streamUrl;
    }

    public String getThumbnailUrl() {
        return item.thumbnailUrl;
    }

    public String getUsername() {
        if (item.user != null && item.user.username != null) {
            return item.user.username;
        } else {
            return "Soundcloud";
        }
    }
}
