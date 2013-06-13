package com.limegroup.gnutella.gui.search;

import java.awt.event.ActionListener;

import com.limegroup.gnutella.gui.tables.AbstractTableMediator;

/**
 * Holds the data for a search result's Source.
 * @author gubatron
 *
 */
public class SourceHolder implements Comparable<SourceHolder> {

    private final ActionListener actionListener;
    private final String sourceNameHTML;
    private final String sourceName;
    private final String sourceURL;
    
    public SourceHolder(ActionListener actionListener, String sourceName, String sourceURL) {
        this.actionListener = actionListener;
        this.sourceName = sourceName;
        this.sourceNameHTML = "<html><a href=\"#\">" + sourceName + "</a></html>";
        this.sourceURL  = sourceURL;
    }
    
    @Override
    public int compareTo(SourceHolder o) {
        return AbstractTableMediator.compare(sourceName, o.getSourceName());
    }
    
    public String getSourceName() {
        return sourceName;
    }
    
    public String getSourceNameHTML() {
        return sourceNameHTML;
    }
    
    public String getSourceURL() {
        return sourceURL;
    }
    
    public ActionListener getActionListener() {
        return actionListener;
    }
}