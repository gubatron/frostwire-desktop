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

package com.frostwire.search.archiveorg;

import java.util.Locale;

import org.apache.commons.io.FilenameUtils;

import com.frostwire.search.AbstractCrawledSearchResult;
import com.frostwire.search.HttpSearchResult;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class ArchiveorgCrawledSearchResult extends AbstractCrawledSearchResult implements HttpSearchResult {

    private static final String DOWNLOAD_URL = "http://archive.org/download/%s/%s";

    private final String filename;
    private final ArchiveorgFile file;

    private final String displayName;
    private final String downloadUrl;

    public ArchiveorgCrawledSearchResult(ArchiveorgSearchResult sr, String filename, ArchiveorgFile file) {
        super(sr);
        this.filename = filename;
        this.file = file;

        this.displayName = FilenameUtils.getBaseName(filename);
        this.downloadUrl = String.format(Locale.US, DOWNLOAD_URL, sr.getItem().identifier, filename);
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
        try {
            return Long.parseLong(file.size);
        } catch (Throwable e) {
            return -1;
        }
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }
}
