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

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import org.gudy.azureus2.core3.torrent.TOTorrentException;

/**
 * This class is NOT thread safe.
 * The same as TOTorentImpl, but it makes visible methods to manipulate the info properties.
 */
public class FWTorrentImpl extends org.gudy.azureus2.core3.torrent.impl.TOTorrentDeserialiseImpl {
    
    public FWTorrentImpl(Map map) throws TOTorrentException {
        super(map);
    }

    public FWTorrentImpl(InputStream is) throws TOTorrentException {
        super(is);
    }
    
    public FWTorrentImpl(File f) throws TOTorrentException {
        super(f);
    }

    public FWTorrentImpl(byte[] b) throws TOTorrentException {
        super(b);
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
}