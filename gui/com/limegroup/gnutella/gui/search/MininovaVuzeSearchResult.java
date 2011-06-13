package com.limegroup.gnutella.gui.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.JPopupMenu;

import com.frostwire.GuiFrostWireUtils;
import com.frostwire.bittorrent.settings.BittorrentSettings;
import com.frostwire.bittorrent.websearch.mininova.MininovaVuzeItem;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.util.PopupUtils;
import com.limegroup.gnutella.xml.LimeXMLDocument;

public class MininovaVuzeSearchResult extends AbstractSearchResult {

    public static String redirectUrl = null;
    private MininovaVuzeItem _item;
    private SearchInformation _info;

    public MininovaVuzeSearchResult(MininovaVuzeItem item, SearchInformation searchInfo) {
        _item = item;
        _info = searchInfo;
    }

    @Override
    public long getCreationTime() {
        SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
        long result = System.currentTimeMillis();
        try {
            result = date.parse(_item.date).getTime();
        } catch (ParseException e) {
        }
        return result;
    }

    @Override
    public String getExtension() {
        return "torrent";
    }

    @Override
    public String getFileName() {
        String titleNoTags = _item.title.replace("<b>", "").replace("</b>", "");
        return titleNoTags + ".torrent";
    }

    @Override
    public String getFilenameNoExtension() {
        return "<html>" + _item.title + "</html>";
    }

    @Override
    public String getHost() {
        return "http://www.mininova.org";
    }

    @Override
    public int getQuality() {
        return QualityRenderer.EXCELLENT_QUALITY;
    }

    public String getHash() {
        return _item.hash;
    }

    public String getTorrentURI() {
        return _item.download;
    }

    @Override
    public int getSecureStatus() {
        return 0;
    }

    @Override
    public long getSize() {
        return Long.valueOf(_item.size);
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
        return "Mininova";
    }

    @Override
    public LimeXMLDocument getXMLDocument() {
        return null;
    }

    @Override
    public void initialize(TableLine line) {
        line.setAddedOn(getCreationTime());
    }

    @Override
    public boolean isMeasuredSpeed() {
        return false;
    }

    @Override
    public void takeAction(TableLine line, GUID guid, File saveDir, String fileName, boolean saveAs, SearchInformation searchInfo) {
        GUIMediator.instance().openTorrentURI(_item.download);
        showTorrentDetails(BittorrentSettings.SHOW_TORRENT_DETAILS_DELAY);
    }

    public void showTorrentDetails(long delay) {
        GuiFrostWireUtils.showTorrentDetails(delay, redirectUrl, _info.getQuery(), _item.cdp, getFileName());
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
                showTorrentDetails(0);
            }
        }, popupMenu, lines.length == 1, 3);

        return popupMenu;
    }

    public int getSeeds() {
        return _item.seeds + _item.superseeds;
    }
    
    public SearchEngine getSearchEngine() {
        return SearchEngine.MININOVA;
    }
}
