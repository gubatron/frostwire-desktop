package com.frostwire.plugins.controllers;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import com.frostwire.plugins.models.MissingStartMethodException;
import com.frostwire.plugins.models.Plugin;
import com.limegroup.gnutella.settings.PluginsSettings;

/**
 * This guy is in charge of executing the plugins.
 * It'll probably borrow a lot from the Jython Interpreter.
 * 
 * It should be able to
 * - run plugins
 * - kill plugin execution
 * 
 * @author gubatron
 *
 */
public class PluginLoader {
	
	private static final Log LOG = LogFactory.getLog(PluginLoader.class);
	
	private static PluginLoader INSTANCE;

	private static String PLUGINS_FOLDER = PluginsSettings.PLUGINS_FOLDER.getValue();
	
	private Hashtable<String,Plugin> loadedPlugins;
	
	/** Hashset of URLs so that we don't load a jar twice */
	private HashSet<URL> loadedJars;
	
	/** Hashtable of URLClassLoaders for each of the jars loaded */
	private Hashtable<String, URLClassLoader> classLoaders;
	
	
	private PluginLoader() {
		loadedJars = new HashSet<URL>();
		classLoaders = new Hashtable<String, URLClassLoader>();
	}

	public static PluginLoader getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PluginLoader();
		}

		return INSTANCE;
	} //getInstance
	
	
	/**
	 * Loads all the plugins that are already on disk.
	 * This is probably meant to be called only once, when the application
	 * is being started. This should probably be used on a thread so it won't
	 * slow down the application startup time.
	 */
	public void loadPlugins() {
	
	}
	
	/**
	 * TODO: Create specific exceptions
	 * @param p
	 */
	public Thread runPlugin(final Plugin p) throws Exception {
	    //check first that the plugin exists.
	    File f = new File(PLUGINS_FOLDER + File.separator + p.getName() + ".jar");
	    String pluginJarPath = null;
	    
	    if (!f.exists()) {
	        throw new Exception("PluginLoader.runPlugin() - " + p.getName() + ".jar doesn't exist.");
	    }
	    
	    if (!p.isValid())
	        throw new Exception("PluginLoader.runPlugin() - " + p.getName() + " is not valid.");
	    
	    if (!PluginManager.getInstance().isPluginInstalled(p))
	        throw new Exception("PluginLoader.runPlugin() - " + p.getName() + " is not installed.");

        try {
           pluginJarPath = f.getCanonicalPath();
        } catch (Exception e) {
          System.out.println(e);   
        }

        System.out.println("PluginLoader.runPlugin() - we got a plugin Jar Path - " + pluginJarPath);           

        
        JythonInterpreterThread t = new JythonInterpreterThread(p,pluginJarPath);
        t.start();
        p.setInterpreterThread(t);
	    return Thread.class.cast(t);
	}
	

	/**
	 * This class is the real deal. This is the one that executes runPythonFromJar()
	 * meaning, it'll instanciate a Jython interpreter and the plugin will live on
	 * this thread.
	 * @author gubatron
	 *
	 */
    private class JythonInterpreterThread extends Thread {
        private String pluginPath;
        private Plugin plugin;
        
        public JythonInterpreterThread(Plugin p,String pluginJarPath) {
            plugin=p;
            pluginPath = pluginJarPath;
        }
        
        public void run() {
            try {
                String instanceName = plugin.getName()+"Instance";
                String module = plugin.getName();
                String className = plugin.getName().substring(0,1).toUpperCase() + 
                                   plugin.getName().substring(1).toLowerCase();
                
                String importStatement = "from " + module + " import " + className;
                
                //System.out.println("PluginLoader.runPlugin().Thread.run() pluginPath: " + pluginPath );
                //System.out.println(importStatement);
                PythonInterpreter i = runPythonFromJar(importStatement,pluginPath);
                plugin.setPythonInterpreter(i);

                //create instance of python plugin
                String execThis = instanceName + " = " + className + "()";
                //System.out.println("PluginLoader.Thread.run() - " + execThis);
                i.exec(execThis);
                
                //pass the python plugin to our java plugin
                plugin.setPyObject(i.get(instanceName));
                
                //put our java plugin on the python world
                i.set("pluginFromJavaWorld", plugin);
                
                //copy our java plugin attributes to the python plugin
                i.exec(instanceName+".copyAttributes(pluginFromJavaWorld)");
                
                //start executing the code on the python plugin
                if (plugin.getPyObject().__findattr__("start") == null)
                    throw new MissingStartMethodException("PluginLoader.JythonInterpreterThread.run() - The Python class [" + className + "] does not define a start method.");
                
                i.exec(instanceName + ".start()");
                
            } catch (MissingStartMethodException missing) {
                missing.printStackTrace();
                PluginManager.getInstance().stopPlugin(plugin);
            } catch (Exception e) {
                System.out.println("JythonInterpreter.run() exception: " + e.getMessage());
                e.printStackTrace();
            } 
        } 
    }
	
	/*
	 * Get's a hold of the actual PyObject plugin object and invokes it's stop
	 * method. 
	 */
	public void stopPlugin(Plugin p) {
	    System.out.println("PluginLoader.stopPlugin() - Plugin class name? " + p.getClass().getName());
	    
	    if (p==null) {
	        System.out.println("PluginLoader.stopPlugin() - nothing to stop. p==null");
	        return;
	    }
	    
	    
        PyObject realPlugin = p.getPyObject();
        
        if (realPlugin == null) {
            System.out.println("PluginLoader.stopPlugin() - " + p.getName() + " didn't have a PyObject to stop.");
            return;
        } 
        
        PyObject stopMethod = realPlugin.__findattr__("stop");
        
        if (stopMethod != null) {
            System.out.println("PluginLoader.stopPlugin() - stopMethod.__call__()!");
            stopMethod.__call__();
        }
        else
            System.out.println("PluginLoader.stopPlugin() - " + p.getName() + " PyObject didn't define a stop() method, oh well, at least we tried.");
        
        //Delete the objects we created on the python interpreter when plugin started running.
        p.getPythonInterpreter().exec("print 'Before:',dir()");
        p.getPythonInterpreter().exec("if vars().has_key('pluginFromJavaWorld'): del pluginFromJavaWorld");
        String className = p.getName().substring(0,1).toUpperCase() + p.getName().substring(1);
        p.getPythonInterpreter().exec("if vars().has_key('"+className+"'): del " + className);
        String instanceName = p.getName() + "Instance";
        p.getPythonInterpreter().exec("if vars().has_key('"+instanceName+"'): del " + instanceName);
        p.getPythonInterpreter().exec("print 'After:',dir()");
        
        
        //Kill interpreter
        p.getPythonInterpreter().cleanup();        
	}
	
	
	/**
	 * Returns true if we already loaded this jar.
	 * @param jarUrl
	 * @return
	 */
	public boolean loadedJar(URL jarUrl) {
		return loadedJars.contains((URL) jarUrl);
	} //loadedJar
	

	public boolean loadedJar(String jarPath) throws java.net.MalformedURLException {
		File f = new File(jarPath);
		return loadedJars.contains(f.toURI().toURL());
	} //loadedJar

	
	public HashSet getLoadedJars() {
		return loadedJars;
	}
	
    /**
     * We Extend URLClassLoader so that we can use addURL()
     * @author gubatron
     *
     */
    private class URLPluginClassLoader extends URLClassLoader {
        public URLPluginClassLoader(URL[] urls) {
            super(urls);
        }
        
        public void addURL(URL url) {
            super.addURL(url);
        }
    }	
	
	/**
	 * Adds a given Jar to the Classpath.
	 * Jython uses both classloaders, and it's JYTHONPATH (sys.path)
	 * The classloaders help it find Java classes, in this case inside the jar
	 * that contains the plugin.
	 * 
	 * @param jarFile
     *
	 * Where jarFile is the path (String) to the jarFile. 
	 * 
	 * I won't delete it though, in case it might be needed to load resources from
	 * the jar. However, I believe the adition to the sys.path of jython will be enough.
	 */
	public void addJar2Classpath(String jarFile) throws Exception {
		if (jarFile == null)
			throw new Exception("PluginLoader.addJar2Classpath() - the jar file path can't be null");
		
		File f = new File(jarFile);
		
		if (!f.exists()) {
			LOG.error("Jar doesn't exist ("+jarFile+")");
			throw new Exception("PluginLoader.addJar2Classpath() - The jar file doesn't exist ("+ jarFile +")");
		}
		
		URL jarURL = null;
		
		try {
			jarURL = new URL("jar:file:"+jarFile+"!/");
			//jarURL = f.toURI().toURL();
			LOG.info("The jar as an url " + jarURL);
		} catch (Exception e) {
			LOG.error("Bad URL for jar ("+jarFile+"):\n"+e.toString()+" ("+jarURL+")\n");
			return;
		}
		
		if (loadedJar(jarURL)) {
			LOG.info("Jar was already loaded ("+jarURL+")");
			return;
		}
		
		synchronized (classLoaders) {
		    System.out.println("PluginLoader.addJar2Classpath() Adding Jar to Classpath: ["+jarURL.toString()+"]");
			getLoadedJars().add((URL) jarURL);
		}
		
		classLoaders.put(jarFile, URLClassLoader.newInstance(new URL[] {jarURL}));
		
		LOG.info("Jar loaded ("+jarURL+")");
	} //addJar2Classpath
	

	/**
	 * Given a python script statement, it'll attempt to run it
	 * it will add the jarFile to the classpath and to the 
	 * python path
	 * 
	 * It will attempt to add the given jar to the classpath before
	 * running the python script, since the python script might want to
	 * load resources contained withing the jarFile.
	 * 
	 * MIGHT DO: Make this private as this will be a low level method
	 *           used to run a plugin.
	 *           
	 * @param pythonScriptImportStatement (e.g. "from package import module")
	 * @param jarFile
	 */
	public PythonInterpreter runPythonFromJar(String pythonScriptImportStatement, String jarFile) throws java.net.MalformedURLException, Exception {
		//If this jar hasn't been loaded before, Jython might need its
		//inner java classes available for import. This will make the jar
		//resources available on the java classpath.
		if (!loadedJar(jarFile))
			try {
				addJar2Classpath(jarFile);
			} catch (Exception e) {
			    System.out.println();
				throw e;
			}
		
		try {
			//The Python Interpreter needs to use the classloader of the Jar
			//to be able to find resources within the jar.
		    PySystemState pySys = new PySystemState();
		    URLClassLoader jarClassLoader = (URLClassLoader) classLoaders.get(jarFile);
		    pySys.setClassLoader(jarClassLoader);

		   
		    //We also have to pass the jar to JYTHON_PATH (sys.path)
			//so that it can properly import inner python modules.
			pySys.path.insert(0,new PyString(jarFile));
			
			//We pass the PythonInterpreter the modified PySystemState
			PythonInterpreter i = new PythonInterpreter(null, pySys);
			i.exec(pythonScriptImportStatement);
			return i;
		} catch (Exception e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
			throw e;
		}
	} //runPythonFromJar
	
	public URLClassLoader getClassLoader4JarClasspath(String jarFile) {
		if (classLoaders.isEmpty() ||
			!classLoaders.containsKey(jarFile))
			return null;
		
		return classLoaders.get(jarFile);
	} //getClassLoader4JarClasspath
}