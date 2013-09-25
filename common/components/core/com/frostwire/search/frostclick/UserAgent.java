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

package com.frostwire.search.frostclick;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.frostwire.util.StringUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class UserAgent {

    /** Should have both the name and vesion number of the Operating System*/
    private static final String OS_KEY = "OS";

    private static final String FW_VERSION_KEY = "FWversion";
    private static final String BUILD_KEY = "FWbuild";

    private final Map<String, String> headerMap;
    private final String uuid;

    public UserAgent(String operatingSystem, String fwVersion, String buildNumber) {
        this.headerMap = initHeadersMap(normalizeUnavailableString(operatingSystem), normalizeUnavailableString(fwVersion), normalizeUnavailableString(buildNumber));
        this.uuid = UUID.randomUUID().toString();
    }

    public Map<String, String> getHeadersMap() {
        return headerMap;
    }

    public String getUUID() {
        return uuid;
    }

    public String toString() {
        return "frostwire-" + headerMap.get(FW_VERSION_KEY) + "-build-" + headerMap.get(BUILD_KEY) + "-" + headerMap.get(OS_KEY);
    }

    private Map<String, String> initHeadersMap(String operatingSystem, String fwVersion, String buildNumber) {
        Map<String, String> map = new HashMap<String, String>(); //can't use Java7 notation :( Dalvik is still behind.
        map.put(OS_KEY, operatingSystem);
        map.put(FW_VERSION_KEY, fwVersion);
        map.put(BUILD_KEY, buildNumber);
        return map;
    }

    private String normalizeUnavailableString(String str) {
        if (StringUtils.isNullOrEmpty(str)) {
            str = "NA";
        }
        return str;
    }
}