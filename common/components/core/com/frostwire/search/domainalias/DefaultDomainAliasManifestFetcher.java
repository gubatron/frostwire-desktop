package com.frostwire.search.domainalias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Default Domain Alias Manifest "fetcher", creates and holds a Domain Alias manifest in memory
 * that can be used while we fetch a signed one from another source.
 * @author gubatron
 *
 */
public class DefaultDomainAliasManifestFetcher extends AbstractDomainAliasManifestFetcher {

    
    public DefaultDomainAliasManifestFetcher(DomainAliasManifestFetcherListener listener) {
        super(listener);
    }

    @Override
    public void fetchManifest() {
        DomainAliasManifest manifest = new DomainAliasManifest();
        manifest.lastUpdated = System.currentTimeMillis();
        manifest.version = 0;
        manifest.aliases = new HashMap<String, List<String>>();

        //KAT
        List<String> katAliases = new ArrayList<String>();
        katAliases.add("kickasstorrents.come.in");
        katAliases.add("www.kat.ph");
        katAliases.add("kickass.pw");
        katAliases.add("katproxy.pw");
        manifest.aliases.put("kickass.to", katAliases);
        
        //TPB
        
        List<String> tpbAliases = new ArrayList<String>();
        tpbAliases.add("pirateproxy.se");
        tpbAliases.add("tpb.unblocked.co");
        tpbAliases.add("thelitebay.com");
        tpbAliases.add("www.proxybay.eu");
        tpbAliases.add("pirateproxy.net");
        tpbAliases.add("tpbunion.com");
        tpbAliases.add("pirate-bay.pw");
        tpbAliases.add("quluxingba.info");
        tpbAliases.add("welovetpb.co.uk");
        tpbAliases.add("tpb.pirati.cz");
        manifest.aliases.put("thepiratebay.sx", tpbAliases);

        notifyManifestFetched(manifest);
    }
}