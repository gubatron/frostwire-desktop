package com.limegroup.gnutella;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.limewire.common.LimeWireCommonModule;
import org.limewire.concurrent.AbstractLazySingletonProvider;
import org.limewire.concurrent.ExecutorsHelper;
import org.limewire.concurrent.SimpleTimer;
import org.limewire.inject.AbstractModule;
import org.limewire.inspection.Inspector;
import org.limewire.inspection.InspectorImpl;
import org.limewire.io.LimeWireIOModule;
import org.limewire.io.LocalSocketAddressProvider;

import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.limegroup.gnutella.licenses.LicenseFactory;
import com.limegroup.gnutella.licenses.LicenseFactoryImpl;
import com.limegroup.gnutella.version.UpdateCollectionFactory;
import com.limegroup.gnutella.version.UpdateCollectionFactoryImpl;
import com.limegroup.gnutella.xml.LimeXMLDocumentFactory;
import com.limegroup.gnutella.xml.LimeXMLDocumentFactoryImpl;

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
        binder().install(new LimeWireIOModule());
        
        bind(LimeWireCore.class);
        
        if(activityCallbackClass != null) {
            bind(ActivityCallback.class).to(activityCallbackClass);
        }        

        bind(DownloadCallback.class).to(ActivityCallback.class);
        bind(NetworkManager.class).to(NetworkManagerImpl.class);
        bind(LocalFileDetailsFactory.class).to(LocalFileDetailsFactoryImpl.class);
        bind(LifecycleManager.class).to(LifecycleManagerImpl.class);
        bind(ApplicationServices.class).to(ApplicationServicesImpl.class);
        bind(DownloadManager.class).to(DownloadManagerImpl.class).asEagerSingleton();
        bind(GuidMapManager.class).to(GuidMapManagerImpl.class);
        bind(PushEndpointCache.class).to(PushEndpointCacheImpl.class);
        bind(LicenseFactory.class).to(LicenseFactoryImpl.class);
        bind(LimeXMLDocumentFactory.class).to(LimeXMLDocumentFactoryImpl.class);
        bind(SaveLocationManager.class).to(DownloadManager.class);
        bind(UpdateCollectionFactory.class).to(UpdateCollectionFactoryImpl.class);
        bind(Inspector.class).to(InspectorImpl.class);
        bind(LocalSocketAddressProvider.class).to(LocalSocketAddressProviderImpl.class);
        bind(BandwidthTracker.class).annotatedWith(Names.named("downloadTracker")).to(DownloadManager.class); // For NodeAssigner.
        
        bindAll(Names.named("unlimitedExecutor"), ExecutorService.class, UnlimitedExecutorProvider.class, Executor.class);
        bindAll(Names.named("backgroundExecutor"), ScheduledExecutorService.class, BackgroundTimerProvider.class, ExecutorService.class, Executor.class);
        bindAll(Names.named("dhtExecutor"), ExecutorService.class, DHTExecutorProvider.class, Executor.class);
        bindAll(Names.named("messageExecutor"), ExecutorService.class, MessageExecutorProvider.class, Executor.class);
                        
        // TODO: This is odd -- move to initialize & LifecycleManager?
        bind(Statistics.class).asEagerSingleton();
    }
    
    
    @Singleton
    private static class UnlimitedExecutorProvider extends AbstractLazySingletonProvider<ExecutorService> {
        protected ExecutorService createObject() {
            return ExecutorsHelper.newThreadPool(ExecutorsHelper.daemonThreadFactory("IdleThread"));
        }
    }
    
    @Singleton
    private static class BackgroundTimerProvider extends AbstractLazySingletonProvider<ScheduledExecutorService> {
        protected ScheduledExecutorService createObject() {
            return new SimpleTimer(true);
        }
    }
    
    @Singleton
    private static class MessageExecutorProvider extends AbstractLazySingletonProvider<ExecutorService> {
        protected ExecutorService createObject() {
            return ExecutorsHelper.newProcessingQueue("Message-Executor");
        }
    }

    @Singleton
    private static class DHTExecutorProvider extends AbstractLazySingletonProvider<ExecutorService> {
        protected ExecutorService createObject() {
            return ExecutorsHelper.newProcessingQueue("DHT-Executor");
        }
    }    
    
    ///////////////////////////////////////////////////////////////////////////
    /// BELOW ARE ALL HACK PROVIDERS THAT NEED TO BE UPDATED TO CONSTRUCT OBJECTS!
    // (This needs to wait till components are injected and stop using singletons too.)

        
    ///////////////////////////////////////////////////////////////
    // !!! DO NOT ADD THINGS BELOW HERE !!!  PUT THEM ABOVE THE HACKS!
}