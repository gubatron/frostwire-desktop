package com.limegroup.gnutella.gui.search;

import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JTable;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

class DateRenderer extends SubstanceDefaultTableCellRenderer {
    
	private static final long serialVersionUID = -5927935873435355689L;
	private static final DateFormat FORMAT =
        DateFormat.getDateInstance(DateFormat.MEDIUM);

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, 
                                                   boolean isSel, 
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        Date d = (Date)value;
        if(d == null)
            return super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, column);
            
        String formatted = FORMAT.format(d);
        return super.getTableCellRendererComponent(table, formatted, isSel, hasFocus, row, column);
    }
}
