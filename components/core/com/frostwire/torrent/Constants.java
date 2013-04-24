/*
 * Created on 16 juin 2003
 * Copyright (C) 2003, 2004, 2005, 2006 Aelitis, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * AELITIS, SAS au capital de 46,603.30 euros
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 *
 */
package com.frostwire.torrent;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 *  
 * @author Olivier
 *
 */

final class Constants {
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String BYTE_ENCODING = "ISO-8859-1";
    public static final Charset BYTE_CHARSET = Charset.forName(Constants.BYTE_ENCODING);
    public static final Charset DEFAULT_CHARSET = Charset.forName(Constants.DEFAULT_ENCODING);

    public static final String APP_NAME = "FrostWire";
    public static final String APP_VERSION = "5.5.7";

    public static final String OSName = System.getProperty("os.name");

    public static final boolean isOSX = OSName.toLowerCase().startsWith("mac os");
    public static final boolean isLinux = OSName.equalsIgnoreCase("Linux");
    public static final boolean isSolaris = OSName.equalsIgnoreCase("SunOS");
    public static final boolean isFreeBSD = OSName.equalsIgnoreCase("FreeBSD");
    public static final boolean isWindowsXP = OSName.equalsIgnoreCase("Windows XP");
    public static final boolean isWindows95 = OSName.equalsIgnoreCase("Windows 95");
    public static final boolean isWindows98 = OSName.equalsIgnoreCase("Windows 98");
    public static final boolean isWindows2000 = OSName.equalsIgnoreCase("Windows 2000");
    public static final boolean isWindowsME = OSName.equalsIgnoreCase("Windows ME");
    public static final boolean isWindows9598ME = isWindows95 || isWindows98 || isWindowsME;

    public static final boolean isWindows = OSName.toLowerCase().startsWith("windows");
    // If it isn't windows or osx, it's most likely an unix flavor
    public static final boolean isUnix = !isWindows && !isOSX;

    // Common Patterns
    public static final Pattern PAT_SPLIT_COMMAWORDS = Pattern.compile("\\s*,\\s*");
    public static final Pattern PAT_SPLIT_COMMA = Pattern.compile(",");
    public static final Pattern PAT_SPLIT_DOT = Pattern.compile("\\.");
    public static final Pattern PAT_SPLIT_SPACE = Pattern.compile(" ");
    public static final Pattern PAT_SPLIT_SLASH_N = Pattern.compile("\n");

    public static final String FILE_WILDCARD = isWindows ? "*.*" : "*";
}
