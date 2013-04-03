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

package com.frostwire.search.youtube;

import org.apache.commons.io.FilenameUtils;

import com.frostwire.search.AbstractCrawledSearchResult;
import com.frostwire.search.HttpSearchResult;
import com.frostwire.search.StreamableSearchResult;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class YouTubeCrawledSearchResult extends AbstractCrawledSearchResult implements HttpSearchResult, StreamableSearchResult {

    private final YouTubeDownloadLink dl;
    private final String filename;
    private final String displayName;
    private final long size;
    private final String streamUrl;
    private final MediaQuality mediaQuality;
    
    public enum MediaQuality {
        LOW_QUALITY("Low Quality"),
        HIGH_QUALITY("High Quality"),
        UNKNOWN("");
        
        private final String name;
        
        MediaQuality(String str) {
            name = str;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }

    public YouTubeCrawledSearchResult(YouTubeSearchResult sr, YouTubeDownloadLink dl) {
        super(sr);

        this.dl = dl;

        this.filename = dl.getFilename();
        this.displayName = FilenameUtils.getBaseName(this.filename);
        this.size = dl.getSize();
        this.streamUrl = dl.getDownloadUrl();
        this.mediaQuality = buildQuality(dl);
    }

    public YouTubeDownloadLink getYouTubeDownloadLink() {
        return dl;
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
    public String getStreamUrl() {
        return streamUrl;
    }

    @Override
    public String getDownloadUrl() {
        return streamUrl;
    }
    
    public MediaQuality getMediaQuality() {
        return mediaQuality;
    }

    private MediaQuality buildQuality(YouTubeDownloadLink dl) {
        MediaQuality result = MediaQuality.UNKNOWN;
        if (dl.getITag() == 22 || dl.getITag() == 37) {
            result = MediaQuality.HIGH_QUALITY;            
        } else if (dl.getITag() == 18) {
            result = MediaQuality.LOW_QUALITY;
        }
        return result;
    }
    
}
