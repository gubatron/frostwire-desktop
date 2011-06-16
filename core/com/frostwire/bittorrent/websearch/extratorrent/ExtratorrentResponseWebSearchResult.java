package com.frostwire.bittorrent.websearch.extratorrent;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.frostwire.bittorrent.websearch.WebSearchResult;

public class ExtratorrentResponseWebSearchResult implements WebSearchResult {
    
    private final ExtratorrentItem _item;

    public ExtratorrentResponseWebSearchResult(ExtratorrentItem item) {
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
      //Wed, 09 Jun 2010 18:08:27 +0100
        SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
        long result = System.currentTimeMillis();
        try {
            result = date.parse(_item.pubDate).getTime();
        } catch (ParseException e) {
        }
        return result;
    }

    public String getVendor() {
        return "Extratorrent";
    }

    public String getFilenameNoExtension() {
        return "<html>" + _item.title + "</html>";
    }

    public String getHash() {
        return _item.hash;
    }

    public String getTorrentURI() {
        return _item.torrentLink;
    }

    public int getSeeds() {
        return _item.seeds;
    }

    public String getTorrentDetailsURL() {
        return _item.link;
    }

}
