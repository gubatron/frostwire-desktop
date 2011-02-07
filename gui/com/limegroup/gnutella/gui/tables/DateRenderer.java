package com.limegroup.gnutella.gui.tables;

import java.awt.Component;
import java.util.Date;

import javax.swing.JTable;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

import com.limegroup.gnutella.gui.GUIUtils;

public class DateRenderer extends SubstanceDefaultTableCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = -5183888170958098871L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        if(value != null) {
            if(value instanceof Date)
                value = GUIUtils.getFullDateTimeFormat().format((Date)value);
            else if(value instanceof Number)
                value = GUIUtils.getFullDateTimeFormat().format(new Date(((Number)value).longValue()));
        }
        
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

}
