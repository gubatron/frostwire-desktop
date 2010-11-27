package com.frostwire.gnutella.gui.android;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class ImageTool {

    public BufferedImage load(String name) {
        String path = "images" + File.separator + name + ".png";
        
        if (name.endsWith(".jpg")) {
            path = "images" + File.separator + name;
        }
        
        URL url = getClass().getResource(path);
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            return null;
        }
    }
    
    public String getImageNameByFileType(int type) {
        switch(type) {
        case DeviceConstants.FILE_TYPE_APPLICATIONS: return "application";
        case DeviceConstants.FILE_TYPE_DOCUMENTS: return "document";
        case DeviceConstants.FILE_TYPE_PICTURES: return "picture";
        case DeviceConstants.FILE_TYPE_VIDEOS: return "video";
        case DeviceConstants.FILE_TYPE_RINGTONES: return "ringtone";
        case DeviceConstants.FILE_TYPE_AUDIO: return "audio";
        default: return "";
        }
    }
}
