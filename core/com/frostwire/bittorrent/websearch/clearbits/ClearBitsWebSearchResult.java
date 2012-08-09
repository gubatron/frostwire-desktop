package com.frostwire.bittorrent.websearch.clearbits;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.frostwire.bittorrent.websearch.WebSearchResult;

public class ClearBitsWebSearchResult implements WebSearchResult {
    
    private final ClearBitsItem _item;
    
    public ClearBitsWebSearchResult(ClearBitsItem item) {
        _item = item;
    }

    public long getCreationTime() {
        //2010-07-15T16:02:42Z
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        long result = System.currentTimeMillis();
        try {
            result = date.parse(_item.created_at).getTime();
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
        return _item.hashstr;
    }
    
    public String getTorrentURI() {
        return _item.torrent_url;
    }

    public long getSize() {
        return Long.valueOf(_item.mb_size * 1024 * 1024);
    }

    public String getVendor() {
        return "ClearBits";
    }

    public int getSeeds() {
        return _item.seeds;
    }

    public String getDetailsUrl() {
        return _item.location;
    }

    @Override
    public String getDisplayName() {
        return _item.title;
    }
}
