package com.limegroup.gnutella.downloader;

import com.google.inject.AbstractModule;

public class LimeWireDownloadModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(RequeryManagerFactory.class).to(RequeryManagerFactoryImpl.class);
        bind(CoreDownloaderFactory.class).to(CoreDownloaderFactoryImpl.class);
    }
}
