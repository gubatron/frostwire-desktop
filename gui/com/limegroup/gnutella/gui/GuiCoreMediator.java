package com.limegroup.gnutella.gui;

import com.limegroup.gnutella.DownloadManager;
import com.limegroup.gnutella.LifecycleManager;
import com.limegroup.gnutella.LimeWireCore;

public class GuiCoreMediator {
    public static DownloadManager getDownloadManager() {  return LimeWireCore.instance().getDownloadManager(); }
    public static LifecycleManager getLifecycleManager() { return LimeWireCore.instance().getLifecycleManager(); }
}
