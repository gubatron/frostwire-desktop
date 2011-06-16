package com.limegroup.gnutella;

import java.io.File;
import java.util.Set;

import org.limewire.io.IpPort;

import com.frostwire.bittorrent.BTDownloader;
import com.google.inject.Singleton;
import com.limegroup.gnutella.browser.MagnetOptions;
import com.limegroup.gnutella.connection.ConnectionLifecycleEvent;
import com.limegroup.gnutella.search.HostData;
import com.limegroup.gnutella.version.UpdateInformation;

@Singleton
public class ActivityCallbackAdapter implements ActivityCallback {

    public void acceptedIncomingChanged(boolean status) {
        
    }

    public void browseHostFailed(GUID guid) {
        
    }

    public void componentLoading(String component) {
        
    }

    public void fileManagerLoaded() {
        
    }

    public void fileManagerLoading() {
        
    }

    public void handleAddressStateChanged() {
        
    }

    public void handleConnectionLifecycleEvent(ConnectionLifecycleEvent evt) {
        
    }

    public boolean handleMagnets(MagnetOptions[] magnets) {
        return false;
    }
    
    public void handleSharedFileUpdate(File file) {
        
    }

    public void handleTorrent(File torrentFile) {
        
    }

    public void installationCorrupted() {
        
    }

    public void restoreApplication() {
        
    }

    public void setAnnotateEnabled(boolean enabled) {
        
    }

    public void updateAvailable(UpdateInformation info) {
        
    }

    public void uploadsComplete() {
        
    }

    public boolean warnAboutSharingSensitiveDirectory(File dir) {
        return false;
    }

    public void addDownload(Downloader d) {
        
    }
    
    public void addDownload(BTDownloader d) {
        
    }

    public void downloadsComplete() {
        
    }

    public String getHostValue(String key) {
        return null;
    }

    public void promptAboutCorruptDownload(Downloader dloader) {
       
    }

    public void removeDownload(Downloader d) {
        
    }

    public void showDownloads() {
        
    }

	@Override
	public void handleTorrentMagnet(String request) {
	}
}
