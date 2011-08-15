package com.limegroup.gnutella.gui.search;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloader;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderCallBackInterface;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderFactory;
import org.gudy.azureus2.core3.util.TorrentUtils;

import com.frostwire.JsonEngine;
import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.gui.bittorrent.TorrentUtil;
import com.frostwire.gui.filters.SearchFilter;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.search.db.SmartSearchDB;
import com.limegroup.gnutella.gui.search.db.TorrentDBPojo;
import com.limegroup.gnutella.gui.search.db.TorrentFileDBPojo;
import com.limegroup.gnutella.settings.SearchSettings;
import com.limegroup.gnutella.util.FrostWireUtils;
import com.limegroup.gnutella.util.FrostWireUtils.IndexedMapFunction;

public class LocalSearchEngine {

	private static final int DEEP_SEARCH_DELAY = 1000;
	private static final int DEEP_SEARCH_MINIMUM_RESULTS = 25;
	private static final int DEEP_SEARCH_ROUNDS = 5;

	private static LocalSearchEngine INSTANCE;

	/**
	 * We'll keep here every info hash we've already processed during the
	 * session
	 */
	private HashSet<String> KNOWN_INFO_HASHES = new HashSet<String>();
	private SmartSearchDB DB;
	private JsonEngine JSON_ENGINE;

	public LocalSearchEngine() {
		DB = new SmartSearchDB(
				SearchSettings.SMART_SEARCH_DATABASE_FOLDER.getValue());
		JSON_ENGINE = new JsonEngine();
	}

	public static LocalSearchEngine instance() {
		if (INSTANCE == null) {
			INSTANCE = new LocalSearchEngine();
		}

		return INSTANCE;
	}

	public final static HashSet<String> IGNORABLE_KEYWORDS;

	static {
		IGNORABLE_KEYWORDS = new HashSet<String>();
		IGNORABLE_KEYWORDS.addAll(Arrays.asList("me", "you", "he", "she",
				"they", "them", "we", "us", "my", "your", "yours", "his",
				"hers", "theirs", "ours", "the", "of", "in", "on", "out", "to",
				"at", "as", "and", "by", "not", "is", "are", "am", "was",
				"were", "will", "be", "for"));
	}

	/**
	 * Avoid possible SQL errors due to escaping. Cleans all double spaces and
	 * trims.
	 * 
	 * @param str
	 * @return
	 */
	private final static String stringSanitize(String str) {
		str = str.replace("\\", "").replace("%", "").replace("_", "")
				.replace(";", "").replace("'", "''");

		while (str.indexOf("  ") != -1) {
			str = str.replace("  ", " ");
		}
		return str;
	}

	/**
	 * @param builder
	 * @param lastIndex
	 * @param uniqueQueryTokensArray
	 * @param columns
	 * @return
	 */
	private static String getWhereClause(final String[] uniqueQueryTokensArray,
			String... columns) {
		final StringBuilder builder = new StringBuilder();
		final int lastIndex = columns.length - 1;

		FrostWireUtils.map(Arrays.asList(columns),
				new IndexedMapFunction<String>() {
					// Create a where clause that considers all the given
					// columns for each of the words in the query.
					public void map(int i, String column) {

						int size = uniqueQueryTokensArray.length;

						for (int j = 0; j < size; j++) {
							String token = uniqueQueryTokensArray[j];
							builder.append(column 
									+ " LIKE '%"
									+ token
									+ "%' "
									+ ((i <= lastIndex || j < size) ? " OR "
											: ""));
						}
					}
				});

		String str = builder.toString();
		int index = str.lastIndexOf(" OR");

		return index > 0 ? str.substring(0, index) : str;
	}

	public final static String getOrWhereClause(String query, String... columns) {
		String[] queryTokens = stringSanitize(query).split(" ");

		// Let's make sure we don't send repeated tokens to SQL Engine
		Set<String> uniqueQueryTokens = new TreeSet<String>();
		for (int i = 0; i < queryTokens.length; i++) {
			String token = queryTokens[i];

			if (token.length() == 1 || uniqueQueryTokens.contains(token)
					|| IGNORABLE_KEYWORDS.contains(token.toLowerCase())) {
				continue;
			}

			uniqueQueryTokens.add(token);
		}

		String[] uniqueQueryTokensArray = uniqueQueryTokens
				.toArray(new String[] {});

		return getWhereClause(uniqueQueryTokensArray, columns);
	}

	/**
	 * Perform a simple Database Search, immediate results should be available
	 * if there are matches.
	 */
	public List<SmartSearchResult> search(String query) {
		query = query.toLowerCase();
		String orWhereClause = getOrWhereClause(query, "fileName");//,
				//"torrentName");
//		String sql = "SELECT Torrents.json, Files.json, torrentName, fileName FROM Torrents, Files WHERE Torrents.torrentId = Files.torrentId AND ("
//				+ orWhereClause + ") ";
		String sql = "SELECT Torrents.json, Files.json, torrentName, fileName FROM Torrents JOIN Files ON Torrents.torrentId = Files.torrentId WHERE ("
			+ orWhereClause + ") ";

		
		System.out.println(sql);

		long start = System.currentTimeMillis();
		List<List<Object>> rows = DB.query(sql);
		long delta = System.currentTimeMillis() - start;
		System.out.println("Found " + rows.size() + " results in " + delta + "ms.");

		List<SmartSearchResult> results = new ArrayList<SmartSearchResult>();

		// GUBENE
		for (List<Object> row : rows) {
			String torrentJSON = (String) row.get(0);
			torrentJSON = torrentJSON.replace("\'","'");
			
			String fileJSON = (String) row.get(1);
			fileJSON = fileJSON.replace("\'","'");
			
			String torrentName = (String) row.get(2);
			String fileName = (String) row.get(3);
			
			
			//if (queryTokens.length == 1
			//		|| allTokensInString(queryTokens, torrentName + " " + fileName)) {
			if (new MatchLogic(query, torrentName, fileName).matchResult()) {
				TorrentDBPojo torrentPojo = JSON_ENGINE.toObject(torrentJSON,
						TorrentDBPojo.class);
				TorrentFileDBPojo torrentFilePojo = JSON_ENGINE.toObject(
						fileJSON, TorrentFileDBPojo.class);
				
				results.add(new SmartSearchResult(torrentPojo, torrentFilePojo));
				KNOWN_INFO_HASHES.add(torrentPojo.hash);
				//System.out.println("Found result -> " + torrentFilePojo);
			}
		}

		return results;
	}


	public List<DeepSearchResult> deepSearch(byte[] guid, String query,
			SearchInformation info) {
		ResultPanel rp = null;

		// Let's wait for enough search results from different search engines.
		sleep();

		// Wait for enough results or die if the ResultPanel has been closed.
		int tries = DEEP_SEARCH_ROUNDS;

		for (int i = tries; i > 0; i--) {
			if ((rp = SearchMediator.getResultPanelForGUID(new GUID(guid))) == null) {
				return null;
			}

			scanAvailableResults(guid, query, info, rp);

			sleep();
		}

		// did they close rp? nothing left to do.
		if (rp == null) {
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
		for (int i = 0; i < Math.min(rp.getSize(), DEEP_SEARCH_MINIMUM_RESULTS); i++) {
			TableLine line = rp.getLine(i);
			WebSearchResult webSearchResult = line.getSearchResult()
					.getWebSearchResult();

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
	 * If it has to download it, it will use a
	 * LocalSearchTorrentDownloaderListener to start scanning, if the torrent
	 * has already been fetched, it will perform an immediate search.
	 * 
	 * @param webSearchResult
	 * @param searchEngine
	 * @param info
	 */
	private void scanDotTorrent(WebSearchResult webSearchResult, byte[] guid,
			String query, SearchEngine searchEngine, SearchInformation info) {
		if (!torrentHasBeenIndexed(webSearchResult.getHash())) {
			// download the torrent
			String saveDir = SearchSettings.SMART_SEARCH_DATABASE_FOLDER.getValue().getAbsolutePath();
			
			TorrentDownloaderFactory.create(
					new LocalSearchTorrentDownloaderListener(guid, query,
							webSearchResult, searchEngine, info),
					TorrentUtil.getMagnet(webSearchResult.getHash()), null,
					saveDir).start();
		}
	}

	private boolean torrentHasBeenIndexed(String infoHash) {
		List<List<Object>> rows = DB.query("SELECT * FROM Torrents WHERE infoHash LIKE '"+infoHash+"'");
		return rows.size() > 0;
	}

	private void indexTorrent(WebSearchResult searchResult, TOTorrent theTorrent, SearchEngine searchEngine) {
		TorrentDBPojo torrentPojo = new TorrentDBPojo();
		torrentPojo.creationTime = searchResult.getCreationTime();
		torrentPojo.fileName = stringSanitize(searchResult.getFileName());
		torrentPojo.hash = searchResult.getHash();
		torrentPojo.searchEngineID = searchEngine.getId();
		torrentPojo.seeds = searchResult.getSeeds();
		torrentPojo.size = searchResult.getSize();
		torrentPojo.torrentDetailsURL = searchResult.getTorrentDetailsURL();
		torrentPojo.torrentURI = searchResult.getTorrentURI();
		torrentPojo.vendor = searchResult.getVendor();
		
		String torrentJSON = JSON_ENGINE.toJson(torrentPojo);
		torrentJSON = torrentJSON.replace("'", "\'");
		
		int torrentID = DB.insert("INSERT INTO Torrents (infoHash, timestamp, torrentName, json) VALUES ('"+torrentPojo.hash+"', " +
				""+ System.currentTimeMillis() + ", '" + torrentPojo.fileName.toLowerCase() +"', '"+ torrentJSON + "')");
		
		TOTorrentFile[] files = theTorrent.getFiles();

		for (TOTorrentFile f : files) {
			TorrentFileDBPojo tfPojo = new TorrentFileDBPojo();
			tfPojo.relativePath = stringSanitize(f.getRelativePath());
			tfPojo.size = f.getLength();
			
			String fileJSON = JSON_ENGINE.toJson(tfPojo);
			fileJSON = fileJSON.replace("'", "\'");
			
			DB.insert("INSERT INTO Files (torrentId, fileName, json) VALUES ("+torrentID+", '"+tfPojo.relativePath.toLowerCase()+"', '"+fileJSON+"')");
			//System.out.println("INSERT INTO Files (torrentId, fileName, json) VALUES ("+torrentID+", '"+tfPojo.relativePath+"', '"+fileJSON+"')");
		}
		
	}

	private class LocalSearchTorrentDownloaderListener implements
			TorrentDownloaderCallBackInterface {

		private AtomicBoolean finished = new AtomicBoolean(false);
		private byte[] guid;
		private List<String> query;
		private SearchEngine searchEngine;
		private WebSearchResult webSearchResult;
		private SearchInformation info;
		private List<String> substractedQuery;
		private List<String> tokensTorrent;

		public LocalSearchTorrentDownloaderListener(byte[] guid, String query,
				WebSearchResult webSearchResult, SearchEngine searchEngine,
				SearchInformation info) {
			this.guid = guid;
			this.query = Arrays.asList(query.toLowerCase().split(" "));
			this.searchEngine = searchEngine;
			this.webSearchResult = webSearchResult;
			this.info = info;

			initSubstractedQuery();
		}

		private void initSubstractedQuery() {
			// substract the query keywords that are already in the
			// webSearchResult title.

			String torrentFileNameNoExtension = webSearchResult
					.getFilenameNoExtension();
			List<String> tokensQuery = new ArrayList<String>(query);
			tokensTorrent = Arrays.asList(torrentFileNameNoExtension
					.toLowerCase().split(" "));

			tokensQuery.removeAll(tokensTorrent);
			this.substractedQuery = tokensQuery;
		}

		@Override
		public void TorrentDownloaderEvent(int state, TorrentDownloader inf) {

			// index the torrent (insert it's structure in the local DB)
			if (state == TorrentDownloader.STATE_FINISHED
					&& finished.compareAndSet(false, true)) {
				try {
					File torrentFile = inf.getFile();
					TOTorrent theTorrent = TorrentUtils.readFromFile(
							torrentFile, false);
					torrentFile.delete();

					indexTorrent(webSearchResult, theTorrent, searchEngine);

					// search right away on this torrent.
					matchResults(theTorrent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void matchResults(TOTorrent theTorrent) {
			ResultPanel rp = SearchMediator
					.getResultPanelForGUID(new GUID(guid));

			// user closed the tab.
			if (rp == null) {
				return;
			}

			SearchFilter filter = SearchMediator.getSearchFilterFactory()
					.createFilter();

			TOTorrentFile[] fs = theTorrent.getFiles();
			for (int i = 0; i < fs.length; i++) {
				DeepSearchResult result = new DeepSearchResult(fs[i],
						webSearchResult, searchEngine, info);

				if (!filter.allow(result))
					continue;

				boolean foundMatch = true;

				List<String> selectedQuery = substractedQuery;

				// if all tokens happened to be on the title of the torrent,
				// we'll just use the full query.
				if (substractedQuery.size() == 0) {
					selectedQuery = query;
				}

				// Steve Jobs style first (like iTunes search logic)
				for (String token : selectedQuery) {
					if (!result.getFileName().toLowerCase().contains(token)) {
						foundMatch = false;
						break;
					}
				}

				// best match ever, Steve Jobs style.
				if (foundMatch) {
					SearchMediator.getSearchResultDisplayer().addQueryResult(
							guid, result, rp);
					return;
				}

				// if Steve Jobs is too good for ya...
				// we'll remove the tokens of the torrent title ONCE from the
				// search result name
				// and we'll perform a match on what's left.
				String resultName = result.getFileName().toLowerCase();

				HashSet<String> torrentTokenSet = new HashSet<String>(
						tokensTorrent);
				for (String torrentToken : torrentTokenSet) {
					try {
						torrentToken = torrentToken.replace("(", "")
								.replace(")", "").replace("[", "")
								.replace("]", "");
						resultName = resultName.replaceFirst(torrentToken, "");
					} catch (Exception e) {
						// shhh
					}
				}

				foundMatch = true; // optimism!

				for (String token : selectedQuery) {
					if (!resultName.contains(token)) {
						foundMatch = false;
						break;
					}
				}

				if (foundMatch) {
					SearchMediator.getSearchResultDisplayer().addQueryResult(
							guid, result, rp);
					return;
				}

			}

		}
	}

    private class MatchLogic {

        private List<String> query;
        private String torrentName;
        private String fileName;
        private List<String> substractedQuery;
        private List<String> tokensTorrent;

        public MatchLogic(String query, String torrentName, String fileName) {
            this.query = Arrays.asList(query.toLowerCase().split(" "));
            this.torrentName = torrentName.toLowerCase();
            this.fileName = fileName.toLowerCase();

            initSubstractedQuery();
        }

        private void initSubstractedQuery() {
            // substract the query keywords that are already in the
            // webSearchResult title.

            String torrentFileNameNoExtension = torrentName;
            List<String> tokensQuery = new ArrayList<String>(query);
            tokensTorrent = Arrays.asList(torrentFileNameNoExtension.toLowerCase().split(" "));

            tokensQuery.removeAll(tokensTorrent);
            this.substractedQuery = tokensQuery;
        }

        public boolean matchResult() {

            boolean foundMatch = true;

            List<String> selectedQuery = substractedQuery;

            // if all tokens happened to be on the title of the torrent,
            // we'll just use the full query.
            if (substractedQuery.size() == 0) {
                selectedQuery = query;
            }

            // Steve Jobs style first (like iTunes search logic)
            for (String token : selectedQuery) {
                if (!fileName.contains(token)) {
                    foundMatch = false;
                    break;
                }
            }

            // best match ever, Steve Jobs style.
            if (foundMatch) {
                return true;
            }

            // if Steve Jobs is too good for ya...
            // we'll remove the tokens of the torrent title ONCE from the
            // search result name
            // and we'll perform a match on what's left.
            String resultName = fileName;

            HashSet<String> torrentTokenSet = new HashSet<String>(tokensTorrent);
            for (String torrentToken : torrentTokenSet) {
                try {
                    torrentToken = torrentToken.replace("(", "").replace(")", "").replace("[", "").replace("]", "");
                    resultName = resultName.replaceFirst(torrentToken, "");
                } catch (Exception e) {
                    // shhh
                }
            }

            foundMatch = true; // optimism!

            for (String token : selectedQuery) {
                if (!resultName.contains(token)) {
                    foundMatch = false;
                    break;
                }
            }

            if (foundMatch) {
                return true;
            }

            return false;
        }
    }
    
    public void shutdown() {
    	DB.close();
    }
}
