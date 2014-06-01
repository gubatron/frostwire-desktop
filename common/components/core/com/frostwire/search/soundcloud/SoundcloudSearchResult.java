/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014,, FrostWire(R). All rights reserved.
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.frostwire.search.AbstractFileSearchResult;
import com.frostwire.search.HttpSearchResult;
import com.frostwire.search.StreamableSearchResult;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SoundcloudSearchResult extends AbstractFileSearchResult implements HttpSearchResult, StreamableSearchResult {

    private static final String DATE_FORMAT = "yyyy/mm/dd HH:mm:ss Z";

    private final String displayName;
    private final String username;
    private final String trackUrl;
    private final String filename;
    private final String source;
    private final String thumbnailUrl;
    private final long date;
    private final String downloadUrl;
    private final long size;

    public SoundcloudSearchResult(SoundcloudItem item, String clientId) {
        this.displayName = item.title;
        this.username = buildUsername(item);
        this.trackUrl = item.permalink_url;
        this.filename = item.permalink + "-soundcloud.mp3";
        this.size = item.original_content_size;
        this.source = buildSource(item);
        this.thumbnailUrl = buildThumbnailUrl(item.artwork_url);
        this.date = buildDate(item.created_at);
        
        final String downloadUrl = ((item.download_url != null) ? item.download_url : item.stream_url) + "?client_id=" + clientId;
        this.downloadUrl = downloadUrl.replace("https://", "http://");
        
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getCreationTime() {
        return date;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getDetailsUrl() {
        return trackUrl;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public String getStreamUrl() {
        return downloadUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    private String buildUsername(SoundcloudItem item) {
        if (item.user != null && item.user.username != null) {
            return item.user.username;
        } else {
            return "";
        }
    }

    private String buildSource(SoundcloudItem item) {
        if (item.user != null && item.user.username != null) {
            return "Soundcloud - " + item.user.username;
        } else {
            return "Soundcloud";
        }
    }

    private String buildThumbnailUrl(String str) {
        //http://i1.sndcdn.com/artworks-000019588274-le8r71-crop.jpg?be0edad
        //https://i1.sndcdn.com/artworks-000019588274-le8r71-t500x500.jpg
        String url = null;
        try {
            url = str.substring(0, str.indexOf("-large.")) + "-t300x300.jpg";
        } catch (Throwable e) {
            // ignore
        }
        return url;
    }

    private long buildDate(String str) {
        try {
            return new SimpleDateFormat(DATE_FORMAT, Locale.US).parse(str).getTime();
        } catch (ParseException e) {
            return System.currentTimeMillis();
        }
    }
}