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
    	System.out.println("AbstractLibraryListPanel instanciated - " + this.getClass().getName());
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
}
