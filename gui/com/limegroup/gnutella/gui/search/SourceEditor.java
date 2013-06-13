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
        Component tableCellRendererComponent = sourceRenderer.getTableCellRendererComponent(table, sourceHolder, false, true, row, column);
        updateMouseAdapters(tableCellRendererComponent);
        return tableCellRendererComponent;
    }

    @Override
    public Object getCellEditorValue() {
        return sourceHolder;
    }
    
    private void updateMouseAdapters(Component tableCellRendererComponent) {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (getSourceHolder() != null) {
                    getSourceHolder().getActionListener().actionPerformed(null);
                }
            }
        };
        
        tableCellRendererComponent.addMouseListener(mouseAdapter);
    }

    protected SourceHolder getSourceHolder() {
        return sourceHolder;
    }
}