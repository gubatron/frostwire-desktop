package com.frostwire.plugins.models;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.util.OSUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.frostwire.updates.UpdateManager;
import com.limegroup.gnutella.util.LimeWireUtils;

/**
 * Makes sure IPlugins are valid.
 * Helps FrostWire load the list of Plugins suitable for this
 * FrostWire instance.
 * @author gubatron
 *
 * IDEA for Modular Validation:
 *  I've been thinking this class will have to validate
 *  different types of plugins.
 *  
 *  Maybe we'll have:
 *   - MetaPluginContentHandler
 *   - PluginContentHandler
 *   
 *   And this guy doesn't need to implement ContentHandler
 *   ... or maybe it does
 *   but what it does is basically interchange the
 *   MetaPluginContentHandler or PluginContentHandler
 *   depending on what kind of plugin it needs to validate
 */
public class PluginValidator implements ContentHandler {
    
    private static final Log LOG = LogFactory.getLog(PluginValidator.class);
    
    private static PluginValidator INSTANCE;

    /**
     * The URL where to download the list of FrostWire approved
     * plugins.
     * 
     * On this URL there will be an XML stream that contains
     * basic information about the plugins, such as:
     * - Plugin download URL(s) (in case plugin needs to be mirrored)
     * - Plugin name
     * - Plugin version
     * - Plugin description
     * - Plugin size
     * - Plugin author
     * - Plugin's JAR file MD5 hash
     */
    private static String AVAILABLE_PLUGINS_MANIFEST_URL = "http://plugins.frostwire.com/list";

    /** Should store a collection of all the remote plugins that 
     * could be installed on this instance of FrostWire 
     * based on their meta data */

    private static Hashtable AVAILABLE_PLUGINS;

    /**
     * A MetaPlugin instance that we'll reuse for every parsed tag tree
     * that represents a plugin.
     * 
     * A MetaPlugin is a class that holds meta information
     * about a Plugin.
     * 
     * Once you have a valid meta plugin add it to the local list.
     */
    private MetaPlugin bufferMetaPlugin;
    
    private PluginValidator() {}
    
    public static PluginValidator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PluginValidator();
        }
        
        return INSTANCE;
    } //getInstance
    
    /**
     * This method should be invoked probably once, or everytime frostwire reconnects.
     * It's responsible for fetching a list of valid Manifest URLs.
     * Each manifest URL points to an XML manifest of each plugin that's been approved
     * by FrostWire.
     */
    public void refreshRemoteAvailablePlugins() {
        //Read plugins list
        HttpURLConnection connection = null;
        InputSource src = null;
        
        try {
            //this will be the launch URL.
            connection = (HttpURLConnection) (new URL(AVAILABLE_PLUGINS_MANIFEST_URL)).openConnection();

            
            String userAgent = "FrostWire-PluginValidator/" + OSUtils.getOS() + "/" + LimeWireUtils.getLimeWireVersion();
            connection.setRequestProperty("User-Agent",userAgent);
            src = new InputSource(connection.getInputStream());

            XMLReader rdr = XMLReaderFactory.
                createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser" );//"org.apache.xerces.parsers.SAXParser" );

            rdr.setContentHandler(this);
            //
            //IDEA for Future: Move our current parsing methods, which only work
            //                 for the remote list of plugins to a class that
            //                 handles that specific type of XML content
            //                 giving the PluginValidator more flexibility
            //                 to validate different representations of Plugins.
            //rdr.setContentHandler(new MetaPluginListContentHandler(this));

            rdr.parse(src); //as it parses it'll update the list of remote valid plugins
            
            connection.getInputStream().close();
            connection.disconnect();
        } catch (IOException e) {
            System.out.println("PluginValidator.refreshRemoteValidPlugins() IOException " + e.toString());
        } catch (SAXException e2) {
            System.out.println("PluginValidator.refreshRemoteValidPlugins() SAXException " + e2.toString());
        }
    } //refreshRemoteAvailablePlugins

    public Hashtable getAvailablePlugins() {
        return AVAILABLE_PLUGINS;
    }
    
    private boolean validateHash(IPlugin p) {
        //look up this plugin on our list of valid plugins
        
        //calculate jar's md5
        
        //compare against hash from our list of valid plugins
        return true;
    }
    
    /**
     * Validation for a minimum version consists in checking
     * if the current version of FrostWire is older than the minimum
     * version.
     * 
     * If FrostWire is older than the minimum version (version number lower
     * than the minimum) then this method will return false.
     *
     * Examples:
     * 
     * minVersion=4.17.1
     * currentVersion=4.13.5
     * false
     * 
     * minVersion=4.17.1
     * currentVresion=4.17.2
     * true
     * 
     * @param givenMinimumVersion
     * @return
     */
    public static boolean validateMinimumVersion(String minVersion) {
        if (minVersion == null || minVersion.equals("")) {
            return true;
        }        
        return !UpdateManager.isFrostWireOld(minVersion);        
    } //validateMinimumVersion

    /**
     * When the "lastVersion" field is specified, it means that
     * was the lastVersion where the plugin was known to work.
     * 
     * This function checks that FrostWire is not newer than that
     * lastVersion.
     * 
     * In this case we validate that FrostWire is not too new for this
     * plugin.
     * 
     * If frostwire is the old enough, it should be good.
     * 
     * This is not a mandatory parameter, the developer may not know
     * until the plugin breaks.
     * 
     * Examples
     * 
     * [old frostwire]
     * lastVersion=4.17.3
     * currentVersion=4.17.1
     * true
     *
     * [newer frostwire]
     * lastVersion=4.17.3
     * currentVersion=4.18.1
     * false
     * 
     * @param lastVersion
     * @return
     */
    public static boolean validateLastVersion(String lastVersion) {
        if (lastVersion == null || lastVersion.equals("")) {
            return true;
        }
        
        return UpdateManager.isFrostWireOld(lastVersion);
    } //validateLastVersion
    
   
    
    ////////////////////////////////////////////////////////////////
    // METHODS TO IMPLEMENT AS A ContentHandler

    
    /** For convenience */
    
    /**
     * Checks if the attribute exists (in the XML tag), if its there it will return the value of the given
     * attribute name. Otherwise returns null.
     * @param atts
     * @param attributeName
     * @return
     */
    private String tryGettingAttribute(Attributes atts, String attributeName) {
        return (atts.getValue(attributeName) != null) ? atts.getValue(attributeName) : null;
    } //tryGettingAttribute

    /**
     * Out of the Tag attributes, update the buffer MetaPlugin instance
     * as we parse.
     * @param atts
     * @return
     */
    private MetaPlugin tryLoadingMetaPluginFromAttributes(Attributes atts) {
        bufferMetaPlugin = new MetaPlugin();
        bufferMetaPlugin.setName(tryGettingAttribute(atts, "name"));
        bufferMetaPlugin.setTitle(tryGettingAttribute(atts, "title"));
        bufferMetaPlugin.setAuthor(tryGettingAttribute(atts, "author"));
        bufferMetaPlugin.setOrganization(tryGettingAttribute(atts, "organization"));
        bufferMetaPlugin.setVersion(tryGettingAttribute(atts, "version"));
        bufferMetaPlugin.setMinimumFrostWireVersionSupported(tryGettingAttribute(atts, "minimumFrostWireVersion"));
        bufferMetaPlugin.setLastFrostWireVersionSupported(tryGettingAttribute(atts, "lastFrostWireVersion"));
        bufferMetaPlugin.setWebsite(tryGettingAttribute(atts, "website"));
        bufferMetaPlugin.setDownloadURL(tryGettingAttribute(atts, "downloadURL"));
        bufferMetaPlugin.setMD5Hash(tryGettingAttribute(atts, "MD5"));
        bufferMetaPlugin.setSize(tryGettingAttribute(atts,"size"));
        
        return bufferMetaPlugin;
    }
    
    /** When an XML Tag opens */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException { 
        //Deal with <plugin> (This is the remote XML tag on the developer's XML manifest)
        if (localName.equalsIgnoreCase("plugin")) {

            tryLoadingMetaPluginFromAttributes(atts);
            
            if (bufferMetaPlugin != null) {
                if (AVAILABLE_PLUGINS == null) {
                    AVAILABLE_PLUGINS = new Hashtable();
                }
                
                if (bufferMetaPlugin.isValid()) {
                    AVAILABLE_PLUGINS.put(bufferMetaPlugin.getName(),(MetaPlugin) bufferMetaPlugin);
                }
            } //if plugin was loaded
            
        } //if <plugin>
    } //startElement
    
    /** When an XML Tag closes */
    public void endElement(String uri, String name, String qName) throws SAXException {
        //For fernando
    }

    public void characters(char[] ch, int start, int length) throws SAXException { 
        /** Use this probably to read contents inside the tags, can't remember */ 
    }
    
    
    /** you'll probably not need any of these methods to parse, since we'll probably define all tags like this 
     * 
     * <plugins>
     *   <plugin name="myplugin"
     *           title="My Plugin"
     *           ... >
     *           <description><![CDATA[
     *           Description here...
     *            ]]></description>
     *   </plugin>
     *   
     * </plugins>
     * 
     * 
     * 
     * */
    public void endPrefixMapping(String arg0) throws SAXException {/** do nothing */}
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {/** do nothing */}
    public void processingInstruction(String arg0, String arg1) throws SAXException {/** do nothing */}
    public void setDocumentLocator(Locator arg0) { /** do nothing */ }
    public void skippedEntity(String arg0) throws SAXException { /** do nothing */ }
    public void startPrefixMapping(String arg0, String arg1) throws SAXException { /** do nothing */ }
    public void startDocument() throws SAXException { /** do nothing */ }
    public void endDocument() throws SAXException { /** do nothing */ }
} //PluginValidator
