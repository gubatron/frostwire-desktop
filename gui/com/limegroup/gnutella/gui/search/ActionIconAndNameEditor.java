package com.limegroup.gnutella.gui.search;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Toolkit;
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

    private final Rectangle _actionRegion;

    private ActionListener _action;

    public ActionIconAndNameEditor(Rectangle actionRegion) {
        _actionRegion = actionRegion;
    }

    public ActionIconAndNameEditor() {
        this(null);
    }

    public Object getCellEditorValue() {
        return null;
    }

    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        ActionIconAndNameHolder in = (ActionIconAndNameHolder) value;
        _action = in.getAction();

        final Component component = new ActionIconAndNameRenderer().getTableCellRendererComponent(table, value, isSelected, true, row, column);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (_actionRegion == null) {
                    component_mousePressed(e);
                } else {
                    if (_actionRegion.contains(e.getPoint())) {
                        component_mousePressed(e);
                    } else {
                        Toolkit.getDefaultToolkit()
                                .getSystemEventQueue()
                                .postEvent(
                                        new MouseEvent(table, e.getClickCount() >= 2 ? MouseEvent.MOUSE_CLICKED : MouseEvent.MOUSE_PRESSED, e.getWhen(), e
                                                .getModifiers(), component.getX() + e.getX(), component.getY() + e.getY(), e.getClickCount(), false));
                    }
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
