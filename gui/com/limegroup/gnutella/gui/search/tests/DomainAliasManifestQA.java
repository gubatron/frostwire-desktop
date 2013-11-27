package com.limegroup.gnutella.gui.search.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.frostwire.AzureusStarter;
import com.frostwire.search.CrawlPagedWebSearchPerformer;
import com.frostwire.search.SearchManager;
import com.frostwire.search.SearchManagerImpl;
import com.frostwire.search.SearchManagerListener;
import com.frostwire.search.SearchPerformer;
import com.frostwire.search.SearchResult;
import com.frostwire.search.VuzeMagnetDownloader;
import com.frostwire.search.WebSearchPerformer;
import com.frostwire.search.domainalias.DefaultDomainAliasManifestFetcher;
import com.frostwire.search.domainalias.DomainAliasManifest;
import com.frostwire.search.domainalias.DomainAliasManifestFetcherListener;
import com.limegroup.gnutella.gui.search.SearchEngine;

/**
 * Performs searches on all domain aliases specified by given manifest.
 * 
 * It will test every domain specified by the manifest, it will use the default domain
 * to fetch the corresponding search engine and instantiate a search manager for each one of them.
 * 
 * Once a search manager is there, it'll create a SearchListener which keeps track of wether or not
 * the search returned any results or if it failed and it will write that down on a testScore (DomainTestScore) object for that test.
 * We add all the test results to a final list of test results in case we need a summary of all the tests.
 * 
 * The score is simply a percentage of the domains that returned results satisfactorily.
 * 
 * @author gubatron
 *
 */
public class DomainAliasManifestQA {
    
    private static long searchTokenCounter = 1;
    
    public static void main(String[] args) {
        //Change here for a different manifest fetcher, or if you have a manifest object, just pass it to the test() method
        //to begin your test.
        new DefaultDomainAliasManifestFetcher(new DomainAliasManifestFetcherListener() {
            
            @Override
            public void onManifestNotFetched() {
            }
            
            @Override
            public void onManifestFetched(DomainAliasManifest manifest) {
                try {
                    test(manifest);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).fetchManifest();
    }

    public static void test(DomainAliasManifest manifest) throws InterruptedException {
        AzureusStarter.start();
        Map<String, List<String>> aliases = manifest.aliases;
        Set<Entry<String, List<String>>> entrySet = aliases.entrySet();
        List<DomainTestScore> testScores = (List<DomainTestScore>) Collections.synchronizedList(new ArrayList<DomainTestScore>());
        
        VuzeMagnetDownloader vuzeMagnetDownloader = new VuzeMagnetDownloader();
        CrawlPagedWebSearchPerformer.setMagnetDownloader(vuzeMagnetDownloader);

        for (Entry<String, List<String>> domainEntry : entrySet) {
            String domainName = domainEntry.getKey();
            List<String> domainAliases = domainEntry.getValue();
            testDomainAliases(domainName, domainAliases, testScores);
        }
        
        AzureusStarter.getAzureusCore().stop();
    }

    private static void testDomainAliases(String domainName, List<String> domainAliases,List<DomainTestScore> testScores) throws InterruptedException {
        SearchEngine SEARCH_ENGINE = SearchEngine.getSearchEngineByDefaultDomainName(domainName);
        SEARCH_ENGINE.getDomainAliasManager().setAliases(domainAliases);
        
        DomainTestScore testScore = new DomainTestScore(domainName, domainAliases);
        
        CountDownLatch latch = new CountDownLatch(domainAliases.size()+1);
        SearchManager searchManager = new SearchManagerImpl();
        searchManager.registerListener(new SearchTestListener(testScores, testScore, latch));
        
        //first search should be with default domain.
        searchManager.perform(SEARCH_ENGINE.getPerformer(searchTokenCounter++,"love"));

        for (String alias : domainAliases) {
            SEARCH_ENGINE.getDomainAliasManager().setDomainNameToUse(alias);
            System.out.println("Started search on " + SEARCH_ENGINE.getDomainAliasManager().getDomainNameToUse() + " in alias: " + alias);
            searchManager.perform(SEARCH_ENGINE.getPerformer(searchTokenCounter++,"love"));
            Thread.sleep(2000);
        }
        
        searchTokenCounter *= 10;
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
    
    private static class SearchTestListener implements SearchManagerListener, Runnable {
        
        private final List<DomainTestScore> testScores;
        private final DomainTestScore testScore;
        private final CountDownLatch latch;
        
        public SearchTestListener(List<DomainTestScore> testScores, DomainTestScore testScore, final CountDownLatch latch) {
            this.testScores = testScores;
            this.testScore = testScore;
            this.latch = latch;
            new Thread(this).start();
        }

        @Override
        public void onResults(SearchPerformer performer, List<? extends SearchResult> results) {
            WebSearchPerformer wsp = (WebSearchPerformer) performer;
            System.out.println("onResults of "+ wsp.getDefaultDomainName() +"@" + wsp.getDomainNameToUse() + "!");
            String domainName = wsp.getDomainNameToUse();
            if (results != null) {
                testScore.recordTestResult(domainName, true);
            }
        }

        @Override
        public void onFinished(long token) {
            if (latch.getCount() > 0) {
                latch.countDown();
                System.out.println("Search #" + token + " finished. (latch for "+ testScore.originalDomainName + " has " + latch.getCount() + " counts left)");
            }
        }

        @Override
        public void run() {
            System.out.println("SearchTestListener waiting for searches to finish...");
            try {
                latch.await(20,TimeUnit.SECONDS);
                System.out.println("SearchTestListener done waiting...");
            } catch (Throwable e) {
                System.out.println("SearchTestListener latch timed out!");
            }
            System.out.println("Searches for " + testScore.originalDomainName + " are finished, calculating test scores:");
            System.out.println("----------------------------------------------------------------------------------------");
            System.out.println("Final Score: " + (testScore.getScore() * 100) + "%");
            Set<Entry<String, Boolean>> entrySet = testScore.domainAliasTestResults.entrySet();
            for (Entry<String,Boolean> testEntry : entrySet) {
                System.out.println("    "+testScore.originalDomainName+"(@" + testEntry.getKey() + ") -> " + testEntry.getValue());
            }
            System.out.println("----------------------------------------------------------------------------------------");
            
            testScores.add(testScore);
        }
    }
}