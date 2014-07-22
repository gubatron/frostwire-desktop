package org.limewire.lifecycle;


public class LimeWireCommonLifecycleModule {
    
    private static LimeWireCommonLifecycleModule INSTANCE;
    
    public static LimeWireCommonLifecycleModule instance() {
        if (INSTANCE == null) {
            INSTANCE = new LimeWireCommonLifecycleModule();
        }
        return INSTANCE;
    }
    
    private final ServiceRegistry serviceRegistry;
    
    private LimeWireCommonLifecycleModule() {
        serviceRegistry = new ServiceRegistryImpl();
    }
    
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
}
