package com.frostwire.plugins.models;

import java.util.Observable;
import java.util.Observer;

import com.frostwire.plugins.controllers.PluginManager;
import com.frostwire.plugins.models.PluginDownloader.DownloadState;

/**
 * When the PluginManager starts an installation of a plugin,
 * it may need to have an Observer to check when a Plugin Download
 * has finished.
 * 
 * The Plugin Manager is supposed to pass this Observer as one of the
 * Observers of the PluginDownloader.
 *
 * This simple Observer is supposed to tell the PluginManager
 * to re-execute the Install Method once the plugin jar
 * has been downloaded. The installPlugin() method will mark
 * the plugin as installed if it finds that the plugin jar has been
 * downloaded sucessfully.
 * 
 * @author gubatron
 *
 */
public final class InstallObserver implements Observer {
    public IPlugin plugin;
    public boolean done;
    
    public InstallObserver(IPlugin p) {
        plugin = p;
        done = false;
    }
    
    public void update(Observable o, Object arg) {
        PluginDownloader pd = (PluginDownloader) o;
        PluginDownloader.DownloadState state = 
            (PluginDownloader.DownloadState) arg;
        
        if (state == DownloadState.FINISHED_DOWNLOADING) {
            try {
                PluginManager.getInstance().installPlugin(plugin, null);
            } catch (Exception e) {}
            done = true;
        } else if (state == DownloadState.CANCELLED_DOWNLOADING) {
            done = true;
            //maybe there should be an invocation here to clean stuff
            //with the PluginManager.
        } else if (state == DownloadState.DOWNLOADED_CHUNK) {
           System.out.println("Downloaded %" + 100*(pd.getContentLength()/pd.getDownloadedBytes()) + " of plugin " + plugin.getName());   
        }
    }

    public boolean isDone() {
        return done;
    }
}