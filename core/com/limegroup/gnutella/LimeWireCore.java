package com.limegroup.gnutella;

import java.util.concurrent.ScheduledExecutorService;

import org.gudy.azureus2.plugins.ipfilter.IPFilter;
import org.limewire.io.NetworkInstanceUtils;
import org.limewire.lifecycle.ServiceRegistry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.limegroup.gnutella.browser.ExternalControl;
import com.limegroup.gnutella.downloader.CoreDownloaderFactory;
import com.limegroup.gnutella.http.FeaturesWriter;
import com.limegroup.gnutella.licenses.LicenseCache;
import com.limegroup.gnutella.licenses.LicenseFactory;
import com.limegroup.gnutella.licenses.LicenseVerifier;
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

    public QueryRequestFactory getQueryRequestFactory() {
        return injector.getInstance(QueryRequestFactory.class);
    }

    public QueryHandlerFactory getQueryHandlerFactory() {
        return injector.getInstance(QueryHandlerFactory.class);
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

    public DownloadManager getDownloadManager() {
        return injector.getInstance(DownloadManager.class);
    }

    public IPFilter getIpFilter() {
        return injector.getInstance(IPFilter.class);
    }

    public Statistics getStatistics() {
        return injector.getInstance(Statistics.class);
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
    
    public PongCacher getPongCacher() {
        return injector.getInstance(PongCacher.class);
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

    public ScheduledExecutorService getBackgroundExecutor() {
        return injector.getInstance(Key.get(ScheduledExecutorService.class, Names.named("backgroundExecutor")));
    }

    public ApplicationServices getApplicationServices() {
        return injector.getInstance(ApplicationServices.class);
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

    public ScheduledExecutorService getNIOExecutor() {
        return injector.getInstance(Key.get(ScheduledExecutorService.class, Names.named("nioExecutor")));
    }

    public GuidMapManager getGuidMapManager() {
        return injector.getInstance(GuidMapManager.class);
    }

    public PushEndpointCache getPushEndpointCache() {
        return injector.getInstance(PushEndpointCache.class);
    }
    
    public UDPReplyHandlerCache getUDPReplyHandlerCache() {
        return injector.getInstance(UDPReplyHandlerCache.class);
    }
    
    public MessageFactory getMessageFactory() {
        return injector.getInstance(MessageFactory.class);
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
    
    public UpdateCollectionFactory getUpdateCollectionFactory() {
        return injector.getInstance(UpdateCollectionFactory.class);
    }

    public LimeCoreGlue getLimeCoreGlue() {
        return injector.getInstance(LimeCoreGlue.class);
    }

    public LicenseVerifier getLicenseVerifier() {
        return injector.getInstance(LicenseVerifier.class);
    }
    
    public NetworkInstanceUtils getNetworkInstanceUtils() {
        return injector.getInstance(NetworkInstanceUtils.class);
    }
    
    public ServiceRegistry getServiceRegistry() {
        return injector.getInstance(ServiceRegistry.class);
    }
}
