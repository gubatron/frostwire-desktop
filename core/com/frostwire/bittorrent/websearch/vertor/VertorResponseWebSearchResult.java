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

package com.frostwire.bittorrent.websearch.vertor;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.frostwire.bittorrent.websearch.WebSearchResult;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class VertorResponseWebSearchResult implements WebSearchResult {
    
    private final VertorItem _item;

    public VertorResponseWebSearchResult(VertorItem item) {
        _item = item;
    }

    public String getFileName() {
        String titleNoTags = _item.name.replace("<b>", "").replace("</b>", "");
        return titleNoTags + ".torrent";
    }

    public long getSize() {
        return Long.valueOf(_item.size);
    }

    public long getCreationTime() {
      //8 Jun 11
        SimpleDateFormat date = new SimpleDateFormat("dd MMM yy");
        long result = System.currentTimeMillis();
        try {
            result = date.parse(_item.cdate).getTime();
        } catch (ParseException e) {
        }
        return result;
    }

    public String getSource() {
        return "Vertor";
    }

    public String getHash() {
        return null;
    }

    public String getTorrentURI() {
        return _item.download;
    }

    public int getSeeds() {
        return Integer.valueOf(_item.seeds);
    }

    public String getDetailsUrl() {
        return _item.url;
    }

    @Override
    public String getDisplayName() {
        return _item.name;
    }
}
