package com.frostwire.updates;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * Simple class to serialize HostilesData information
 * @author gubatron
 *
 */
public final class HostilesMetaData implements Serializable {
    private static final long serialVersionUID = -3319232630233496049L;
    //The only object that gets serialized (containing all property values)
    private java.util.Hashtable<Byte, Object> _properties;
    
    private transient Object _statusLock;
    
    //This object will be serialized as a Object Dictionary
    //That way we can evolve this object without breaking
    //serialization in future versions of FrostWire.
    
    public static transient final Byte DATE = 0x03;
    public static transient final Byte MD5 = 0x01;
    public static transient final Byte SEED_RATIO = 0x05;
    public static transient final Byte STATUS = 0x04;
    public static transient final Byte TORRENT_DATA_LOCATION = 0x07;
    public static transient final Byte TORRENT_FILE_LOCATION = 0x06;
    public static transient final Byte TORRENT_URL = 0x02;
    //Transient Keys used to reference the serialized properties
    public static transient final Byte VERSION = 0x00;
    
    /**
    * ERRORED - An error ocurred during download
    * INITIATED - We created a HostileData object out of a message.
    * DOWNLOADING - We started downloading the torrent
    * DOWNLOADED - We finished downloading the torrent
    * PROCESSING - The torrent was downloaded but we're still processing it
    * PROCESSED - The torrent finished processing
    */
    public static transient final Byte STATUS_ERRORED = 0x00;
    public static transient final Byte STATUS_INITIATED = 0x01;
    public static transient final Byte STATUS_DOWNLOADING = 0x02;
    public static transient final Byte STATUS_DOWNLOADED = 0x03;
    public static transient final Byte STATUS_PROCESSING = 0x04;
    public static transient final Byte STATUS_PROCESSED = 0x05;
    
    /**
     * Returns -1 if a date hasn't been set.
     * Should return the last time we saved (or updated) the object.
     * @return the _date
     */
    public final long get_date() {
        Long date = (Long) get_property(DATE);
        if (date == null)
            return (long) -1;
        return date.longValue();
    }
    
    /**
     * The expected md5 of the unzipped file that comes in the torrent.
     * @return the _md5
     */
    public final String get_md5() {
        return (String) get_property(MD5);
    }

    private final Object get_property(Byte key) {
        try {
            if (_properties.containsKey(key))
                return _properties.get(key);
        } catch (Exception e) {
        }
            
        return null;
    }

    /**
     * @return the _seedRatio
     */
    public final int get_seedRatio() {
        Integer ratio = new Integer(-1);
        
        try {
            ratio = (Integer) get_property(SEED_RATIO);
        } catch (NullPointerException e) {
            ratio = new Integer(-1);
        }
        
        if (ratio == null)
            ratio = new Integer(-1);
        
        return ratio.intValue();
    }

    /**
     * @return the _status
     */
    public final Byte get_status() {
       return (Byte) get_property(STATUS);
    }

    /**
     * The location where the torrent contents (hostiles.txt.n.zip) is saved when it's
     * downloaded.
     * @return the _torrentDataLocation
     */
    public final String get_torrent_data_save_location() {
        return (String) get_property(TORRENT_DATA_LOCATION);
    }

    /**
     * The location where the .torrent file is saved.
     * @return the _torrentFileLocation
     */
    public final String get_torrent_file_save_location() {
        return (String) get_property(TORRENT_FILE_LOCATION);
    }

    /**
     * @return the _torrentURL
     */
    public final String get_torrentURL() {
        return (String) get_property(TORRENT_URL);
    }

    /**
     * @return the _version
     */
    public final String get_version() {
        return (String) get_property(VERSION);
    }

    public final boolean is_valid() {
        if (get_date() < 0 ||
            get_seedRatio() < 0 ||
            get_md5() == null ||
            get_torrentURL() == null ||
            get_version() == null)
            return false;
        
        return true;
    }

    /**
     * @param l the _date to set
     */
    public final void set_date(final long l) {
        set_property(DATE,new Long(l));
    }

    /**
     * @param _md5 the _md5 to set
     */
    public final void set_md5(final String md5) {
        set_property(MD5, md5);
    }
    
    private final void set_property(Byte key, Object value) {
        assert(key != null);
        assert(value != null);
        
        if (_properties == null) {
            _properties = new Hashtable<Byte, Object>();
        }
        
        if (_statusLock == null)
            _statusLock = new Object();

        synchronized(_statusLock) {
            _properties.put(key, value);
        }
    }

    /**
     * @param seedRatio the _seedRatio to set
     */
    public final void set_seedRatio(final int seedRatio) {
        set_property(SEED_RATIO, new Integer(seedRatio));
    }

    /**
     * @param _status the _status to set
     */
    public final void set_status(final Byte status) {
        set_property(STATUS,status);
    }

    /**
     * Set the location where the torrent contents (hostiles.txt.n.zip) is saved when it's
     * downloaded. For convenience if we are to seed the file next time frostwire starts.
     * @param torrentDataLocation the _torrentDataLocation to set
     */
    public final void set_torrent_data_save_location(final String torrentDataLocation) {
        set_property(TORRENT_DATA_LOCATION, torrentDataLocation);
    }

    /**
     * Set The location where the .torrent file is saved.
     * @param torrentFileLocation the _torrentFileLocation to set
     */
    public final void set_torrent_file_save_location(final String torrentFileLocation) {
       set_property(TORRENT_FILE_LOCATION,torrentFileLocation);
    }

    /**
     * @param _torrenturl the _torrentURL to set
     */
    public final void set_torrentURL(final String torrenturl) {
        set_property(TORRENT_URL,torrenturl);
    }

    /**
     * @param _version the _version to set
     */
    public final void set_version(final String version) {
        set_property(VERSION, (String) version); 
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }
    
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("_date : " + get_date() + "\n");

        if (get_md5() != null)
            s.append("_md5 : " + get_md5() + "\n");
        
        if (get_status() != null) {
            String[] str_statuses = new String[] { "ERRORED",
              "INITIATED","DOWNLOADING","DOWNLOADED","PROCESSING",
              "PROCESSED"}; 
            s.append("_status : " + str_statuses[(int) get_status()] + "\n");
        }
        
        if (get_torrentURL() != null)
            s.append("_torrentURL : " + get_torrentURL() + "\n");
        
        if (get_version() != null)
             s.append("_version : " + get_version() + "\n");
        
        //s.append("_seedRatio : " + get_seedRatio() + "\n");
        
        if (get_torrent_file_save_location() != null)
            s.append("_torrent_file_location : " + get_torrent_file_save_location() + "\n");
        
        if (get_torrent_data_save_location() != null)
            s.append("_torrent_file_data_location : " + get_torrent_data_save_location() + "\n");
        return s.toString();
    }
}
