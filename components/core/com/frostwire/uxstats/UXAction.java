/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2013, FrostWire(R). All rights reserved.
 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.uxstats;

import java.lang.reflect.Field;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class UXAction {
    public static final int CONFIGURATION_WIZARD_BASE = 0;
    public static final int CONFIGURATION_WIZARD_FIRST_TIME = CONFIGURATION_WIZARD_BASE + 1;
    public static final int CONFIGURATION_WIZARD_AFTER_UPDATE = CONFIGURATION_WIZARD_BASE + 2;

    public static final int SEARCH_BASE = 1000;
    public static final int SEARCH_STARTED_ENTER_KEY = SEARCH_BASE + 1;
    public static final int SEARCH_STARTED_SMALL_SEARCH_ICON_CLICK = SEARCH_BASE + 2;
    public static final int SEARCH_STARTED_SEARCH_TAB_BUTTON = SEARCH_BASE + 3;
    public static final int SEARCH_RESULT_CLICK_DOWNLOAD = SEARCH_BASE + 4;
    public static final int SEARCH_RESULT_ENTER_KEY_DOWNLOAD = SEARCH_BASE + 5;
    public static final int SEARCH_RESULT_BIG_BUTTON_DOWNLOAD = SEARCH_BASE + 6;
    public static final int SEARCH_RESULT_ROW_BUTTON_DOWNLOAD = SEARCH_BASE + 7;
    public static final int SEARCH_RESULT_CLICKED = SEARCH_BASE + 8;
    public static final int SEARCH_RESULT_AUDIO_PREVIEW = SEARCH_BASE + 9;
    public static final int SEARCH_RESULT_VIDEO_PREVIEW = SEARCH_BASE + 10;
    public static final int SEARCH_RESULT_DETAIL_VIEW = SEARCH_BASE + 11;
    public static final int SEARCH_RESULT_SOURCE_VIEW = SEARCH_BASE + 12;
    public static final int SEARCH_RESULT_FILE_TYPE_CLICK = SEARCH_BASE + 13;

    public static final int DOWNLOAD_BASE = 2000;
    public static final int DOWNLOAD_FULL_TORRENT_FILE = DOWNLOAD_BASE + 1;
    public static final int DOWNLOAD_PARTIAL_TORRENT_FILE = DOWNLOAD_BASE + 2;
    public static final int DOWNLOAD_CLOUD_FILE = DOWNLOAD_BASE + 3;
    public static final int DOWNLOAD_CLOUD_URL_FROM_FILE_ACTION = DOWNLOAD_BASE + 4;
    public static final int DOWNLOAD_CLOUD_URL_FROM_SEARCH_FIELD = DOWNLOAD_BASE + 5;    
    public static final int DOWNLOAD_TORRENT_URL_FROM_FILE_ACTION = DOWNLOAD_BASE + 6;
    public static final int DOWNLOAD_TORRENT_URL_FROM_SEARCH_FIELD = DOWNLOAD_BASE + 7;
    public static final int DOWNLOAD_MAGNET_URL_FROM_FILE_ACTION = DOWNLOAD_BASE + 8;
    public static final int DOWNLOAD_MAGNET_URL_FROM_SEARCH_FIELD = DOWNLOAD_BASE + 9;
    public static final int DOWNLOAD_PAUSE = DOWNLOAD_BASE + 10;
    public static final int DOWNLOAD_RESUME = DOWNLOAD_BASE + 11;
    public static final int DOWNLOAD_REMOVE = DOWNLOAD_BASE + 12;

    public static final int SHARING_BASE = 3000;
    public static final int SHARING_TORRENT_CREATED_FORMALLY = SHARING_BASE + 1;
    public static final int SHARING_TORRENT_CREATED_WITH_SEND_TO_FRIEND_FROM_LIBRARY = SHARING_BASE + 2;
    public static final int SHARING_TORRENT_CREATED_WITH_SEND_TO_FRIEND_FROM_MENU = SHARING_BASE + 3;
    public static final int SHARING_TORRENT_CREATED_WITH_SEND_TO_FRIEND_FROM_PLAYER = SHARING_BASE + 4;
    public static final int SHARING_TORRENT_CREATED_WITH_SEND_TO_FRIEND_FROM_DND = SHARING_BASE + 5;
    public static final int SHARING_SEEDING_ENABLED = SHARING_BASE + 6;
    public static final int SHARING_SEEDING_DISABLED = SHARING_BASE + 7;
    public static final int SHARING_PARTIAL_SEEDING_ENABLED = SHARING_BASE + 8;
    public static final int SHARING_PARTIAL_SEEDING_DISABLED = SHARING_BASE + 9;
    
    public static final int LIBRARY_BASE = 4000;
    public static final int LIBRARY_PLAY_AUDIO_FROM_FILE = LIBRARY_BASE + 1;
    public static final int LIBRARY_PLAY_AUDIO_FROM_PLAYLIST = LIBRARY_BASE + 2;
    public static final int LIBRARY_PLAY_AUDIO_FROM_STARRED_PLAYLIST = LIBRARY_BASE + 3;
    public static final int LIBRARY_PLAY_AUDIO_FROM_RADIO = LIBRARY_BASE + 4;
    public static final int LIBRARY_STARRED_AUDIO_FROM_PLAYLIST = LIBRARY_BASE + 5;
    public static final int LIBRARY_PLAYLIST_CREATED = LIBRARY_BASE + 6;
    public static final int LIBRARY_PLAYLIST_REMOVED = LIBRARY_BASE + 7;
    public static final int LIBRARY_PLAYLIST_RENAMED = LIBRARY_BASE + 8;
    public static final int LIBRARY_VIDEO_PLAY = LIBRARY_BASE + 9;
    public static final int LIBRARY_VIDEO_TOGGLE_FULLSCREEN = LIBRARY_BASE + 10;
    public static final int LIBRARY_BROWSE_AUDIO_FILES = LIBRARY_BASE + 11;
    public static final int LIBRARY_BROWSE_RINGTONE_FILES = LIBRARY_BASE + 12;
    public static final int LIBRARY_BROWSE_VIDEO_FILES = LIBRARY_BASE + 13;
    public static final int LIBRARY_BROWSE_PICTURE_FILES = LIBRARY_BASE + 14;
    public static final int LIBRARY_BROWSE_APP_FILES = LIBRARY_BASE + 15;
    public static final int LIBRARY_BROWSE_DOC_FILES = LIBRARY_BASE + 16;    
    
    public static final int WIFI_SHARING_BASE = 5000;
    public static final int WIFI_SHARING_SHARED = WIFI_SHARING_BASE + 1;
    public static final int WIFI_SHARING_UNSHARED = WIFI_SHARING_BASE + 2;
    public static final int WIFI_SHARING_PREVIEW = WIFI_SHARING_BASE + 3;
    public static final int WIFI_SHARING_DOWNLOAD = WIFI_SHARING_BASE + 4;
    public static final int WIFI_SHARING_DND_UPLOAD_TO_DEVICE = WIFI_SHARING_BASE + 5;
    //public static final int WIFI_SHARING_MENU_UPLOAD_TO_DEVICE = WIFI_SHARING_BASE + 6;

    UXAction(int code, long time) {
        this.code = code;
        this.time = time;
    }

    public final int code;
    public final long time;
    
    public static String getActionName(int code) {
        Field[] declaredFields = UXAction.class.getDeclaredFields();
        for (Field f : declaredFields) {
            try {
                if (f.getInt(null) == code) {
                    return f.getName();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "UNKNOWN_ACTION";
    }
}
