package com.limegroup.gnutella.gui.search;

import java.util.Date;

import javax.swing.Icon;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.tables.ActionIconAndNameHolder;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;

/**
 * Simple collection of table columns.
 */
final class SearchTableColumns {
    
    // It is important that all the columns be non static,
    // so that the multiple search tables all have their own
    // columns.

    static final int QUALITY_IDX = 0;
    private final LimeTableColumn QUALITY_COLUMN =
        new SearchColumn(QUALITY_IDX, "RESULT_PANEL_QUALITY", I18n.tr("Quality"),
                            55, true,  QualityHolder.class);
    
    static final int COUNT_IDX = 1;
    private final LimeTableColumn COUNT_COLUMN =
        new SearchColumn(COUNT_IDX, "RESULT_PANEL_COUNT", I18n.tr("Seeds"),
                            24, true,  Integer.class);
                            
    static final int TYPE_IDX = 2;
    private final LimeTableColumn TYPE_COLUMN =
        new SearchColumn(TYPE_IDX, "RESULT_PANEL_ICON", I18n.tr("Type"),
                    18, true, Icon.class);
    
    static final int NAME_IDX = 3;
    private final LimeTableColumn NAME_COLUMN =
        new SearchColumn(NAME_IDX, "RESULT_PANEL_NAME", I18n.tr("Name"),
                            272, true,  ActionIconAndNameHolder.class);
                            
    static final int SIZE_IDX = 4;
    private final LimeTableColumn SIZE_COLUMN =
        new SearchColumn(SIZE_IDX, "RESULT_PANEL_SIZE", I18n.tr("Size"),
                            53, true, String.class);
                            
   
    static final int SOURCE_IDX = 5;
    private final LimeTableColumn SOURCE_COLUMN = 
        new SearchColumn(SOURCE_IDX, "RESULT_PANEL_SOURCE", I18n.tr("Source"),
                            55, true, String.class);
                            
    static final int ADDED_IDX = 6;
    private final LimeTableColumn ADDED_COLUMN =
        new SearchColumn(ADDED_IDX, "RESULT_PANEL_ADDED", I18n.tr("Created"),
                            55, true, Date.class);
                            
    /**
     * The number of default columns.
     */
    static final int COLUMN_COUNT = 7;
    
    /**
     * Constructs a new SearchTableColumns.
     */
    SearchTableColumns() {

    }
    
    /**
     * Gets the column for the specified index.
     */
    LimeTableColumn getColumn(int idx) {
        switch (idx) {
        case QUALITY_IDX: return QUALITY_COLUMN;
        case COUNT_IDX: return COUNT_COLUMN;
        case TYPE_IDX: return TYPE_COLUMN;
        case NAME_IDX: return NAME_COLUMN;
        case SIZE_IDX: return SIZE_COLUMN;
        case SOURCE_IDX: return SOURCE_COLUMN;
        case ADDED_IDX: return ADDED_COLUMN;
        default: return null;
        }
    }
}    
    
