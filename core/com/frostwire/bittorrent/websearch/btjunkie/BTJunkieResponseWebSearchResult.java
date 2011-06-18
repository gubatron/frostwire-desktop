package com.frostwire.bittorrent.websearch.btjunkie;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.frostwire.bittorrent.websearch.WebSearchResult;

public class BTJunkieResponseWebSearchResult implements WebSearchResult {
    
    private final BTJunkieItem _item;

    public BTJunkieResponseWebSearchResult(BTJunkieItem item) {
        _item = item;
    }

    public String getFileName() {
        String titleNoTags = _item.title.replace("<b>", "").replace("</b>", "");
        return titleNoTags + ".torrent";
    }

    public long getSize() {
        return _item.size;
    }

    public long getCreationTime() {
      //Sat, 10 Oct 2009 00:00:00 +0000
        SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
        long result = System.currentTimeMillis();
        try {
            result = date.parse(_item.date).getTime();
        } catch (ParseException e) {
        }
        return result;
    }

    public String getVendor() {
        return "BTJunkie";
    }

    public String getFilenameNoExtension() {
        return "<html>" + _item.title + "</html>";
    }

    public String getHash() {
        return _item.hash;
    }

    public String getTorrentURI() {
        return _item.download;
    }

    public int getSeeds() {
        return _item.seeds;
    }

    public String getTorrentDetailsURL() {
        return _item.cdp;
    }

}
