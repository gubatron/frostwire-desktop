package com.frostwire.plugins.models;

import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.lang.Thread;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.InputSource;

import com.frostwire.plugins.models.MetaPlugin;
import com.frostwire.plugins.controllers.PluginLoader;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * The Actual Plugin class the user will use to implement
 * it's own plugins.
 * 
 * It extends from MetaPlugin so that it can provide with all
 * the Meta data related functionality.
 * 
 * But it'll go beyond and help the PluginManager on things
 * like validating the integrity of the plugin on disk.
 * 
 * It should provide the user with means to easily:
 * - Create and save settings related to this plugin
 * - Run the plugin (most likely on its own thread using an Executor)
 * - Stop
 * - Communicate with other plugins
 * 
 * @author gubatron
 *
 */
public class Plugin extends MetaPlugin {
    private Thread interpreterThread;
    private PythonInterpreter interpreter;
    private PyObject pyObject;
    
    public Plugin() {
        super();
    }
    
    public void copyAttributes(Plugin p) {
        //Used by the plugin script to copy the attributes known by it's
        //MetaPlugin instance.
        if (p==null) {
          System.out.println("Plugin.copyAttributes() - No attributes were copied, source plugin null");
          return;
        }
        
        setAuthor(p.getAuthor());
        setDownloadURL(p.getDownloadURL());
        setInterpreterThread(p.getInterpreterThread());
        setLastFrostWireVersionSupported(p.getLastFrostWireVersionSupported());
        setMD5Hash(p.getMD5Hash());
        setMinimumFrostWireVersionSupported(p.getMinimumFrostWireVersionSupported());
        setName(p.getName());
        setOrganization(p.getOrganization());
        setPythonInterpreter(p.getPythonInterpreter());
        setTitle(p.getTitle());
        setVersion(p.getVersion());
        setWebsite(p.getWebsite());
        
        System.out.println("Plugin.copyAttributes() - Attributes copied");
    }

    public Thread getInterpreterThread() {
        return interpreterThread;
    }
    
    public void setInterpreterThread(Thread t) { interpreterThread = t; }
    
    public PythonInterpreter getPythonInterpreter() {
        return interpreter;
    }
    
    public void setPythonInterpreter(PythonInterpreter pi) {interpreter = pi; }
    
    public PyObject getPyObject() {
        return pyObject;
    }
    
    public void setPyObject(PyObject po) { pyObject = po; }
    
	/**
	 * Instanciates a Plugin object given a file path to the Jar
	 * that holds the plugin.
	 * 
	 * Mandatory contents for a plugin jar file.
     * 
     * Given a foo.jar plugin, it has to contain:
     * 
     * /meta.xml   (xml metadata manifest to describe this plugin)
     * /foo.py     (main jython module to execute plugin, should define a
     *              Plugin extended class with all the basic methods)
     *
     * ====
     * If the plugin will define python packages, it should have:
     * /package/
     * /package/__init__.py
     * /package/module1.py 
     * /package/module2.py
     * 
     * If you want to use Java classes, you'll also be able to do
     * things like:
     * /com
     * /com/mycompany/
     * /com/mycompany/mypackage/
     * /com/mycompany/mypackage/MyClass.class
     * 
     * ====
     * 
     * And you'll be able to import that class on your python code.
     * The Jython interpreter will add the Jar to its classpath, so it'll
     * be able to find all the java objects available on the classpath.
     * 
     * If you need to add jars, native libraries, or static resources
     * we recommend you add the following polders
     * 
     * /lib
     * /resources
     *
     * If it's not a valid plugin it returns null
	 * @param jarFile - Complete path to the jar file
	 * @return
	 */
	public static Plugin loadFromFile(String jarFile) {
	    File f = new File(jarFile);
	    
	    if (!f.exists())
	        return null;
	    
	    ZipInputStream zis;
	    ZipFile zipFile;
	    
	    try {
	        zis = new ZipInputStream(new FileInputStream(f));
	        zipFile = new ZipFile(jarFile);
        } catch (java.io.FileNotFoundException fnf) {
            return null;
        } catch (java.io.IOException ioe) {
            return null;
        }
            
	    ZipEntry entry;
	    //try to find the meta file
	    while (true) {
	        try {
	           entry  = zis.getNextEntry();
	        } catch (java.io.IOException ioe) {
	            break;
	        }

	        if (entry == null)
	            break;

	        //If you find a meta.xml try to parse it and initialize this.
	        if (entry.getName().endsWith("meta.xml")) {
	            PluginMetaContentHandler parser = 
	                new PluginMetaContentHandler(entry,zipFile);
	            parser.parse();
	            Plugin p=parser.getPlugin();
	            p.setSize((int) new File(jarFile).length());
	            return parser.getPlugin();
	        }
	    }
		return null;
	}

	
	@Override
    public boolean isValid() {
		boolean resultSoFar = super.isValid();
		
		return resultSoFar; //&& ...
	}
		
	static private final class PluginMetaContentHandler implements ContentHandler {
	    private ZipEntry metaFile;
	    private ZipFile zipFile;
	    private XMLReader rdr;
	    private Plugin plugin;//the final product, if any...
	    
	    public PluginMetaContentHandler(ZipEntry metaFileEntry, ZipFile zip) {
	        metaFile = metaFileEntry;
	        zipFile = zip;
	        plugin = null;
	        
	        try {
                rdr = XMLReaderFactory.
                createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser" );
                rdr.setContentHandler(this);
	        } catch (SAXException e2) {
	            System.out.println("PluginMetaContentHandler() SAXException " + e2.toString());
	        }
	    }
	    
	    public void parse() {
	        if (metaFile == null) {
	            plugin = null;
	            return;
	        }
	            
            try {
                rdr.parse(new InputSource(zipFile.getInputStream(metaFile)));
                zipFile.close();
            } catch (IOException e) {
                System.out.println("PluginMetaContentHandler.parse() IOException " + e.toString());
            } catch (SAXException e2) {
                System.out.println("PluginMetaContentHandler.parse() SAXException " + e2.toString());
            }
	    }

	    public void startElement(String uri, String localName, String name, Attributes atts)
                throws SAXException {
            if (name.equalsIgnoreCase("plugin")) {
                plugin=tryLoadingPluginFromAttributes(atts);
            }
        }

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
	     * Out of the Tag attributes create the Plugin object and populate it's
	     * attributes.
	     * We can't know from here the XML attributes the size or MD5
	     * (and I'm not sure if it even matters to know about these at this
	     * point, maybe we could compare against its remote counterpart)
	     * @param atts
	     * @return
	     */
	    private Plugin tryLoadingPluginFromAttributes(Attributes atts) {
	        plugin = new Plugin();
	        plugin.setName(tryGettingAttribute(atts, "name"));
	        plugin.setTitle(tryGettingAttribute(atts, "title"));
	        plugin.setAuthor(tryGettingAttribute(atts, "author"));
	        plugin.setOrganization(tryGettingAttribute(atts, "organization"));
	        plugin.setVersion(tryGettingAttribute(atts, "version"));
	        plugin.setMinimumFrostWireVersionSupported(tryGettingAttribute(atts, "minimumFrostWireVersion"));
	        plugin.setLastFrostWireVersionSupported(tryGettingAttribute(atts, "lastFrostWireVersion"));
	        plugin.setWebsite(tryGettingAttribute(atts, "website"));
	        plugin.setDownloadURL(tryGettingAttribute(atts, "downloadURL"));
	        
	        return plugin;
	    }	    
	    
        public Plugin getPlugin() {
	        return plugin;
	    }
	    
        public void characters(char[] ch, int start, int length) throws SAXException {
            // TODO Auto-generated method stub
            
        }

        public void endDocument() throws SAXException {
            // TODO Auto-generated method stub
            
        }

        public void endElement(String uri, String localName, String name) throws SAXException {
            // TODO Auto-generated method stub
            
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            // TODO Auto-generated method stub
            
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            // TODO Auto-generated method stub
            
        }

        public void processingInstruction(String target, String data) throws SAXException {
            // TODO Auto-generated method stub
            
        }

        public void setDocumentLocator(Locator locator) {
            // TODO Auto-generated method stub
            
        }

        public void skippedEntity(String name) throws SAXException {
            // TODO Auto-generated method stub
            
        }

        public void startDocument() throws SAXException {
            // TODO Auto-generated method stub
            
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            // TODO Auto-generated method stub
            
        }
	    
	}
}//Plugin