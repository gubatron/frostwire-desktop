package com.limegroup.gnutella.gui.library;

import java.io.File;

import javax.swing.Icon;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.search.NamedMediaType;
import com.limegroup.gnutella.settings.SharingSettings;

public class TorrentMetaFileDirectoryHolder extends AbstractDirectoryHolder{
    
    private String name;
    private String desc;
    
    public TorrentMetaFileDirectoryHolder() {
        this.name = I18n.tr
                ("List of .torrent files");
        this.desc = I18n.tr
                (".torrent files");
    }

    public File getDirectory() {
        //return SharingUtils.APPLICATION_SPECIAL_SHARE;
    	return SharingSettings.DEFAULT_SHARED_TORRENTS_DIR;
    }
    
    @Override
    public boolean accept(File pathname) {
        return super.accept(pathname) && pathname.getName().endsWith(".torrent");
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public String getName() {
        return desc;
    }
    
    public Icon getIcon() {
    	return NamedMediaType.getFromExtension("torrent").getIcon();
    }

}
