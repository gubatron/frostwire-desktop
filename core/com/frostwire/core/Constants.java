/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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

package com.frostwire.core;

import com.limegroup.gnutella.util.FrostWireUtils;

/**
 * Static class containing all constants in one place.
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class Constants {

    private Constants() {
    }

    // generic file types
    public static final byte FILE_TYPE_AUDIO = CommonConstants.FILE_TYPE_AUDIO;
    public static final byte FILE_TYPE_PICTURES = CommonConstants.FILE_TYPE_PICTURES;
    public static final byte FILE_TYPE_VIDEOS = CommonConstants.FILE_TYPE_VIDEOS;
    public static final byte FILE_TYPE_DOCUMENTS = CommonConstants.FILE_TYPE_DOCUMENTS;
    public static final byte FILE_TYPE_APPLICATIONS = CommonConstants.FILE_TYPE_APPLICATIONS;
    public static final byte FILE_TYPE_RINGTONES = CommonConstants.FILE_TYPE_RINGTONES;
    public static final byte FILE_TYPE_TORRENTS = CommonConstants.FILE_TYPE_TORRENTS;

    public static final String FROSTWIRE_VERSION_STRING = FrostWireUtils.getFrostWireVersion();

    public static final int EXTERNAL_CONTROL_LISTENING_PORT = 45100;
}