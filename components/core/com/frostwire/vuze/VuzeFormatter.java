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

package com.frostwire.vuze;

import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.DisplayFormatters;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeFormatter {

    private VuzeFormatter() {
    }

    public static String formatHash(byte[] hash) {
        String hex = "";
        for (int i = 0; i < hash.length; i++) {
            String t = Integer.toHexString(hash[i] & 0xFF);
            if (t.length() < 2) {
                t = "0" + t;
            }
            hex += t;
        }

        return hex;
    }
    
    public static String formatShareRatio(int sr) {
        if (sr == Integer.MAX_VALUE) {
            sr = Integer.MAX_VALUE - 1;
        }
        if (sr == -1) {
            sr = Integer.MAX_VALUE;
        }

        String shareRatio = "";

        if (sr == Integer.MAX_VALUE) {
            shareRatio = Constants.INFINITY_STRING;
        } else {
            shareRatio = DisplayFormatters.formatDecimal((double) sr / 1000, 3);
        }

        return shareRatio;
    }

    public static String formatSeedToPeerRatio(int seeds, int peers) {
        float ratio = -1;

        if (peers < 0 || seeds < 0) {
            ratio = 0;
        } else {
            if (peers == 0) {
                if (seeds == 0)
                    ratio = 0;
                else
                    ratio = Float.POSITIVE_INFINITY;
            } else {
                ratio = (float) seeds / peers;
            }
        }

        if (ratio == -1) {
            return "";
        } else if (ratio == 0) {
            return "??";
        } else {
            return DisplayFormatters.formatDecimal(ratio, 3);
        }
    }

    public static String formatPeers(int peers, int connectedPeers, boolean hasStarted, boolean hasScrape) {
        String tmp;
        if (hasStarted) {
            tmp = hasScrape ? (connectedPeers > peers ? "%1" : "%1 " + "/" + " %2") : "%1";
        } else {
            tmp = hasScrape ? "%2" : "";
        }

        tmp = tmp.replaceAll("%1", String.valueOf(connectedPeers));
        tmp = tmp.replaceAll("%2", String.valueOf(peers));

        return tmp;
    }

    public static String formatSeeds(int seeds, int connectedSeeds, boolean hasStarted, boolean hasScrape) {
        String tmp;

        if (hasStarted) {
            tmp = hasScrape ? (connectedSeeds > seeds ? "%1" : "%1 " + "/" + " %2") : "%1";
        } else {
            tmp = hasScrape ? "%2" : "";
        }
        tmp = tmp.replaceAll("%1", String.valueOf(connectedSeeds));
        String param2 = "?";
        if (seeds != -1) {
            param2 = String.valueOf(seeds);
        }
        tmp = tmp.replaceAll("%2", param2);

        return tmp;
    }
}
