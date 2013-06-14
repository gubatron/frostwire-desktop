package com.limegroup.gnutella.gui.search;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
        sourceHolder = (SourceHolder) value;
        Component tableCellRendererComponent = sourceRenderer.getTableCellRendererComponent(table, sourceHolder, true, true, row, column);
        updateMouseAdapters(tableCellRendererComponent);
        return tableCellRendererComponent;
    }

    @Override
    public Object getCellEditorValue() {
        return sourceHolder;
    }
    
    private void updateMouseAdapters(final Component tableCellRendererComponent) {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (getSourceHolder() != null) {
                    getSourceHolder().getUISearchResult().showDetails(true);
                    e.consume();
                }
            }
        };
        
        MouseListener[] mouseListeners = tableCellRendererComponent.getMouseListeners();
        for (MouseListener m : mouseListeners) {
            tableCellRendererComponent.removeMouseListener(m);
        }
        
        tableCellRendererComponent.addMouseListener(mouseAdapter);
    }

    protected SourceHolder getSourceHolder() {
        return sourceHolder;
    }
}