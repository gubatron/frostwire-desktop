package com.limegroup.gnutella;

import org.limewire.common.LimeWireCommonModule;

/**
 * The module that defines what implementations are used within
 * LimeWire's core.  This class can be constructed with or without
 * an ActivitiyCallback class.  If it is without, then another module
 * must explicitly identify which class is going to define the
 * ActivityCallback.
 */
public class LimeWireCoreModule {
    
    private static LimeWireCoreModule INSTANCE;
    
    public static LimeWireCoreModule instance(ActivityCallback activitCallback) {
        if (INSTANCE == null) {
            INSTANCE = new LimeWireCoreModule(activitCallback);
        }
        return INSTANCE;
    }
 
    private final ActivityCallback activityCallback;
    private final LimeWireCommonModule limeWireCommonModule;
    private final LifecycleManager lifecycleManager;
    private final DownloadManager downloadManager;
    
    private LimeWireCoreModule(ActivityCallback activitCallback) {
        this.activityCallback = activitCallback;
        limeWireCommonModule = LimeWireCommonModule.instance();
        downloadManager = new DownloadManagerImpl(activitCallback);
        lifecycleManager = new LifecycleManagerImpl(activitCallback, downloadManager, LimeCoreGlue.instance(), limeWireCommonModule.getLimeWireCommonLifecycleModule().getServiceRegistry());
    }
    
    public ActivityCallback getActivityCallback() {
        return activityCallback;
    }
	
	public LimeWireCommonModule getLimeWireCommonModule() {
	    return limeWireCommonModule;
	}
	
	public LifecycleManager getLifecycleManager() {
	    return lifecycleManager;
	}
	
	public DownloadManager getDownloadManager() {
	    return downloadManager;
	}
}