/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
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

package com.frostwire.search.youtube;

import org.apache.commons.io.FilenameUtils;

import com.frostwire.search.AbstractCrawledSearchResult;
import com.frostwire.search.HttpSearchResult;
import com.frostwire.search.extractors.YouTubeExtractor.LinkInfo;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class YouTubeCrawledSearchResult extends AbstractCrawledSearchResult implements HttpSearchResult {

    private final LinkInfo video;
    private final LinkInfo audio;
    private final String filename;
    private final String displayName;
    private final long size;
    private final String downloadUrl;

    public YouTubeCrawledSearchResult(YouTubeSearchResult sr, LinkInfo video, LinkInfo audio) {
        super(sr);

        this.video = video;
        this.audio = audio;

        this.filename = buildFilename(video, audio);
        this.displayName = FilenameUtils.getBaseName(this.filename);
        this.size = buildSize(video, audio);
        this.downloadUrl = buildDownloadUrl(video, audio);
    }

    public LinkInfo getVideo() {
        return video;
    }

    public LinkInfo getAudio() {
        return audio;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    private String buildFilename(LinkInfo video, LinkInfo audio) {
        String filename;
        if (video != null && audio == null) {
            filename = String.format("%s_%s_%s_%s.%s", video.filename, video.format.video, video.format.audio, video.format.quality, video.format.ext);
        } else if (video == null && audio != null) {
            filename = String.format("%s_%s_%s_%s.%s", audio.filename, audio.format.video, audio.format.audio, audio.format.quality, audio.format.ext);
        } else if (video != null && audio != null) {
            filename = String.format("%s_%s_%s_%s.%s", video.filename, video.format.video, audio.format.audio, video.format.quality, "mp4");
        } else {
            throw new IllegalArgumentException("No track defined");
        }

        return filename;
    }

    private long buildSize(LinkInfo video, LinkInfo audio) {
        long size;
        if (video != null && audio == null) {
            size = video.size;
        } else if (video == null && audio != null) {
            size = audio.size;
        } else if (video != null && audio != null) {
            size = video.size + video.size;
        } else {
            throw new IllegalArgumentException("No track defined");
        }

        return size;
    }

    private String buildDownloadUrl(LinkInfo video, LinkInfo audio) {
        String downloadUrl;
        if (video != null && audio == null) {
            downloadUrl = video.link;
        } else if (video == null && audio != null) {
            downloadUrl = audio.link;
        } else if (video != null && audio != null) {
            downloadUrl = null;
        } else {
            throw new IllegalArgumentException("No track defined");
        }

        return downloadUrl;
    }
}
