package com.frostwire;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
    
    public Image getImage(URL url, OnLoadedListener listener) {
        if (isCached(url)) {
            return loadFromCache(url);
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

    private boolean isCached(URL url) {
        File file = getCacheFile(url);
        return file.exists();
    }
    
    private Image loadFromCache(URL url) {
        try {
            File file = getCacheFile(url);
            return ImageIO.read(file);
        } catch (IOException e) {
            return null;
        }
    }
    
    private Image loadFromResource(URL url) {
        try {
            BufferedImage image = ImageIO.read(url);
            saveToCache(url, image);
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
                    byte[] data = fetcher.fetch();
                    if (data != null) {
                        image = ImageIO.read(new ByteArrayInputStream(data));
                        saveToCache(url, image);
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
    
    private void saveToCache(URL url, BufferedImage image) {
        try {
            File file = getCacheFile(url);

            String filename = file.getName();
            int dotIndex = filename.lastIndexOf('.');
            String ext = filename.substring(dotIndex + 1);

            String formatName = ImageIO.getImageReadersBySuffix(ext).next().getFormatName();

            if (file.mkdirs()) {
                ImageIO.write(image, formatName, file);
            }
        } catch (IOException e) {
        }
    }

    public interface OnLoadedListener {
        public void onLoaded(URL url, Image image);
    }
}
