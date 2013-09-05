/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
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
import com.frostwire.search.UserAgent;
import com.frostwire.search.archiveorg.ArchiveorgSearchPerformer;
import com.frostwire.search.clearbits.ClearBitsSearchPerformer;
import com.frostwire.search.extratorrent.ExtratorrentSearchPerformer;
import com.frostwire.search.frostclick.FrostClickSearchPerformer;
import com.frostwire.search.isohunt.ISOHuntSearchPerformer;
import com.frostwire.search.kat.KATSearchPerformer;
import com.frostwire.search.mininova.MininovaSearchPerformer;
import com.frostwire.search.monova.MonovaSearchPerformer;
import com.frostwire.search.soundcloud.SoundcloudSearchPerformer;
import com.frostwire.search.tbp.TPBSearchPerformer;
import com.frostwire.search.vertor.VertorSearchPerformer;
import com.frostwire.search.youtube2.YouTubeSearchPerformer;
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

    public static final int CLEARBITS_ID = 0;
    public static final int MININOVA_ID = 1;
    public static final int ISOHUNT_ID = 2;
    public static final int KAT_ID = 8;
    public static final int EXTRATORRENT_ID = 4;
    public static final int VERTOR_ID = 5;
    public static final int TPB_ID = 6;
    public static final int MONOVA_ID = 7;
    public static final int YOUTUBE_ID = 9;
    public static final int SOUNDCLOUD_ID = 10;
    public static final int ARCHIVEORG_ID = 11;
    public static final int FROSTCLICK_ID = 12;

    public static final SearchEngine CLEARBITS = new SearchEngine(CLEARBITS_ID, "ClearBits", SearchEnginesSettings.CLEARBITS_SEARCH_ENABLED) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new ClearBitsSearchPerformer(token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine MININOVA = new SearchEngine(MININOVA_ID, "Mininova", SearchEnginesSettings.MININOVA_SEARCH_ENABLED) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new MininovaSearchPerformer(token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine ISOHUNT = new SearchEngine(ISOHUNT_ID, "ISOHunt", SearchEnginesSettings.ISOHUNT_SEARCH_ENABLED) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new ISOHuntSearchPerformer(token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine KAT = new SearchEngine(KAT_ID, "KAT", SearchEnginesSettings.KAT_SEARCH_ENABLED) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new KATSearchPerformer(token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine EXTRATORRENT = new SearchEngine(EXTRATORRENT_ID, "Extratorrent", SearchEnginesSettings.EXTRATORRENT_SEARCH_ENABLED) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new ExtratorrentSearchPerformer(token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine VERTOR = new SearchEngine(VERTOR_ID, "Vertor", SearchEnginesSettings.VERTOR_SEARCH_ENABLED) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new VertorSearchPerformer(token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine TPB = new SearchEngine(TPB_ID, "TPB", SearchEnginesSettings.TPB_SEARCH_ENABLED) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new TPBSearchPerformer(token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine MONOVA = new SearchEngine(MONOVA_ID, "Monova", SearchEnginesSettings.MONOVA_SEARCH_ENABLED) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new MonovaSearchPerformer(token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine YOUTUBE = new SearchEngine(YOUTUBE_ID, "YouTube", SearchEnginesSettings.YOUTUBE_SEARCH_ENABLED) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new YouTubeSearchPerformer(token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine SOUNDCLOUD = new SearchEngine(SOUNDCLOUD_ID, "Soundcloud", SearchEnginesSettings.SOUNDCLOUD_SEARCH_ENABLED) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new SoundcloudSearchPerformer(token, keywords, DEFAULT_TIMEOUT);
        }
    };

    public static final SearchEngine ARCHIVEORG = new SearchEngine(ARCHIVEORG_ID, "Archive.org", SearchEnginesSettings.ARCHIVEORG_SEARCH_ENABLED) {
        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new ArchiveorgSearchPerformer(token, keywords, DEFAULT_TIMEOUT);
        }
    };
    
    
    public static final SearchEngine FROSTCLICK = new SearchEngine(FROSTCLICK_ID, "FrostClick", SearchEnginesSettings.FROSTCLICK_SEARCH_ENABLED) {
        private final UserAgent userAgent = new UserAgent(org.limewire.util.OSUtils.getFullOS(), FrostWireUtils.getFrostWireVersion(), String.valueOf(FrostWireUtils.getBuildNumber()));

        @Override
        public SearchPerformer getPerformer(long token, String keywords) {
            return new FrostClickSearchPerformer(token, keywords, DEFAULT_TIMEOUT, userAgent);
        }
    };
        

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

    public static List<SearchEngine> getEngines() {
        return Arrays.asList(FROSTCLICK, ISOHUNT, YOUTUBE, CLEARBITS, MININOVA, KAT, EXTRATORRENT, VERTOR, TPB, MONOVA, SOUNDCLOUD, ARCHIVEORG);
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

    public BooleanSetting getEnabledSetting() {
        return _setting;
    }
}
