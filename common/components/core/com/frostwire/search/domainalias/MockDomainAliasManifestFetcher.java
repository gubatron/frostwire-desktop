package com.frostwire.search.domainalias;

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
            System.out.println("Mock-fetching domain alias manifest.");
            Thread.sleep(10000);
        } catch (Throwable t) {
            notifyManifestNotFetched();
        }
        
        //notifyAliasListFetched(manifest);
    }
}