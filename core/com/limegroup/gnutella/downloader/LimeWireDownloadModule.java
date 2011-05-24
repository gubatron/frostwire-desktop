package com.limegroup.gnutella.downloader;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.limegroup.gnutella.DownloadCallback;
import com.limegroup.gnutella.downloader.serial.DownloadSerializeSettings;
import com.limegroup.gnutella.downloader.serial.DownloadSerializeSettingsImpl;
import com.limegroup.gnutella.downloader.serial.DownloadSerializer;
import com.limegroup.gnutella.downloader.serial.DownloadSerializerImpl;
import com.limegroup.gnutella.downloader.serial.OldDownloadConverter;
import com.limegroup.gnutella.downloader.serial.conversion.OldDownloadConverterImpl;

public class LimeWireDownloadModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(RemoteFileDescFactory.class).to(RemoteFileDescFactoryImpl.class);
        bind(DownloadCallback.class).annotatedWith(Names.named("inNetwork")).to(InNetworkCallback.class);        
        bind(RequeryManagerFactory.class).to(RequeryManagerFactoryImpl.class);
        bind(PushedSocketHandlerRegistry.class).to(PushDownloadManager.class);
        bind(CoreDownloaderFactory.class).to(CoreDownloaderFactoryImpl.class);
        bind(DownloadSerializer.class).to(DownloadSerializerImpl.class);
        bind(DownloadSerializeSettings.class).to(DownloadSerializeSettingsImpl.class);
        bind(OldDownloadConverter.class).to(OldDownloadConverterImpl.class);
    }

}
