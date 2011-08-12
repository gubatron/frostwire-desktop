package com.limegroup.gnutella.gui.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JPopupMenu;

import org.gudy.azureus2.core3.torrent.TOTorrentFile;

import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.gui.GuiFrostWireUtils;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.util.PopupUtils;
import com.limegroup.gnutella.settings.BittorrentSettings;

public class DeepSearchResult extends AbstractSearchResult {

    private WebSearchResult _item;
    private SearchEngine _searchEngine;
    private SearchInformation _info;
    private TOTorrentFile _torrentFile;

    public DeepSearchResult(TOTorrentFile torrentFile, WebSearchResult item, SearchEngine searchEngine, SearchInformation searchInfo) {
        _item = item;
        _searchEngine = searchEngine;
        _info = searchInfo;
        _torrentFile = torrentFile;
    }

    @Override
    public long getCreationTime() {
        return _item.getCreationTime();
    }

    @Override
    public String getExtension() {
        return _torrentFile.getRelativePath().substring(_torrentFile.getRelativePath().lastIndexOf(".")+1);
    }

    @Override
    public String getFileName() {
    	String fName = new File(_torrentFile.getRelativePath()).getName();
    	if (fName.startsWith("/")) {
    		return fName.substring(1);
    	}
    	return fName;
    }

    @Override
    public String getFilenameNoExtension() {
    	if (_torrentFile.getRelativePath().indexOf("/") != -1) {
    		String fileName = _torrentFile.getRelativePath().substring(_torrentFile.getRelativePath().lastIndexOf("/"));
    		return fileName.substring(0,fileName.lastIndexOf("."));
    	}
    	
    	String fName = _torrentFile.getRelativePath().substring(0, _torrentFile.getRelativePath().lastIndexOf("."));
    	if (fName.startsWith("/")) {
    		return fName.substring(1);
    	}
    	
    	return fName;
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
        return _torrentFile.getLength();
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
        GUIMediator.instance().openTorrentSearchResult(_item, _torrentFile.getRelativePath());
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
}
