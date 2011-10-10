package com.frostwire;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.limewire.util.OSUtils;

import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.util.FrostWireUtils;

public class ImageCache {

    private static ImageCache INSTANCE;
    
    private ImageCache() {
    }
    
    public synchronized static ImageCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ImageCache();
        }
        return INSTANCE;
    }
    
    public BufferedImage getImage(URL url, OnLoadedListener listener) {
        if (isCached(url)) {
        	return loadFromCache(url, listener);
        } else if (!url.getProtocol().equals("http")) {
            return loadFromResource(url, listener);
        } else {
            loadFromUrl(url, listener);
            return null;
        }
    }
    
    private File getCacheFile(URL url) {
        
        String host = url.getHost();
        String path = url.getPath();
        if (host == null || host.length() == 0) { // dealing with local resource images, not perfect
            host = "localhost";         
            path = new File(path).getName();
        }
        
        return new File(SharingSettings.getImageCacheDirectory(), File.separator + host + File.separator + path);
    }
    
    /**
     * Given the remote URL if the image has been cached this will return the local URL of the cached image on disk.
     * 
     * @param remoteURL
     * @return The URL of the cached file. null if it's not been cached yet.
     */
    public URL getCachedFileURL(URL remoteURL) {
    	if (isCached(remoteURL)) {
    		try {
				return getCacheFile(remoteURL).toURI().toURL();
			} catch (MalformedURLException e) {
				return null;
			}
    	}
    	return null;
    }

    private boolean isCached(URL url) {
        File file = getCacheFile(url);
        long now = System.currentTimeMillis();
        return file.exists() && (now - file.lastModified()) < 2592000000L;
    }
    
    private BufferedImage loadFromCache(URL url, OnLoadedListener listener) {
        try {
            File file = getCacheFile(url);
            BufferedImage image = ImageIO.read(file);
            listener.onLoaded(url, image, true, false);
            return image;
        } catch (Throwable e) {
            if (e instanceof OutOfMemoryError) {
                e.printStackTrace(); // this is a special condition
            }
            listener.onLoaded(url, null, false, true);
            return null;
        }
    }
    
    private BufferedImage loadFromResource(URL url, OnLoadedListener listener) {
        try {
            BufferedImage image = ImageIO.read(url);
            saveToCache(url, image, 0);
            listener.onLoaded(url, image, false, false);
            return image;
        } catch (Exception e) {
            listener.onLoaded(url, null, false, true);
            return null;
        }
    }
    
    private void loadFromUrl(final URL url, final OnLoadedListener listener) {
        // TODO: may be I must use BackgroundExecutorService
        new Thread(new Runnable() {
            public void run() {                
                try {
                    BufferedImage image = null;
                    
                    String userAgent = "FrostWire/" + OSUtils.getOS() + "/" + FrostWireUtils.getFrostWireVersion();
                    HttpFetcher fetcher = new HttpFetcher(url.toURI(), userAgent);
                    Object[] result = fetcher.fetch(false);
                    
                    if (result == null) {
                    	throw new IOException("HttpFetcher.fetch() got nothing at " + url.toString());
                    }
                    
                    byte[] data = (byte[]) result[0];
                    long date = (Long) result[1];
                    if (data != null) {
                        image = ImageIO.read(new ByteArrayInputStream(data));
                        saveToCache(url, image, date);
                    }
                    if (listener != null && image != null) {
                        listener.onLoaded(url, image, false, false);
                    }
                } catch (Exception e) {
                    listener.onLoaded(url, null, false, true);
                	e.printStackTrace();
                }
                
            }
        }).start();
    }
    
    private void saveToCache(URL url, BufferedImage image, long date) {
        try {
            File file = getCacheFile(url);
            
            if (file.exists()) {
                file.delete();
            }
            
            String filename = file.getName();
            int dotIndex = filename.lastIndexOf('.');
            String ext = filename.substring(dotIndex + 1);

            String formatName = ImageIO.getImageReadersBySuffix(ext).next().getFormatName();
            
            if (!file.getParentFile().exists()) {
                file.mkdirs();
            }
            ImageIO.write(image, formatName, file);
            file.setLastModified(date);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface OnLoadedListener {
    	
        /**
    	 * This is called in the event that the image was downloaded and cached
    	 */
        public void onLoaded(URL url, BufferedImage image, boolean fromCache, boolean fail);
    }
}
