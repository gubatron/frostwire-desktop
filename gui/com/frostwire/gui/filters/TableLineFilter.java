package com.frostwire.gui.filters;

/**
 * Interface that all Filters should implement
 * if they wish to filter out TableLines.
 */
public interface TableLineFilter<T> {
	/**
     * Determines whether or not the specified
     * TableLine should be displayed.
     */ 
    public boolean allow(T node);
}
    
    
