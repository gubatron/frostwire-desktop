package com.frostwire.bittorrent.websearch;

import java.util.List;

public interface WebSearchPerformer {
    public List<WebSearchResult> search(String keywords);
}
