package com.frostwire.plugins.tests;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import junit.framework.TestCase;

import com.frostwire.plugins.models.PluginDownloader;

public class PluginDownloaderTest extends TestCase implements Observer {
    private PluginDownloader downloader;
    private Thread worker;
  
    public void update(Observable o, Object arg) {
        PluginDownloader pd = (PluginDownloader) o;
        PluginDownloader.DownloadState state = 
            (PluginDownloader.DownloadState) arg;
        
        if (state == PluginDownloader.DownloadState.CONNECTION_ESTABLISHED) {
            System.out.println(pd.getPluginShortName() + " downloader has established a connection");
        }

        if (state == PluginDownloader.DownloadState.CONTENT_LENGTH_SET) {
            System.out.println(pd.getPluginShortName() + " is about to download " + pd.getContentLength() + " bytes");
        }
        
        if (state == PluginDownloader.DownloadState.DOWNLOADED_CHUNK) {
            System.out.println(pd.getPluginShortName() + " has downloaded " + pd.getDownloadedBytes() + " bytes");
        }
        
        if (state == PluginDownloader.DownloadState.FINISHED_DOWNLOADING) {
            System.out.println(pd.getPluginShortName() + " has ended.");
        }
    }
    
    public PluginDownloaderTest(String name) {
        super(name);
    }
    
    public void setUp() {
        HashSet<Observer> observers = new HashSet();
        observers.add(this);
        
        downloader = new PluginDownloader("brooklyn",
                "http://newyork1.frostwire.com/frostwire/4.17.2/frostwire-4.17.2.dmg",
                observers);
    }
    
    public void testSimpleDownload() {
        try {
            downloader.connect();
            
            //We need this function to stay alive while the thread is running
            //otherwise the test will end and the thread object will be lost.
            Thread t = downloader.startDownload();
            while (t.isAlive()) {
                Thread.currentThread().sleep(1000);
            }
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException interrupted) {
            interrupted.printStackTrace();
        }
    }

}