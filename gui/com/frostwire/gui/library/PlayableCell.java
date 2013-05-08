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
package com.frostwire.gui.library;

import org.limewire.util.StringUtils;

import com.limegroup.gnutella.gui.tables.SizeHolder;

public class PlayableCell implements Comparable<PlayableCell> {

    private Object dataLine;
    private Object wrappedObject;
    private final boolean isPlaying;
    private final int columnIndex;

    public PlayableCell(Object dataLine, Object wrapMe, boolean isPlaying, int columnIndex) {
        this.dataLine = dataLine;
        this.wrappedObject = wrapMe;
        this.isPlaying = isPlaying;
        this.columnIndex = columnIndex;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public String toString() {
        if (wrappedObject != null) {

            if (wrappedObject instanceof SizeHolder) {
                if (((SizeHolder) wrappedObject).getSize() == 0) {
                    return "--";
                }
            }

            return wrappedObject.toString();
        }
        return "";
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public int compareTo(PlayableCell o) {
        if (wrappedObject instanceof Comparable && wrappedObject != null && o.wrappedObject != null && wrappedObject.getClass().equals(o.wrappedObject.getClass())) {

            if (dataLine instanceof LibraryInternetRadioTableDataLine && columnIndex == LibraryInternetRadioTableDataLine.BITRATE_IDX) {
                return compareByBitrate((String) wrappedObject, (String) o.wrappedObject);
            }

            return ((Comparable) wrappedObject).compareTo(o.wrappedObject);
        }

        return toString().compareTo(o.toString());
    }

    private static int compareByBitrate(String v1, String v2) {
        if (StringUtils.isNullOrEmpty(v1) && !StringUtils.isNullOrEmpty(v2)) {
            return 1;
        } else if (!StringUtils.isNullOrEmpty(v1) && StringUtils.isNullOrEmpty(v2)) {
            return -1;
        } else if (StringUtils.isNullOrEmpty(v1) && StringUtils.isNullOrEmpty(v2)) {
            return 0;
        }

        try {
            return Integer.valueOf(v1.toLowerCase().replace("kbps", "").trim()).compareTo(Integer.valueOf(v2.toLowerCase().replace("kbps", "").trim()));
        } catch (Exception e) {
            return 0;
        }
    }
    
    public Object getDataLine() {
        return dataLine;
    }
}
