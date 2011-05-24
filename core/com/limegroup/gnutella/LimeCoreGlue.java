package com.limegroup.gnutella;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.limewire.util.CommonUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.limegroup.gnutella.util.FrostWireUtils;

/**
 * This class is the glue that holds LimeWire together.
 * All various components are wired together here.
 */
@Singleton
public class LimeCoreGlue {
	
    private static AtomicBoolean preinstalled = new AtomicBoolean(false);
    private AtomicBoolean installed = new AtomicBoolean(false);
    
    @Inject
    public LimeCoreGlue() {

    }    
    
    /**
     * Wires initial pieces together that are required for nearly everything.
     * 
     * @param userSettingsDir the preferred directory for user settings
     */
    public static void preinstall() throws InstallFailedException {
        preinstall(FrostWireUtils.getRequestedUserSettingsLocation());
    }
    
    /**
     * Wires initial pieces together that are required for nearly everything.
     * 
     * @param userSettingsDir the preferred directory for user settings
     */
    public static void preinstall(File userSettingsDir) throws InstallFailedException {
        // Only preinstall once
        if(!preinstalled.compareAndSet(false, true))
            return;
        
        // This looks a lot more complicated than it really is.
        // The excess try/catch blocks are just to make debugging easier,
        // to keep track of what messages each successive IOException is.
        // The flow is basically:
        //  - Try to set the settings dir to the requested location.
        //  - If that doesn't work, try getting a temporary directory to use.
        //  - If we can't find a temporary directory, deleting old stale ones & try again.
        //  - If it still doesn't work, bail.
        //  - If it did work, mark it for deletion & set it as the settings directory.
        //  - If it can't be set, bail.
        //  - Otherwise, success.
        try {
            CommonUtils.setUserSettingsDir(userSettingsDir);
        } catch(Exception e) {
            throw new InstallFailedException("Settings Directory Failure", e);
        }
    }

    /** Wires all various components together. */
    public void install() {
    	//SIMPP 
    	//if (!ApplicationSettings.USE_SIMPP.getValue()) {
    	//	LOG.debug("No more SIMPP, turned off");
    	//	return;
    	//}
    	
        // Only install once.
        if(!installed.compareAndSet(false, true))
            return;
        
        preinstall(); // Ensure we're preinstalled.
    }
    
    /** Simple exception for failure to install. */
    public static class InstallFailedException extends RuntimeException {
        /**
         * 
         */
        private static final long serialVersionUID = 3989727284459714644L;

        public InstallFailedException() {
            super();
        }

        public InstallFailedException(String message, Throwable cause) {
            super(message, cause);
        }

        public InstallFailedException(String message) {
            super(message);
        }

        public InstallFailedException(Throwable cause) {
            super(cause);
        }
        
    }

}
