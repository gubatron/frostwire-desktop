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

package com.limegroup.gnutella.gui.search;

import com.frostwire.gui.filters.TableLineFilter;
import com.limegroup.gnutella.settings.SearchSettings;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class MediaTypeFilter implements TableLineFilter<SearchResultDataLine> {

    public MediaTypeFilter() {
    }

    @Override
    public boolean allow(SearchResultDataLine node) {
        try {
            // hard coding disable youtube extension
            if (node.getExtension().equals("youtube")) {
                return false;
            }
        } catch (Throwable e) {
            // ignore
        }

        NamedMediaType nmt = node.getNamedMediaType();
        if (nmt != null) {
            return SearchSettings.LAST_MEDIA_TYPE_USED.getValue().equals(nmt.getMediaType().getMimeType());
        }

        return false;
    }
}
