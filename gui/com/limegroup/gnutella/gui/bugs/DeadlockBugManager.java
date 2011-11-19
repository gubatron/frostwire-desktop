package com.limegroup.gnutella.gui.bugs;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.util.Version;
import org.limewire.util.VersionFormatException;

import com.frostwire.HttpFetcher;
import com.limegroup.gnutella.gui.LimeWireModule;
import com.limegroup.gnutella.settings.BugSettings;
import com.limegroup.gnutella.util.FrostWireUtils;

public class DeadlockBugManager {
    
    private static final Log LOG = LogFactory.getLog(DeadlockBugManager.class);

   private DeadlockBugManager() {}
    
    /** Handles a deadlock bug. */
    public static void handleDeadlock(DeadlockException bug, String threadName, String message) {
        bug.printStackTrace();
        System.err.println("Detail: " + message);
        
        LocalClientInfo info = LimeWireModule.instance().getLimeWireGUIModule().getLimeWireGUI().getLocalClientInfoFactory().createLocalClientInfo(bug, threadName, message, false);
        // If it's a sendable version & we're either a beta or the user said to send it, send it
        if(isSendableVersion() && (BugSettings.SEND_DEADLOCK_BUGS.getValue())) {
            sendToServlet(info);
        }
    }
    
    /** Determines if we're allowed to send a bug report. */
    private static boolean isSendableVersion() {
        Version myVersion;
        Version lastVersion;
        try {
            myVersion = new Version(FrostWireUtils.getFrostWireVersion());
            lastVersion = new Version(BugSettings.LAST_ACCEPTABLE_VERSION.getValue());
        } catch(VersionFormatException vfe) {
            return false;
        }
        
        return myVersion.compareTo(lastVersion) >= 0;
    }
    
    private static void sendToServlet(LocalClientInfo info) {
        try {
            new HttpFetcher(new URI(BugSettings.BUG_REPORT_SERVER.getValue())).post(info.toBugReport(), "text/plain");
        } catch (Exception e) {
            LOG.error("Error sending bug report", e);
        }
    }
}
