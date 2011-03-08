package com.limegroup.gnutella.gui.search;

import java.awt.Component;

import javax.swing.JTable;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

/**
 * a TableCellRenderer rendering a Float as percentage 
 */
public class PercentageRenderer extends SubstanceDefaultTableCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 433287296880631126L;

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, 
                                                   boolean isSel, 
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        if(value == null)
            return super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, column);

		Float f = (Float)value;
            
        String percentage = "" + (int)(100 * f.floatValue()) + "%";
        return super.getTableCellRendererComponent(table, percentage, isSel, hasFocus, row, column);
    }
}
