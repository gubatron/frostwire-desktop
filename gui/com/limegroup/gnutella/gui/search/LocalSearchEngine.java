package com.limegroup.gnutella.gui.search;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloader;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderCallBackInterface;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderFactory;
import org.gudy.azureus2.core3.util.TorrentUtils;

import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.gui.bittorrent.TorrentUtil;
import com.frostwire.gui.filters.SearchFilter;
import com.limegroup.gnutella.GUID;

public class LocalSearchEngine {
	
	private static final int DEEP_SEARCH_DELAY = 1000;
	private static final int DEEP_SEARCH_MINIMUM_RESULTS = 25;
	private static final int DEEP_SEARCH_ROUNDS = 5;

	private static LocalSearchEngine INSTANCE;

	/** We'll keep here every info hash we've already processed during the session */
	private HashSet<String> KNOWN_INFO_HASHES = new HashSet<String>();
	
	public static LocalSearchEngine instance() {
		if (INSTANCE == null) {
			INSTANCE = new LocalSearchEngine();
		}

		return INSTANCE;
	}

	/** Perform a simple Database Search, immediate results should be available if there are matches.*/
	public List<LocalSearchResult> search(String query) {
		//TODO
		return new ArrayList<LocalSearchResult>();
	}
	
	public List<LocalSearchResult> deepSearch(byte[] guid, String query, SearchInformation info) {
		ResultPanel rp = null;
		
		//Let's wait for enough search results from different search engines.
		sleep();

		//Wait for enough results or die if the ResultPanel has been closed.
		int tries = DEEP_SEARCH_ROUNDS;

		for (int i=tries; i > 0; i--) {
			if ((rp=SearchMediator.getResultPanelForGUID(new GUID(guid)))==null) {
				return null;
			}
			
			scanAvailableResults(guid, query, info, rp);
			
			sleep();
		}
		
		//did they close rp? nothing left to do.
		if (rp==null) {
			return null;
		}
		
		return null;
	}

	public void sleep() {
		try {
			Thread.sleep(DEEP_SEARCH_DELAY);
		} catch (InterruptedException e1) {
		}
	}

	public void scanAvailableResults(byte[] guid, String query,
			SearchInformation info, ResultPanel rp) {
		for (int i=0; i < Math.min(rp.getSize(), DEEP_SEARCH_MINIMUM_RESULTS); i++) {
			TableLine line = rp.getLine(i);
			WebSearchResult webSearchResult = line.getSearchResult().getWebSearchResult();

			if (!KNOWN_INFO_HASHES.contains(webSearchResult.getHash())) {
				KNOWN_INFO_HASHES.add(webSearchResult.getHash());
				SearchEngine searchEngine = line.getSearchEngine();
				scanDotTorrent(webSearchResult, guid, query, searchEngine, info);
			}
		}
	}
	
	/**
	 * Will decide wether or not to fetch the .torrent from the DHT.
	 * 
	 * If it has to download it, it will use a LocalSearchTorrentDownloaderListener to start scanning,
	 * if the torrent has already been fetched, it will perform an immediate search.
	 * @param webSearchResult
	 * @param searchEngine 
	 * @param info 
	 */
	private void scanDotTorrent(WebSearchResult webSearchResult, byte[] guid, String query, SearchEngine searchEngine, SearchInformation info) {
		if (!torrentHasBeenIndexed()) {
			//download the torrent
			String saveDir =null;
			TorrentDownloaderFactory.create(new LocalSearchTorrentDownloaderListener(guid, query, webSearchResult, searchEngine, info), TorrentUtil.getMagnet(webSearchResult.getHash()), null, saveDir).start();
		}
	}

	private boolean torrentHasBeenIndexed() {
		return false;
	}
	
	private void indexTorrent(TOTorrent theTorrent) {
		// TODO Auto-generated method stub
		System.out.println("LocalSearchEngine.indexTorrent() UNIMPLEMENTED.");
	}


	private class LocalSearchTorrentDownloaderListener implements TorrentDownloaderCallBackInterface {
		
		private AtomicBoolean finished = new AtomicBoolean(false);
		private byte[] guid;
		private List<String> query;
		private SearchEngine searchEngine;
		private WebSearchResult webSearchResult;
		private SearchInformation info;
		private List<String> substractedQuery;
		private List<String> tokensTorrent;
		
		public LocalSearchTorrentDownloaderListener(byte[] guid, String query, WebSearchResult webSearchResult, SearchEngine searchEngine, SearchInformation info) {
			this.guid = guid;
			this.query = Arrays.asList(query.toLowerCase().split(" "));
			this.searchEngine = searchEngine;
			this.webSearchResult = webSearchResult;
			this.info =info;
			
			initSubstractedQuery();
		}

		private void initSubstractedQuery() {
			//substract the query keywords that are already in the webSearchResult title.
			
			String torrentFileNameNoExtension = webSearchResult.getFilenameNoExtension();
			List<String> tokensQuery = new ArrayList<String>(query);
			tokensTorrent = Arrays.asList(torrentFileNameNoExtension.toLowerCase().split(" "));
			
			tokensQuery.removeAll(tokensTorrent);
			this.substractedQuery = tokensQuery;
		}

		@Override
		public void TorrentDownloaderEvent(int state, TorrentDownloader inf) {
			
			//index the torrent (insert it's structure in the local DB)
			if (state == TorrentDownloader.STATE_FINISHED && finished.compareAndSet(false,true)) {
				try {
					File torrentFile = inf.getFile();
					TOTorrent theTorrent = TorrentUtils.readFromFile(torrentFile , false);
					torrentFile.delete();
					
					indexTorrent(theTorrent);
					
					//search right away on this torrent.
					matchResults(theTorrent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void matchResults(TOTorrent theTorrent) {
			ResultPanel rp = SearchMediator.getResultPanelForGUID(new GUID(guid));
			
			//user closed the tab.
			if (rp == null) {
				return;
			}
			
			SearchFilter filter = SearchMediator.getSearchFilterFactory().createFilter();
			
			
			TOTorrentFile[] fs = theTorrent.getFiles();
			for (int i=0; i < fs.length ; i++) {
				LocalSearchResult result = new LocalSearchResult(fs[i],webSearchResult, searchEngine, info);
				
				if (!filter.allow(result))
					continue;
				
				boolean foundMatch = true;
				
				List<String> selectedQuery = substractedQuery;
				
				//if all tokens happened to be on the title of the torrent, we'll just use the full query.
				if (substractedQuery.size() == 0) {
					selectedQuery = query;
				}

				//Steve Jobs style first (like iTunes search logic)
				for (String token : selectedQuery) {
					if (!result.getFileName().toLowerCase().contains(token)) {
						foundMatch = false;
						break;
					}
				}
				
				//best match ever, Steve Jobs style.
				if (foundMatch) {
					SearchMediator.getSearchResultDisplayer().addQueryResult(guid, result, rp);
					return;
				}

				//if Steve Jobs is too good for ya...
				//we'll remove the tokens of the torrent title ONCE from the search result name
				//and we'll perform a match on what's left.
				String resultName = result.getFileName().toLowerCase();
				
				HashSet<String> torrentTokenSet = new HashSet<String>(tokensTorrent);
				for (String torrentToken : torrentTokenSet) {
					try {
						torrentToken = torrentToken.replace("(", "").replace(")", "").replace("[", "").replace("]","");
						resultName = resultName.replaceFirst(torrentToken, "");
					} catch (Exception e) {
						//shhh
					}
				}
				
				foundMatch = true; //optimism!
				
				for (String token : selectedQuery) {
					if (!resultName.contains(token)) {
						foundMatch = false;
						break;
					}
				}

				if (foundMatch) {
					SearchMediator.getSearchResultDisplayer().addQueryResult(guid, result, rp);
					return;
				}
				
				//desperate OR search? we'll see.
				
			}
			
		}
	}

}
