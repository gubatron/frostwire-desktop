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

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class UXAction {

    public static final int SEARCH_STARTED = 0;
    public static final int SEARCH_RESULT_CLICKED = 1;
    public static final int SEARCH_RESULT_AUDIO_PREVIEW = 2;
    public static final int SEARCH_RESULT_VIDEO_PREVIEW = 3;
    public static final int SEARCH_RESULT_DETAIL_VIEW = 4;
    public static final int PARTIAL_DOWNLOAD_CLICKED = 5;
    public static final int DOWNLOAD_PAUSED = 6;
    public static final int DOWNLOAD_RESUMED = 7;
    public static final int DOWNLOAD_CANCELLED = 8;
    public static final int PLAY_AUDIO_FROM_LIBRARY = 9;
    public static final int PLAY_AUDIO_FROM_PLAYLIST = 10;
    public static final int PLAYLIST_CREATED = 11;
    public static final int PLAYLIST_REMOVED = 12;
    public static final int PLAYLIST_RENAMED = 13;
    public static final int TORRENT_CREATED_FORMALLY = 14;
    public static final int TORRENT_CREATED_WITH_SEND_TO_FRIEND = 15;
    public static final int SEEDING_ENABLED = 16;
    public static final int SEEDING_DISABLED = 17;
    public static final int PARTIAL_SEEDING_ENABLED = 18;
    public static final int PARTIAL_SEEDING_DISABLED = 19;

    private final int code;
    private final long time;

    UXAction(int code, long time) {
        this.code = code;
        this.time = time;
    }

    public int getCode() {
        return code;
    }

    public long getTime() {
        return time;
    }
}
