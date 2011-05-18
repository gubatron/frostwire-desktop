package com.limegroup.gnutella.gui.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.licenses.License;
import com.limegroup.gnutella.settings.SearchSettings;
import com.limegroup.gnutella.xml.LimeXMLSchema;
import com.limegroup.gnutella.xml.SchemaFieldInfo;

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
        new SearchColumn(COUNT_IDX, "RESULT_PANEL_COUNT", I18n.tr("#"),
                            24, true,  Integer.class);
                            
    static final int LICENSE_IDX = 2;
    private final LimeTableColumn LICENSE_COLUMN =
        new SearchColumn(LICENSE_IDX, "RESULT_PANEL_LICENSE", I18n.tr("License"),
                            40, true, License.class);
    
    static final int ICON_IDX = 3;
    private final LimeTableColumn ICON_COLUMN =
        new SearchColumn(ICON_IDX, "RESULT_PANEL_ICON", I18n.tr("Icon"),
		    GUIMediator.getThemeImage("question_mark"),
                    18, true, Icon.class);
    
    static final int NAME_IDX = 4;
    private final LimeTableColumn NAME_COLUMN =
        new SearchColumn(NAME_IDX, "RESULT_PANEL_NAME", I18n.tr("Name"),
                            272, true,  ResultNameHolder.class);
                            
    static final int SIZE_IDX = 5;
    private final LimeTableColumn SIZE_COLUMN =
        new SearchColumn(SIZE_IDX, "RESULT_PANEL_SIZE", I18n.tr("Size"),
                            53, true, String.class);
                            
   
    static final int SOURCE_IDX = 6;
    private final LimeTableColumn SOURCE_COLUMN = 
        new SearchColumn(SOURCE_IDX, "RESULT_PANEL_SOURCE", I18n.tr("Source"),
                            55, false, String.class);
                            
    static final int ADDED_IDX = 7;
    private final LimeTableColumn ADDED_COLUMN =
        new SearchColumn(ADDED_IDX, "RESULT_PANEL_ADDED", I18n.tr("Created"),
                            55, false, Date.class);
                            
    /**
     * The number of default columns.
     */
    static final int DEFAULT_COLUMN_COUNT = 8;
    
    /**
     * The number of extra XML columns.
     */
    static final int EXTRA_COLUMN_COUNT;
    
    /**
     * The actual XML columns.
     */
    private final List<XMLSearchColumn>  EXTRA_COLUMNS = new ArrayList<XMLSearchColumn> ();
    
    /**
     * The total number of columns.
     */
    static final int COLUMN_COUNT;
    
    // initializes EXTRA_COLUMN_COUNT & COLUMN_COUNT.
    static {
        List<XMLSearchColumn> columns = new LinkedList<XMLSearchColumn> ();
        addColumns(columns);
        EXTRA_COLUMN_COUNT = columns.size();
        COLUMN_COUNT = EXTRA_COLUMN_COUNT + DEFAULT_COLUMN_COUNT;
    }
    
    /**
     * Constructs a new SearchTableColumns.
     */
    SearchTableColumns() {
        addColumns(EXTRA_COLUMNS);
    }
    
    /**
     * Adds all available XML columns into the list.
     */
    private static void addColumns(List<? super XMLSearchColumn> columns) {
        Collection<LimeXMLSchema> schemas = GuiCoreMediator.getLimeXMLSchemaRepository().getAvailableSchemas();
        int idx = DEFAULT_COLUMN_COUNT;
        for(LimeXMLSchema schema : schemas) {
            for(SchemaFieldInfo sfi : schema.getCanonicalizedFields()) {
                if(sfi.isHidden())
                    continue;
                
                XMLSearchColumn ltc = new XMLSearchColumn(idx, sfi);
                columns.add(ltc);
                idx++;
            }
        }
    }
    
    /**
     * Gets the column for the specified index.
     */
    LimeTableColumn getColumn(int idx) {
        switch (idx) {
        case QUALITY_IDX: return QUALITY_COLUMN;
        case COUNT_IDX: return COUNT_COLUMN;
        case ICON_IDX: return ICON_COLUMN;
        case NAME_IDX: return NAME_COLUMN;
        case SIZE_IDX: return SIZE_COLUMN;
        case SOURCE_IDX: return SOURCE_COLUMN;
        case ADDED_IDX: return ADDED_COLUMN;
        case LICENSE_IDX: return LICENSE_COLUMN;
        default:
            if (idx == -1 && SearchSettings.moveJunkToBottom()) {
                // idx is -1 if BasicDataLineModel is set to unsorted 
                // (_activeColumn is -1). We must still sort the results
                // somehow if 'move junk to bottom' selected!
                return QUALITY_COLUMN;
            } else if(idx >= DEFAULT_COLUMN_COUNT && idx < COLUMN_COUNT) {
                return EXTRA_COLUMNS.get(idx - DEFAULT_COLUMN_COUNT);
            } else {
                throw new IllegalStateException("illegal idx: " + idx);
            }
        }
    }
}    
    
