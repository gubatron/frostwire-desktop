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
package com.frostwire.bittorrent.websearch.kat;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.limegroup.gnutella.util.UrlUtils;

public class KATWebSearchResult implements WebSearchResult {
    
    private final KATItem _item;
    
    public KATWebSearchResult(KATItem item) {
        _item = item;
    }

    public long getCreationTime() {
        //Saturday 26 Jan 2008 01:01:52 +0000
        SimpleDateFormat date = new SimpleDateFormat("EEEE dd MMM yyyy HH:mm:ss Z ");
        long result = System.currentTimeMillis();
        try {
            result = date.parse(_item.pubDate).getTime();
        } catch (ParseException e) {
        }
        return result;
    }

    public String getFileName() {
        String titleNoTags = _item.title.replace("<b>", "").replace("</b>", "");
        return titleNoTags + ".torrent";
    }

    public String getFilenameNoExtension() {
        return "<html>" + _item.title + "</html>";
    }

    public String getHash() {
        return _item.hash;
    }
    
    public String getTorrentURI() {
        //KAT is no longer allowing torrent hot-linking, thus making the network more de-centralized.
        //Go magnets.
        return "magnet:?xt=urn:btih:" +
                   getHash() +
                   "&dn="+UrlUtils.encode(getFileName()) + 
                   "&tr=http%3A%2F%2Ftracker.publicbt.com%2Fannounce";
    }

    public long getSize() {
        return _item.size;
    }

    public String getVendor() {
        return "KAT";
    }

    public int getSeeds() {
        return _item.seeds;
    }

    public String getTorrentDetailsURL() {
        return _item.link;
    }
}