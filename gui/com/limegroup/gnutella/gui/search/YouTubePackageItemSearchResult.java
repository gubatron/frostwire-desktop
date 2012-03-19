package com.limegroup.gnutella.gui.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JPopupMenu;

import org.limewire.util.FilenameUtils;

import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.gui.GuiFrostWireUtils;
import com.frostwire.websearch.youtube.YouTubeSearchResult;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.util.PopupUtils;
import com.limegroup.gnutella.settings.BittorrentSettings;

public final class YouTubePackageItemSearchResult extends AbstractSearchResult {

    private final YouTubeSearchResult sr;
    private final FilePackage filePackage;
    private final SearchEngine searchEngine;
    private final SearchInformation info;
    
    private final String filename;
    
    public YouTubePackageItemSearchResult(YouTubeSearchResult sr, FilePackage filePackage, SearchEngine searchEngine, SearchInformation info) {
        this.sr = sr;
        this.filePackage = filePackage;
        this.searchEngine = searchEngine;
        this.info = info;
        
        this.filename = readFilename(filePackage);
    }
    
    @Override
    public String getFileName() {
        return filename;
    }
    
    @Override
    public String getFilenameNoExtension() {
        return FilenameUtils.getBaseName(filename);
    }

    @Override
    public long getSize() {
        return sr.getSize();
    }

    @Override
    public long getCreationTime() {
        return sr.getCreationTime();
    }

    @Override
    public String getVendor() {
        return sr.getVendor();
    }

    @Override
    public int getSpeed() {
        return Integer.MAX_VALUE - 2;
    }

    @Override
    public boolean isMeasuredSpeed() {
        return false;
    }

    @Override
    public int getQuality() {
        return 0;
    }

    @Override
    public float getSpamRating() {
        return 0;
    }

    @Override
    public void takeAction(SearchResultDataLine line, GUID guid, File saveDir, String fileName, boolean saveAs, SearchInformation searchInfo) {
        GUIMediator.instance().openYouTubeItem(filePackage);
    }

    @Override
    public void initialize(SearchResultDataLine line) {
        line.setAddedOn(getCreationTime());
    }

    @Override
    public JPopupMenu createMenu(JPopupMenu popupMenu, SearchResultDataLine[] lines, SearchResultMediator rp) {
        PopupUtils.addMenuItem(SearchMediator.BUY_NOW_STRING, rp.BUY_LISTENER, popupMenu, lines.length == 1, 0);
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                takeAction(null, null, null, null, false, null);
            }
        }, popupMenu, lines.length > 0, 1);
        PopupUtils.addMenuItem(SearchMediator.YOUTUBE_DETAILS_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showTorrentDetails(-1);
            }
        }, popupMenu, lines.length == 1, 2);

        return popupMenu;
    }

    @Override
    public void showTorrentDetails(long delay) {
        GuiFrostWireUtils.showTorrentDetails(delay, searchEngine.redirectUrl, info.getQuery(), sr.getTorrentDetailsURL(), getFileName());
    }

    @Override
    public String getHash() {
        return sr.getHash();
    }

    @Override
    public String getTorrentURI() {
        return sr.getTorrentURI();
    }

    @Override
    public int getSeeds() {
        return sr.getSeeds();
    }

    @Override
    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    @Override
    public WebSearchResult getWebSearchResult() {
        return sr;
    }
    
    private String readFilename(FilePackage filePackage) {
        DownloadLink dl = filePackage.getChildren().get(0);
        if (dl.getStringProperty("convertto", "").equals("AUDIOMP3")) {
            return FilenameUtils.getBaseName(dl.getName()) + ".mp3";
        }

        return dl.getName();
    }
}
