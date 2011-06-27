package com.limegroup.gnutella.gui.search;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.limegroup.gnutella.gui.tables.ActionIconAndNameRenderer;

public class ActionIconAndNameEditor extends AbstractCellEditor implements TableCellEditor {

    /**
     * 
     */
    private static final long serialVersionUID = 2661028644256459921L;

    public Object getCellEditorValue() {
        return null;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return new ActionIconAndNameRenderer().getTableCellRendererComponent(table, value, isSelected, true, row, column);
    }
}
