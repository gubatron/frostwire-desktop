package com.limegroup.gnutella;
import com.google.inject.Binder;
import com.google.inject.Module;

/** The master LimeWire module. */
public class LimeWireModule implements Module {

    public void configure(Binder binder) {
        binder.install(new LimeWireCoreModule());
        //binder.install(new LimeWireGUIModule());
    }

}
