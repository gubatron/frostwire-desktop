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
import com.frostwire.search.torrentsfm.TorrentsfmSearchPerformer;
import com.frostwire.search.yify.YifySearchPerformer;
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
    public static final int EZTV_ID = 15;
    public static final int TORRENTS_ID = 16;
    public static final int YIFI_ID = 17;
    
    public static final DomainAliasManagerBroker DOMAIN_ALIAS_MANAGER_BROKER = new DomainAliasManagerBroker();

    public static final SearchEngine MININOVA = new SearchEngine(MININOVA_ID, "Mininova", SearchEnginesSettings.MININOVA_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("www.mininova.org")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("www.mininova.org");
            return new MininovaSearchPerformer(m, token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine KAT = new SearchEngine(KAT_ID, "KAT", SearchEnginesSettings.KAT_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("kickass.so")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("kickass.so");
            return new KATSearchPerformer(m, token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine EXTRATORRENT = new SearchEngine(EXTRATORRENT_ID, "Extratorrent", SearchEnginesSettings.EXTRATORRENT_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("extratorrent.cc")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("extratorrent.cc");
            return new ExtratorrentSearchPerformer(m, token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine TPB = new SearchEngine(TPB_ID, "TPB", SearchEnginesSettings.TPB_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("thepiratebay.se")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("thepiratebay.se");
            return new TPBSearchPerformer(m, token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine MONOVA = new SearchEngine(MONOVA_ID, "Monova", SearchEnginesSettings.MONOVA_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("www.monova.org")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("www.monova.org");
            return new MonovaSearchPerformer(m, token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine YOUTUBE = new SearchEngine(YOUTUBE_ID, "YouTube", SearchEnginesSettings.YOUTUBE_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("gdata.youtube.com")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("gdata.youtube.com");
            return new YouTubeSearchPerformer(m,token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine SOUNDCLOUD = new SearchEngine(SOUNDCLOUD_ID, "Soundcloud", SearchEnginesSettings.SOUNDCLOUD_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("api.sndcdn.com")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("api.sndcdn.com");
            return new SoundcloudSearchPerformer(m,token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine ARCHIVEORG = new SearchEngine(ARCHIVEORG_ID, "Archive.org", SearchEnginesSettings.ARCHIVEORG_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("archive.org")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("archive.org");
            return new ArchiveorgSearchPerformer(m,token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine FROSTCLICK = new SearchEngine(FROSTCLICK_ID, "FrostClick", SearchEnginesSettings.FROSTCLICK_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("api.frostclick.com")) {
        private final UserAgent userAgent = new UserAgent(org.limewire.util.OSUtils.getFullOS(), FrostWireUtils.getFrostWireVersion(), String.valueOf(FrostWireUtils.getBuildNumber()));

        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("api.frostclick.com");
            return new FrostClickSearchPerformer(m, token, keywords, DEFAULT_TIMEOUT, userAgent);
        }
    };

    public static final SearchEngine BITSNOOP = new SearchEngine(BITSNOOP_ID, "BitSnoop", SearchEnginesSettings.BITSNOOP_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("bitsnoop.com")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("bitsnoop.com");
            return new BitSnoopSearchPerformer(m, token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine TORLOCK = new SearchEngine(TORLOCK_ID, "TorLock", SearchEnginesSettings.TORLOCK_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("www.torlock.com")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("www.torlock.com");
            return new TorLockSearchPerformer(m, token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine EZTV = new SearchEngine(EZTV_ID, "Eztv", SearchEnginesSettings.EZTV_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("eztv.it")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("eztv.it");
            return new EztvSearchPerformer(m, token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine TORRENTS = new SearchEngine(TORRENTS_ID, "Torrents", SearchEnginesSettings.TORRENTS_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("torrents.fm")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("torrents.fm");
            return new TorrentsfmSearchPerformer(m, token, keywords, DEFAULT_TIMEOUT);
        }
    };
    
    public static final SearchEngine YIFY = new SearchEngine(YIFI_ID, "Yify", SearchEnginesSettings.YIFY_SEARCH_ENABLED, DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("www.yify-torrent.org")) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            DomainAliasManager m = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("www.yify-torrent.org");
            return new YifySearchPerformer(m, token, keywords, DEFAULT_TIMEOUT);
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
        return obj !=null && _id == ((SearchEngine) obj)._id;
    }

    public static List<SearchEngine> getEngines() {
        return Arrays.asList(YOUTUBE, EXTRATORRENT, TPB, BITSNOOP, TORRENTS, SOUNDCLOUD, FROSTCLICK, MININOVA, KAT, MONOVA, ARCHIVEORG, TORLOCK, EZTV, YIFY);
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
