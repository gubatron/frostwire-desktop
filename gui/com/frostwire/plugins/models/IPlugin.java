package com.frostwire.plugins.models;

/**
 * FrostWire Plugin base interface
 * @author gubatron
 *
 * All plugins should have a manifest file that declares the
 * following attributes
 * 
 * - Plugin name
 * - Plugin Title (human readable name)
 * - Plugin version
 * - Icon File (still undecided if this should be part of the meta data on the manifest
 *              or if it should be a standard file, which if not present will be suplemented
 *              with a standard default icon)
 * - Minimum FrostWire Supported Version
 * - Last FrostWire Supported Version
 * - Operating systems supported (win,mac,linux)
 * - Author
 * - Organization Name
 * - Website
 * - Download URL: The url where to download the actual plugin (jar)
 * - File Size (bytes)
 * - MD5 checksum
 * 
 * SOME IDEAS:
 * - FrostWire should be able to tell if a plugin comes from FrostWire
 *   or a third party. Our plugins should have more privileges or should come pre-installed.
 * - We probably need to implement a Plugin Debug window to be able
 *   to develop your plugin with nothing but a simple text editor
 *   and frostwire running.
 *   
 * - License
 * - Language / i18n (plugins might have to be translated as well)
 * 
 * Plugins can have several states:
 * - INSTALLED - The plugin has been installed after being discovered by the PluginLoader
 * - DISABLED - The plugin has been installed before, but it's not meant to run by the PluginLoader
 * - RUNNING - The plugin is currently being executed.
 * - STOPPED - The user requested the plugin stops being executed. 
 *             Ideally the user should not see it at all in the UI,
 *             unless it's running (if it's an UI plugin)
 */
public interface IPlugin {
	public String asXML();
	
	/**
	 * The name of the plugin, has to be the same as the name of the jar
	 * pluginName.jar -> pluginName
	 * @return String
	 */
	public String getName();
	public void setName(String s);
	
	public String getMD5Hash();
	public void setMD5Hash(String md5);

	public int getSize();
	public void setSize(int n);
	
	/**
	 * The plugin name to show to the end user
	 * @return
	 */
	public String getTitle();
	public void setTitle(String t);
	
	/**
	 * Returns the version number
	 * Versions have a major.minor format
	 * 
	 * Examples "0.234", "1.2"
	 *
	 * @return
	 */
	public String getVersion();
	public void setVersion(String v);

	
	/**
	 * This is usually the version of FrostWire at the time
	 * the plugin was released.
	 * @return
	 */
	public String getMinimumFrostWireVersionSupported();
	public void setMinimumFrostWireVersionSupported(String v);
	
	
	/**
	 * If not provided, we'll assume it works for the latest FrostWire version.
	 * If the developer realizes his plugin is no longer compatible, he'll
	 * have to notify us while he releases an update.
	 * @return
	 */
	public String getLastFrostWireVersionSupported();
	public void setLastFrostWireVersionSupported(String v);
	
	public String getAuthor();
	public void setAuthor(String v);
	
	public String getOrganization();
	public void setOrganization(String o);
	
	public String getWebsite();
	public void setWebsite(String w);
	
	public String getDownloadURL();
	public void setDownloadURL(String u);
	
    public boolean isValid();
} //Plugin