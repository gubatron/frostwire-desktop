package com.frostwire.search.tests;

import com.frostwire.search.domainalias.DomainAliasManager;
import com.frostwire.search.domainalias.DomainAliasManagerBroker;

public class DomainAliasManagerTest{
    public static void main(String[] args) throws InterruptedException {
        DomainAliasManager domainAliasManager = DomainAliasManagerBroker.getDomainAliasManager("kickass.to");//www.kat.ph");
        
        //assert(domainAliasManager.getDefaultDomain().equals("www.kat.ph"));
        
        while (true) {
            domainAliasManager.checkStatuses();
            System.out.println("...");
            Thread.sleep(5000);
        }
        
        /**
        SearchEngine kat = SearchEngine.KAT;
        SearchManager manager = new SearchManagerImpl();
        manager.registerListener(new SearchManagerListener() {
            
            @Override
            public void onResults(SearchPerformer performer, List<? extends SearchResult> results) {                
                System.out.println(performer.getToken() + " got results -> " + results.size() + " (from "+ ((WebSearchPerformer) performer).getDomainName() +")");
                for (SearchResult r : results) {
                    System.out.println(r.getDisplayName());
                }
            }
            
            @Override
            public void onFinished(long token) {
                System.out.println("search done - token:" + token);
            }
        });
        long tokenId = 2312389382l;
        manager.perform(kat.getPerformer(2312389382l, "frostwire"));

        while (true) {
            System.out.println("Waiting 10 secs for next search...");
            Thread.sleep(10000);
            tokenId += 2312389382l;
            manager.perform(kat.getPerformer(tokenId, "frostwire"));
            
        }
        */
    }
}