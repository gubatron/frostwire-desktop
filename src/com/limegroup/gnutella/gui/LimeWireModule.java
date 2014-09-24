package com.limegroup.gnutella.gui;

import com.limegroup.gnutella.LimeWireCoreModule;

/** The master LimeWire module. */
public class LimeWireModule {
    
    private static LimeWireModule INSTANCE;

    public static LimeWireModule instance() {
        if (INSTANCE == null) {
            INSTANCE = new LimeWireModule();
        }
        return INSTANCE;
    }
    
    private final LimeWireCoreModule limeWireCoreModule;
    private final LimeWireGUIModule limeWireGUIModule;
    
    private LimeWireModule() {
        limeWireCoreModule = LimeWireCoreModule.instance(VisualConnectionCallback.instance());
        limeWireGUIModule = LimeWireGUIModule.instance();
    }
    
    public LimeWireCoreModule getLimeWireCoreModule() {
        return limeWireCoreModule;
    }
    
    public LimeWireGUIModule getLimeWireGUIModule() {
        return limeWireGUIModule;
    }
}
