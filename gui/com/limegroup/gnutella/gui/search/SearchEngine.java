package com.limegroup.gnutella.gui.search;

import java.util.Arrays;
import java.util.List;

import org.limewire.setting.BooleanSetting;

import com.frostwire.bittorrent.websearch.SearchEnginesSettings;

public final class SearchEngine {

    private final int _id;
    private final String _name;
    private final BooleanSetting _setting;
    
    public static final int ISOHUNT_ID = 0;
    public static final int CLEARBITS_ID = 1;
    public static final int MININOVA_ID = 2;
    
    public static final SearchEngine ISOHUNT = new SearchEngine(ISOHUNT_ID, "ISOHunt", SearchEnginesSettings.ISOHUNT_SEARCH_ENABLED);
    public static final SearchEngine CLEARBITS = new SearchEngine(CLEARBITS_ID, "ClearBits", SearchEnginesSettings.CLEARBITS_SEARCH_ENABLED);
    public static final SearchEngine MININOVA = new SearchEngine(MININOVA_ID, "Mininova", SearchEnginesSettings.MININOVA_SEARCH_ENABLED);
    
    private SearchEngine(int id, String name, BooleanSetting setting) {
        _id = id;
        _name = name;
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
        return Arrays.asList(ISOHUNT, CLEARBITS, MININOVA);
    }
}
