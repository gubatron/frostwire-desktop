package com.frostwire.search;

import java.util.LinkedList;
import java.util.List;

public final class PerformerResultListener implements SearchListener {

    private final SearchManagerImpl manager;

    public PerformerResultListener(SearchManagerImpl manager) {
        this.manager = manager;
    }

    @Override
    public void onResults(SearchPerformer performer, List<? extends SearchResult> results) {
        List<SearchResult> list = new LinkedList<SearchResult>();

        for (SearchResult sr : results) {
            if (sr instanceof CrawlableSearchResult) {
                CrawlableSearchResult csr = (CrawlableSearchResult) sr;

                if (csr.isComplete()) {
                    list.add(sr);
                }

                manager.crawl(performer, csr);
            } else {
                list.add(sr);
            }
        }

        if (!list.isEmpty()) {
            manager.onResults(performer, list);
        }
    }

    @Override
    public void onNoData(SearchPerformer performer) {
        //don't notify the manager since the performer
        //might be trying to switch domains and continue the search
    }
    
    public SearchManager getSearchManager() {
        return manager;
    }
}