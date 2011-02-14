package com.limegroup.gnutella.gui.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.limewire.io.IpPort;

import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.BitziLookupAction;
import com.limegroup.gnutella.gui.actions.CopyMagnetLinkToClipboardAction;
import com.limegroup.gnutella.gui.util.PopupUtils;
import com.limegroup.gnutella.search.HostData;
import com.limegroup.gnutella.settings.UISettings;
import com.limegroup.gnutella.xml.LimeXMLDocument;

/**
 * A single SearchResult.
 *
 * (A collection of RemoteFileDesc, HostData, and Set of alternate locations.)
 */
final class GnutellaSearchResult extends AbstractSearchResult {
    private final RemoteFileDesc RFD;
    private final HostData DATA;
    private Set<IpPort> _alts;
    
    /**
     * Constructs a new SearchResult with the given data.
     */
    GnutellaSearchResult(RemoteFileDesc rfd, HostData data, Set<IpPort> alts) {
        RFD = rfd;
        DATA = data;
        if(UISettings.UI_ADD_REPLY_ALT_LOCS.getValue())
            _alts = alts;
        else
            _alts = Collections.emptySet();
    }
    
    @Override
    public boolean canBeMarkedAsJunk() {
    	return true;
    }
    
    /** Gets the RemoteFileDesc */
    RemoteFileDesc getRemoteFileDesc() { return RFD; }
    
    /** Gets the HostData */
    HostData getHostData() { return DATA; }
    
    /** Gets the Alternate Locations */
    Set<IpPort> getAlts() { return _alts; }
    
    /**
     * Clears the alternate locations for this SearchResult.
     */
    void clearAlts() {
        _alts = null;
    }
    
    /**
     * Sets the alternate locations for this SearchResult.
     */
    void setAlts(Set<IpPort> alts) {
        _alts = alts;
    }
    
    public String getFileName() {
        return RFD.getFileName();
    }

    public long getSize() {
        return RFD.getSize();
    }

    public URN getSHA1Urn() {
        return RFD.getSHA1Urn();
    }

    public LimeXMLDocument getXMLDocument() {
        return RFD.getXMLDocument();
    }

    public long getCreationTime() {
        return RFD.getCreationTime();
    }

    public boolean isDownloading() {
        return RFD.isDownloading();
    }

    public String getVendor() {
        return RFD.getVendor();
    }

    public int getQuality() {
        return RFD.getQuality();
    }

    public int getSecureStatus() {
        return RFD.getSecureStatus();
    }

    public int getSpeed() {
        return DATA.getSpeed();
    }

    public boolean isMeasuredSpeed() {
        return false;
    }

    public float getSpamRating() {
        return RFD.getSpamRating();
    }

    public String getHost() {
        return RFD.getHost();
    }

    public void takeAction(TableLine line, GUID guid, File saveDir, String fileName, boolean saveAs, SearchInformation searchInfo) {
        SearchMediator.downloadGnutellaLine(line, guid, saveDir, fileName, saveAs, searchInfo);
    }

    public void initialize(TableLine line) {
        RemoteFileDesc rfd = getRemoteFileDesc();
        Set<IpPort> alts = getAlts();

        if (rfd.isChatEnabled()) {
            line.setChatHost(rfd);
        }
        if (rfd.isBrowseHostEnabled()) {
            line.setBrowseHost(rfd);
        }
        if (!rfd.isFirewalled()) {
            line.setNonFirewalledHost(rfd);
        }
        line.createEndpointHolder(
            rfd.getHost(), rfd.getPort(),
            rfd.isReplyToMulticast());

        line.setAddedOn(rfd.getCreationTime());

        if(alts != null && !alts.isEmpty()) {
            Set<IpPort> as = line.getAltIpPortSet();
            as.addAll(alts);
            clearAlts();
            line.getLocation().addHosts(alts);
        }
    }

    public JPopupMenu createMenu(JPopupMenu popupMenu, TableLine[] lines, boolean markAsSpam, boolean markAsNot, ResultPanel resultPanel) {
        
    	PopupUtils.addMenuItem(I18n.tr("Buy this item now"), resultPanel.BUY_LISTENER, 
    			popupMenu, lines.length == 1, 0);
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_STRING, resultPanel.DOWNLOAD_LISTENER,
                popupMenu, lines.length > 0, 1);
        PopupUtils.addMenuItem(I18n.tr("Download As..."), resultPanel.DOWNLOAD_AS_LISTENER,
                popupMenu, lines.length == 1, 2);
        PopupUtils.addMenuItem(I18n.tr("View License"), new LicenseListener(resultPanel),
                popupMenu, lines.length > 0 && lines[0].isLicenseAvailable(), 3);
        PopupUtils.addMenuItem(SearchMediator.CHAT_STRING, resultPanel.CHAT_LISTENER, popupMenu,
                lines.length > 0 && lines[0].isChatEnabled(), 4);
        PopupUtils.addMenuItem(SearchMediator.BROWSE_HOST_STRING, resultPanel.BROWSE_HOST_LISTENER,
                popupMenu, lines.length > 0 && lines[0].isBrowseHostEnabled(), 5);
        PopupUtils.addMenuItem(SearchMediator.BLOCK_STRING, new BlockListener(resultPanel),
                popupMenu, lines.length > 0, 6);
        
        JMenu spamMenu = new JMenu(SearchMediator.MARK_AS_STRING);
        spamMenu.setEnabled(markAsSpam || markAsNot);
        PopupUtils.addMenuItem(SearchMediator.SPAM_STRING, resultPanel.MARK_AS_SPAM_LISTENER,
                spamMenu, markAsSpam);
        PopupUtils.addMenuItem(SearchMediator.NOT_SPAM_STRING,
                resultPanel.MARK_AS_NOT_SPAM_LISTENER, spamMenu, markAsNot);
        popupMenu.add(spamMenu, 6);
        
        popupMenu.add(new JPopupMenu.Separator(), 7);
        popupMenu.add(createAdvancedMenu(lines.length > 0 ? lines[0] : null, resultPanel), 8);
        
        return popupMenu;
    }
    
    
    /**
     * This may return null for non-gnutella search results.
     * 
     * @param line
     * @return
     */
    private JMenu createAdvancedMenu(TableLine line, ResultPanel resultPanel) {
        JMenu menu = new JMenu(I18n.tr("Advanced"));
        
        if (line == null) {
            menu.setEnabled(false);
            return menu;
        }
        
        BitziLookupAction bitziAction = new BitziLookupAction(resultPanel);

        bitziAction.setEnabled(line.getRemoteFileDesc().getSHA1Urn() != null);
        menu.add(new JMenuItem(bitziAction));
        
        CopyMagnetLinkToClipboardAction magnet =
            new CopyMagnetLinkToClipboardAction(resultPanel);
        magnet.setEnabled(line.hasNonFirewalledRFD());
        menu.add(new JMenuItem(magnet));
        
        // launch action
        if(line.isLaunchable()) {
            menu.addSeparator();
            PopupUtils.addMenuItem(SearchMediator.LAUNCH_STRING, resultPanel.DOWNLOAD_LISTENER, 
                    menu.getPopupMenu(), true);
        }

        
        return menu;
    }    
    


    private static class LicenseListener implements ActionListener {
        private final ResultPanel p;
        LicenseListener(ResultPanel p) {
            this.p = p;
        }

        public void actionPerformed(ActionEvent e) {
            p.showLicense();
        }
    }

    private static class BlockListener implements ActionListener {
        private final ResultPanel p;
        BlockListener(ResultPanel p) {
            this.p = p;
        }

        public void actionPerformed(ActionEvent e) {
            p.blockHost();
        }
    }
   


}