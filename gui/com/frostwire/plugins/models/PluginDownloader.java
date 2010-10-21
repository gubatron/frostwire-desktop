package com.frostwire.plugins.models;

import java.util.Iterator;
import java.util.Observable;
import java.util.Collection;
import java.util.Observer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;

import org.python.modules.synchronize;

import com.limegroup.gnutella.settings.PluginsSettings;

/**
 * This class downloads a plugin from an URL
 * and notifies all possible Observers of the progress of the download.
 * 
 * The download occurs on a thread. As it downloads it sends notifications
 * to the observers.
 * @author gubatron
 *
 */
public final class PluginDownloader extends Observable {
   private String pluginShortName;
   private int contentLength = -1;
   private int downloadedBytes = -1;
   private String pluginUrl = null;
   private HttpURLConnection connection = null;
   private String plugin_folder_path = PluginsSettings.PLUGINS_FOLDER.getValue();
   private File temp; //temp file where to write while we download
   private PluginDownloaderWorker worker;//the thread that will perform the download
   
   //Will send a DownloadState when a notification is sent to the observers
   public enum DownloadState {
       CONNECTION_ESTABLISHED,
       CONTENT_LENGTH_SET,
       DOWNLOADED_CHUNK,
       FINISHED_DOWNLOADING,
       CANCELLED_DOWNLOADING
   }

   public PluginDownloader(IPlugin p, Collection<Observer> observers) {
       this(p.getName(),p.getDownloadURL(), observers);
   }
   
   /**
    * Sets the URL and Prepares Observers if any available
    * @param pluginName - The short name of the plugin
    * @param url - The download url for the plugin
    * @param observers - Optional. Collection of Observer objects
    */
   public PluginDownloader(String pluginName,
                           String url, 
                           Collection<Observer> observers) {
       pluginShortName = pluginName;
       pluginUrl = url;
       
       //If there are observers for this download, let them watch me
       if (observers != null && observers.size() > 0) {
           Iterator<Observer> iterator = observers.iterator();
           while (iterator.hasNext()) {
               addObserver(iterator.next());
               System.out.println("(+) -.- [added observer] ");
           }
       }
   } //constructor

   /**
    * Creates the HttpURLConnection object and tries to establish a connection
    * Will notify all observers if any that a connection has been established/
    * 
    * Sets the content length if available from HTTP Headers
    * 
    * And sets the downloaded byte count to 0
    * @throws IOException
    */
   public void connect() throws IOException {
       try {
           URL url = new URL(pluginUrl);
           connection = (HttpURLConnection) url.openConnection();
           sendNotification(DownloadState.CONNECTION_ESTABLISHED);
           
           contentLength = connection.getContentLength();
           sendNotification(DownloadState.CONTENT_LENGTH_SET);
           
           downloadedBytes = 0;
       } catch (java.io.IOException e) {
           e.printStackTrace();
           throw e;
       }
   } //connect()
   
   /**
    * Non blocking method that will start the download of the 
    * plugin on a thread. As it downloads the chunks it will attempt to
    * notify whatever observer is watching about the current state by sending
    * a DownloadState enum. Then it's up to the observer to ask this
    * PluginDownloader what is it's state.
    * 
    * Also, as it downloads, it'll save the data to a temporary file.
    * eg "myPlugin.jar.temp" on the plugins folder.
    * If we sucessfully finish a download, then we overwrite any old plugin
    * if it's there, otherwise we don't bother.
    * 
    */
   public PluginDownloaderWorker startDownload() {
       temp = new File(plugin_folder_path + File.separator + pluginShortName + ".jar.temp");
       
       try {
           //clear any temp file for this same plugin download
           if (temp.exists())
               temp.delete();

           //create a new one
           temp.createNewFile();
       } catch (Exception e) {
           e.printStackTrace();
           sendNotification(DownloadState.CANCELLED_DOWNLOADING);
       }
       
       worker = new PluginDownloaderWorker();
       worker.start();
       return worker;
   }
   
   public String getPluginShortName() {
       return pluginShortName;
   }
   
   public int getContentLength() {
       return contentLength;
   }
   
   public PluginDownloaderWorker getWorker() {
       return worker;
   }
   
   /**
    * There could be a case where the web server won't be able to tell us
    * the length of the file to be downloaded. In this instance the Observer
    * may have in hand a MetaPlugin instance that knows what the expected size
    * is.
    * 
    * Do not invoke this method once the download has started. You should know
    * in advance what the content length is before you download by all means.
    */
   public void setContentLength(int length) {
       contentLength = length;
   }
   
   public synchronized int getDownloadedBytes() {
       return downloadedBytes;
   }
       
   public void sendNotification(DownloadState state) {
       setChanged();
       if (countObservers() > 0) {
           System.out.println("Sending notification -> " + state);
           notifyObservers(state);
       } else {
           System.out.println("Nobody is watching me");
       }
   }
   
   /**
    * The thread that does the downloading.
    * Put it on the side to include a method to stop the download on demand.
    * @author gubatron
    *
    */
   public class PluginDownloaderWorker extends Thread {
       private boolean keepGoing;
       
       public synchronized void cancelDownload() {
           keepGoing = false;
       }
       
       public void run() {
           keepGoing = true; 
           try {
               BufferedInputStream bis = 
                   new BufferedInputStream(connection.getInputStream());
               BufferedOutputStream bos = new BufferedOutputStream(
                       new FileOutputStream(temp));
               
               byte[] readBuffer = new byte[1024*10];//10k max
               int bytesRead = 0;
               while ((bytesRead = bis.read(readBuffer)) != -1 &&
                       keepGoing) {
                   downloadedBytes += bytesRead;
                   bos.write(readBuffer,0,bytesRead);
                   System.out.println(bytesRead);
                   sendNotification(DownloadState.DOWNLOADED_CHUNK);
               }
               bos.flush();
               bos.close();
               bis.close();
               connection.disconnect();
               
               //let everyone know we're done
               if (downloadedBytes >= getContentLength()) {
                   //now we can overwrite any old plugin and wipe out temp
                   File pFile = new File(plugin_folder_path + File.separator + pluginShortName + ".jar");
                   if (pFile.exists()) {
                       pFile.delete();
                       pFile = new File(plugin_folder_path + File.separator + pluginShortName + ".jar");
                   }
                   
                   //rename temp to new one
                   pFile.createNewFile();
                   temp.renameTo(pFile);
               }
           } catch (java.io.EOFException eof) {
               System.out.println("PluginDownloader.startDownload()::downloadWorker: We reached the end!");
               sendNotification(DownloadState.FINISHED_DOWNLOADING);
           } catch (java.io.IOException ioe) {
               ioe.printStackTrace();
               sendNotification(DownloadState.CANCELLED_DOWNLOADING);
           } catch (Exception e) {
               System.out.println("Unexpected Exception downloading " + pluginShortName);
               e.printStackTrace();
           } finally {               
               if (!keepGoing) {
                   System.out.println("PluginDownloader.run() -> somebody told me to stop!");
                   sendNotification(DownloadState.CANCELLED_DOWNLOADING);
               } else {
                   //let observers know we're donny drako
                   sendNotification(DownloadState.FINISHED_DOWNLOADING);
               }
           }
       }       
   } //PluginDownloaderWorker
   
} //PluginDownloader