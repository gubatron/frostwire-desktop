package org.limewire.common;

import org.limewire.lifecycle.LimeWireCommonLifecycleModule;

public class LimeWireCommonModule {
    
    private static LimeWireCommonModule INSTANCE;
    
    public static LimeWireCommonModule instance() {
        if (INSTANCE == null) {
            INSTANCE = new LimeWireCommonModule();
        }
        return INSTANCE;
    }
    
    private final LimeWireCommonLifecycleModule limewireCommonLifecycleModule;
    
    private LimeWireCommonModule() {
        limewireCommonLifecycleModule = LimeWireCommonLifecycleModule.instance();
    }
    
    public LimeWireCommonLifecycleModule getLimeWireCommonLifecycleModule() {
        return limewireCommonLifecycleModule;
    }
}
