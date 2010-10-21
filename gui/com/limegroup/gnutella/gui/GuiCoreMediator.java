package com.limegroup.gnutella.gui;

import java.util.concurrent.ScheduledExecutorService;

import org.limewire.http.LimeHttpClient;
import org.limewire.io.NetworkInstanceUtils;

import com.google.inject.Inject;
import com.limegroup.bittorrent.TorrentManager;
import com.limegroup.gnutella.Acceptor;
import com.limegroup.gnutella.ActivityCallback;
import com.limegroup.gnutella.ApplicationServices;
import com.limegroup.gnutella.BandwidthManager;
import com.limegroup.gnutella.ConnectionManager;
import com.limegroup.gnutella.ConnectionServices;
import com.limegroup.gnutella.DownloadManager;
import com.limegroup.gnutella.DownloadServices;
import com.limegroup.gnutella.FileManager;
import com.limegroup.gnutella.LifecycleManager;
import com.limegroup.gnutella.LimeWireCore;
import com.limegroup.gnutella.LocalFileDetailsFactory;
import com.limegroup.gnutella.NetworkManager;
import com.limegroup.gnutella.ResponseFactory;
import com.limegroup.gnutella.SavedFileManager;
import com.limegroup.gnutella.SearchServices;
import com.limegroup.gnutella.SpamServices;
import com.limegroup.gnutella.UDPService;
import com.limegroup.gnutella.UPnPManager;
import com.limegroup.gnutella.UploadManager;
import com.limegroup.gnutella.UploadServices;
import com.limegroup.gnutella.altlocs.AltLocManager;
import com.limegroup.gnutella.browser.ExternalControl;
import com.limegroup.gnutella.browser.LocalAcceptor;
import com.limegroup.gnutella.browser.LocalHTTPAcceptor;
import com.limegroup.gnutella.chat.ChatManager;
import com.limegroup.gnutella.connection.ConnectionCheckerManager;
import com.limegroup.gnutella.dht.DHTManager;
import com.limegroup.gnutella.downloader.DiskController;
import com.limegroup.gnutella.downloader.RemoteFileDescFactory;
import com.limegroup.gnutella.filters.IPFilter;
import com.limegroup.gnutella.http.HttpExecutor;
import com.limegroup.gnutella.licenses.LicenseVerifier;
import com.limegroup.gnutella.messages.QueryRequestFactory;
import com.limegroup.gnutella.metadata.MetaDataFactory;
import com.limegroup.gnutella.spam.RatingTable;
import com.limegroup.gnutella.spam.SpamManager;
import com.limegroup.gnutella.statistics.TcpBandwidthStatistics;
import com.limegroup.gnutella.uploader.UploadSlotManager;
import com.limegroup.gnutella.xml.LimeXMLDocumentFactory;
import com.limegroup.gnutella.xml.LimeXMLProperties;
import com.limegroup.gnutella.xml.LimeXMLSchemaRepository;
import com.limegroup.gnutella.xml.SchemaReplyCollectionMapper;

// DPINJ:  This is a temporary measure to delay refactoring the GUI.
public class GuiCoreMediator {
    
    @Inject private static LimeWireCore core;
    
    public static LimeWireCore getCore() { return core; }
    
    public static Acceptor getAcceptor() {  return core.getAcceptor(); }    
    public static UDPService getUdpService() {  return core.getUdpService(); }    
    public static NetworkManager getNetworkManager() {  return core.getNetworkManager(); }    
    public static ConnectionManager getConnectionManager() {  return core.getConnectionManager(); }    
    public static DHTManager getDHTManager() {  return core.getDhtManager(); }    
    public static TorrentManager getTorrentManager() {  return core.getTorrentManager(); }    
    public static UploadManager getUploadManager() {  return core.getUploadManager(); }
    public static FileManager getFileManager() {  return core.getFileManager(); }
    public static UploadSlotManager getUploadSlotManager() {  return core.getUploadSlotManager(); }
    public static QueryRequestFactory getQueryRequestFactory() {  return core.getQueryRequestFactory(); }    
    public static DiskController getDiskController() {  return core.getDiskController(); }    
    public static LocalFileDetailsFactory getLocalFileDetailsFactory() {  return core.getLocalFileDetailsFactory(); }
    public static DownloadManager getDownloadManager() {  return core.getDownloadManager(); }
    public static LocalAcceptor getLocalAcceptor() {  return core.getLocalAcceptor(); }
    public static LocalHTTPAcceptor getLocalHTTPAcceptor() {  return core.getLocalHTTPAcceptor(); }
    public static AltLocManager getAltLocManager() {  return core.getAltLocManager(); }
    public static IPFilter getIpFilter() {  return core.getIpFilter(); }
    public static BandwidthManager getBandwidthManager() {  return core.getBandwidthManager(); }
    public static HttpExecutor getHttpExecutor() {  return core.getHttpExecutor(); }
    public static SavedFileManager getSavedFileManager() { return core.getSavedFileManager(); }
    public static UPnPManager getUPnPManager() { return core.getUPnPManager(); }
    public static LimeXMLSchemaRepository getLimeXMLSchemaRepository() { return core.getLimeXMLSchemaRepository(); }
    public static SchemaReplyCollectionMapper getSchemaReplyCollectionMapper() { return core.getSchemaReplyCollectionMapper(); }
    public static LimeXMLProperties getLimeXMLProperties() { return core.getLimeXMLProperties(); }
    public static RatingTable getRatingTable() { return core.getRatingTable(); }
    public static SpamManager getSpamManager() { return core.getSpamManager(); }
    public static LifecycleManager getLifecycleManager() { return core.getLifecycleManager(); }
    public static ConnectionServices getConnectionServices() { return core.getConnectionServices(); }
    public static SearchServices getSearchServices() { return core.getSearchServices(); }
    public static ScheduledExecutorService getCoreBackgroundExecutor() { return core.getBackgroundExecutor(); }
    public static DownloadServices getDownloadServices() { return core.getDownloadServices(); }
    public static UploadServices  getUploadServices() { return core.getUploadServices(); }
    public static ApplicationServices getApplicationServices() { return core.getApplicationServices(); }
    public static SpamServices getSpamServices() { return core.getSpamServices(); }
    public static ConnectionCheckerManager getConnectionCheckerManager() { return core.getConnectionCheckerManager(); }
    public static ExternalControl getExternalControl() { return core.getExternalControl(); }
    public static ActivityCallback getActivityCallback() { return core.getActivityCallback(); }
    public static ChatManager getChatManager() { return core.getChatManager(); }
    public static LimeXMLDocumentFactory getLimeXMLDocumentFactory() { return core.getLimeXMLDocumentFactory(); }
    public static MetaDataFactory getMetaDataFactory() { return core.getMetaDataFactory(); }
    public static LicenseVerifier getLicenseVerifier() { return core.getLicenseVerifier(); }
    public static ResponseFactory getResponseFactory() { return core.getResponseFactory(); }
    public static RemoteFileDescFactory getRemoteFileDescFactory() { return core.getRemoteFileDescFactory(); }
    public static LimeHttpClient getLimeHttpClient() { return core.getLimeHttpClient(); }
    public static TcpBandwidthStatistics getTcpBandwidthStatistics() { return core.getTcpBandwidthStatistics(); }
    public static NetworkInstanceUtils getNetworkInstanceUtils() { return core.getNetworkInstanceUtils(); }
}
