package com.limegroup.gnutella.gui.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JPopupMenu;

import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.gui.GuiFrostWireUtils;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.search.db.TorrentDBPojo;
import com.limegroup.gnutella.gui.search.db.TorrentFileDBPojo;
import com.limegroup.gnutella.gui.util.PopupUtils;
import com.limegroup.gnutella.settings.BittorrentSettings;

public class SmartSearchResult extends AbstractSearchResult implements BittorrentSearchResult {
    private WebSearchResult _item;
    private SearchEngine _searchEngine;

    TorrentDBPojo torrent;
    TorrentFileDBPojo file;

    public SmartSearchResult(TorrentDBPojo torrentPojo, TorrentFileDBPojo torrentFilePojo) {
        torrent = torrentPojo;
        _item = new WebSearchResultProxy(torrent);
        file = torrentFilePojo;
        _searchEngine = SearchEngine.getSearchEngineById(torrentPojo.searchEngineID);
    }

    @Override
    public long getCreationTime() {
        return _item.getCreationTime();
    }

    @Override
    public String getExtension() {
        return file.relativePath.substring(file.relativePath.lastIndexOf(".") + 1).toLowerCase();
    }

    @Override
    public String getFileName() {
        if (file.relativePath.startsWith("/")) {
            file.relativePath = file.relativePath.substring(1);
        }

        return new File(file.relativePath).getName();
    }

    @Override
    public String getFilenameNoExtension() {

        if (file.relativePath.indexOf("/") != -1) {
            String fileName = file.relativePath.substring(file.relativePath.lastIndexOf("/"));

            if (fileName.startsWith("/")) {
                fileName = fileName.substring(1);
            }

            return fileName.substring(0, fileName.lastIndexOf("."));
        }

        return file.relativePath.substring(0, file.relativePath.lastIndexOf("."));
    }

    @Override
    public int getQuality() {
        return 0;
    }

    public String getHash() {
        return _item.getHash();
    }

    public String getTorrentURI() {
        return _item.getTorrentURI();
    }

    @Override
    public long getSize() {
        return file.size;
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
    public void initialize(SearchResultDataLine line) {
        line.setAddedOn(getCreationTime());

        //hack this to show the icon for mininova or for isohunt.
    }

    @Override
    public boolean isMeasuredSpeed() {
        return false;
    }

    @Override
    public void takeAction(SearchResultDataLine line, GUID guid, File saveDir, String fileName, boolean saveAs, SearchInformation searchInfo) {
        GUIMediator.instance().openTorrentSearchResult(_item, file.relativePath);
        showTorrentDetails(BittorrentSettings.SHOW_TORRENT_DETAILS_DELAY);
    }

    public void showTorrentDetails(long delay) {
        GuiFrostWireUtils.showTorrentDetails(delay, _searchEngine.redirectUrl, "", _item.getTorrentDetailsURL(), getFileName());
    }

    @Override
    public JPopupMenu createMenu(JPopupMenu popupMenu, SearchResultDataLine[] lines, SearchResultMediator resultPanel) {
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                takeAction(null, null, null, null, false, null);
            }
        }, popupMenu, lines.length > 0, 1);

        PopupUtils.addMenuItem(SearchMediator.TORRENT_DETAILS_STRING, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showTorrentDetails(-1);
            }
        }, popupMenu, lines.length == 1, 2);

        return popupMenu;
    }

    public int getSeeds() {
        return _item.getSeeds();
    }

    public SearchEngine getSearchEngine() {
        return _searchEngine;
    }

    public WebSearchResult getWebSearchResult() {
        return _item;
    }

    private class WebSearchResultProxy implements WebSearchResult {

        private TorrentDBPojo _torrentDBPojo;

        public WebSearchResultProxy(TorrentDBPojo torrentDBPojo) {
            _torrentDBPojo = torrentDBPojo;
        }

        @Override
        public String getFileName() {
            return _torrentDBPojo.fileName;
        }

        @Override
        public long getSize() {
            return _torrentDBPojo.size;
        }

        @Override
        public long getCreationTime() {
            return _torrentDBPojo.creationTime;
        }

        @Override
        public String getVendor() {
            return _torrentDBPojo.vendor;
        }

        @Override
        public String getFilenameNoExtension() {
            return _torrentDBPojo.fileNameNoExtension;
        }

        @Override
        public String getHash() {
            return _torrentDBPojo.hash;
        }

        @Override
        public String getTorrentURI() {
            return _torrentDBPojo.torrentURI;
        }

        @Override
        public int getSeeds() {
            return _torrentDBPojo.seeds;
        }

        @Override
        public String getTorrentDetailsURL() {
            return _torrentDBPojo.torrentDetailsURL;
        }

    }
}
