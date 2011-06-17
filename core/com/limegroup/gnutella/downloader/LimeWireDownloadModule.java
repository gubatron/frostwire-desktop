package com.limegroup.gnutella.downloader;

import com.google.inject.AbstractModule;
import com.limegroup.gnutella.downloader.serial.DownloadSerializeSettings;
import com.limegroup.gnutella.downloader.serial.DownloadSerializeSettingsImpl;
import com.limegroup.gnutella.downloader.serial.DownloadSerializer;
import com.limegroup.gnutella.downloader.serial.DownloadSerializerImpl;

public class LimeWireDownloadModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(RequeryManagerFactory.class).to(RequeryManagerFactoryImpl.class);
        bind(CoreDownloaderFactory.class).to(CoreDownloaderFactoryImpl.class);
        bind(DownloadSerializer.class).to(DownloadSerializerImpl.class);
        bind(DownloadSerializeSettings.class).to(DownloadSerializeSettingsImpl.class);
    }
}
