package com.limegroup.gnutella.gui.search.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.frostwire.search.SearchManager;
import com.frostwire.search.SearchManagerImpl;
import com.frostwire.search.SearchManagerListener;
import com.frostwire.search.SearchPerformer;
import com.frostwire.search.SearchResult;
import com.frostwire.search.WebSearchPerformer;
import com.frostwire.search.domainalias.DomainAliasManifest;
import com.limegroup.gnutella.gui.search.SearchEngine;

/**
 * Performs searches on all domain aliases specified by given manifest.
 * @author gubatron
 *
 */
public class DomainAliasManifestQA {

    public static void test(DomainAliasManifest manifest) {
        Map<String, List<String>> aliases = manifest.aliases;
        Set<Entry<String, List<String>>> entrySet = aliases.entrySet();
        List<DomainTestScore> testScores = (List<DomainTestScore>) Collections.synchronizedCollection(new ArrayList<DomainTestScore>());

        //Update the manifest for all search engines.
        //SearchEngine.DOMAIN_ALIAS_MANAGER_BROKER.onManifestFetched(manifest);

        for (Entry<String, List<String>> domainEntry : entrySet) {
            String domainName = domainEntry.getKey();
            List<String> domainAliases = domainEntry.getValue();
            testDomainAliases(domainName, domainAliases, testScores);
        }
    }

    private static void testDomainAliases(String domainName, List<String> domainAliases,List<DomainTestScore> testScores) {
        SearchEngine SEARCH_ENGINE = SearchEngine.getSearchEngineByDefaultDomainName(domainName);
        SEARCH_ENGINE.getDomainAliasManager().setAliases(domainAliases);
        
        DomainTestScore testScore = new DomainTestScore(domainName, domainAliases);
        
        CountDownLatch latch = new CountDownLatch(domainAliases.size() + 1);

        SearchManager searchManager = new SearchManagerImpl();
        searchManager.registerListener(new SearchTestListener(testScore, latch));
        
        
        //first search should be with default domain.
        searchManager.perform(SEARCH_ENGINE.getPerformer(System.currentTimeMillis(),"love"));
        System.out.println("Started search on domain " + SEARCH_ENGINE.getDomainAliasManager().getDomainNameToUse());
        
        for (int i=0; i < domainAliases.size(); i++) {
            SEARCH_ENGINE.getDomainAliasManager().getNextOnlineDomainAlias();
            System.out.println("Started search on alias: " + SEARCH_ENGINE.getDomainAliasManager().getDomainNameToUse());
            searchManager.perform(SEARCH_ENGINE.getPerformer(System.currentTimeMillis(),"love"));
        }
        
    }
    
    private static class DomainTestScore {
        public final String originalDomainName;
        public final Map<String, Boolean> domainAliasTestResults;
        
        public DomainTestScore(String originalDomainName, List<String> domainAliases) {
            this.originalDomainName = originalDomainName;
            
            domainAliasTestResults = new HashMap<String, Boolean>();
            domainAliasTestResults.put(originalDomainName, false);
            for (String domainAlias : domainAliases) {
                domainAliasTestResults.put(domainAlias, false);
            }
        }
        
        public void recordTestResult(String domainName, boolean passed) {
            domainAliasTestResults.put(domainName, passed);
        }
        
        public float getScore() {
            Set<String> keySet = domainAliasTestResults.keySet();
            int totalTests = domainAliasTestResults.size(); //original domain + aliases
            int passed = 0;
            for (String domain :  keySet) {
                if (domainAliasTestResults.get(domain)) {
                    passed++;
                }
            }
            
            float result = 0;
            if (totalTests > 0) {
                result = (float) passed/totalTests;
            }
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            return originalDomainName.equals(((DomainTestScore) obj).originalDomainName);
        }
        
        @Override
        public int hashCode() {
            return originalDomainName.hashCode();
        }
    }
    
    private static class SearchTestListener implements SearchManagerListener {
        
        private final DomainTestScore testScore;
        private final CountDownLatch latch;
        
        public SearchTestListener(DomainTestScore testScore, final CountDownLatch latch) {
            this.testScore = testScore;
            this.latch = latch;
        }

        @Override
        public void onResults(SearchPerformer performer, List<? extends SearchResult> results) {
            String domainName = ((WebSearchPerformer) performer).getDomainName();
            if (results != null) {
                testScore.recordTestResult(domainName, true);
            }
        }

        @Override
        public void onFinished(long token) {
            latch.countDown();
            System.out.println("Search " + token + " finished. (" + latch.getCount() + " left)");
        }
    }
}