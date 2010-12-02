package com.frostwire.gnutella.gui.android;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.limegroup.gnutella.gui.I18n;

public class UITool {
    
    private static String[] BYTE_UNITS = new String[] { "b", "KB", "Mb", "Gb", "Tb" };
    private static HashMap<String, Integer> FILE_TYPES;
    
    public UITool() {
        initFileTypes();
    }

    public BufferedImage loadImage(String name) {
        String path = "images" + File.separator + name + ".png";
        
        if (name.endsWith(".jpg")) {
            path = "images" + File.separator + name;
        }
        
        URL url = getClass().getResource(path);
        try {
            initFileTypes();
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
    
    public String getBytesInHuman(long size) {

        int i = 0;
        float fSize = (float) size;

        for (i = 0; size > 1024; i++) {
            size /= 1024;
            fSize = fSize / 1024f;
        }

        return String.format("%.2f ", fSize) + BYTE_UNITS[i];
    }
    
    public int getFileTypeByExt(String ext) {
        
        if (ext == null) {
            return DeviceConstants.FILE_TYPE_DOCUMENTS;
        }
        
        ext = ext.replace("\\.", "").trim().toLowerCase();
        
        Integer type = FILE_TYPES.get(ext);
        if (type != null) {
            return type;
        } else {
            return DeviceConstants.FILE_TYPE_DOCUMENTS;
        }
    }
    
    public static String getFileTypeAsString(int type) {

        switch (type) {
        case DeviceConstants.FILE_TYPE_APPLICATIONS:
            return I18n.tr("Applications");
        case DeviceConstants.FILE_TYPE_AUDIO:
            return I18n.tr("Audio");
        case DeviceConstants.FILE_TYPE_DOCUMENTS:
            return I18n.tr("Documents");
        case DeviceConstants.FILE_TYPE_PICTURES:
            return I18n.tr("Pictures");
        case DeviceConstants.FILE_TYPE_RINGTONES:
            return I18n.tr("Ringtones");
        case DeviceConstants.FILE_TYPE_VIDEOS:
            return I18n.tr("Video");
        default:
            return "Unkown file type";
        }
    }
    
    private static void initFileTypes() {
        
        if (FILE_TYPES != null) {
            return;
        }
        
        FILE_TYPES = new HashMap<String, Integer>();
        
        FILE_TYPES.put("mp3", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("wma", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("wav", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("flac", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("ogg", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("aac", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("aa3", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("ac3", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("cdr", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("midi", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("f4a", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("m4a", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("m4b", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("m4p", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("mid", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("mka", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("mp1", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("mp2", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("mpa", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("mpga", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("au", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("oga", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("omf", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("pcast", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("pls", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("ra", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("ram", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("aa", DeviceConstants.FILE_TYPE_AUDIO);
        FILE_TYPES.put("at3", DeviceConstants.FILE_TYPE_AUDIO);
        
        FILE_TYPES.put("3gp", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("3g2", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("asf", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("asx", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("avi", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("bdm", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("bsf", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("cpi", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("divx", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("dmsm", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("dream", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("dvdmedia", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("dvr-ms", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("dzm", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("dzp", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("f4v", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("fbr", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("flv", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("hdmov", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("imovieproj", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("m2p", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("m4v", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("mkv", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("mod", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("moi", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("mov", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("mp4", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("mpeg", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("mpeg4", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("mts", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("mxf", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("ogm", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("ogv", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("pds", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("prproj", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("psh", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("rsproject", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("rm", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("rmvb", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("scm", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("smil", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("srt", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("stx", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("swf", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("tix", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("trp", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("ts", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("veg", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("fv", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("vob", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("vro", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("wmv", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("wtv", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("xvid", DeviceConstants.FILE_TYPE_VIDEOS);
        FILE_TYPES.put("yuv", DeviceConstants.FILE_TYPE_VIDEOS);
        
        FILE_TYPES.put("afx", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("arw", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("bmp", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("cpt", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("cr2", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("dcm", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("dds", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("dib", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("dng", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("dt2", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("gif", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("hdp", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("ipx", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("itc2", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("jpe", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("jpeg", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("jpg", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("jps", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("jpx", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("max", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("mng", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("nef", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("pictclipping", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("png", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("ppm", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("psd", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("psp", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("pspimage", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("raw", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("rw2", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("sdr", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("srf", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("tga", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("thm", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("tif", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("tiff", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("wb1", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("wbc", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("wbd", DeviceConstants.FILE_TYPE_PICTURES);
        FILE_TYPES.put("wbz", DeviceConstants.FILE_TYPE_PICTURES);
        
        FILE_TYPES.put("rng", DeviceConstants.FILE_TYPE_RINGTONES);
        FILE_TYPES.put("m4r", DeviceConstants.FILE_TYPE_RINGTONES);
        FILE_TYPES.put("nrt", DeviceConstants.FILE_TYPE_RINGTONES);
        FILE_TYPES.put("rng", DeviceConstants.FILE_TYPE_RINGTONES);
        
        FILE_TYPES.put("apk", DeviceConstants.FILE_TYPE_APPLICATIONS);
    }
}
