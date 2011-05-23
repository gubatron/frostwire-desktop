package com.limegroup.gnutella.gui.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.JPopupMenu;

import com.frostwire.GuiFrostWireUtils;
import com.frostwire.bittorrent.settings.BittorrentSettings;
import com.frostwire.bittorrent.websearch.isohunt.ISOHuntItem;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.util.PopupUtils;
import com.limegroup.gnutella.xml.LimeXMLDocument;

public class ISOHuntSearchResult extends AbstractSearchResult  {

	public static String redirectUrl=null;
	private boolean _isDownloading;
	private ISOHuntItem _item;
	private SearchInformation _info;
	
	public ISOHuntSearchResult(ISOHuntItem item, SearchInformation searchInfo) {
		_item = item;
		_info = searchInfo;
		_isDownloading = false;
	}

	@Override
	public long getCreationTime() {
		//Thu, 29 Apr 2010 16:32:44 GMT
		SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		long result = System.currentTimeMillis();
		try {
			result = date.parse(_item.pubDate).getTime();
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
		return  titleNoTags + ".torrent";
	}

	@Override
	public String getFilenameNoExtension() {
		return "<html>"+_item.title+"</html>";
	}

	@Override
	public String getHost() {
		return _item.tracker;
	}

	@Override
	public int getQuality() {
		return QualityRenderer.EXCELLENT_QUALITY;//(int)(Double.parseDouble(_item.Seeds) / Double.parseDouble(_item.leechers));
	}

	public String getHash() {
        return _item.hash;
    }

	@Override
	public int getSecureStatus() {
		return 0;
	}

	@Override
	public long getSize() {
		return Long.valueOf(_item.length);
	}

	@Override
	public float getSpamRating() {
		return 0;
	}

	@Override
	public int getSpeed() {
		return Integer.MAX_VALUE-2;
	}

	@Override
	public String getVendor() {
		return "ISOHunt";
	}

	@Override
	public LimeXMLDocument getXMLDocument() {
		return null;
	}

	@Override
	public void initialize(TableLine line) {
		line.setAddedOn(getCreationTime());
		int seeds = 0;
		
		try {
		 seeds = Integer.valueOf(_item.Seeds);
		} catch (Exception e) {
			//oh well
		}

		line.initLocations(seeds);
		
		//hack this to show the icon for mininova or for isohunt.
	}

	@Override
	public boolean isDownloading() {
		return _isDownloading;
	}

	@Override
	public boolean isMeasuredSpeed() {
		return false;
	}

	@Override
	public void takeAction(TableLine line, GUID guid, File saveDir,
			String fileName, boolean saveAs, SearchInformation searchInfo) {
		_isDownloading = true;
		try {
			GUIMediator.instance().openTorrentURI(new URI(_item.enclosure_url));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		showTorrentDetails(BittorrentSettings.SHOW_TORRENT_DETAILS_DELAY);
	}
	
	public void showTorrentDetails(long delay) {
		GuiFrostWireUtils.showTorrentDetails(delay,redirectUrl,_info.getQuery(),_item.link,getFileName());
	}

	@Override
	public JPopupMenu createMenu(JPopupMenu popupMenu, TableLine[] lines,
			boolean markAsSpam, boolean markAsNot, ResultPanel resultPanel) {
		
		PopupUtils.addMenuItem(I18n.tr("Buy this item now"), resultPanel.BUY_LISTENER, 
    			popupMenu, lines.length == 1, 0);
        PopupUtils.addMenuItem(SearchMediator.DOWNLOAD_STRING, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				takeAction(null, null, null, null, false, null);
			}},
                popupMenu, lines.length > 0, 1);
        
        PopupUtils.addMenuItem(I18n.tr("Torrent Details"), new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showTorrentDetails(0);
			}}, popupMenu, lines.length == 1, 2);
        
        return popupMenu;
	}
}
