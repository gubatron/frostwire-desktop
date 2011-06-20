package com.limegroup.gnutella.gui;

import java.util.concurrent.ScheduledExecutorService;

import org.limewire.io.NetworkInstanceUtils;

import com.google.inject.Inject;
import com.limegroup.gnutella.ActivityCallback;
import com.limegroup.gnutella.ApplicationServices;
import com.limegroup.gnutella.DownloadManager;
import com.limegroup.gnutella.LifecycleManager;
import com.limegroup.gnutella.LimeWireCore;
import com.limegroup.gnutella.LocalFileDetailsFactory;
import com.limegroup.gnutella.NetworkManager;
import com.limegroup.gnutella.SavedFileManager;
import com.limegroup.gnutella.browser.ExternalControl;
import com.limegroup.gnutella.licenses.LicenseVerifier;
import com.limegroup.gnutella.xml.LimeXMLDocumentFactory;
import com.limegroup.gnutella.xml.LimeXMLProperties;
import com.limegroup.gnutella.xml.LimeXMLSchemaRepository;

// DPINJ:  This is a temporary measure to delay refactoring the GUI.
public class GuiCoreMediator {
    
    @Inject private static LimeWireCore core;
    
    public static LimeWireCore getCore() { return core; }
    
    public static NetworkManager getNetworkManager() {  return core.getNetworkManager(); }    
    public static LocalFileDetailsFactory getLocalFileDetailsFactory() {  return core.getLocalFileDetailsFactory(); }
    public static DownloadManager getDownloadManager() {  return core.getDownloadManager(); }
    public static SavedFileManager getSavedFileManager() { return core.getSavedFileManager(); }
    public static LimeXMLSchemaRepository getLimeXMLSchemaRepository() { return core.getLimeXMLSchemaRepository(); }
    public static LimeXMLProperties getLimeXMLProperties() { return core.getLimeXMLProperties(); }
    public static LifecycleManager getLifecycleManager() { return core.getLifecycleManager(); }
    public static ScheduledExecutorService getCoreBackgroundExecutor() { return core.getBackgroundExecutor(); }
    public static ApplicationServices getApplicationServices() { return core.getApplicationServices(); }
    public static ExternalControl getExternalControl() { return core.getExternalControl(); }
    public static ActivityCallback getActivityCallback() { return core.getActivityCallback(); }
    public static LimeXMLDocumentFactory getLimeXMLDocumentFactory() { return core.getLimeXMLDocumentFactory(); }
    public static LicenseVerifier getLicenseVerifier() { return core.getLicenseVerifier(); }
    public static NetworkInstanceUtils getNetworkInstanceUtils() { return core.getNetworkInstanceUtils(); }
}
