package com.limegroup.gnutella;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.limewire.common.LimeWireCommonModule;
import org.limewire.concurrent.AbstractLazySingletonProvider;
import org.limewire.concurrent.SimpleTimer;
import org.limewire.inject.AbstractModule;
import org.limewire.io.LocalSocketAddressProvider;

import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.limegroup.gnutella.licenses.LicenseFactory;
import com.limegroup.gnutella.licenses.LicenseFactoryImpl;

/**
 * The module that defines what implementations are used within
 * LimeWire's core.  This class can be constructed with or without
 * an ActivitiyCallback class.  If it is without, then another module
 * must explicitly identify which class is going to define the
 * ActivityCallback.
 */
public class LimeWireCoreModule extends AbstractModule {
 
	private final Class<? extends ActivityCallback> activityCallbackClass;
    
    public LimeWireCoreModule() {
        this(null);
    }
    
    public LimeWireCoreModule(Class<? extends ActivityCallback> activityCallbackClass) {
        this.activityCallbackClass = activityCallbackClass;
    }
    
    @Override
    protected void configure() {
        binder().install(new LimeWireCommonModule());
        
        bind(LimeWireCore.class);
        
        if(activityCallbackClass != null) {
            bind(ActivityCallback.class).to(activityCallbackClass);
        }        

        bind(DownloadCallback.class).to(ActivityCallback.class);
        bind(NetworkManager.class).to(NetworkManagerImpl.class);
        bind(LocalFileDetailsFactory.class).to(LocalFileDetailsFactoryImpl.class);
        bind(LifecycleManager.class).to(LifecycleManagerImpl.class);
        bind(DownloadManager.class).to(DownloadManagerImpl.class).asEagerSingleton();
        bind(LicenseFactory.class).to(LicenseFactoryImpl.class);
        bind(SaveLocationManager.class).to(DownloadManager.class);
        bind(LocalSocketAddressProvider.class).to(LocalSocketAddressProviderImpl.class);
        
        bindAll(Names.named("backgroundExecutor"), ScheduledExecutorService.class, BackgroundTimerProvider.class, ExecutorService.class, Executor.class);
    }
    
    @Singleton
    private static class BackgroundTimerProvider extends AbstractLazySingletonProvider<ScheduledExecutorService> {
        protected ScheduledExecutorService createObject() {
            return new SimpleTimer(true);
        }
    }
}