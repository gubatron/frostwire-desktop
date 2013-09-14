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

package com.frostwire.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class URLUtils {

    private URLUtils() {
    }

    public static String encode(String str) {
        String enc = "";

        if (str != null) {
            try {
                enc = URLEncoder.encode(str, "UTF-8").replaceAll("\\+", "%20");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Impossible to run in an environment with lack of UTF-8 support", e);
            }
        }

        return enc;
    }
}
