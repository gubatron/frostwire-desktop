package com.frostwire.search.domainalias;

public interface DomainAliasManifestFetcherListener {
    public void onManifestFetched(DomainAliasManifest manifest);
    public void onManifestNotFetched();
}
