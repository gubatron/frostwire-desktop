package com.limegroup.gnutella.gui.search;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.limegroup.gnutella.gui.tables.ActionIconAndNameHolder;
import com.limegroup.gnutella.gui.tables.ActionIconAndNameRenderer;

public class ActionIconAndNameEditor extends AbstractCellEditor implements TableCellEditor {

    /**
     * 
     */
    private static final long serialVersionUID = 2661028644256459921L;

    private ActionListener _action;
    private static final Rectangle ICON_REGION = new Rectangle(3, 3, 13, 13);

    public Object getCellEditorValue() {
        return null;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        ActionIconAndNameHolder in = (ActionIconAndNameHolder) value;
        _action = in.getAction();

        Component component = new ActionIconAndNameRenderer().getTableCellRendererComponent(table, value, isSelected, true, row, column);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (ICON_REGION.contains(e.getPoint())) {
                    component_mousePressed(e);
                }
            }
        });

        return component;
    }

    protected void component_mousePressed(MouseEvent e) {
        if (_action != null) {
            try {
                _action.actionPerformed(new ActionEvent(e.getSource(), e.getID(), ""));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
