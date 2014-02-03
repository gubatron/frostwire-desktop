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

package com.frostwire.search.clearbits;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.frostwire.search.torrent.AbstractTorrentSearchResult;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class ClearBitsSearchResult extends AbstractTorrentSearchResult {

    private final ClearBitsItem item;

    private final long creationTime;
    private final String filename;
    private final long size;

    public ClearBitsSearchResult(ClearBitsItem item) {
        this.item = item;
        this.creationTime = parseCreationTime(item);
        this.filename = parseFilename(item);
        this.size = item.mb_size * 1024 * 1024;
    }

    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String getHash() {
        return item.hashstr;
    }

    public String getTorrentUrl() {
        return item.torrent_url;
    }

    public long getSize() {
        return size;
    }

    @Override
    public int getSeeds() {
        return item.seeds;
    }

    @Override
    public String getDisplayName() {
        return getFilename();
    }

    @Override
    public String getSource() {
        return "ClearBits";
    }

    @Override
    public String getDetailsUrl() {
        return item.location;
    }

    private long parseCreationTime(ClearBitsItem item) {
        //2010-07-15T16:02:42Z
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        long result = System.currentTimeMillis();
        try {
            result = date.parse(item.created_at).getTime();
        } catch (ParseException e) {
        }
        return result;
    }

    private String parseFilename(ClearBitsItem item) {
        String titleNoTags = item.title.replace("<b>", "").replace("</b>", "");
        return titleNoTags + ".torrent";
    }
}
