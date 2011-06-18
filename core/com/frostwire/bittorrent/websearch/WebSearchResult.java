package com.frostwire.bittorrent.websearch;

public interface WebSearchResult {

    public String getFileName();

    public long getSize();

    public long getCreationTime();

    public String getVendor();

    public String getFilenameNoExtension();

    public String getHash();

    public String getTorrentURI();

    public int getSeeds();

    public String getTorrentDetailsURL();
}
