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

    public static final String AAC_LOW_QUALITY = "(AAC)";
    public static final String AAC_HIGH_QUALITY = "(AAC-High Quality)";

    private final YouTubeDownloadLink dl;
    private final String filename;
    private final String displayName;
    private final long size;
    private final String streamUrl;

    public YouTubeCrawledSearchResult(YouTubeSearchResult sr, YouTubeDownloadLink dl) {
        super(sr);

        this.dl = dl;

        this.filename = dl.getFilename();
        this.displayName = buildDisplayName(this.filename);
        this.size = dl.getSize();
        this.streamUrl = dl.getDownloadUrl();
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

    private String buildDisplayName(String filename2) {
        String fname = FilenameUtils.getBaseName(filename);
        String result = fname;
        if (dl.getITag() == 22 || dl.getITag() == 37) {
            result = AAC_HIGH_QUALITY + " " + fname;            
        } else if (dl.getITag() == 18) {
            result = AAC_LOW_QUALITY + " " + fname;
        }
        return result;
    }
}
