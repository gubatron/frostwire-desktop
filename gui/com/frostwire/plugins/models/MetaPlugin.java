package com.frostwire.plugins.models;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * This is just a place holder for the information
 * about a Plugin.
 * 
 * Used by the PluginValidator to hold information parsed
 * from the XML it reads from the plugin directory server.
 * @author gubatron
 *
 */
public class MetaPlugin implements IPlugin, Serializable {
    private static final Log LOG = LogFactory.getLog(MetaPlugin.class);

    protected String name;
    protected String title;
    protected String version;
    protected String minimumVersion;
    protected String lastVersion;
    protected String author;
    protected String organization;
    protected String website;
    protected String downloadURL;
    protected String md5Hash;
    protected int size;

    public MetaPlugin() { }
    
    public String asXML() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<plugin name=\"%s\" ");
    	sb.append("title=\"%s\" ");             
        sb.append("author=\"%s\" ");                                                           
        sb.append("organization=\"%s\" ");                                                     
        sb.append("version=\"%s\" ");                                                          
        sb.append("minimumFrostWireVersion=\"%s\" ");                                          
        sb.append("lastFrostWireVersion=\"%s\" ");                                             
        sb.append("website=\"%s\" ");                                                          
        sb.append("downloadURL=\"%s\" ");                                                      
        sb.append("MD5=\"%s\" ");                                                              
        sb.append("size=\"%s\"/>");
        
        return String.format(sb.toString(),	 
        		getName(),
        		getTitle(),
        		getAuthor(),
        		getOrganization(),
        		getVersion(),
        		getMinimumFrostWireVersionSupported(),
        		getLastFrostWireVersionSupported(),
        		getWebsite(),
        		getDownloadURL(),
        		getMD5Hash(),
        		String.valueOf(getSize()));
    }

    /** Methods that implement the Plugin Interface */
    
    public String getMD5Hash() {
        return md5Hash;
    }
    
    public void setMD5Hash(String md5) { md5Hash = md5; }

    public int getSize() { return size; }


    public void setSize(String s) {
        if (s==null ||
            s.equals("")) {
            setSize(0);
            return;
        }
        
        setSize(Integer.parseInt(s));
    }

    public void setSize(int s) { size = Math.abs(s); }
    
    public String getName() {
        return name;
    }

    public void setName(String n) { name = n; }
    
    public String getTitle() {
        return title;
    }
    
    
    public void setTitle(String t) { title = t; }    

    public String getVersion() {
        return version;
    }
    
    public void setVersion(String v) { 
        version=v; 
    }
    
    public String getMinimumFrostWireVersionSupported() {
        return minimumVersion;
    }
    public void setMinimumFrostWireVersionSupported(String v) { minimumVersion = v; } ;

    public String getLastFrostWireVersionSupported() { 
        return lastVersion;
    }
    public void setLastFrostWireVersionSupported(String v) { lastVersion = v; };

    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String a) { author = a; }
    
    public String getOrganization() {
        return organization;
    }
    
    public void setOrganization(String o) { organization = o; }

    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String w) { website = w; }

    
    public String getDownloadURL() {
        return downloadURL;
    }
    
    public void setDownloadURL(String u) { downloadURL = u; }    

    /** For convenience, check if String variable is empty */
    private boolean isEmpty(String s) {
        return s==null || s.equals("");
    }
    

    /** Tells wether or not the meta data of the plugin is valid 
     * TODO: Add OS compatibilitity checking
     * */
    public boolean isValid() {
        //check mandatory properties
        if (isEmpty(getName())) {
            LOG.error("Plugin has no name");
            return false;
        }

        if (isEmpty(getTitle())) {
            LOG.error("Plugin " + getName() + " has no title");
            return false;
        }
	    
        if (getSize() < 1) {
            LOG.error("Plugin " + getName() + " has no size");
            return false;
        }
        
        //check if its compatible with this OS
        if (!PluginValidator.validateMinimumVersion(getMinimumFrostWireVersionSupported())) {
            LOG.error("Plugin " + getName() + " didn't comply with minimum version validation");
            return false;
        }
        
        if (!PluginValidator.validateLastVersion(getLastFrostWireVersionSupported())) {
            LOG.error("Plugin " + getName() + " didn't comply with last version validation");
            return false;
        }

        return true;
    }
    
    /** Implementation of Serializable Interface **
     * 
     */
    private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
    	out.defaultWriteObject();
    } //writeObject

    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    } //readObject
    
    private void readObjectNoData() throws ObjectStreamException {}
  
} //MetaPlugin