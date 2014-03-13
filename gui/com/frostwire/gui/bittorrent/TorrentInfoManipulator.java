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

package com.frostwire.gui.bittorrent;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.gudy.azureus2.core3.util.LightHashMap;

public class TorrentInfoManipulator {
    private Map additional_info_properties = new LightHashMap(4);

    public TorrentInfoManipulator(TOTorrent torrent) {
        initAdditionalInfoPropertiesReference(torrent);
    }
    
    public TorrentInfoManipulator(File torrentFile) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(torrentFile);
            TOTorrent toTorrent = TOTorrentFactory.deserialiseFromBEncodedInputStream(fileInputStream);
            fileInputStream.close();
            initAdditionalInfoProperties(toTorrent);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fileInputStream);//this does a NPE check inside... so no worries.
        }
    }

    @SuppressWarnings("unchecked")
    public void addAdditionalInfoProperty(String name, Object value) {
        if (additional_info_properties != null) {
            additional_info_properties.put(name, value);
        }
    }

    public Map getAdditionalInfoProperties() {
        return (additional_info_properties);
    }
    
    private void initAdditionalInfoPropertiesReference(TOTorrent delegate) {
        if (delegate != null) {
            initAdditionalInfoProperties(delegate);
        }
    }
    
    private void initAdditionalInfoProperties(TOTorrent delegate) {
        if (delegate != null) {
            try {
                Field additional_info_properties_field = org.gudy.azureus2.core3.torrent.impl.TOTorrentImpl.class.getDeclaredField("additional_info_properties");
                additional_info_properties_field.setAccessible(true);
                Object additional_info_object = additional_info_properties_field.get(delegate);
                additional_info_properties_field.setAccessible(false);
                
                if (additional_info_object != null) {
                    additional_info_properties = (Map) additional_info_object;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}