package com.frostwire.search.domainalias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Dummy Manifest fetcher, that has a fake delay, and creates a Manifest in memory.
 * @author gubatron
 *
 */
public class MockDomainAliasManifestFetcher extends AbstractDomainAliasManifestFetcher {

    
    public MockDomainAliasManifestFetcher(DomainAliasManifestFetcherListener listener) {
        super(listener);
    }

    @Override
    public void fetchManifest() {
        try {
            System.out.println("Mock-fetching domain alias manifest... wait 5 secs..");
            Thread.sleep(5000);
        } catch (Throwable t) {
            notifyManifestNotFetched();
        }
        
        DomainAliasManifest manifest = new DomainAliasManifest();
        manifest.lastUpdated = System.currentTimeMillis();
        manifest.version = 0;
        manifest.aliases = new HashMap<String, List<String>>();
        
        List<String> katAliases = new ArrayList<String>();
        katAliases.add("kickass.pw");
        katAliases.add("katproxy.pw");
        manifest.aliases.put("kickass.to", katAliases);

        System.out.println("Mock manifest built! update managers.");
        notifyManifestFetched(manifest);
    }
}