package com.frostwire.plugins.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Observer;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.setting.StringSetting;

import com.frostwire.plugins.models.IPlugin;
import com.frostwire.plugins.models.InstallObserver;
import com.frostwire.plugins.models.Plugin;
import com.frostwire.plugins.models.PluginDownloader;
import com.frostwire.plugins.models.PluginInstallException;
import com.frostwire.plugins.models.PluginValidator;
import com.limegroup.gnutella.settings.PluginsSettings;


/**
 * This class will be in charge of managing the following tasks:
 * 
 * - Detection of available plugins
 * - Installing/Uninstalling Plugins
 * - Checking for plugin updates
 * - Working with the PluginLoader to execute plugins
 * - In the future connecting to a web service and fetching information
 *   about plugins on a directory (searching if necessary)
 * 
 * @author gubatron
 *
 */
final public class PluginManager {
	private static final Log LOG = LogFactory.getLog(PluginManager.class);
	
	private static PluginManager INSTANCE;
	
	public final static File PLUGINS_FOLDER = 
		new File(PluginsSettings.PLUGINS_FOLDER.getValue());
	
	private Hashtable<String,IPlugin> AVAILABLE_PLUGINS; //all the plugins on the plugin folder
	private Hashtable<String,IPlugin> INSTALLED_PLUGINS; //all plugins installed
	private Hashtable<String,IPlugin> RUNNING_PLUGINS; //plugins currently being executed
	
	private Hashtable<String,PluginDownloader.PluginDownloaderWorker> DOWNLOADER_THREADS; //keep downloading threads here while they're running

	/** Get a hold of the PluginManager singleton */
    public static PluginManager getInstance() {
    	if (INSTANCE == null) {
    		INSTANCE = new PluginManager();
    	}
    	return INSTANCE;
    } //getInstance
    public Hashtable<String,IPlugin> getAvailablePlugins() { return AVAILABLE_PLUGINS; }
	public Hashtable<String,IPlugin> getInstalledPlugins() { return INSTALLED_PLUGINS; }
	public Hashtable<String,IPlugin> getRunningPlugins() { return RUNNING_PLUGINS; }
	
	private PluginManager() {
		LOG.debug("Created Plugin Manager Instance");
	}
	
	/**
	 * Clears from memory the remote available plugins.
	 */
	public void clearAvailablePlugins() { 
		AVAILABLE_PLUGINS = null;
        File f = new File(PluginsSettings.AVAILABLE_PLUGINS_FILE.getValue());
        if (f.exists())
            try { f.delete(); } catch (Exception e) {}		
	} //clearAvailablePlugins
	
	public void clearInstalledPlugins() { 
		INSTALLED_PLUGINS = null; 
		File f = new File(PluginsSettings.INSTALLED_PLUGINS_FILE.getValue());
		if (f.exists())
			try { f.delete(); } catch (Exception e) {}
	} //clearInstalledPlugins
	
    /**
     * Get a Object<String> array that contains the names of the files
     * that end with 'type' on the given folderPath
     *
     * e.g
     *
     * String[] jarFiles = getFiles(".",".jar");
     *
     */
    private final Object[] getFiles(String folderPath, String type) {
        File f = new File(folderPath);

        String[] files = f.list();

        Vector results = new Vector();

        for (int i=0; i < files.length; i++) {
            if (files[i].endsWith(type)) {
                results.add(files[i]);
            }
        }

        if (results.size() == 0)
            return null;

        return results.toArray();
    } //getFiles
    
    private Object[] getPossiblePluginFileNames() throws java.io.IOException {
        try {
            Object[] jars = getFiles(PLUGINS_FOLDER.getCanonicalPath(),".jar");
            Object[] zips = getFiles(PLUGINS_FOLDER.getCanonicalPath(),".zip");
            Object[] plugins = getFiles(PLUGINS_FOLDER.getCanonicalPath(),".plugin");

            Vector<String> candidates = new Vector();

            if (jars != null) 
                for (Object jarName : jars)
                    candidates.add((String) jarName);
            

            if (zips != null) 
                for (Object zipName : zips)
                    candidates.add((String) zipName);

            if (plugins != null)
                for (Object pluginName : plugins)
                    candidates.add((String) pluginName);

            if (candidates.size()==0)
                return null;
            
            return candidates.toArray();
            
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        }        
    }
	
	/**
     * To avoid code repetition, this function will read from a file
     * a Hashtable that holds the representation of plugins to be used.
     * These could be Installed Plugins, or valid Available Plugins (remote list)
     */
    private Hashtable readPlugins(StringSetting PLUGINS_FILE) {
    	Hashtable PLUGINS;
		try {
		    File f = new File(PLUGINS_FILE.getValue());
		    System.out.println("PluginManager.readPlugins() Plugins file ->" + f.getCanonicalPath());
		    if (f.exists() && f.length() <= 0) {
		        LOG.error("The INSTALLED_PLUGINS File is brand new. 0 bytes.");
		        PLUGINS=null;
		        return null;
		    }
			FileInputStream input = new FileInputStream(PLUGINS_FILE.getValue());
			ObjectInputStream objectStream = new ObjectInputStream(input);
			PLUGINS = (Hashtable) objectStream.readObject();
		} catch (Exception e) {
		    e.printStackTrace();
			LOG.error("The INSTALLED_PLUGINS Hashset could not be loaded due to " + e.getMessage());
			PLUGINS = null;
		}
		return PLUGINS;
    } //readPlugins

    /**
     * Analog to readPlugins() but for writing to disk the given list of plugins
     */
    private void writePlugins(Hashtable PLUGINS, StringSetting PLUGINS_FILE) {
		if (PLUGINS == null || 
			PLUGINS.size() == 0) {
			//nothing to save.
			return;
		}
		
		//make sure there is a file where to write the plugins
		File f = new File(PLUGINS_FILE.getValue());
		if (!f.exists()) {
			//create new file
			try { f.createNewFile(); } catch (Exception e) { LOG.error(e);}
		} else {
			//reset it
			f.delete();
			
			//since files are immutable objects
			f = new File(PLUGINS_FILE.getValue());
			try { f.createNewFile(); } catch (Exception e) { LOG.error(e);}
		}

		try {
			FileOutputStream output = new FileOutputStream(PLUGINS_FILE.getValue());
			ObjectOutputStream objectStream = new ObjectOutputStream(output);
			
			objectStream.writeObject((Hashtable) PLUGINS);
			objectStream.flush();
			objectStream.close();
			output.flush();
			output.close();
		} catch (Exception e) {
			System.out.println(e);
			LOG.error("The Hashset could not be written due to " + e.getMessage());
		}
    } //writePlugins


    /**
     * Given the single name or complete name of the plugin file.
     * Say, if you have a plugin called: "rssReader"
     * 
     * It could exist as a file named "rssReader.jar" or
     * "rssReader.zip" or "rssReader.plugin"
     * 
     * It'll look for the plugin on the default PLUGIN_FOLDER
     *
     * @param name
     * @return
     */
    public boolean pluginExists(String name) {
        verifyPluginEnvironment();
        String plugins_folder_path = null;
        String candidate_path = null;
        
        if (name == null || name.equals("")) {
            return false;
        }
        
        try { 
            plugins_folder_path = PLUGINS_FOLDER.getCanonicalPath(); 
        } catch (java.io.IOException e) {
            return false;
        }
        
        //They gave us a full plugin file name
        if (name.endsWith(".jar") ||
            name.endsWith(".zip") ||
            name.endsWith(".plugin")) {
            
            candidate_path = plugins_folder_path + File.separator + name;
            File f = new File(candidate_path);
            
            return f.exists();
        } 

        //They give us a name with no extension, we try them all.
        String[] suffixes = new String[] {".jar",
                                          ".zip",
                                          ".plugin"};

        for (String suffix : suffixes) {
            candidate_path = plugins_folder_path +
                File.separator + name + suffix;

            File f = new File(candidate_path);
            if (f.exists()) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isPluginInstalled(IPlugin p) {
        if (p == null)
            return false;
        
        verifyInstalledPlugins();
        
        if (INSTALLED_PLUGINS == null ||
            INSTALLED_PLUGINS.size() == 0)
            return false;
        
        return INSTALLED_PLUGINS.containsKey(p.getName());
    }
    
    /**
     * Loads the INSTALLED_PLUGINS Hashtable from disk.
     */
	public void readInstalledPlugins() {
        INSTALLED_PLUGINS = readPlugins(PluginsSettings.INSTALLED_PLUGINS_FILE);
	} //readInstalledPlugins

    /**
     * Saves the Hashtable that represents the installed Plugins.
     */
	public void writeInstalledPlugins() {
		writePlugins(INSTALLED_PLUGINS, PluginsSettings.INSTALLED_PLUGINS_FILE);
	} //writeInstalledPLugins

	
	/**
     * Loads the AVAILABLE_PLUGINS Hashtable from disk if available.
     */
    public void readAvailablePlugins() {
        AVAILABLE_PLUGINS = readPlugins(PluginsSettings.AVAILABLE_PLUGINS_FILE);
    } //readAvailablePlugins

    /**
     * Saves the Hashtable that represents all the remotely available
     * valid plugins we could install on this instance of FrostWire
     */
    public void writeAvailablePlugins() {
        writePlugins(AVAILABLE_PLUGINS, PluginsSettings.AVAILABLE_PLUGINS_FILE);
    } //writeValidPlugins
	
    
    /**
     * Makes sure that:
     * - A plugins folder exists
     */
    public void verifyPluginEnvironment() {
    	//make sure plugin folder exists
    	if (!PLUGINS_FOLDER.exists()) {
    		PLUGINS_FOLDER.mkdir();
    		
    		//in this case we know for a fact, nobody has been installed
    		clearInstalledPlugins();
    	}
    	
    	if (DOWNLOADER_THREADS == null) {
    	    DOWNLOADER_THREADS = new Hashtable<String, PluginDownloader.PluginDownloaderWorker>();
    	}
    
    	/** NOT SURE ANYMORE THIS SHOULD HAPPEN HERE:
    	//We should scan for plugins available remotely and add them to our
    	//AVAILABLE_PLUGINS hashtable.
    	if (refreshRemotePlugins || 
            PluginManager.getInstance().AVAILABLE_PLUGINS == null ||
    		PluginManager.getInstance().AVAILABLE_PLUGINS.size() == 0) {
            PluginManager.getInstance().checkForAvailablePluginsRemotely();
    	}
    	*/
    } //verifyPluginEnviroment
    /**
     * Checks if the application has any plugins installed.
     * If none has been installed, it'll create the INSTALLED_PLUGINS_FILE
     * for future checks.
     * 
     * If INSTALLED_PLUGINS_FILE exists it'll double check on disk
     * that the plugins that are supposed to be installed are still on disk.
     * If a plugin is not found on disk, it's removed from INSTALLED_PLUGINS. 
     *
     * NOTE:Not sure if this will send any notifications to a UI component later
     */
	public void verifyInstalledPlugins() {
		File f = new File(PluginsSettings.INSTALLED_PLUGINS_FILE.getValue());
		
		//It's official, no plugins are considered to be installed
        //We gotta create the file where to keep track what's been installed
		if (!f.exists()) {
			INSTALLED_PLUGINS = null;
			try { f.createNewFile(); } catch (Exception e) {}
			f = new File(PluginsSettings.INSTALLED_PLUGINS_FILE.getValue());
			//System.out.println("PluginManager.checkForInstalledPlugins() - Nothing installed initializing INSTALLED_PLUGINS_FILE");
			return;
		} else {
		    readInstalledPlugins(); 
       }

        if (INSTALLED_PLUGINS == null)
            INSTALLED_PLUGINS = new Hashtable<String, IPlugin>();
		
		boolean installedPluginsChanged = false;
		
		if (getInstalledPlugins() != null) {
			Set<Map.Entry<String,IPlugin>> iPlugins = getInstalledPlugins().entrySet();
			Iterator<Map.Entry<String, IPlugin>> iterator = iPlugins.iterator();
			while (iterator.hasNext()) {
			    
				Map.Entry<String, IPlugin> entry = iterator.next();
				//System.out.println("PluginManager.checkForInstalledPlugins() got entry -> " + entry.getKey());
				
				//Check if the plugin still exists on disk
				if (!pluginExists(entry.getKey())) {
				    installedPluginsChanged = true;
				    getInstalledPlugins().remove(entry.getKey());
				}
			}
		}
		
		//If a plugin was deleted, then we save the record we have of INSTALLED
		//plugins back on disk
		if (installedPluginsChanged) {
		    writeInstalledPlugins();
		}

	} //verifyInstalledPlugins()
	
	/**
	 * Checks on disk for what plugins are available.
	 * The idea behind this method, would be to check
     * If the (advanced) user has dragged and dropped a plugin
     * file to the plugins folder, and this plugin doesn't
     * appear on our INSTALLED_PLUGINS.
     */
	public void checkForAvailablePluginsLocally(boolean clearAvailablePlugins) 
	throws java.io.IOException{
	    //Scan the PLUGINS_FOLDER
	    verifyPluginEnvironment();

	    if (clearAvailablePlugins) {
	        clearAvailablePlugins();
	    }
	    
	    Object[] fileNames = getPossiblePluginFileNames();
	    
        if (fileNames == null)
            return;
	    
	    System.out.println("PluginManager.checkForAvailablePluginsLocally() - Total possible plugin filenames -> " + fileNames.length);
	    
	    //easy case, just add what you find to available plugins
	    //we haven't checked remote plugins yet
	    if (getAvailablePlugins()==null ||
	        getAvailablePlugins().size()==0) {

	        for (Object fileName : fileNames) {
	            String pluginPath = PLUGINS_FOLDER.getCanonicalPath() + File.separator + fileName; 
	            Plugin p = Plugin.loadFromFile(pluginPath);
	            if (p != null && p.isValid()) {
	                if (getAvailablePlugins()==null)
	                    AVAILABLE_PLUGINS = new Hashtable<String, IPlugin>();
	                getAvailablePlugins().put(p.getName(), p);
	            }
	        }
	        
	    } else {
	        //in case there were some plugins available already
	        //e.g. we checked remote plugins before the ones we have here
	    }
	    
	    //save
	    if (getAvailablePlugins()!=null &&
	        getAvailablePlugins().size() > 0)
	        writeAvailablePlugins();	    
	} //checkForAvailablePluginsLocally
	
	/**
	 * Checks remotely for the list of available plugins.
	 * It will get a list of available plugins and it'll update
	 * the AVAILABLE_PLUGINS Hashtable with what it finds.
	 * 
	 * 
	 */
	public void checkForAvailablePluginsRemotely(boolean clearAvailablePlugins) {
		//Download list of available plugins
		PluginValidator.getInstance().refreshRemoteAvailablePlugins();

		//If you want to start clean or we had none available
		//it's simple, we just assign.    
	    if (clearAvailablePlugins || getAvailablePlugins() == null) {
	        PluginManager.getInstance().clearAvailablePlugins();
	        AVAILABLE_PLUGINS = PluginValidator.getInstance().getAvailablePlugins();
	    } else {
	        Hashtable<String, IPlugin> available = PluginValidator.getInstance().getAvailablePlugins();
	        
	        if (available!=null && 
	            available.size() > 0 &&
	            AVAILABLE_PLUGINS == null)
	            AVAILABLE_PLUGINS = new Hashtable();
	        
            Set<Map.Entry<String,IPlugin>> iPlugins = getAvailablePlugins().entrySet();
            Iterator<Map.Entry<String, IPlugin>> iterator = iPlugins.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, IPlugin> entry = iterator.next();
                AVAILABLE_PLUGINS.put(entry.getKey(),entry.getValue());
            }
	    }
            
        if (getAvailablePlugins()!=null)
            writeAvailablePlugins();
	} //checkForAvailablePluginsRemotely
	
	/**
	 * Downloads a plugin from the given url and saves it to the
	 * PLUGINS_FOLDER. It'll overwrite if the file exists already.
	 * @param pluginUrl
	 */
	public void downloadPlugin(String pluginUrl) {
	    
	}
	
	
	/** 
     * Installs a plugin.
     * Returns an InstallObserver object if it has to download the plugin.
     * Returns null if it's done with the installation.
     * @param p - a valid IPlugin object.
     * @param observers - optional. A Collection of observing objects that might
     *                              want to know about the download, or
     *                              installation process.
     *                              There's no need to pass a InstallObserver
     *                              object on this collection, one will be
     *                              created by this method. You'll only need
     *                              to provide with UI level observers, or others.
	 * 
     */
	public InstallObserver installPlugin(IPlugin p, Collection<Observer> observers) throws PluginInstallException {
		//make sure plugin is valid
		if (!p.isValid()) 
			throw new PluginInstallException("Plugin " + p.getName() +" invalid");
		
		getInstance().verifyPluginEnvironment();  
		
		//If the jar is not here already, then download it and put the worker
		//on the DOWNLOADER_THREADS HashSet.
		if (!pluginExists(p.getName())) {
		    InstallObserver installObserver = new InstallObserver(p);
		    
		    if (observers == null) {
		        observers = new HashSet<Observer>();
		    }
		    
		    observers.add(installObserver);
		    
		    //Start a downloader and put it's worker thread on a hashset
		    //so it won't die after this method is finished.
		    tryCancellingPreviousPluginDownload(p);
		    
		    PluginDownloader pd = new PluginDownloader(p, observers);
		    try {
		        pd.connect();
		    } catch (IOException ioe) {
		        System.out.println("PluginManager.installPlugin() - My downloader could not connect.");
		        System.out.println(ioe.getMessage());
		        pd.sendNotification(PluginDownloader.DownloadState.CANCELLED_DOWNLOADING);
		        return null;
		    }

		    PluginDownloader.PluginDownloaderWorker worker = pd.startDownload();
		    DOWNLOADER_THREADS.put(p.getName(),worker);
		    
		    //return the observer to monitor the download and the install process.
		    return installObserver;
		}
		
		//If we make it here, it means the Jar is available on disk.
		
		//Try to remove any DOWNLOADER_THREADS If any.
		System.out.println("installPlugin() - This should only appear after the download is done. Try Cancelling NEXT");
		tryCancellingPreviousPluginDownload(p);
		
		//Mark the plugin as installed
		verifyInstalledPlugins();
		INSTALLED_PLUGINS.put(p.getName(), p);
		writeInstalledPlugins();
		System.out.println("installPlugin() - Installed Plugins should be updated");
		
		//IDEA: for the future
		//if (p.needsRestartAfterInstall())
		//    display advice to restart for the plugin to take effect
		//    will have to add additional attributes to meta info
		
		//maybe run it?
		runPlugin((Plugin) p);
		
		return null;
	} //installPlugin
	
	/**
	 * If you need to stop a plugin download, invoke this mehtod.
	 * 
	 * This will look up the PluginDownloader.PluginDownlaoderWorker thread
	 * and tell it to stop, and remove it from the DOWNLOADER_THREADS Hashtable.
	 * @param p
	 */
	public void tryCancellingPreviousPluginDownload(IPlugin p) {
	    System.out.println("PluginManager::tryCancellingPreviousPluginDownload("+p.getName()+")");
        if (DOWNLOADER_THREADS !=null &&
            DOWNLOADER_THREADS.containsKey(p.getName())) {
            //we might have not wiped out an old downloading thread
            //or the user might want to restart a download of a plugin.
            PluginDownloader.PluginDownloaderWorker worker =
                DOWNLOADER_THREADS.get(p.getName());
            
            synchronized (worker) {
                worker.cancelDownload();
                worker.interrupt();
                System.out.println("PluginManager::tryCancellingPreviousPluginDownload("+p.getName()+") Cancelled. (still not removed)");
            }
            
            synchronized (DOWNLOADER_THREADS) {
                DOWNLOADER_THREADS.remove(p.getName());
            }
        } else {
            System.out.println("PluginManager::tryCancellingPreviousPluginDownload("+p.getName()+") Nothing to cancel.");
        }
	}

	/**
	 * TODO
	 * @param p
	 */
	public void uninstallPlugin(IPlugin p) {
	    if (p == null)
	        return;

        //make sure its not anymore in INSTALLED_PLUGINS
        readInstalledPlugins();
        if (INSTALLED_PLUGINS.containsKey(p.getName())) {
            INSTALLED_PLUGINS.remove(p.getName());
        }
        writeInstalledPlugins();
	    
	    
	    //stop it if its running
	    stopPlugin((Plugin) p);
	    
	    //we'll leave it on disk
	    
	}
	
	/**
	 * Executes a Plugin.
	 * If its already running, it will, stop it and restart it.
	 * 
	 * @param p
	 */
	public void runPlugin(Plugin p) {
	    if (p == null ||
	        !pluginExists(p.getName()) || 
	        !p.isValid()) 
	        return;
	    
	    if (RUNNING_PLUGINS == null) {
	        RUNNING_PLUGINS = new Hashtable<String, IPlugin>();
	        System.out.println("PluginManager.runPlugin("+p.getName()+") first plugin to run.");
	    }
	    
	    if (RUNNING_PLUGINS.containsKey(p.getName())) {
	        stopPlugin(p);
	    } 
	    
	    //yet to be seen...
	    System.out.println("PluginManager.runPlugin(p) -> p.start()!");
	    RUNNING_PLUGINS.put(p.getName(), p);
	    
        try {
            System.out.println("PluginLoader.getInstance.runPlugin(ME!)");
            PluginLoader.getInstance().runPlugin(p);
        } catch (Exception e) {
            e.printStackTrace();
        }	    
	    
	} //runPlugin
	
	/** Stops a plugin.
	 * Removes it from RUNNING_PLUGINS,
	 * and if its being downloaded for an update we try to stop the download
	 * just in case.
	 */
	public void stopPlugin(Plugin p) {
		//Make sure its not in RUNNING_PLUGINS any more.
		if (RUNNING_PLUGINS!=null &&
			RUNNING_PLUGINS.containsKey(p.getName())) {
		    
			RUNNING_PLUGINS.remove(p.getName());
		}

		//In case a download for this plugin was happening
		tryCancellingPreviousPluginDownload(p);
		
        //Have the PluginLoader tell the PyObject to stop before we destroy him
        PluginLoader.getInstance().stopPlugin(p);
	} //stopPlugin
	
} //PluginManager