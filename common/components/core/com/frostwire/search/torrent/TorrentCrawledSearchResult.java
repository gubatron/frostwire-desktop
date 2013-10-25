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

package com.frostwire.search.torrent;

import org.apache.commons.io.FilenameUtils;

import com.frostwire.search.AbstractCrawledSearchResult;
import com.frostwire.torrent.TOTorrentFile;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public class TorrentCrawledSearchResult extends AbstractCrawledSearchResult implements TorrentSearchResult {

    private final TorrentCrawlableSearchResult sr;
    private final String relativePath;
    private final String displayName;
    private final String filename;
    private final long size;

    public TorrentCrawledSearchResult(TorrentCrawlableSearchResult sr, TOTorrentFile file) {
        super(sr);
        this.sr = sr;
        this.relativePath = file.getRelativePath();
        this.filename = FilenameUtils.getName(this.relativePath);
        this.size = file.getLength();
        this.displayName = FilenameUtils.getBaseName(this.filename);
    }

    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public String getDisplayName() {
        return displayName;
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
        return sr.getCreationTime();
    }

    @Override
    public String getTorrentUrl() {
        return sr.getTorrentUrl();
    }

    @Override
    public int getSeeds() {
        return sr.getSeeds();
    }

    @Override
    public String getHash() {
        return sr.getHash();
    }
}
