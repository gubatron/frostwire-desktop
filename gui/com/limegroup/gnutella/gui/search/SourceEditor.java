package com.limegroup.gnutella.gui.search;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class SourceEditor extends AbstractCellEditor implements TableCellEditor {

    private final SourceRenderer sourceRenderer;
    
    public SourceEditor() {
        super();
        sourceRenderer = new SourceRenderer();
    }
    
    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        Component tableCellRendererComponent = sourceRenderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
        return tableCellRendererComponent;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }
}