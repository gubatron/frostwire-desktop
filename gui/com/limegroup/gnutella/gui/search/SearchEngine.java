package com.limegroup.gnutella.gui.search;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.limewire.setting.BooleanSetting;

import com.frostwire.bittorrent.websearch.SearchEnginesSettings;
import com.frostwire.bittorrent.websearch.WebSearchPerformer;
import com.frostwire.bittorrent.websearch.btjunkie.BTJunkieWebSearchPerformer;
import com.frostwire.bittorrent.websearch.clearbits.ClearBitsWebSearchPerformer;
import com.frostwire.bittorrent.websearch.extratorrent.ExtratorrentWebSearchPerformer;
import com.frostwire.bittorrent.websearch.isohunt.ISOHuntWebSearchPerformer;
import com.frostwire.bittorrent.websearch.mininova.MininovaWebSearchPerformer;
import com.frostwire.bittorrent.websearch.monova.MonovaWebSearchPerformer;
import com.frostwire.bittorrent.websearch.tpb.TPBWebSearchPerformer;
import com.frostwire.bittorrent.websearch.vertor.VertorWebSearchPerformer;

public final class SearchEngine {

    public String redirectUrl = null;

    private final int _id;
    private final String _name;
    private final WebSearchPerformer _performer;
    private final BooleanSetting _setting;

    public static final int CLEARBITS_ID = 0;
    public static final int MININOVA_ID = 1;
    public static final int ISOHUNT_ID = 2;
    public static final int BTJUNKIE_ID = 3;
    public static final int EXTRATORRENT_ID = 4;
    public static final int VERTOR_ID = 5;
    public static final int TPB_ID = 6;
    public static final int MONOVA_ID = 7;

    public static final SearchEngine CLEARBITS = new SearchEngine(CLEARBITS_ID, "ClearBits", new ClearBitsWebSearchPerformer(),
            SearchEnginesSettings.CLEARBITS_SEARCH_ENABLED);
    public static final SearchEngine MININOVA = new SearchEngine(MININOVA_ID, "Mininova", new MininovaWebSearchPerformer(),
            SearchEnginesSettings.MININOVA_SEARCH_ENABLED);
    public static final SearchEngine ISOHUNT = new SearchEngine(ISOHUNT_ID, "ISOHunt", new ISOHuntWebSearchPerformer(),
            SearchEnginesSettings.ISOHUNT_SEARCH_ENABLED);
    public static final SearchEngine BTJUNKIE = new SearchEngine(BTJUNKIE_ID, "BTJunkie", new BTJunkieWebSearchPerformer(),
            SearchEnginesSettings.BTJUNKIE_SEARCH_ENABLED);
    public static final SearchEngine EXTRATORRENT = new SearchEngine(EXTRATORRENT_ID, "Extratorrent", new ExtratorrentWebSearchPerformer(),
            SearchEnginesSettings.EXTRATORRENT_SEARCH_ENABLED);
    public static final SearchEngine VERTOR = new SearchEngine(VERTOR_ID, "Vertor", new VertorWebSearchPerformer(),
            SearchEnginesSettings.VERTOR_SEARCH_ENABLED);
    public static final SearchEngine TPB = new SearchEngine(TPB_ID, "TPB", new TPBWebSearchPerformer(),
            SearchEnginesSettings.TPB_SEARCH_ENABLED);
    public static final SearchEngine MONOVA = new SearchEngine(MONOVA_ID, "Monova", new MonovaWebSearchPerformer(), SearchEnginesSettings.MONOVA_SEARCH_ENABLED);

    private SearchEngine(int id, String name, WebSearchPerformer performer, BooleanSetting setting) {
        _id = id;
        _name = name;
        _performer = performer;
        _setting = setting;
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public boolean isEnabled() {
        return _setting.getValue();
    }

    @Override
    public boolean equals(Object obj) {
        return _id == ((SearchEngine) obj)._id;
    }

    public static List<SearchEngine> getSearchEngines() {
        return Arrays.asList(CLEARBITS, MININOVA, ISOHUNT, BTJUNKIE, EXTRATORRENT, VERTOR, TPB, MONOVA);
    }
    
    public WebSearchPerformer getPerformer() {
        return _performer;
    }

	public static SearchEngine getSearchEngineById(int searchEngineID) {
		List<SearchEngine> searchEngines = getSearchEngines();
		
		for (SearchEngine engine : searchEngines) {
			if (engine.getId()==searchEngineID) {
				return engine;
			}
		}
		
		return null;
	}
	
	public static Map<Integer, SearchEngine> getSearchEngineMap() {
		HashMap<Integer,SearchEngine> m = new HashMap<Integer, SearchEngine>();
		List<SearchEngine> searchEngines = getSearchEngines();
		
		for (SearchEngine engine : searchEngines) {
			m.put(engine.getId(), engine);
		}
		return m;
	}

	public BooleanSetting getEnabledSetting() {
		return _setting;
	}
}
