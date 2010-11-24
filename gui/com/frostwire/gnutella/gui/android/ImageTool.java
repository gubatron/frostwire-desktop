package com.frostwire.gnutella.gui.android;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class ImageTool {

    public Image load(String name) {
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
}
