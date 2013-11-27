package com.frostwire.search.domainalias;

import java.util.ArrayList;
import java.util.Collections;
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
        katAliases.add("kickass.pw");
        katAliases.add("katproxy.co"); //*
        katAliases.add("kickass.to.proxy.piratenpartij.nl"); //*
        katAliases.add("proxykat.me"); //*
        
        //katAliases.add("www.kat.ph");
        //katAliases.add("kickass.pw");
        //katAliases.add("katproxy.pw");
        manifest.aliases.put("kickass.to", katAliases);
        
        //TPB
        
        List<String> tpbAliases = new ArrayList<String>();
        tpbAliases.add("pirateproxy.net"); //*
        tpbAliases.add("proxybay.de"); //*
        
        tpbAliases.add("pirateproxy.se");
        tpbAliases.add("tpb.unblocked.co");
        tpbAliases.add("thelitebay.com");
        tpbAliases.add("www.proxybay.eu");
        
        tpbAliases.add("tpbunion.com");
        tpbAliases.add("pirate-bay.pw");
        tpbAliases.add("quluxingba.info");
        tpbAliases.add("tpb.pirati.cz");
        manifest.aliases.put("thepiratebay.sx", tpbAliases);
        
        
        //ISOHUNT
        /*
        List<String> isoHuntAliases = new ArrayList<String>();
        isoHuntAliases.add("isohunt.come.in");
        manifest.aliases.put("isohunt.to",isoHuntAliases);
        */
        
        manifest.aliases.put("extratorrent.cc", Collections.EMPTY_LIST);
        notifyManifestFetched(manifest);
    }
}