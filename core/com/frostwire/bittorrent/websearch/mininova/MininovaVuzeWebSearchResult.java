package com.frostwire.bittorrent.websearch.mininova;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.frostwire.bittorrent.websearch.WebSearchResult;

public class MininovaVuzeWebSearchResult implements WebSearchResult {

    private MininovaVuzeItem _item;

    public MininovaVuzeWebSearchResult(MininovaVuzeItem item) {
        _item = item;
    }

    public long getCreationTime() {
        SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
        long result = System.currentTimeMillis();
        try {
            result = date.parse(_item.date).getTime();
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
        return _item.download;
    }

    public long getSize() {
        return Long.valueOf(_item.size);
    }

    public String getVendor() {
        return "Mininova";
    }

    public int getSeeds() {
        return _item.seeds + _item.superseeds;
    }

    public String getDetailsUrl() {
        return _item.cdp;
    }

    @Override
    public String getDisplayName() {
        return _item.title;
    }
}
