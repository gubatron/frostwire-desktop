package com.frostwire.guice;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.limewire.concurrent.AbstractLazySingletonProvider;
import org.limewire.concurrent.SimpleTimer;
import org.limewire.http.LimeWireHttpModule;
import org.limewire.inject.AbstractModule;
import org.limewire.io.LimeWireIOModule;
import org.limewire.io.LocalSocketAddressProvider;
import org.limewire.net.LimeWireNetModule;

import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.limegroup.bittorrent.BTDownloader;
import com.limegroup.bittorrent.BTDownloaderImpl;
import com.limegroup.gnutella.downloader.LimeWireDownloadModule;
import com.limegroup.gnutella.http.DefaultHttpExecutor;
import com.limegroup.gnutella.http.HttpExecutor;
import com.limegroup.gnutella.settings.ConnectionSettings;
import com.limegroup.gnutella.settings.SSLSettings;
import com.limegroup.gnutella.settings.SettingsBackedProxySettings;
import com.limegroup.gnutella.settings.SettingsBackedSocketBindingSettings;

public class FrostWireCoreModule extends AbstractModule {
 
	public FrostWireCoreModule() {
    }
    
    @Override
    protected void configure() {
    	
    	binder().install(new LimeWireNetModule(SettingsBackedProxySettings.class, SettingsBackedSocketBindingSettings.class));
        binder().install(new LimeWireHttpModule());
        binder().install(new LimeWireIOModule());
        
        bind(HttpExecutor.class).to(DefaultHttpExecutor.class);
        bind(LocalSocketAddressProvider.class).to(MyLocalSocketAddressProviderImpl.class);
        
        bindAll(Names.named("backgroundExecutor"), ScheduledExecutorService.class, BackgroundTimerProvider.class, ExecutorService.class, Executor.class);
    }
    
    @Singleton
    private static class BackgroundTimerProvider extends AbstractLazySingletonProvider<ScheduledExecutorService> {
        protected ScheduledExecutorService createObject() {
            return new SimpleTimer(true);
        }
    }
    
    @Singleton
    private static class MyLocalSocketAddressProviderImpl implements LocalSocketAddressProvider {
        
        public byte[] getLocalAddress() {
        	return null;
        }

        public int getLocalPort() {
            return 0;
        }

        public boolean isLocalAddressPrivate() {
            return ConnectionSettings.LOCAL_IS_PRIVATE.getValue();
        }
        
        public boolean isTLSCapable() {
            return SSLSettings.isIncomingTLSEnabled();
        }
    }
}