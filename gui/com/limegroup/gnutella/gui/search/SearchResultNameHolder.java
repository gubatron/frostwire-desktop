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

package com.limegroup.gnutella.gui.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;

import org.pushingpixels.substance.internal.utils.icon.SubstanceIconFactory;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class SearchResultNameHolder implements Comparable<SearchResultNameHolder> {

    private final ActionListener moreAction;
    private final ActionListener torrentDetailsAction;
    private final SearchResult sr;

    public SearchResultNameHolder(final SearchResult sr) {
        moreAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sr instanceof BittorrentSearchResult) {
                    GUIMediator.instance().openTorrentSearchResult(sr.getWebSearchResult(), true, torrentDetailsAction);
                }
            }
        };
        torrentDetailsAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sr.showDetails(false);
            }
        };
        this.sr = sr;
    }

    public int compareTo(SearchResultNameHolder o) {
        return AbstractTableMediator.compare(sr.getFilenameNoExtension(), o.sr.getFilenameNoExtension());
    }

    public String getName() {
        return sr.getFilenameNoExtension();
    }

    public String toString() {
        return sr.getFilenameNoExtension();
    }

    public ActionListener getAction() {
        return moreAction;
    }
}
