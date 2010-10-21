package com.frostwire.plugins.tests;

import java.io.File;

import junit.framework.TestCase;

import com.frostwire.plugins.controllers.PluginManager;
import com.frostwire.plugins.models.Plugin;
import com.limegroup.gnutella.settings.PluginsSettings;

public class PluginManagerTest extends TestCase {
    
    private PluginManager PLUGIN_MANAGER;
    private String PLUGIN_FOLDER = PluginsSettings.PLUGINS_FOLDER.getValue();
    
	public PluginManagerTest(String name) {
		super(name);
	}
	
	public void setUp() {
	    PLUGIN_MANAGER = PluginManager.getInstance();
	}
	
	
	/**
	 * This test, runs a full installation.
	 * It gets the available plugins remotely.
	 * Download and Installs brooklyn.jar plugin
	 * Deletes the jar from disk
	 * And then when we ask if the plugin is still installed
	 * the verifyInstalledPlugin comes in and saves the day by
	 * automatically removing the deleted plugin from our list of installed
	 * plugin
	 */
	/*
	public void testDownloadInstallDeleteAndVerify() {
       System.out.println("\n=========");
       System.out.println("PluginManagerTest.testDownloadInstallDeleteAndVerify()");
       System.out.println("=========");
	        
	    if (PLUGIN_MANAGER.pluginExists("brooklyn")) {
	        File brooklynJar = new File(PLUGIN_FOLDER + File.separator + "brooklyn.jar");
	        brooklynJar.delete();
	        System.out.println("testDownloadInstallDeleteAndVerify() - Deleting old jar.");
	    }
	    
	    PLUGIN_MANAGER.checkForAvailablePluginsRemotely(false);//dont clear em
	    
	    if (PLUGIN_MANAGER.getAvailablePlugins().containsKey("brooklyn")) {
	        try {
	            MetaPlugin brooklynPlugin = (MetaPlugin) PLUGIN_MANAGER.getAvailablePlugins().get("brooklyn");
	            
	            System.out.println("testDownloadInstallDeleteAndVerify() Got the brooklynPlugin on available ->\n"+brooklynPlugin.asXML()+"\n");
	            InstallObserver observer = PLUGIN_MANAGER.installPlugin(brooklynPlugin, null);
	            System.out.println("testDownloadInstallDeleteAndVerify() - Started install (download).");
	            
	            assert(observer!=null);
	            
	            while (!observer.isDone()) {
	                System.out.println("Let's wait for plugin to finish downloading...");
	                Thread.currentThread().sleep(1000);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            fail(e.getMessage());
	        }
	        
	        //make sure it's installed
	        try {
    	        File brooklynJar = new File(PLUGIN_FOLDER + File.separator + "brooklyn.jar");
    	        Plugin p = Plugin.loadFromFile(brooklynJar.getCanonicalPath());
    	        assertNotNull(p);
    	        assertTrue(PLUGIN_MANAGER.isPluginInstalled(p));
    	        
    	        //deleting it should automatically uninstall it (when we ask for it)
    	        
    	        brooklynJar.delete();
    
    	        assertFalse(brooklynJar.exists());
    	        assertFalse(PLUGIN_MANAGER.isPluginInstalled(p));
	        } catch (Exception e) {
    	        fail(e.getMessage());
    	    }
	    }
	}
	*/
	
	
	/** Uncomment this one if you want it to download and install the jar
	public void testPluginExistsAndInstall() {
	    System.out.println("\n=========");
	    System.out.println("PluginManagerTest.testPluginExists()");
	    System.out.println("=========");
        
	    assertFalse(PLUGIN_MANAGER.pluginExists("bubu"));

	    PLUGIN_MANAGER.checkForAvailablePluginsRemotely(false);
        MetaPlugin brooklyn = (MetaPlugin) PLUGIN_MANAGER.getAvailablePlugins().get("brooklyn");
	    
	    if (!PLUGIN_MANAGER.pluginExists("brooklyn.jar")) {
	        if (PLUGIN_MANAGER.getAvailablePlugins().containsKey("brooklyn")) {
	            
	           try {
	            assertNotNull(brooklyn);
	            InstallObserver observer = PLUGIN_MANAGER.installPlugin(brooklyn, null);
	            assertNotNull(observer);
	            
	            while (!observer.isDone()) {
	                System.out.println("PluginManagerTest.testPluginExists(): Waiting for brooklyn.jar to download.");
	                Thread.currentThread().sleep(1000);
	            }
	            
	            assertTrue(PLUGIN_MANAGER.pluginExists("brooklyn.jar"));
	            assertTrue(PLUGIN_MANAGER.pluginExists("brooklyn"));
	            
	           } catch (Exception e) {
	               fail("PluginManagerTest.testPluginExists(): " + e.getMessage());
	           }
	        }
	    }
	    assertTrue(PLUGIN_MANAGER.pluginExists("brooklyn.jar"));
	    assertTrue(PLUGIN_MANAGER.pluginExists("brooklyn"));
	    assertFalse(PLUGIN_MANAGER.pluginExists("brooklyn.plugin"));
	}
	*/
	
	
	/**
	public void testCheckPluginsAvailableLocally() {
        System.out.println("\n=========");
        System.out.println("PluginManagerTest.testCheckPluginsAvailableLocally()");
        System.out.println("=========");

	    //We always should have brooklyn.jar on disk to perform this test.
	    File brooklynJar = new File(PLUGIN_FOLDER + File.separator + "brooklyn.jar");
	    if (!brooklynJar.exists()) {
	        //fail("testCheckPluginsAvailableLocally can't run the test without brooklyn.jar");
	        return; //abort test
	    }

	    try {
	        PLUGIN_MANAGER.checkForAvailablePluginsLocally(false);
	        //System.out.println(PLUGIN_MANAGER.getAvailablePlugins().size());
	        //PLUGIN_MANAGER.checkForAvailablePluginsRemotely(false);
	        //System.out.println(PLUGIN_MANAGER.getAvailablePlugins().size());
	    } catch (java.io.IOException ioe) {
	        fail(ioe.getMessage());
	    }
	}
	*/
	
	public void testInstallAndRunPlugin() {
        System.out.println("\n=========");
        System.out.println("PluginManagerTest.testInstallAndRunPlugin()");
        System.out.println("=========");

	    
	    if (!PLUGIN_MANAGER.pluginExists("brooklyn.jar")) {
	        System.out.println("Aborting, brooklyn.jar not there");
	        return; //abort
	    }

	    try {
	        PLUGIN_MANAGER.checkForAvailablePluginsLocally(false);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    if (PLUGIN_MANAGER.getAvailablePlugins().containsKey("brooklyn")) {
	        System.out.println("Brooklyn is available!");
	        Plugin p = Plugin.loadFromFile(PLUGIN_FOLDER + File.separator + "brooklyn.jar");
	        
	        if (PLUGIN_MANAGER.isPluginInstalled(p)) {
	            System.out.println("Plugin is installed, let's PLUGIN_MANAGER.runPlugin(p)");
	            PLUGIN_MANAGER.runPlugin(p);
	            //assertNotNull(p.getInterpreterThread());
	            //assertNotNull(p.getPyObject());
	            //assertNotNull(p.getPythonInterpreter());
	            try {
	                int invokedStop = 0;
	                int seconds = 10;
	                while (seconds > 0) {
	                    Thread.currentThread().sleep(1000);
	                    seconds--;
	                    if (seconds < 7) {
	                        if (invokedStop==0) {
	                            PLUGIN_MANAGER.stopPlugin(p);
	                            invokedStop = 1;
	                        }
	                    }
	                    System.out.println(seconds + "...");
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        } else {
	            System.out.println("Plugin is not installed. Installing. Run Test again please.");
	            try {
	                PLUGIN_MANAGER.installPlugin(p,null);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    
	}
	
}