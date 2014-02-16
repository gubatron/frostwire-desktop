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

package com.limegroup.gnutella.gui.search;

import com.frostwire.gui.bittorrent.BTDownload;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class BTDownloadPaymentOptionsHolder implements Comparable<BTDownloadPaymentOptionsHolder> {

    private final BTDownload btDownload;
    private final String displayName;

    public BTDownloadPaymentOptionsHolder(final BTDownload download) {
        this.btDownload = download;
        this.displayName = download.getDisplayName();
    }
    
    public int compareTo(BTDownloadPaymentOptionsHolder o) {
        return AbstractTableMediator.compare(btDownload.getDisplayName(), o.btDownload.getDisplayName());
    }

    public BTDownload getBTDownload() {
        return btDownload;
    }

    public String toString() {
        return displayName;
    }
}