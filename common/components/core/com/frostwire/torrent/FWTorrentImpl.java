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

package com.frostwire.torrent;

import java.net.URL;
import java.util.Map;

import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.impl.TOTorrentImpl;
import org.gudy.azureus2.core3.util.TorrentUtils.ExtendedTorrent;

/**
 * This class is NOT thread safe.
 * The same as TOTorentImpl, but it makes visible methods to manipulate the info properties.
 */
public class FWTorrentImpl extends TOTorrentImpl implements ExtendedTorrent {
    
    
    
    public FWTorrentImpl() {
        super();
    }
    
    public FWTorrentImpl(String _torrent_name, URL _announce_url, boolean _simple_torrent) throws TOTorrentException {
        super(_torrent_name, _announce_url, _simple_torrent);
    }
    
    public void addAdditionalInfoProperty(String name, Object value) {
        super.addAdditionalInfoProperty(name, value);
    }
    
    public Map<String, Object> getAdditionalInfoProperties() {
        return super.getAdditionalInfoProperties();
    }
    
    public static String getStringFromEncodedMap(String key, Map<String,Object> map) {
        String result = null;
        if (map.get(key)!=null && map.get(key) instanceof byte[]) {
            result = new String((byte[]) map.get(key));    
        } else if (map.get(key)!=null && map.get(key) instanceof String && !((String) map.get(key)).isEmpty()) {
            result = (String) map.get(key);
        }
        return result;
    }

    @Override
    public byte[][] peekPieces() throws TOTorrentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDiscardFluff(boolean discard) {
        // TODO Auto-generated method stub
        
    }
}