package com.frostwire.search.domainalias;

public abstract class AbstractDomainAliasManifestFetcher {

    private DomainAliasManifest manifest;
    private DomainAliasManifestFetcherListener listener;
    
    public AbstractDomainAliasManifestFetcher(DomainAliasManifestFetcherListener listener) {
        setListener(listener);
    }
    
    abstract public void fetchManifest();
    
    public final DomainAliasManifest getDomainAliasManifest() {
        return manifest;
    }

    public final void setListener(DomainAliasManifestFetcherListener listener) {
        this.listener = listener;
    }

    public final void notifyManifestFetched(DomainAliasManifest manifest) {
        if (listener != null) {
            listener.onManifestFetched(manifest);
        }
    }

    public final void notifyManifestNotFetched() {
        if (listener != null) {
            listener.onManifestNotFetched();
        }
    }
}