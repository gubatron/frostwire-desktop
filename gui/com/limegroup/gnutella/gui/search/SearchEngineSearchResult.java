package com.limegroup.gnutella.gui.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.JPopupMenu;

import com.frostwire.GuiFrostWireUtils;
import com.frostwire.bittorrent.settings.BittorrentSettings;
import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.bittorrent.websearch.isohunt.ISOHuntItem;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.util.PopupUtils;
import com.limegroup.gnutella.xml.LimeXMLDocument;

public class SearchEngineSearchResult extends AbstractSearchResult {

    private WebSearchResult _item;
    private SearchEngine _searchEngine;
    private SearchInformation _info;

    public SearchEngineSearchResult(WebSearchResult item, SearchEngine searchEngine, SearchInformation searchInfo) {
        _item = item;
        _searchEngine = searchEngine;
        _info = searchInfo;
    }

    @Override
    public long getCreationTime() {
        return _item.getCreationTime();
    }

    @Override
    public String getExtension() {
        return "torrent";
    }

    @Override
    public String getFileName() {
        return _item.getFileName();
    }

    @Override
    public String getFilenameNoExtension() {
        return _item.getFilenameNoExtension();
    }

    @Override
    public int getQuality() {
        return QualityRenderer.EXCELLENT_QUALITY;//(int)(Double.parseDouble(_item.Seeds) / Double.parseDouble(_item.leechers));
    }

    public String getHash() {
        return _item.getHash();
    }

    public String getTorrentURI() {
        return _item.getTorrentURI();
    }

    @Override
    public long getSize() {
        return _item.getSize();
    }

    @Override
    public float getSpamRating() {
        return 0;
    }

    @Override
    public int getSpeed() {
        return Integer.MAX_VALUE - 2;
    }

    @Override
    public String getVendor() {
        return _item.getVendor();
    }

    @Override
    public LimeXMLDocument getXMLDocument() {
        return null;
    }

    @Override
    public void initialize(TableLine line) {
        line.setAddedOn(getCreationTime());
        

        //hack this to show the icon for mininova or for isohunt.
    }

    @Override
    public boolean isMeasuredSpeed() {
        return false;
    }

    @Override
    public void takeAction(TableLine line, GUID guid, File saveDir, String fileName, boolean saveAs, SearchInformation searchInfo) {
        GUIMediator.instance().openTorrentURI(getTorrentURI());
        showTorrentDetails(BittorrentSettings.SHOW_TORRENT_DETAILS_DELAY);
    }

    public void showTorrentDetails(long delay) {
        GuiFrostWireUtils.showTorrentDetails(delay, _searchEngine.redirectUrl, _info.getQuery(), _item.getTorrentDetailsURL(), getFileName());
    }

    @Override
    public JPopupMenu createMenu(JPopupMenu popupMenu, TableLine[] lines, ResultPanel resultPanel) {

        PopupUtils.addMenuItem(SearchMediator.BUY_NOW_STRING, resultPanel.BUY_LISTENER, popupMenu, lines.length == 1, 0);
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                takeAction(null, null, null, null, false, null);
            }
        }, popupMenu, lines.length > 0, 1);
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_PARTIAL_FILES_STRING, resultPanel.DOWNLOAD_PARTIAL_FILES_LISTENER, popupMenu, lines.length == 1, 2);
        PopupUtils.addMenuItem(SearchMediator.TORRENT_DETAILS_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showTorrentDetails(-1);
            }
        }, popupMenu, lines.length == 1, 3);

        return popupMenu;
    }

    public int getSeeds() {
        return _item.getSeeds();
    }
    
    public SearchEngine getSearchEngine() {
        return _searchEngine;
    }
}
