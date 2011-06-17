package com.limegroup.gnutella.downloader;

import com.google.inject.AbstractModule;

public class LimeWireDownloadModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(CoreDownloaderFactory.class).to(CoreDownloaderFactoryImpl.class);
    }
}
