/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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

    public ClearBitsSearchResult(ClearBitsItem item) {
        this.item = item;
    }

    public long getCreationTime() {
        //2010-07-15T16:02:42Z
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        long result = System.currentTimeMillis();
        try {
            result = date.parse(item.created_at).getTime();
        } catch (ParseException e) {
        }
        return result;
    }

    @Override
    public String getFilename() {
        String titleNoTags = item.title.replace("<b>", "").replace("</b>", "");
        return titleNoTags + ".torrent";
    }

    @Override
    public String getHash() {
        return item.hashstr;
    }

    public String getTorrentUrl() {
        return item.torrent_url;
    }

    public long getSize() {
        return Long.valueOf(item.mb_size * 1024 * 1024);
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
}
