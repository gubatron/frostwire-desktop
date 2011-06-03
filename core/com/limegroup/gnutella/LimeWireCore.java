package com.limegroup.gnutella;

import java.util.concurrent.ScheduledExecutorService;

import org.limewire.io.NetworkInstanceUtils;
import org.limewire.lifecycle.ServiceRegistry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.limegroup.gnutella.altlocs.AltLocManager;
import com.limegroup.gnutella.auth.ContentManager;
import com.limegroup.gnutella.auth.IpPortContentAuthorityFactory;
import com.limegroup.gnutella.browser.ExternalControl;
import com.limegroup.gnutella.connection.MessageReaderFactory;
import com.limegroup.gnutella.connection.RoutedConnectionFactory;
import com.limegroup.gnutella.downloader.CoreDownloaderFactory;
import com.limegroup.gnutella.filters.IPFilter;
import com.limegroup.gnutella.filters.MutableGUIDFilter;
import com.limegroup.gnutella.filters.SpamFilterFactory;
import com.limegroup.gnutella.http.FeaturesWriter;
import com.limegroup.gnutella.licenses.LicenseCache;
import com.limegroup.gnutella.licenses.LicenseFactory;
import com.limegroup.gnutella.licenses.LicenseVerifier;
import com.limegroup.gnutella.messagehandlers.InspectionRequestHandler;
import com.limegroup.gnutella.messagehandlers.UDPCrawlerPingHandler;
import com.limegroup.gnutella.messages.MessageFactory;
import com.limegroup.gnutella.messages.PingReplyFactory;
import com.limegroup.gnutella.messages.PingRequestFactory;
import com.limegroup.gnutella.messages.QueryReplyFactory;
import com.limegroup.gnutella.messages.QueryRequestFactory;
import com.limegroup.gnutella.messages.StaticMessages;
import com.limegroup.gnutella.messages.vendor.CapabilitiesVMFactory;
import com.limegroup.gnutella.messages.vendor.HeadPongFactory;
import com.limegroup.gnutella.messages.vendor.MessagesSupportedVendorMessage;
import com.limegroup.gnutella.messages.vendor.ReplyNumberVendorMessageFactory;
import com.limegroup.gnutella.messages.vendor.UDPCrawlerPongFactory;
import com.limegroup.gnutella.messages.vendor.VendorMessageFactory;
import com.limegroup.gnutella.metadata.MetaDataFactory;
import com.limegroup.gnutella.metadata.MetaDataReader;
import com.limegroup.gnutella.search.HostDataFactory;
import com.limegroup.gnutella.search.QueryDispatcher;
import com.limegroup.gnutella.search.QueryHandlerFactory;
import com.limegroup.gnutella.search.SearchResultHandler;
import com.limegroup.gnutella.spam.RatingTable;
import com.limegroup.gnutella.spam.SpamManager;
import com.limegroup.gnutella.statistics.QueryStats;
import com.limegroup.gnutella.statistics.TcpBandwidthStatistics;
import com.limegroup.gnutella.uploader.UploadSlotManager;
import com.limegroup.gnutella.version.UpdateCollectionFactory;
import com.limegroup.gnutella.xml.LimeXMLDocumentFactory;
import com.limegroup.gnutella.xml.LimeXMLDocumentHelper;
import com.limegroup.gnutella.xml.LimeXMLProperties;
import com.limegroup.gnutella.xml.LimeXMLSchemaRepository;

/**
 * Contains mostly all references to singletons within LimeWire.
 * This class should only be used if it is not possible to inject
 * the correct values into what you're using.  In most cases,
 * it should be possible to just get the injector and call
 * injector.injectMembers(myObject), which is still a superior
 * option to retrieving the individual objects from this class.
 */
@Singleton
public class LimeWireCore {
        
    private final Injector injector;
    
    @Inject
    public LimeWireCore(Injector injector) {
        this.injector = injector;
    }

    public Injector getInjector() {
        return injector;
    }

    public LocalFileDetailsFactory getLocalFileDetailsFactory() {
        return injector.getInstance(LocalFileDetailsFactory.class);
    }

    public HostDataFactory getHostDataFactory() {
        return injector.getInstance(HostDataFactory.class);
    }

    public RoutedConnectionFactory getManagedConnectionFactory() {
        return injector.getInstance(RoutedConnectionFactory.class);
    }

    public QueryRequestFactory getQueryRequestFactory() {
        return injector.getInstance(QueryRequestFactory.class);
    }

    public QueryHandlerFactory getQueryHandlerFactory() {
        return injector.getInstance(QueryHandlerFactory.class);
    }

    public UploadSlotManager getUploadSlotManager() {
        return injector.getInstance(UploadSlotManager.class);
    }

    public FileManager getFileManager() {
        return injector.getInstance(FileManager.class);
    }

    public UploadManager getUploadManager() {
        return injector.getInstance(UploadManager.class);
    }

    public HeadPongFactory getHeadPongFactory() {
        return injector.getInstance(HeadPongFactory.class);
    }

    public FeaturesWriter getFeaturesWriter() {
        return injector.getInstance(FeaturesWriter.class);
    }

    public PushEndpointFactory getPushEndpointFactory() {
        return injector.getInstance(PushEndpointFactory.class);
    }
    
    public PingReplyFactory getPingReplyFactory() {
        return injector.getInstance(PingReplyFactory.class);
    }

    public NetworkManager getNetworkManager() {
        return injector.getInstance(NetworkManager.class);
    }

    public UDPService getUdpService() {
        return injector.getInstance(UDPService.class);
    }
    
    public QueryUnicaster getQueryUnicaster() {
        return injector.getInstance(QueryUnicaster.class);
    }

    public MessageRouter getMessageRouter() {
        return injector.getInstance(MessageRouter.class);
    }

    public DownloadManager getDownloadManager() {
        return injector.getInstance(DownloadManager.class);
    }
    
    public PushManager getPushManager() {
        return injector.getInstance(PushManager.class);
    }

    public SearchResultHandler getSearchResultHandler() {
        return injector.getInstance(SearchResultHandler.class);
    }

    public AltLocManager getAltLocManager() {
        return injector.getInstance(AltLocManager.class);
    }

    public ContentManager getContentManager() {
        return injector.getInstance(ContentManager.class);
    }

    public IPFilter getIpFilter() {
        return injector.getInstance(IPFilter.class);
    }
    
    public QueryStats getQueryStats() {
        return injector.getInstance(QueryStats.class);
    }

    public NodeAssigner getNodeAssigner() {
        return injector.getInstance(NodeAssigner.class);
    }

    public Statistics getStatistics() {
        return injector.getInstance(Statistics.class);
    }

    public CreationTimeCache getCreationTimeCache() {
        return injector.getInstance(CreationTimeCache.class);
    }
    
    public UrnCache getUrnCache() {
        return injector.getInstance(UrnCache.class);
    }

    public FileManagerController getFileManagerController() {
        return injector.getInstance(FileManagerController.class);
    }

    public ResponseFactory getResponseFactory() {
        return injector.getInstance(ResponseFactory.class);
    }

    public QueryReplyFactory getQueryReplyFactory() {
        return injector.getInstance(QueryReplyFactory.class);
    }

    public StaticMessages getStaticMessages() {
        return injector.getInstance(StaticMessages.class);
    }

    public SavedFileManager getSavedFileManager() {
        return injector.getInstance(SavedFileManager.class);
    }
    
    public DownloadCallback getInNetworkCallback() {
        return injector.getInstance(Key.get(DownloadCallback.class, Names.named("inNetwork")));
    }

    public MessageDispatcher getMessageDispatcher() {
        return injector.getInstance(MessageDispatcher.class);
    }

    public MulticastService getMulticastService() {
        return injector.getInstance(MulticastService.class);
    }

    public PongCacher getPongCacher() {
        return injector.getInstance(PongCacher.class);
    }

    public MutableGUIDFilter getMutableGUIDFilter() {
        return injector.getInstance(MutableGUIDFilter.class);
    }

    public LicenseCache getLicenseCache() {
        return injector.getInstance(LicenseCache.class);
    }

    public MessagesSupportedVendorMessage getMessagesSupportedVendorMessage() {
        return injector.getInstance(MessagesSupportedVendorMessage.class);
    }

    public QueryDispatcher getQueryDispatcher() {
        return injector.getInstance(QueryDispatcher.class);
    }

    public RatingTable getRatingTable() {
        return injector.getInstance(RatingTable.class);
    }

    public SpamManager getSpamManager() {
        return injector.getInstance(SpamManager.class);
    }

    public LimeXMLProperties getLimeXMLProperties() {
        return injector.getInstance(LimeXMLProperties.class);
    }

    public LimeXMLSchemaRepository getLimeXMLSchemaRepository() {
        return injector.getInstance(LimeXMLSchemaRepository.class);
    }

    public CapabilitiesVMFactory getCapabilitiesVMFactory() {
        return injector.getInstance(CapabilitiesVMFactory.class);
    }
    
    public ActivityCallback getActivityCallback() {
        return injector.getInstance(ActivityCallback.class);
    }
    
    public LifecycleManager getLifecycleManager() {
        return injector.getInstance(LifecycleManager.class);
    }

    public SearchServices getSearchServices() {
        return injector.getInstance(SearchServices.class);
    }

    public ScheduledExecutorService getBackgroundExecutor() {
        return injector.getInstance(Key.get(ScheduledExecutorService.class, Names.named("backgroundExecutor")));
    }

    public DownloadServices getDownloadServices() {
        return injector.getInstance(DownloadServices.class);
    }

    public UploadServices getUploadServices() {
        return injector.getInstance(UploadServices.class);
    }

    public ApplicationServices getApplicationServices() {
        return injector.getInstance(ApplicationServices.class);
    }

    public SpamServices getSpamServices() {
        return injector.getInstance(SpamServices.class);
    }
    
    public ExternalControl getExternalControl() {
        return injector.getInstance(ExternalControl.class);
    }
    
    public DownloadCallback getDownloadCallback() {
        return injector.getInstance(DownloadCallback.class);
    }

    public ReplyNumberVendorMessageFactory getReplyNumberVendorMessageFactory() {
        return injector.getInstance(ReplyNumberVendorMessageFactory.class);
    }

    public UDPPinger getUDPPinger() {
        return injector.getInstance(UDPPinger.class);
    }

    public UniqueHostPinger getUniqueHostPinger() {
        return injector.getInstance(UniqueHostPinger.class);
    }

    public ScheduledExecutorService getNIOExecutor() {
        return injector.getInstance(Key.get(ScheduledExecutorService.class, Names.named("nioExecutor")));
    }

    public GuidMapManager getGuidMapManager() {
        return injector.getInstance(GuidMapManager.class);
    }

    public PushEndpointCache getPushEndpointCache() {
        return injector.getInstance(PushEndpointCache.class);
    }
    
    public SpamFilterFactory getSpamFilterFactory() {
        return injector.getInstance(SpamFilterFactory.class);
    }

    public UDPReplyHandlerFactory getUDPReplyHandlerFactory() {
        return injector.getInstance(UDPReplyHandlerFactory.class);
    }

    public UDPReplyHandlerCache getUDPReplyHandlerCache() {
        return injector.getInstance(UDPReplyHandlerCache.class);
    }
    
    public Provider<InspectionRequestHandler> getInspectionRequestHandlerFactory() {
        return injector.getProvider(InspectionRequestHandler.class);
    }
    
    public Provider<UDPCrawlerPingHandler> getUDPCrawlerPingHandlerFactory() {
        return injector.getProvider(UDPCrawlerPingHandler.class);
    }

    public MessageFactory getMessageFactory() {
        return injector.getInstance(MessageFactory.class);
    }

    public MessageReaderFactory getMessageReaderFactory() {
        return injector.getInstance(MessageReaderFactory.class);
    }

    public VendorMessageFactory getVendorMessageFactory() {
        return injector.getInstance(VendorMessageFactory.class);
    }

    public UDPCrawlerPongFactory getUDPCrawlerPongFactory() {
        return injector.getInstance(UDPCrawlerPongFactory.class);
    }
    
    public LicenseFactory getLicenseFactory() {
        return injector.getInstance(LicenseFactory.class);
    }

    public LimeXMLDocumentFactory getLimeXMLDocumentFactory() {
        return injector.getInstance(LimeXMLDocumentFactory.class);
    }

    public MetaDataFactory getMetaDataFactory() {
        return injector.getInstance(MetaDataFactory.class);
    }

    public LimeXMLDocumentHelper getLimeXMLDocumentHelper() {
        return injector.getInstance(LimeXMLDocumentHelper.class);
    }

    public MetaDataReader getMetaDataReader() {
        return injector.getInstance(MetaDataReader.class);
    }
    
    public CoreDownloaderFactory getCoreDownloaderFactory() {
    	return injector.getInstance(CoreDownloaderFactory.class);
    }

    public PingRequestFactory getPingRequestFactory() {
        return injector.getInstance(PingRequestFactory.class);
    }

    public IpPortContentAuthorityFactory getIpPortContentAuthorityFactory() {
        return injector.getInstance(IpPortContentAuthorityFactory.class);
    }

    public UpdateCollectionFactory getUpdateCollectionFactory() {
        return injector.getInstance(UpdateCollectionFactory.class);
    }

    public LimeCoreGlue getLimeCoreGlue() {
        return injector.getInstance(LimeCoreGlue.class);
    }

    public LicenseVerifier getLicenseVerifier() {
        return injector.getInstance(LicenseVerifier.class);
    }
    
    public TcpBandwidthStatistics getTcpBandwidthStatistics() {
        return injector.getInstance(TcpBandwidthStatistics.class);
    }
    
    public NetworkInstanceUtils getNetworkInstanceUtils() {
        return injector.getInstance(NetworkInstanceUtils.class);
    }
    
    public ServiceRegistry getServiceRegistry() {
        return injector.getInstance(ServiceRegistry.class);
    }

}
