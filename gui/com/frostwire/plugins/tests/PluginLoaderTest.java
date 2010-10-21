package com.frostwire.plugins.tests;

import java.net.URLClassLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import com.frostwire.plugins.controllers.PluginLoader;
import com.frostwire.plugins.controllers.PluginManager;
import com.limegroup.gnutella.settings.PluginsSettings;

public class PluginLoaderTest extends TestCase {

	private static Log LOG = LogFactory.getLog(PluginLoaderTest.class);
	private PluginManager PM;
	private PluginLoader PL;
	private ExecutorService executorService;
	
	public PluginLoaderTest(String name) {
		super(name);
	}
	
	public void setUp() {
		if (PM!=null)
			return;

		PM = PluginManager.getInstance();
	}

	/**
	 * On this test, the Plugin is instanciated on the PythonInterpreter
	 * And then we ask the object (in the java world) to give us it's
	 * reference to the PluginManager.
	 * 
	 * Then we compare the hashCode of that instance with the one that we've
	 * previously instanciated here in the java world, the hashcodes have
	 * to be the same, otherwise the plugin can't get a hold of the singletons
	 * frostwire has to offer.
	 * 
	 * All this is done on this same thread.
	 */
	
	public void testItCanFetchSingletonsFromMainThread() {
		String hashCode = null;
		try {
			PythonInterpreter i = new PythonInterpreter();
			i.execfile("plugins/test/test.py");
			i.exec("plugin = MyPlugin()");
			PyObject plugin = i.get("plugin");
			
			PyObject plugin_manager = plugin.invoke("getPluginManagerInstance");
			hashCode = plugin_manager.invoke("hashCode").toString();
			//LOG.info("The HashCode received from the Python world - " + hashCode);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		
		assertEquals(hashCode,String.valueOf(PM.hashCode()));
	} //testItCanFetchSingletonsFromMainThread
	

	/**
	 * A Callable task which returns the hashCode of the PluginManager Instance.
	 * @author gubatron
	 *
	 */
	class GetPluginManagerHashCodeCallable implements Callable<String> {
		public String call() {
			String hashCode = null;
			try {
				PythonInterpreter i = new PythonInterpreter();
				i.execfile("plugins/test/test.py");
				i.exec("plugin = MyPlugin()");
				PyObject plugin = i.get("plugin");
				
				PyObject plugin_manager = plugin.invoke("getPluginManagerInstance");
				hashCode = plugin_manager.invoke("hashCode").toString();
				//LOG.info("The HashCode received from the Python world - " + hashCode);
			} catch (Exception e) {
				System.out.println(e);
			}
			System.out.println("CALL() -> " + hashCode);
			return hashCode;
		}
	}
	
	
	/**
	 * Singleton fetching test, done from a Callable task
	 * which runs on a ThreadPool.
	 * 
	 * Compares this thread's Plugin manager reference to the one created on the
	 * executorService thread.
	 *
	 */
	
	public void testItCanFetchSingletonsFromPluginOnSeparateThread() {
		assertNotNull(PM);
		try {

			executorService = new ThreadPoolExecutor(10, 
					20, 
					300, 
					java.util.concurrent.TimeUnit.SECONDS, 
					new SynchronousQueue<Runnable>());
			
			Future<String> pluginFuture = executorService.submit(new GetPluginManagerHashCodeCallable());

			while (pluginFuture.isDone()==false) {
				LOG.info("waiting for future task...");
				Thread.currentThread().sleep(200);
			}
			
			assertNotNull(pluginFuture);
			assertTrue(pluginFuture.isDone());
			assertFalse(pluginFuture.isCancelled());
			
			String resultFromFuture = pluginFuture.get();
			//LOG.info("Result from the future -> ["+resultFromFuture+"]");
			System.out.println("Result from this thread -> ["+String.valueOf(PM.hashCode())+"]");
			System.out.println("Result from the future -> ["+resultFromFuture+"]");
			
			assertNotNull(resultFromFuture);
			assertNotNull(PM.hashCode());
			
			//make sure hashCode of PluginManager instance on Java World == Instance in Plugin World
			assertTrue(resultFromFuture.equals(String.valueOf(PM.hashCode())));
			
		} catch (Exception e) {
			LOG.error(e);
			//e.printStackTrace();
		}
	} //testItCanFetchSingletonsFromPluginOnSeparateThread
	
	
	/**
	 * Shows a message saying [Press Enter] and stops whatever execution
	 * is going on until the user presses enter.
	 */
	private void pressEnter() {
	    // code needed for keyboard input
		System.out.println("[Press Enter]");
	    java.io.BufferedReader br = new java.io.BufferedReader(
	                          new java.io.InputStreamReader(System.in));
	    try { br.readLine(); } catch (Exception e) {}
	}
	
	/**
	 * Instanciates an object from a Java class contained in the jar,
	 * and executes a method on it, using Java Reflexion.
	 */
    
	public void testRunningClassFromDinamicallyAddedJar() throws Exception {
		LOG.info("java.home  = " + System.getProperty("java.home"));
		try { 
			PluginLoader LOADER = PluginLoader.getInstance();
			String FOLDER_PLUGIN = PluginsSettings.PLUGINS_FOLDER.getValue();

			String jar_path = FOLDER_PLUGIN + "/brooklyn.jar";

			//pressEnter();

			LOADER.addJar2Classpath(jar_path);
			URLClassLoader classLoader = LOADER.getClassLoader4JarClasspath(jar_path);

			Class clazz = classLoader.loadClass("brooklyn.HelloFromJar");
			LOG.info("I GOT THE brooklyn.HelloFromJar class " + clazz);
			assertNotNull(clazz);
			java.lang.reflect.Method sayHello = clazz.getMethod("sayHello",(Class[]) null);
			assertNotNull(sayHello);
			Object obj = clazz.newInstance();
			sayHello.invoke(obj,(Object[]) null);
		} catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
			throw e;
		}
	} //testAddingPluginJarsToClasspath
	

	/**
	 * The test.jar will contain a simple plugin
	 * The plugin will try to instanciate a FrostWire mediator
	 * and a class included in the plugin jar as well.
	 */
	
	public void testRunningPluginFromJar() throws Exception {
		PluginLoader LOADER = PluginLoader.getInstance();
		String FOLDER_PLUGIN = PluginsSettings.PLUGINS_FOLDER.getValue();

		//String jar_path = FOLDER_PLUGIN + "/brooklyn.jar";
		String jar2_path = FOLDER_PLUGIN + "/twoScriptsTest.jar";

		try {
			LOADER.runPythonFromJar("from twoScriptsTest.guba import test",jar2_path);
		} catch (Exception e) {
			LOG.error(e);
			throw e;
		}
	} //testRunningPluginFromJar
	
}