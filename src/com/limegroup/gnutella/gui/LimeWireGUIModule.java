package com.limegroup.gnutella.gui;

import com.limegroup.gnutella.ActivityCallback;

public class LimeWireGUIModule {
    
    private static LimeWireGUIModule INSTANCE;
    
    public static LimeWireGUIModule instance() {
        if (INSTANCE == null) {
            INSTANCE = new LimeWireGUIModule();
        }
        return INSTANCE;
    }

    private LimeWireGUIModule() {
    }

    public LimeWireGUI getLimeWireGUI() {
        return LimeWireGUI.instance();
    }
}
