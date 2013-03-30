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

package com.frostwire.search.youtube2;

import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

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

    private final String filename;
    private final String displayName;
    private final long size;
    private final String streamUrl;

    public YouTubeCrawledSearchResult(YouTubeSearchResult sr, FilePackage filePackage) {
        super(sr);

        this.filename = readFilename(filePackage);
        this.displayName = buildDisplayName(this.filename);
        this.size = filePackage.getChildren().get(0).getLongProperty("size", -1);
        this.streamUrl = filePackage.getChildren().get(0).getDownloadURL();
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

    private String readFilename(FilePackage filePackage) {
        DownloadLink dl = filePackage.getChildren().get(0);
        if (dl.getStringProperty("convertto", "").equals("AUDIOMP3")) {
            return FilenameUtils.getBaseName(dl.getName()) + ".mp3";
        }

        return dl.getName();
    }

    private String buildDisplayName(String filename2) {
        String fname = FilenameUtils.getBaseName(filename);
        if (fname.indexOf(AAC_HIGH_QUALITY) > 0) {
            return AAC_HIGH_QUALITY + " " + fname.replace(AAC_HIGH_QUALITY, "");
        } else if (fname.indexOf(AAC_LOW_QUALITY) > 0) {
            return AAC_LOW_QUALITY + " " + fname.replace(AAC_LOW_QUALITY, "");
        }

        return fname;
    }
}
