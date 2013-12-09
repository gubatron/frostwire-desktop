package com.frostwire.search.domainalias;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/** This guy is in charge of managing DomainAliasManager instances
 * fetching the domain list files and updating each of the domain alias managers it has.
 * If you need a DomainAliasManager you just give it the default 
 * @author gubatron
 *
 */
public class DomainAliasManagerBroker implements DomainAliasManifestFetcherListener {

    private final HashMap<String, DomainAliasManager> managers;
    
    public DomainAliasManagerBroker() {
        managers = new HashMap<String, DomainAliasManager>();
        fetchDomainAliasManifest();
    }

    private void fetchDomainAliasManifest() {
        new Thread("DomainAliasManagerBroker-domain-alias-manifest-fetcher") {
            @Override
            public void run() {
                DefaultDomainAliasManifestFetcher fetcher = new DefaultDomainAliasManifestFetcher(DomainAliasManagerBroker.this); 
                fetcher.fetchManifest();
            }

        }.start();
    }

    public DomainAliasManager getDomainAliasManager(String defaultDomainKey) {
        if (!managers.containsKey(defaultDomainKey)) {
            managers.put(defaultDomainKey, new DomainAliasManager(defaultDomainKey));
        }
        return managers.get(defaultDomainKey);
    }

    @Override
    public void onManifestFetched(DomainAliasManifest aliasManifest) {
        System.out.println("DomainAliasManagerBroker: Got the manifest, updating DomainAliasManagers!!!");
        updateManagers(aliasManifest);
    }

    @Override
    public void onManifestNotFetched() {
        System.err.println("DomainAliasManagerBroker:  Could not fetch alias list, should we try again later? attempts left");
        //attempts++;?? timestamp, to try again later. etc.
    }

    private void updateManagers(DomainAliasManifest aliasManifest) {
        Map<String, List<String>> aliases = aliasManifest.aliases;
        Set<Entry<String, List<String>>> aliasSet = aliases.entrySet();
        for (Entry<String, List<String>> entry : aliasSet) {
            final List<String> aliasNames = entry.getValue();
            DomainAliasManager domainAliasManager = getDomainAliasManager(entry.getKey());
            System.out.println("DomainAliasManagerBroker.updateManagers() About to update aliases for " + entry.getKey());
            domainAliasManager.updateAliases(aliasNames);
        }
    }
}