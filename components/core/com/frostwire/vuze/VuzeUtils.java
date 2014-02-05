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

import java.util.HashSet;
import java.util.Set;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfoSet;
import org.gudy.azureus2.core3.download.DownloadManager;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeUtils {

    private VuzeUtils() {
    }

    public static void start(VuzeDownloadManager dm) {
        ManagerUtils.start(dm.getDM());
    }

    public static void stop(VuzeDownloadManager dm) {
        ManagerUtils.stop(dm.getDM());
    }

    static Set<DiskManagerFileInfo> getFileInfoSet(DownloadManager dm, InfoSetQuery q) {

        Set<DiskManagerFileInfo> set = new HashSet<DiskManagerFileInfo>();
        DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
        for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
            switch (q) {
            case SKIPPED:
                if (fileInfo.isSkipped()) {
                    set.add(fileInfo);
                }
                break;
            case NO_SKIPPED:
                if (!fileInfo.isSkipped()) {
                    set.add(fileInfo);
                }
                break;
            case ALL:
            default:
                set.add(fileInfo);
                break;
            }
        }

        return set;
    }

    enum InfoSetQuery {
        ALL, SKIPPED, NO_SKIPPED
    }
}
