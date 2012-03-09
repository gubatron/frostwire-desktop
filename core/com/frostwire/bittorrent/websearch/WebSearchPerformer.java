package com.frostwire.bittorrent.websearch;

import java.util.List;

public interface WebSearchPerformer {
    
    public static final int HTTP_TIMEOUT = 5000;
    
    public List<WebSearchResult> search(String keywords);
}
