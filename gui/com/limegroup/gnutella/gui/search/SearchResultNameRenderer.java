package com.limegroup.gnutella.gui.search;

import java.awt.Component;

import javax.swing.JTable;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

/**
 * Renders an icon along with a label.
 */
public final class SearchResultNameRenderer extends SubstanceDefaultTableCellRenderer {

    private static final long serialVersionUID = -1624943333769190212L;

    public SearchResultNameRenderer() {
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        SearchResultNameHolder in = (SearchResultNameHolder) value;
        return super.getTableCellRendererComponent(table, "test", isSelected, hasFocus, row, column);
    }
}
