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
package com.frostwire.gui.library;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.limegroup.gnutella.gui.RefreshListener;

/**
 * The left hand side JPanels that contain JLists when used
 * switch and refresh the right hand side mediators (aka Tables)
 * as necessary.
 * 
 * Since it takes some time to refresh the contents of the table,
 * we cannot order these tables to scroll down to a certain position
 * until all the data has been loaded.
 * 
 * This Abstract class has been created to enqueue Runnable tasks
 * that should be performed once the right hand side mediators
 * have finished loading.
 * 
 * Use enqueueRunnable on your implementations of the {@link AbstractLibraryListPanel}
 * 
 * @author gubatron
 *
 */
public abstract class AbstractLibraryListPanel extends JPanel implements RefreshListener {
	private static final long serialVersionUID = 2600384627889697339L;

	private List<Runnable> PENDING_RUNNABLES;
    
    public AbstractLibraryListPanel() {
    	    PENDING_RUNNABLES = new ArrayList<Runnable>();
    }
    
    public void enqueueRunnable(Runnable r) {
        PENDING_RUNNABLES.add(r);
    }
    
    public void executePendingRunnables() {
        if (PENDING_RUNNABLES != null && PENDING_RUNNABLES.size() > 0) {
            for (Runnable t : PENDING_RUNNABLES) {
                try {
                    t.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            PENDING_RUNNABLES.clear();
        }
    }

    public List<Runnable> getPendingRunnables() {
        return PENDING_RUNNABLES;
    }
}
