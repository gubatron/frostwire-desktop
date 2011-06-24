package com.limegroup.gnutella.gui;

import com.limegroup.gnutella.DownloadManager;
import com.limegroup.gnutella.LifecycleManager;
import com.limegroup.gnutella.LimeWireCore;
import com.limegroup.gnutella.xml.LimeXMLProperties;

// DPINJ:  This is a temporary measure to delay refactoring the GUI.
public class GuiCoreMediator {
    public static DownloadManager getDownloadManager() {  return LimeWireCore.instance().getDownloadManager(); }
    public static LimeXMLProperties getLimeXMLProperties() { return LimeWireCore.instance().getLimeXMLProperties(); }
    public static LifecycleManager getLifecycleManager() { return LimeWireCore.instance().getLifecycleManager(); }
}
