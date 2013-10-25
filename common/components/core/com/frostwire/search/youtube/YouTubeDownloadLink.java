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

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class YouTubeDownloadLink {

    private final String filename;
    private final long size;
    private final String downloadUrl;
    private final int iTag;
    /** http://en.wikipedia.org/wiki/YouTube */
    private final boolean audio;

    public YouTubeDownloadLink(String filename, long size, String downloadUrl, int iTag) {
        this(filename, size, downloadUrl, iTag, false);
    }

    public YouTubeDownloadLink(String filename, long size, String downloadUrl, int iTag, boolean audio) {
        this.filename = filename;
        this.size = size;
        this.downloadUrl = downloadUrl;
        this.iTag = iTag;
        this.audio = audio;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public int getITag() {
        return iTag;
    }

    public boolean isAudio() {
        return audio;
    }
}