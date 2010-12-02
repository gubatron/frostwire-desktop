package com.frostwire;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.limewire.util.OSUtils;

import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.util.LimeWireUtils;

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
        	BufferedImage result = loadFromCache(url);
        	listener.wasAlreadyCached(getCachedFileURL(url), result);
            return result;
        } else if (!url.getProtocol().equals("http")) {
            return loadFromResource(url);
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
    
    private BufferedImage loadFromCache(URL url) {
        try {
            File file = getCacheFile(url);
            return ImageIO.read(file);
        } catch (IOException e) {
            return null;
        }
    }
    
    private BufferedImage loadFromResource(URL url) {
        try {
            BufferedImage image = ImageIO.read(url);
            saveToCache(url, image, 0);
            return image;
        } catch (IOException e) {
            return null;
        }
    }
    
    private void loadFromUrl(final URL url, final OnLoadedListener listener) {
        // TODO: may be I must use BackgroundExecutorService
        new Thread(new Runnable() {
            public void run() {                
                try {
                    BufferedImage image = null;
                    
                    String userAgent = "FrostWire/" + OSUtils.getOS() + "/" + LimeWireUtils.getLimeWireVersion();
                    HttpFetcher fetcher = new HttpFetcher(url.toURI(), userAgent);
                    Object[] result = fetcher.fetchWithDate();
                    byte[] data = (byte[]) result[0];
                    long date = (Long) result[1];
                    if (data != null) {
                        image = ImageIO.read(new ByteArrayInputStream(data));
                        saveToCache(url, image, date);
                    }
                    if (listener != null && image != null) {
                        listener.onLoaded(url, image);
                    }
                } catch (URISyntaxException e) {
                } catch (IOException e) {
                }
                
            }
        }).start();
    }
    
    private void saveToCache(URL url, BufferedImage image, long date) {
        try {
            File file = getCacheFile(url);
            
            if (file.exists() && file.lastModified() < date) {
                file.delete();
            }
            
            String filename = file.getName();
            int dotIndex = filename.lastIndexOf('.');
            String ext = filename.substring(dotIndex + 1);

            String formatName = ImageIO.getImageReadersBySuffix(ext).next().getFormatName();

            if (file.mkdirs()) {
                ImageIO.write(image, formatName, file);
                file.setLastModified(date);
            }
        } catch (IOException e) {
        }
    }

    public interface OnLoadedListener {
    	/** This is called in the event that the image was downloaded and cached*/
        public void onLoaded(URL url, BufferedImage image);

        /** This is called in the event that the image was already cached */
		public void wasAlreadyCached(URL cachedFileURL, BufferedImage imageFromCache);
    }
}
