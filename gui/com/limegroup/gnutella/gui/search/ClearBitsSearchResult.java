package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.JPopupMenu;

import com.frostwire.bittorrent.websearch.clearbits.ClearBitsItem;
import com.limegroup.bittorrent.settings.BittorrentSettings;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.themes.ThemeFileHandler;
import com.limegroup.gnutella.gui.util.PopupUtils;
import com.limegroup.gnutella.http.HTTPUtils;
import com.limegroup.gnutella.xml.LimeXMLDocument;

public class ClearBitsSearchResult extends AbstractSearchResult {
	public static String redirectUrl=null;
	
	private boolean _isDownloading;
	private ClearBitsItem _item;
	private SearchInformation _info;
	
	public ClearBitsSearchResult(ClearBitsItem item, SearchInformation info) {
		_item = item;
		_info = info;
		_isDownloading = false;
	}
	
	@Override
	public long getCreationTime() {
		//2010-07-15T16:02:42Z
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		long result = System.currentTimeMillis();
		try {
			result = date.parse(_item.created_at).getTime();
		} catch (ParseException e) {
		}
		return result;
	}

	@Override
	public Color getEvenRowColor() {
		return ThemeFileHandler.TABLE_BACKGROUND_COLOR.getValue();
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
		return "http://www.mininova.org";
	}

	@Override
	public Color getOddRowColor() {
		return ThemeFileHandler.TABLE_ALTERNATE_COLOR.getValue();
	}

	@Override
	public int getQuality() {
		return QualityRenderer.EXCELLENT_QUALITY;
	}

	@Override
	public URN getSHA1Urn() {
		return null;
	}

	@Override
	public int getSecureStatus() {
		return 0;
	}

	@Override
	public long getSize() {
		return Long.valueOf(_item.mb_size * 1024 * 1024);
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
		return "ClearBits";
	}

	@Override
	public LimeXMLDocument getXMLDocument() {
		return null;
	}

	@Override
	public void initialize(TableLine line) {
		line.setAddedOn(getCreationTime());
		int seeds = Integer.valueOf(_item.seeds);
		line.initLocations(seeds);
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
			GUIMediator.instance().openTorrentURI(new URI(_item.torrent_url));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		showTorrentDetails();
	}
	
	private void showTorrentDetails() {
		showTorrentDetails(1250);
	}
	
	private void showTorrentDetails(long delay) {
		if (!BittorrentSettings.TORRENT_DETAIL_PAGE_SHOWN_AFTER_DOWNLOAD.getValue())
			return;
		
		try {
			if (redirectUrl != null) {
				String queryParam="q="+ HTTPUtils.encode(_info.getQuery(), "utf-8");
				String torrentDetailsURL = "u="+ HTTPUtils.encode(_item.location,"utf-8");
				String torrentFileName = "t=" + HTTPUtils.encode(getFileName(),"utf-8");
				GUIMediator.waitAndOpenURL(redirectUrl + "?"+queryParam+"&"+torrentDetailsURL+"&"+torrentFileName,delay);
			} else
				GUIMediator.waitAndOpenURL(_item.location, delay);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
