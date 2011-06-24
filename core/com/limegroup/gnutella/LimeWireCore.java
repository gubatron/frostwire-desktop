package com.limegroup.gnutella;

import java.util.concurrent.ScheduledExecutorService;

import org.limewire.lifecycle.ServiceRegistry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.limegroup.gnutella.browser.ExternalControl;
import com.limegroup.gnutella.xml.LimeXMLProperties;

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

    public DownloadManager getDownloadManager() {
        return injector.getInstance(DownloadManager.class);
    }
    
    public LimeXMLProperties getLimeXMLProperties() {
        return injector.getInstance(LimeXMLProperties.class);
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

    public LimeCoreGlue getLimeCoreGlue() {
        return injector.getInstance(LimeCoreGlue.class);
    }
    
    public ServiceRegistry getServiceRegistry() {
        return injector.getInstance(ServiceRegistry.class);
    }
}
