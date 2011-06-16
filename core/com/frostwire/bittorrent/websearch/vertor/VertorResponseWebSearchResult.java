package com.frostwire.bittorrent.websearch.vertor;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.frostwire.bittorrent.websearch.WebSearchResult;

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

    public String getVendor() {
        return "Vertor";
    }

    public String getFilenameNoExtension() {
        return "<html>" + _item.name + "</html>";
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

    public String getTorrentDetailsURL() {
        return _item.url;
    }

}
