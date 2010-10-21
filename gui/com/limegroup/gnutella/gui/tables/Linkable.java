package com.limegroup.gnutella.gui.tables;

/** A cell that can have a link. */
public interface Linkable {
    
    boolean isLink();
    
    String getLinkUrl();

}
