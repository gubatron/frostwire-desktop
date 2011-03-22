package com.frostwire.gnutella.gui.chat;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.limegroup.gnutella.gui.I18n;

/**
 *  Wrapper for a local or remote audio file/stream that is to be added to the 
 *  playlist queue to be played by the local media player. Audio properties 
 *  such as artist, title, etc.. can also be passed in as a Map for audio 
 *  sources that may not contain META-TAG information
 */
public class ChatItem implements Comparable<ChatItem>{
    
    // Values to be used in the properties map
    public static final String ALBUM = "Album";
    public static final String ARTIST = "Artist";
    public static final String BITRATE = "BitRate";
    public static final String COMMENT = "Comment";
    public static final String GENRE = "Genre";
    public static final String LENGTH = "Length";
    public static final String TIME = "Time";
    public static final String SIZE = "Size";
    public static final String TITLE = "Title";
    public static final String TRACK = "Track";
    public static final String TYPE = "Type";
    public static final String YEAR = "Year";
    public static final String VBR = "VBR";    
   
    /**
     * Default name of the audio source to display if no META information is available
     */
    private final String name="";
    
    /**
     * A flag for determining if the audio source is on the local filesystem. 
     */
    private final boolean isLocal=true;
    
    /**
     * A flag for setting whether this audio source is a preview clip from the LimeWire store.
     * When this flag is set, a buy/info button is presented in the playlist for purchasing
     * this item
     */
    @SuppressWarnings("unused") 
    private final boolean isStorePreview=false;
    
    /**
     * A map of audio properties that can be displayed about this item. Audio clips from the store
     * for example, may not contain audio properties embeded in the Tag but are transfered in XML 
     * to the client.
     */
    private final Map<String,String> properties = null;
    
    /**
     * List of all meta data for this song in human readable form for display
     * as a tooltip.
     */
    private String[] toolTips = null;
   
    
    /**
     * Adds a local file to the playlist. Creation on a PlayListItem will
     * result in disk access to obtain the ID3 tag. This should never be
     * instantiated on the swing event queue.
     * 
     * @param file - file to be added
     */
/*
    public ChatItem(File file) {
        this( file.toURI(), new AudioSource(file), file.getName(), true);
    }
   */ 
  

 
    /**
     * @return default name to display if not meta information 
     */
    public String getName(){
        return name;
    }
           
    /**
     * Looks up a property using a key
     * @param key
     * @return - the String representing the key, null if the key doesn't exist
     */
    public String getProperty(String key) {
	return properties.get(key);
    }
    
    /**
     * Looks up a property using a key. If the key does not exist in the table,
     * returns the default value passed in.
     * 
     * @param key - key to search the map with
     * @param defaultKey - value to return if the key does not exist in map
     * @return
     */
    public String getProperty(String key, String defaultValue) {
        String value = properties.get(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * @return true if this is a file on the local filesystem
     */
    public boolean isFile(){
        return isLocal;
    }
    
    /**
     * @return true if this a streaming preview from the LimeWire store. 
     */
    public boolean isStorePreview(){
        // buttons for store songs are currently disabled till later
        return false;//return isStorePreview;
    }
    
    /**
     * Creates a tooltip that displays all the known meta data about this song
     * @return an array of string that contains readable meta data
     */
    public String[] getToolTips(){
        // lazy initializer, doesn't get called until the first request for
        //  a tooltip
        if( toolTips == null ) {
            List<String> allData = new LinkedList<String>();   
            allData.add(getName());
            if( properties.get(TITLE) != null )
                allData.add( I18n.tr("Title") + ": " + properties.get(TITLE));
            if( properties.get(ARTIST) != null )
                allData.add( I18n.tr("Artist") + ": " + properties.get(ARTIST));
            if( properties.get(ALBUM) != null )
                allData.add( I18n.tr("Album") + ": " + properties.get(ALBUM));
            if( properties.get(GENRE) != null )
                allData.add(I18n.tr("Genre") + ": " + properties.get(GENRE));
            if( properties.get(TRACK) != null )
                allData.add(I18n.tr("Track") + ": " + properties.get(TRACK));
            if( properties.get(YEAR) != null )
                allData.add(I18n.tr("Year") + ": " + properties.get(YEAR));
            if( properties.get(TIME) != null )
                allData.add(I18n.tr("Length") + ": " + properties.get(TIME));
            if( properties.get(BITRATE) != null )
                allData.add(I18n.tr("Bitrate") + ": " + properties.get(BITRATE) + " kbps" + (properties.get(VBR).equals("true") ? " (VBR)" : "") );
            
            toolTips = allData.toArray( new String[allData.size()]);
        }
        return toolTips;
    }

    public int compareTo(ChatItem o) {
	return 0;
        //return o.getURI().compareTo(uri);
    }
}
