package com.frostwire.gnutella.gui.chat;

import com.limegroup.gnutella.gui.tables.AbstractDataLine;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;

public final class ChatDataLine extends AbstractDataLine<ChatDataLine> {

    /**
     * Total number of columns
     */
    static final int NUMBER_OF_COLUMNS = 13;
    static final int MESSAGE_IDX = 1;
/*
    public void initialize(ChatItem item) {
        super.initialize(item);
        //updateTheme();
    }
*/
    public int getTypeAheadColumn() {
        return MESSAGE_IDX;
    }

    /** Returns the value for the specified index. */
    public Object getValueAt(int idx) {
/*
        switch(idx) {
            case ICON_IDX:
                return initializer.getType().getIcon();
            case MESSAGE_IDX:
                return initializer.getMessage();
            case TIME_IDX:
                return initializer.getTime();
        }
*/
        return null;
    }

    public boolean isClippable(int idx) {
        return true;
    }

    public boolean isDynamic(int idx) {
/*
	    switch(idx) {
	        case HITS_IDX:
	        case ALT_LOC_IDX:
	        case UPLOADS_IDX:
	            return true;
	    }
*/
	    return false;
	}

    public int getColumnCount() { return 9; }
	
/**
	 * Return the table column for this index.
	 */
	public LimeTableColumn getColumn(int idx) {
	    //return ltColumns[idx];
	return null;
    	}

}
