package org.limewire.common;

import org.limewire.lifecycle.LimeWireCommonLifecycleModule;

import com.google.inject.AbstractModule;

public class LimeWireCommonModule extends AbstractModule {
    
    @Override
    protected void configure() {
        install(new LimeWireCommonLifecycleModule());
    }

}
