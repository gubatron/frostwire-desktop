package com.limegroup.gnutella;

import org.limewire.lifecycle.ServiceRegistry;

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
public class LimeWireCore {
    
    private static LimeWireCore INSTANCE;
    
    public static LimeWireCore instance() {
        if (INSTANCE == null) {
            INSTANCE = new LimeWireCore();
        }
        return INSTANCE;
    }
        
    private LimeWireCore() {
    }
    
    public DownloadManager getDownloadManager() {
        return LimeWireCoreModule.instance(null).getDownloadManager();
    }
    
    public LimeXMLProperties getLimeXMLProperties() {
        return LimeXMLProperties.instance();
    }
    
    public LifecycleManager getLifecycleManager() {
        return LimeWireCoreModule.instance(null).getLifecycleManager();
    }
    
    public ExternalControl getExternalControl() {
        return new ExternalControl(LimeWireCoreModule.instance(null).getActivityCallback());
    }
    
    public LimeCoreGlue getLimeCoreGlue() {
        return LimeCoreGlue.instance();
    }
    
    public ServiceRegistry getServiceRegistry() {
        return LimeWireCoreModule.instance(null).getLimeWireCommonModule().getLimeWireCommonLifecycleModule().getServiceRegistry();
    }
}
