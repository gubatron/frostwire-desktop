package com.limegroup.gnutella;

import java.util.concurrent.ScheduledExecutorService;

import org.gudy.azureus2.plugins.ipfilter.IPFilter;
import org.limewire.lifecycle.ServiceRegistry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.limegroup.gnutella.browser.ExternalControl;
import com.limegroup.gnutella.http.FeaturesWriter;
import com.limegroup.gnutella.licenses.LicenseCache;
import com.limegroup.gnutella.licenses.LicenseFactory;
import com.limegroup.gnutella.licenses.LicenseVerifier;
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

    public FeaturesWriter getFeaturesWriter() {
        return injector.getInstance(FeaturesWriter.class);
    }

    public NetworkManager getNetworkManager() {
        return injector.getInstance(NetworkManager.class);
    }

    public DownloadManager getDownloadManager() {
        return injector.getInstance(DownloadManager.class);
    }

    public IPFilter getIpFilter() {
        return injector.getInstance(IPFilter.class);
    }
    
    public SavedFileManager getSavedFileManager() {
        return injector.getInstance(SavedFileManager.class);
    }
    
    public DownloadCallback getInNetworkCallback() {
        return injector.getInstance(Key.get(DownloadCallback.class, Names.named("inNetwork")));
    }

    public LicenseCache getLicenseCache() {
        return injector.getInstance(LicenseCache.class);
    }
    
    public LimeXMLProperties getLimeXMLProperties() {
        return injector.getInstance(LimeXMLProperties.class);
    }

    public LimeXMLSchemaRepository getLimeXMLSchemaRepository() {
        return injector.getInstance(LimeXMLSchemaRepository.class);
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
    
    public ExternalControl getExternalControl() {
        return injector.getInstance(ExternalControl.class);
    }
    
    public DownloadCallback getDownloadCallback() {
        return injector.getInstance(DownloadCallback.class);
    }

    public ScheduledExecutorService getNIOExecutor() {
        return injector.getInstance(Key.get(ScheduledExecutorService.class, Names.named("nioExecutor")));
    }
    
    public LicenseFactory getLicenseFactory() {
        return injector.getInstance(LicenseFactory.class);
    }

    public LimeCoreGlue getLimeCoreGlue() {
        return injector.getInstance(LimeCoreGlue.class);
    }

    public LicenseVerifier getLicenseVerifier() {
        return injector.getInstance(LicenseVerifier.class);
    }
    
    public ServiceRegistry getServiceRegistry() {
        return injector.getInstance(ServiceRegistry.class);
    }
}
