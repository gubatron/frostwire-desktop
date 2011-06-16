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
import org.limewire.statistic.LimeWireStatisticsModule;

import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.limegroup.gnutella.auth.IpPortContentAuthorityFactory;
import com.limegroup.gnutella.auth.IpPortContentAuthorityFactoryImpl;
import com.limegroup.gnutella.connection.MessageReaderFactory;
import com.limegroup.gnutella.connection.MessageReaderFactoryImpl;
import com.limegroup.gnutella.connection.RoutedConnectionFactory;
import com.limegroup.gnutella.connection.RoutedConnectionFactoryImpl;
import com.limegroup.gnutella.downloader.LimeWireDownloadModule;
import com.limegroup.gnutella.filters.SpamFilterFactory;
import com.limegroup.gnutella.filters.SpamFilterFactoryImpl;
import com.limegroup.gnutella.licenses.LicenseFactory;
import com.limegroup.gnutella.licenses.LicenseFactoryImpl;
import com.limegroup.gnutella.messagehandlers.MessageHandlerBinderImpl;
import com.limegroup.gnutella.messages.MessageFactory;
import com.limegroup.gnutella.messages.MessageFactoryImpl;
import com.limegroup.gnutella.messages.MessageParserBinder;
import com.limegroup.gnutella.messages.MessageParserBinderImpl;
import com.limegroup.gnutella.messages.PingReplyFactory;
import com.limegroup.gnutella.messages.PingReplyFactoryImpl;
import com.limegroup.gnutella.messages.PingRequestFactory;
import com.limegroup.gnutella.messages.PingRequestFactoryImpl;
import com.limegroup.gnutella.messages.QueryReplyFactory;
import com.limegroup.gnutella.messages.QueryReplyFactoryImpl;
import com.limegroup.gnutella.messages.QueryRequestFactory;
import com.limegroup.gnutella.messages.QueryRequestFactoryImpl;
import com.limegroup.gnutella.messages.vendor.CapabilitiesVMFactory;
import com.limegroup.gnutella.messages.vendor.CapabilitiesVMFactoryImpl;
import com.limegroup.gnutella.messages.vendor.HeadPongFactory;
import com.limegroup.gnutella.messages.vendor.HeadPongFactoryImpl;
import com.limegroup.gnutella.messages.vendor.InspectionResponseFactory;
import com.limegroup.gnutella.messages.vendor.InspectionResponseFactoryImpl;
import com.limegroup.gnutella.messages.vendor.ReplyNumberVendorMessageFactory;
import com.limegroup.gnutella.messages.vendor.ReplyNumberVendorMessageFactoryImpl;
import com.limegroup.gnutella.messages.vendor.UDPCrawlerPongFactory;
import com.limegroup.gnutella.messages.vendor.UDPCrawlerPongFactoryImpl;
import com.limegroup.gnutella.messages.vendor.VendorMessageFactory;
import com.limegroup.gnutella.messages.vendor.VendorMessageFactoryImpl;
import com.limegroup.gnutella.messages.vendor.VendorMessageParserBinder;
import com.limegroup.gnutella.messages.vendor.VendorMessageParserBinderImpl;
import com.limegroup.gnutella.metadata.MetaDataFactory;
import com.limegroup.gnutella.metadata.MetaDataFactoryImpl;
import com.limegroup.gnutella.search.HostDataFactory;
import com.limegroup.gnutella.search.HostDataFactoryImpl;
import com.limegroup.gnutella.search.QueryDispatcher;
import com.limegroup.gnutella.search.QueryDispatcherImpl;
import com.limegroup.gnutella.search.QueryHandlerFactory;
import com.limegroup.gnutella.search.QueryHandlerFactoryImpl;
import com.limegroup.gnutella.statistics.LimeWireGnutellaStatisticsModule;
import com.limegroup.gnutella.version.UpdateCollectionFactory;
import com.limegroup.gnutella.version.UpdateCollectionFactoryImpl;
import com.limegroup.gnutella.version.UpdateMessageVerifier;
import com.limegroup.gnutella.version.UpdateMessageVerifierImpl;
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
        binder().install(new LimeWireDownloadModule());
        binder().install(new LimeWireStatisticsModule());
        binder().install(new LimeWireGnutellaStatisticsModule());
        binder().install(new LimeWireIOModule());
        
        bind(LimeWireCore.class);
        
        if(activityCallbackClass != null) {
            bind(ActivityCallback.class).to(activityCallbackClass);
        }        

        bind(DownloadCallback.class).to(ActivityCallback.class);
        bind(NetworkManager.class).to(NetworkManagerImpl.class);
        bind(PingReplyFactory.class).to(PingReplyFactoryImpl.class);
        bind(PushEndpointFactory.class).to(PushEndpointFactoryImpl.class);
        bind(HeadPongFactory.class).to(HeadPongFactoryImpl.class);
        bind(QueryHandlerFactory.class).to(QueryHandlerFactoryImpl.class);
        bind(QueryRequestFactory.class).to(QueryRequestFactoryImpl.class);
        bind(RoutedConnectionFactory.class).to(RoutedConnectionFactoryImpl.class);
        bind(HostDataFactory.class).to(HostDataFactoryImpl.class);
        bind(LocalFileDetailsFactory.class).to(LocalFileDetailsFactoryImpl.class);
        bind(ResponseFactory.class).to(ResponseFactoryImpl.class);
        bind(QueryReplyFactory.class).to(QueryReplyFactoryImpl.class);
        bind(CapabilitiesVMFactory.class).to(CapabilitiesVMFactoryImpl.class);
        bind(LifecycleManager.class).to(LifecycleManagerImpl.class);
        bind(SearchServices.class).to(SearchServicesImpl.class);
        bind(DownloadServices.class).to(DownloadServicesImpl.class);
        bind(ApplicationServices.class).to(ApplicationServicesImpl.class);
        bind(SpamServices.class).to(SpamServicesImpl.class);
        bind(SpamFilterFactory.class).to(SpamFilterFactoryImpl.class);
        bind(UDPReplyHandlerFactory.class).to(UDPReplyHandlerFactoryImpl.class);
        bind(UDPReplyHandlerCache.class).to(UDPReplyHandlerCacheImpl.class);
        bind(DownloadManager.class).to(DownloadManagerImpl.class).asEagerSingleton();
        bind(ReplyNumberVendorMessageFactory.class).to(ReplyNumberVendorMessageFactoryImpl.class);
        bind(GuidMapManager.class).to(GuidMapManagerImpl.class);
        bind(PushEndpointCache.class).to(PushEndpointCacheImpl.class);
        bind(MessageFactory.class).to(MessageFactoryImpl.class);
        bind(MessageReaderFactory.class).to(MessageReaderFactoryImpl.class);
        bind(MessageParserBinder.class).to(MessageParserBinderImpl.class);
        bind(VendorMessageFactory.class).to(VendorMessageFactoryImpl.class);
        bind(VendorMessageParserBinder.class).to(VendorMessageParserBinderImpl.class);
        bind(UDPCrawlerPongFactory.class).to(UDPCrawlerPongFactoryImpl.class);
        bind(LicenseFactory.class).to(LicenseFactoryImpl.class);
        bind(LimeXMLDocumentFactory.class).to(LimeXMLDocumentFactoryImpl.class);
        bind(MetaDataFactory.class).to(MetaDataFactoryImpl.class);
        bind(SaveLocationManager.class).to(DownloadManager.class);
        bind(PingRequestFactory.class).to(PingRequestFactoryImpl.class);
        bind(IpPortContentAuthorityFactory.class).to(IpPortContentAuthorityFactoryImpl.class);
        bind(UpdateCollectionFactory.class).to(UpdateCollectionFactoryImpl.class);
        bind(UDPPinger.class).to(UDPPingerImpl.class);
        bind(Inspector.class).to(InspectorImpl.class);
        bind(LocalSocketAddressProvider.class).to(LocalSocketAddressProviderImpl.class);
        bind(MessageRouter.class).to(StandardMessageRouter.class);
        bind(PongCacher.class).to(PongCacherImpl.class);        
        bind(BandwidthTracker.class).annotatedWith(Names.named("downloadTracker")).to(DownloadManager.class); // For NodeAssigner.
        bind(MessageHandlerBinder.class).to(MessageHandlerBinderImpl.class);
        bind(QueryDispatcher.class).to(QueryDispatcherImpl.class);
        bind(UpdateMessageVerifier.class).to(UpdateMessageVerifierImpl.class);
        bind(InspectionResponseFactory.class).to(InspectionResponseFactoryImpl.class);
        bind(NodeAssigner.class).to(NodeAssignerImpl.class);
        
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