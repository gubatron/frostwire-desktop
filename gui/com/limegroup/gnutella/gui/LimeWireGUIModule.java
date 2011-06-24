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
    
    private final ActivityCallback activityCallback;
    private final LocalClientInfoFactory localClientInfoFactory;
    
    private LimeWireGUIModule() {
        activityCallback = VisualConnectionCallback.instance();
        localClientInfoFactory = LocalClientInfoFactoryImpl.instance();
    }
    
    public ActivityCallback getActivityCallback() {
        return activityCallback;
    }
    
    public LocalClientInfoFactory getLocalClientInfoFactory() {
        return localClientInfoFactory;
    }
    
    public LimeWireGUI getLimeWireGUI() {
        return LimeWireGUI.instance();
    }
}