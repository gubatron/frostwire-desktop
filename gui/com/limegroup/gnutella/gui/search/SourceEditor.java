package com.limegroup.gnutella.gui.search;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class SourceEditor extends AbstractCellEditor implements TableCellEditor {

    private SourceHolder sourceHolder;
    private final SourceRenderer sourceRenderer;
    
    public SourceEditor() {
        super();
        sourceRenderer = new SourceRenderer();
    }
    
    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        sourceHolder = (SourceHolder) sourceHolder;
        updateMouseAdapters();
        return sourceRenderer.getTableCellRendererComponent(table, sourceHolder, isSelected, true, row, column);
    }

    @Override
    public Object getCellEditorValue() {
        return sourceHolder;
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
        
        sourceRenderer.addMouseListener(mouseAdapter);
    }

    protected SourceHolder getSourceHolder() {
        return sourceHolder;
    }

}
