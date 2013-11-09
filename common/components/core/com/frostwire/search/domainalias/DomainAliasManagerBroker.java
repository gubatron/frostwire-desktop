package com.frostwire.search.domainalias;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.limewire.concurrent.ExecutorsHelper;

/** This guy is in charge of managing DomainAliasManager instances
 * fetching the domain list files and updating each of the domain alias managers it has.
 * If you need a DomainAliasManager you just give it the default 
 * @author gubatron
 *
 */
public class DomainAliasManagerBroker implements DomainAliasManifestFetcherListener {

    private final static DomainAliasManagerBroker INSTANCE = new DomainAliasManagerBroker();
    private final ExecutorService executor;
    private final HashMap<String, DomainAliasManager> managers;
    
    //might not be necessary... or maybe to know if we got something or not...
    private DomainAliasManifest manifest;

    private DomainAliasManagerBroker() {
        managers = new HashMap<String, DomainAliasManager>();
        executor = ExecutorsHelper.newThreadPool("DomainAliasMockFetcherExecutorHelper");
        fetchDomainAliasManifest();
    }

    private void fetchDomainAliasManifest() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                MockDomainAliasManifestFetcher fetcher = new MockDomainAliasManifestFetcher(INSTANCE); 
                fetcher.fetchManifest();
            }
        });
    }

    public static DomainAliasManager getDomainAliasManager(String defaultDomainKey) {
        if (!INSTANCE.managers.containsKey(defaultDomainKey)) {
            INSTANCE.managers.put(defaultDomainKey, new DomainAliasManager(defaultDomainKey));
        }
        return INSTANCE.managers.get(defaultDomainKey);
    }

    @Override
    public void onManifestFetched(DomainAliasManifest aliasManifest) {
        System.out.println("DomainAliasManagerBroker: Got the manifest!!!");
        updateManagers(aliasManifest);
        manifest = aliasManifest;
    }

    @Override
    public void onManifestNotFetched() {
        System.err.println("DomainAliasManagerBroker:  Could not fetch alias list, should we try again later? attempts left");
        manifest = null;
        //attempts++;?? timestamp, to try again later. etc.
    }

    private void updateManagers(DomainAliasManifest aliasManifest) {
        Map<String, List<String>> aliases = aliasManifest.aliases;
        Set<Entry<String, List<String>>> aliasSet = aliases.entrySet();
        for (Entry<String, List<String>> entry : aliasSet) {
            List<String> aliasNames = entry.getValue();
            DomainAliasManager domainAliasManager = getDomainAliasManager(entry.getKey());
            domainAliasManager.updateAliases(aliasNames);
        }
    }
}