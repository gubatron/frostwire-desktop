package com.frostwire.overlays;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import org.limewire.util.CommonUtils;
import org.limewire.util.FileUtils;

public class OverlayImageManager {
    private final static int MAX_CACHE_SIZE = 2;
    
    private static OverlayImageManager INSTANCE;
    
    //The Image Cache. The key will be the imgUrl
    private static Hashtable<String,OverlayImageData> _savedImages; 
    
    private OverlayImageManager(){
    }
    
    public static OverlayImageManager getInstance() {
        if (OverlayImageManager.INSTANCE == null)
            OverlayImageManager.INSTANCE = new OverlayImageManager();
        
        return OverlayImageManager.INSTANCE;
    }
    
    /**
     * The one method you'll need outside of this class.
     * 
     * Given the URL of an image, it'll do everything
     * and it'll return the local path (in a url like string)
     * of the saved image.
     * 
     * It'll download it and save it if the image needs to be updated
     * It'll purge the cache and clean old files, you don't need
     * to worry about how it works, you just get the path to the image
     * you need on disk.
     * 
     * @param imgUrl
     * @param remoteMD5
     * @return A URL like String to the local path of the imgUrl
     */
    public String getCachedImagePath(String imgUrl, String remoteMD5) throws Exception {
        //Initialize the cache if this is the first time
        if (_savedImages == null) {
            loadCacheData();
        }

        //////////////////////////////////////////////////////////
        // DEAL WITH KNOWN IMAGE
        //////////////////////////////////////////////////////////
        
        //Check if we have an object for this imgUrl
        if (_savedImages.containsKey(imgUrl)) {
            OverlayImageData cached = _savedImages.get(imgUrl);
            
            //If the image has same name but changed remotely
            if (!cached.getMD5().equalsIgnoreCase(remoteMD5)) {
                cached.deleteFromDisk();
                cached.setMD5(remoteMD5);
                cached.saveToDisk();
            } else {
                System.out.println("OverlayImageManager.getCachedImagePath(): Image didn't change, using cached");
            }
            
            cached.updateLastTimeUsed();
            saveCacheData();

            return "file://" + getLocalOverlayPath() + cached.getFileName();
        }
        //////////////////////////////////////////////////////////
        
        //////////////////////////////////////////////////////////
        // DEAL WITH NEW IMAGE
        //////////////////////////////////////////////////////////
        // wipe out images if we need room for new one.
        purgeCacheIfNeeded(false);//false: don't serialize now _savedImages
        
        OverlayImageData overlayImage = new OverlayImageData(imgUrl,remoteMD5);
        overlayImage.updateLastTimeUsed();
        _savedImages.put(overlayImage.getImageURL(), overlayImage);

        try {
          saveCacheData();
          overlayImage.saveToDisk();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "file://" + getLocalOverlayPath() + overlayImage.getFileName();
    }

    /**
     * loads _savedImages from disk
     */
    @SuppressWarnings("unchecked")
    private static void loadCacheData() {
        File f = getConfigFile();
        _savedImages = new Hashtable<String,OverlayImageData>();

        if (f.length() == 0) {
            return; // overlay's file exist but its empty
        }
    
        try {
            ObjectInputStream objectstream = new ObjectInputStream(new FileInputStream(f));
            _savedImages = (Hashtable<String, OverlayImageData>) objectstream.readObject();
            objectstream.close();
        } catch (InvalidClassException eice) {
            try {
                f.createNewFile();
            }
            catch (Exception e) {
                System.out.println("OverlayImageManager.loadOverlays() - Unable to reset the overlays file.");
            }
        }
        catch (Exception e) {
            System.out.println("OverlayImageManager.loadoverlays() - Cannot deserialize - ");
            System.out.println(e);
            System.out.println("------------");
        }
    } 

    /**
     * saves _savedImages on disk
     */
    private static void saveCacheData() {
        getLocalOverlayPath(); 
    
        if (_savedImages == null || _savedImages.size() < 1) {
            return;
        }
    
        File f = getConfigFile();
    
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject((Hashtable<String,OverlayImageData>) _savedImages);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This should only be invoked if the cache has reached it's maximum
     * size, and we're about to a new Image to it.
     * 
     * This method, will find the oldest element in the cache and
     * delete it from disk, from _savedImages and serialize if you
     * want it to.
     */
    private void purgeCacheIfNeeded(boolean serialize) {
        //if and only if the cache needs to do this
        if (_savedImages == null || _savedImages.size() < OverlayImageManager.MAX_CACHE_SIZE)
            return;
        
        //First find the oldest OverlayImageData object in the cache
        OverlayImageData oldest = getOldestCachedOverlayImageData();
        
        if (oldest == null)
            return;
        
        oldest.deleteFromDisk();
        _savedImages.remove(oldest.getImageURL());
        
        if (serialize)
            saveCacheData();
    }

    private OverlayImageData getOldestCachedOverlayImageData() {
        if (_savedImages ==null || _savedImages.size()==0) {
            return null;
        }
        
        OverlayImageData oldest = null;
        Iterator<String> iter = _savedImages.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            if (oldest == null)
                oldest = _savedImages.get(key);
            else {
                OverlayImageData current = _savedImages.get(key);
                if (current.getLastTimeUsed() < oldest.getLastTimeUsed())
                    oldest = current;
            }
        }
        return oldest;
    }
    
    private static String getLocalOverlayPath() { 
        File fullpath = new File(CommonUtils.getUserSettingsDir(), "overlays");               

        if (!fullpath.exists()) {
            fullpath.mkdirs(); // create path if doesn't exist for some reason (1st time).
            FileUtils.setWriteable(fullpath);
        }

        return fullpath + File.separator; // Supported in all platforms           
    }


    private static File getConfigFile() {
        File f = new File(CommonUtils.getUserSettingsDir(), "overlays.dat");
        if (!f.exists()) {
            try { 
                f.createNewFile(); 
            } catch (Exception e) { 
                System.out.println("OverlayImageManager.getConfigFile() cannot create file to serialize seen messages"); 
            }
        }        
        
        return f;
    }

    /**
     * Simple class to store information about the image that will help us
     * determine if we need to download a new version of it or not.
     * @author gubatron
     */
    private static class OverlayImageData implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = -5341644185050835045L;

        //remote url of the image
        private String imgUrl;
        
        //only the name, eg. "my_overlay.png" Doesn't include folder path
        private String fileName;
        
        //the file MD5
        private String MD5;
        
        private long lastTimeUsed;
        
        public OverlayImageData(String url, String md5) {
            if (url!=null) {
                setImageURL(url);
                setFileName(getFileNameFromUrl(url));
            }
            
            setMD5(md5);
        } // the empty constructor
        
        public void setImageURL(String url) {
            imgUrl = url;
        }
        
        public String getImageURL() {
            return imgUrl;
        }

        public void setMD5(String md5) {
            MD5 = md5;
        }
        
        public String getMD5() {
            return MD5;
        }
        
        private String getFileNameFromUrl(String url) {
            String name = url.substring(url.lastIndexOf("/")+1,url.length());
            return name;
        }
        
        public void setFileName(String fname) {
            if (fname.startsWith("http://"))
                fname = getFileNameFromUrl(fname);
            fileName = fname;
        }
        
        public String getFileName() {
            return fileName;
        }

        public void setLastTimeUsed(long timestamp) {
            lastTimeUsed = timestamp;
        }
        
        public long getLastTimeUsed() {
            return lastTimeUsed;
        }

        public void updateLastTimeUsed() {
            lastTimeUsed = (new Date()).getTime();
        }
        
        /**
         * This function saves the image binary data to a file on disk.
         * It uses the file name, and puts it on the overlay directory
         * @param url
         * @param localname
         * @return
         * @throws Exception
         */
        private boolean saveToDisk() throws Exception {
            URL u = new URL(getImageURL());
            URLConnection uc = u.openConnection();
            String contentType = uc.getContentType();
            int contentLength = uc.getContentLength();
        
            if (!contentType.startsWith("image/") || contentLength == -1) {
                throw new IOException("*****OverlayImageManager.SaveRemoteImage: Invalid image file");	    
            }
        
            InputStream raw = uc.getInputStream();
            InputStream in = new BufferedInputStream(raw);
            byte[] data = new byte[contentLength];
            int bytesRead = 0;
            int offset = 0;

            while (offset < contentLength) {
                bytesRead = in.read(data, offset, data.length - offset);
                if (bytesRead == -1)
                    break;
                offset += bytesRead;
            }
            
            in.close();
        
            if (offset != contentLength) {
                throw new IOException("ERROR WHILE READING REMOTE IMAGE. Bytes readed: " + offset + " bytes; Bytes expected " + contentLength + " bytes");	    
            }
        
            String filename = getLocalOverlayPath() + getFileName();
        
            File tempfile = new File(filename);                       
            tempfile.createNewFile(); // blank file to test writing permission.               
        
            if (!tempfile.exists())
                return false;                     
        
            FileOutputStream out = new FileOutputStream(filename);
            out.write(data);
            out.flush();
            out.close();
            
            return true;
        }
        
        public void deleteFromDisk() {
            String filename = getLocalOverlayPath() + getFileName();
            File f = new File(filename);
            f.delete();
        }
        
        public int hashCode() {
            //Will be the sum of all the charcodes in the url
            String url = getImageURL();
            int hash=0;
            for (int i=0; i < url.length(); i++)
                hash+=(int) url.charAt(i);
            return hash;
        }
        
        //Implementing Serializable
        
        private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
            out.defaultWriteObject();
        }
        
        private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
            in.defaultReadObject();
        }
    } //OverlayImageData
}