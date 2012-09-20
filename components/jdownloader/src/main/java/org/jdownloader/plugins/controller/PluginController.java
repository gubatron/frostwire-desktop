package org.jdownloader.plugins.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import jd.plugins.Plugin;

import org.appwork.utils.Application;
import org.appwork.utils.logging.Log;
import org.jdownloader.plugins.controller.PluginClassLoader.PluginClassLoaderChild;

public class PluginController<T extends Plugin> {

    @SuppressWarnings("unchecked")
    public ArrayList<PluginInfo<T>> scan(String hosterpath) {
        File path = null;
        PluginClassLoaderChild cl = null;

        path = Application.getRootByClass(jd.Launcher.class, hosterpath);

        cl = PluginClassLoader.getInstance().getChild();

        final File[] files = path.listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".class") && !name.contains("$");
            }
        });

        final ArrayList<PluginInfo<T>> ret = new ArrayList<PluginInfo<T>>();
        final String pkg = hosterpath.replace("/", ".");
        if (files != null) {
            for (final File f : files) {

                try {
                    String classFileName = f.getName().substring(0, f.getName().length() - 6);
                    ret.add(new PluginInfo<T>(f, (Class<T>) cl.loadClass(pkg + "." + classFileName)));
                    Log.L.finer("Loaded from: " + cl.getResource(hosterpath + "/" + f.getName()));
                } catch (Throwable e) {
                    Log.exception(e);
                }
            }
        }
        
        // hack for now, later I (aldenml) need to figure out how to add from jars
        if (ret.size() == 0) {
            try {
                Log.L.finer("Hard coding loading from: lw-jdownloader.jar");
                if (hosterpath.contains("hoster")) {
                    ret.add(new PluginInfo<T>(new File("lw-jdownloader.jar"), (Class<T>) cl.loadClass("jd.plugins.hoster.DirectHTTP")));
                    ret.add(new PluginInfo<T>(new File("lw-jdownloader.jar"), (Class<T>) cl.loadClass("jd.plugins.hoster.Youtube")));
                    ret.add(new PluginInfo<T>(new File("lw-jdownloader.jar"), (Class<T>) cl.loadClass("jd.plugins.hoster.SoundcloudCom")));
                } else if (hosterpath.contains("decrypter")) {
                    ret.add(new PluginInfo<T>(new File("lw-jdownloader.jar"), (Class<T>) cl.loadClass("jd.plugins.decrypter.TbCm")));
                    ret.add(new PluginInfo<T>(new File("lw-jdownloader.jar"), (Class<T>) cl.loadClass("jd.plugins.decrypter.SoundCloudComDecrypter")));
                }
            } catch (Throwable e) {
                Log.exception(e);
            }
        }
        
        return ret;

    }
}
