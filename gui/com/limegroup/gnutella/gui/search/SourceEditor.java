package com.limegroup.gnutella.gui.search;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class SourceEditor extends AbstractCellEditor implements TableCellEditor {

    private SourceHolder sourceHolder;
    
    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        sourceHolder = (SourceHolder) sourceHolder;
        
        return null;
    }

    @Override
    public Object getCellEditorValue() {
        // TODO Auto-generated method stub
        return null;
    }
    
    private void updateMouseAdapters() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {
                if (getSourceHolder() != null) {
                    getSourceHolder().getActionListener().actionPerformed(null);
                }
            }
        };
        
        sourceIcon.addMouseListener(mouseAdapter);
        sourceLabel.addMouseListener(mouseAdapter);
        this.addMouseListener(mouseAdapter);
    }

    protected SourceHolder getSourceHolder() {
        return sourceHolder;
    }

}
