package com.limegroup.gnutella.gui.search;

import java.io.File;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.limewire.io.IpPort;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;
import com.limegroup.gnutella.gui.themes.SkinPopupMenu;
import com.limegroup.gnutella.xml.LimeXMLDocument;

/**
 *  A Single Shared Folder search result. This is generated when the user
 *  wishes to view all the files they are currently sharing. This behaves 
 *  similar to a GnutellaSearchResult except the FileDesc is passed along
 *  to make unsharing of a particular result easy. 
 */
public class SharedSearchResult extends AbstractSearchResult {

    private FileDesc fileDesc;
    
    private RemoteFileDesc rfd;
    
    private Set<? extends IpPort> _alts;
    
    public SharedSearchResult(FileDesc fileDesc, RemoteFileDesc rfd,  Set<? extends IpPort> alts ){
        this.fileDesc = fileDesc;
        this.rfd = rfd;
        this._alts = alts;
    }
    
    public FileDesc getFileDesc(){
        return fileDesc;
    }
    
    /** Gets the RemoteFileDesc */
    RemoteFileDesc getRemoteFileDesc() { return rfd; }
    
    
    /** Gets the Alternate Locations */
    Set<? extends IpPort> getAlts() { return _alts; }
    
    /**
     * Clears the alternate locations for this SearchResult.
     */
    void clearAlts() {
        _alts = null;
    }

    public String getFileName() {
        return rfd.getFileName();
    }

    public long getSize() {
        return rfd.getSize();
    }

    public URN getSHA1Urn() {
        return rfd.getSHA1Urn();
    }

    public LimeXMLDocument getXMLDocument() {
        return rfd.getXMLDocument();
    }

    public long getCreationTime() {
        return rfd.getCreationTime();
    }

    public boolean isDownloading() {
        return rfd.isDownloading();
    }

    public String getVendor() {
        return rfd.getVendor();
    }

    public int getQuality() {
        return rfd.getQuality();
    }

    public int getSecureStatus() {
        return rfd.getSecureStatus();
    }

    public int getSpeed() {
        return 0;
    }

    public boolean isMeasuredSpeed() {
        return false;
    }

    public float getSpamRating() {
        return rfd.getSpamRating();
    }

    public String getHost() {
        return rfd.getHost();
    }

    public void takeAction(TableLine line, GUID guid, File saveDir, String fileName, boolean saveAs, SearchInformation searchInfo) {
    }

    public JPopupMenu createMenu(JPopupMenu popupMenu, TableLine[] lines, boolean markAsSpam, boolean markAsNot, ResultPanel resultPanel) {
        JPopupMenu menu = new SkinPopupMenu(); 
        
        JMenuItem item = new SkinMenuItem(I18n.tr("Path") + ": " + getFileDesc().getPath());
        menu.add(item);
        menu.addSeparator();
        
        // TODO: fix this hidden coupling, SharedSearchResult needs to be tied MyFilesResultPanel
        //        explicitly
        menu.add(new SkinMenuItem(((MySharedFilesResultPanel)resultPanel).STOP_SHARING_FILE_LISTENER));
 

        return menu;
    }

    public void initialize(TableLine line) {
        line.createEndpointHolder("127.0.0.1", this.rfd.getPort(), false);
        
        line.setAddedOn(this.rfd.getCreationTime());
    }
}
