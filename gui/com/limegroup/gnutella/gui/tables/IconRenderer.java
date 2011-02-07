package com.limegroup.gnutella.gui.tables;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

/**
 * Renders the column in the search window that displays an icon for
 * whether or not the host returning the result is chattable.
 */
public final class IconRenderer extends SubstanceDefaultTableCellRenderer
                                implements TableCellRenderer {
	
	/**
     * 
     */
    private static final long serialVersionUID = 8144602599802586291L;

    /**
	 * The constructor sets this <tt>JLabel</tt> to be opaque and sets the
	 * border.
	 */
	public IconRenderer() {
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	/**
	 * Returns the <tt>Component</tt> that displays the stars based
	 * on the number of stars in the <tt>QualityHolder</tt> object.
	 */
	public Component getTableCellRendererComponent
		(JTable table,Object value,boolean isSelected,
		 boolean hasFocus,int row,int column) {
	
	    setIcon((Icon)value);
        return super.getTableCellRendererComponent(
            table, null, isSelected, hasFocus, row, column);
	}
}
