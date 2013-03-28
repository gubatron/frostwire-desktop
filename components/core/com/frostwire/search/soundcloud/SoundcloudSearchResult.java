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

package com.frostwire.search.soundcloud;

import com.frostwire.search.AbstractFileSearchResult;
import com.frostwire.search.CrawlableSearchResult;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SoundcloudSearchResult extends AbstractFileSearchResult implements CrawlableSearchResult {

    private final SoundcloudItem item;
    private final String trackUrl;

    public SoundcloudSearchResult(SoundcloudItem item) {
        this.item = item;
        trackUrl = "http://soundcloud.com" + item.uri;
    }

    @Override
    public String getFilename() {
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

    public int getRank() {
        return 10000;
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

    public String getTitle() {
        return item.title;
    }

    public String getUsername() {
        return item.user.username;
    }

    @Override
    public boolean isComplete() {
        return true;
    }
}