/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.limegroup.gnutella.gui.search;

import java.util.Arrays;
import java.util.List;

import org.limewire.setting.BooleanSetting;

import com.frostwire.search.SearchPerformer;
import com.frostwire.search.archiveorg.ArchiveorgSearchPerformer;
import com.frostwire.search.bitsnoop.BitSnoopSearchPerformer;
import com.frostwire.search.clearbits.ClearBitsSearchPerformer;
import com.frostwire.search.domainalias.DomainAliasManager;
import com.frostwire.search.domainalias.DomainAliasManagerBroker;
import com.frostwire.search.extratorrent.ExtratorrentSearchPerformer;
import com.frostwire.search.eztv.EztvSearchPerformer;
import com.frostwire.search.frostclick.FrostClickSearchPerformer;
import com.frostwire.search.frostclick.UserAgent;
import com.frostwire.search.kat.KATSearchPerformer;
import com.frostwire.search.mininova.MininovaSearchPerformer;
import com.frostwire.search.monova.MonovaSearchPerformer;
import com.frostwire.search.soundcloud.SoundcloudSearchPerformer;
import com.frostwire.search.tbp.TPBSearchPerformer;
import com.frostwire.search.torlock.TorLockSearchPerformer;
import com.frostwire.search.youtube.YouTubeSearchPerformer;
import com.limegroup.gnutella.settings.SearchEnginesSettings;
import com.limegroup.gnutella.util.FrostWireUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public abstract class SearchEngine {

    private static final int DEFAULT_TIMEOUT = 5000;

    public String redirectUrl = null;

    private final int _id;
    private final String _name;
    private final BooleanSetting _setting;
    private final DomainAliasManager _domainAliasManager;

    public static final int CLEARBITS_ID = 0;
    public static final int MININOVA_ID = 1;
    public static final int KAT_ID = 8;
    public static final int EXTRATORRENT_ID = 4;
    public static final int TPB_ID = 6;
    public static final int MONOVA_ID = 7;
    public static final int YOUTUBE_ID = 9;
    public static final int SOUNDCLOUD_ID = 10;
    public static final int ARCHIVEORG_ID = 11;
    public static final int FROSTCLICK_ID = 12;
    public static final int BITSNOOP_ID = 13;
    public static final int TORLOCK_ID = 14;
    public static final int EZTV_ID = 14;
    
    public static final DomainAliasManagerBroker DOMAIN_ALIAS_MANAGER_BROKER = new DomainAliasManagerBroker();

    public static final SearchEngine CLEARBITS = new SearchEngine(CLEARBITS_ID, "ClearBits", SearchEnginesSettings.CLEARBITS_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("www.clearbits.net")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new ClearBitsSearchPerformer(SearchEngine.CLEARBITS.getDomainAliasManager(), token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine MININOVA = new SearchEngine(MININOVA_ID, "Mininova", SearchEnginesSettings.MININOVA_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("www.mininova.org")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new MininovaSearchPerformer(SearchEngine.MININOVA.getDomainAliasManager(), token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine KAT = new SearchEngine(KAT_ID, "KAT", SearchEnginesSettings.KAT_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("kickass.to")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new KATSearchPerformer(SearchEngine.KAT.getDomainAliasManager(), token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine EXTRATORRENT = new SearchEngine(EXTRATORRENT_ID, "Extratorrent", SearchEnginesSettings.EXTRATORRENT_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("extratorrent.cc")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new ExtratorrentSearchPerformer(SearchEngine.EXTRATORRENT.getDomainAliasManager(), token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine TPB = new SearchEngine(TPB_ID, "TPB", SearchEnginesSettings.TPB_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("thepiratebay.sx")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new TPBSearchPerformer(SearchEngine.TPB.getDomainAliasManager(), token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine MONOVA = new SearchEngine(MONOVA_ID, "Monova", SearchEnginesSettings.MONOVA_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("www.monova.org")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new MonovaSearchPerformer(SearchEngine.MONOVA.getDomainAliasManager(), token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine YOUTUBE = new SearchEngine(YOUTUBE_ID, "YouTube", SearchEnginesSettings.YOUTUBE_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("gdata.youtube.com")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new YouTubeSearchPerformer(SearchEngine.YOUTUBE.getDomainAliasManager(),token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine SOUNDCLOUD = new SearchEngine(SOUNDCLOUD_ID, "Soundcloud", SearchEnginesSettings.SOUNDCLOUD_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("api.sndcdn.com")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new SoundcloudSearchPerformer(SearchEngine.SOUNDCLOUD.getDomainAliasManager(),token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine ARCHIVEORG = new SearchEngine(ARCHIVEORG_ID, "Archive.org", SearchEnginesSettings.ARCHIVEORG_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("archive.org")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new ArchiveorgSearchPerformer(SearchEngine.ARCHIVEORG.getDomainAliasManager(),token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine FROSTCLICK = new SearchEngine(FROSTCLICK_ID, "FrostClick", SearchEnginesSettings.FROSTCLICK_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("api.frostclick.com")) {
        private final UserAgent userAgent = new UserAgent(org.limewire.util.OSUtils.getFullOS(), FrostWireUtils.getFrostWireVersion(), String.valueOf(FrostWireUtils.getBuildNumber()));

        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new FrostClickSearchPerformer(SearchEngine.FROSTCLICK.getDomainAliasManager(), token, keywords, DEFAULT_TIMEOUT, userAgent);
        }
    };

    public static final SearchEngine BITSNOOP = new SearchEngine(BITSNOOP_ID, "BitSnoop", SearchEnginesSettings.BITSNOOP_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("bitsnoop.com")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new BitSnoopSearchPerformer(SearchEngine.BITSNOOP.getDomainAliasManager(), token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine TORLOCK = new SearchEngine(TORLOCK_ID, "TorLock", SearchEnginesSettings.TORLOCK_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("www.torlock.com")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new TorLockSearchPerformer(SearchEngine.TORLOCK.getDomainAliasManager(), token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine EZTV = new SearchEngine(EZTV_ID, "Eztv", SearchEnginesSettings.EZTV_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("eztv.it")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new EztvSearchPerformer(SearchEngine.EZTV.getDomainAliasManager(), token, keywords, DEFAULT_TIMEOUT);
        }
    };

    private SearchEngine(int id, String name, BooleanSetting setting, DomainAliasManager domainAliasManager) {
        _id = id;
        _name = name;
        _setting = setting;
        _domainAliasManager = domainAliasManager;
    }

    public DomainAliasManager getDomainAliasManager() {
        return _domainAliasManager;
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }
    
    public String getDefaultDomain() {
        return _domainAliasManager.getDefaultDomain();
    }

    public boolean isEnabled() {
        return _setting.getValue();
    }

    @Override
    public boolean equals(Object obj) {
        return _id == ((SearchEngine) obj)._id;
    }

    public static List<SearchEngine> getEngines() {
        return Arrays.asList(EXTRATORRENT, BITSNOOP, SOUNDCLOUD, YOUTUBE, FROSTCLICK, CLEARBITS, MININOVA, KAT, TPB, MONOVA, ARCHIVEORG, TORLOCK, EZTV);
    }

    public abstract SearchPerformer getPerformer(long token, String keywords);

    public static SearchEngine getSearchEngineByName(String name) {
        List<SearchEngine> searchEngines = getEngines();

        for (SearchEngine engine : searchEngines) {
            if (name.startsWith(engine.getName())) {
                return engine;
            }
        }

        return null;
    }
    
    /**
     * Used in Domain Alias Manifest QA test, don't delete.
     */
    public static SearchEngine getSearchEngineByDefaultDomainName(String domainName) {
        List<SearchEngine> searchEngines = getEngines();

        for (SearchEngine engine : searchEngines) {
            if (domainName.equalsIgnoreCase(engine.getDefaultDomain())) {
                return engine;
            }
        }
        return null;
    }

    public BooleanSetting getEnabledSetting() {
        return _setting;
    }
}