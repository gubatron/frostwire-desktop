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

package com.frostwire.bittorrent.libtorrent;

import com.frostwire.bittorrent.BTDownload;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;

/**
 * @author gubatron
 * @author aldenml
 */
public final class LTDownload implements BTDownload {

    private final TorrentHandle th;

    private final TorrentInfo ti;
    private final String name;
    private final long size;

    public LTDownload(TorrentHandle th) {
        this.th = th;

        this.ti = this.th.getTorrentInfo();
        this.name = this.ti.getName();
        this.size = this.ti.getTotalSize();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return size;
    }
}
