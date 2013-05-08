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

package com.frostwire.search.vertor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.frostwire.search.torrent.AbstractTorrentSearchResult;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class VertorSearchResult extends AbstractTorrentSearchResult {

    private final VertorItem item;

    public VertorSearchResult(VertorItem item) {
        this.item = item;
    }

    @Override
    public String getFilename() {
        String titleNoTags = item.name.replace("<b>", "").replace("</b>", "");
        return titleNoTags + ".torrent";
    }

    @Override
    public long getSize() {
        return Long.valueOf(item.size);
    }

    public long getCreationTime() {
        //8 Jun 11
        SimpleDateFormat date = new SimpleDateFormat("dd MMM yy", Locale.US);
        long result = System.currentTimeMillis();
        try {
            result = date.parse(item.cdate).getTime();
        } catch (ParseException e) {
        }
        return result;
    }

    @Override
    public String getSource() {
        return "Vertor";
    }

    @Override
    public String getHash() {
        return null;
    }

    public String getTorrentUrl() {
        return item.download;
    }

    @Override
    public int getSeeds() {
        return Integer.valueOf(item.seeds);
    }

    @Override
    public String getDisplayName() {
        return getFilename();
    }

    @Override
    public String getDetailsUrl() {
        return item.url;
    }
}
