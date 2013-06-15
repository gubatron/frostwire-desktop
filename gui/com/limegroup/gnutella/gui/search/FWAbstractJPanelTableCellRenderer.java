package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.plaf.TableUI;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.frostwire.gui.theme.SkinTableUI;
import com.frostwire.gui.theme.ThemeMediator;

abstract public class FWAbstractJPanelTableCellRenderer extends JPanel implements TableCellRenderer {

    private boolean foundLabelsOnFirstPass = true;
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        initEventForwarding(table);
        updateUIData(value, table, row);
        setOpaque(true);
        setEnabled(table.isEnabled());

        if (isSelected) {
            setBackground(ThemeMediator.TABLE_SELECTED_BACKGROUND_ROW_COLOR);
        } else {
            setBackground(row % 2 == 1 ? ThemeMediator.TABLE_ALTERNATE_ROW_COLOR : Color.WHITE);
        }

        //fix labels if you have any
        if (foundLabelsOnFirstPass) {
            Component[] components = getComponents();
            boolean foundLabels = false;
            for (Component c : components) {
                if (c instanceof JLabel) {
                    ThemeMediator.fixLabelFont((JLabel) c);
                    foundLabels = true;
                }
            }
            foundLabelsOnFirstPass = foundLabels;
        }
        
        return this;
    }
    
    private void initEventForwarding(final JTable table) {
        final Component component = FWAbstractJPanelTableCellRenderer.this;
        if (component.getMouseListeners() == null || component.getMouseListeners().length == 0) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (!e.getSource().equals(this)) {
                            Toolkit.getDefaultToolkit()
                                    .getSystemEventQueue()
                                    .postEvent(
                                            new MouseEvent(component, e.getID(), e.getWhen(), e.getModifiers(), component.getX() + e.getX(), component.getY() + e.getY(), e.getClickCount(), e
                                                    .isPopupTrigger(), e.getButton()));
                        }
                        Toolkit.getDefaultToolkit()
                                .getSystemEventQueue()
                                .postEvent(
                                        new MouseEvent(table, e.getID(), e.getWhen(), e.getModifiers(), component.getX() + e.getX(), component.getY() + e.getY(), e.getClickCount(), false, e
                                                .getButton()));
                    } else {
                        Toolkit.getDefaultToolkit()
                                .getSystemEventQueue()
                                .postEvent(
                                        new MouseEvent(table, e.getID(), e.getWhen(), e.getModifiers(), component.getX() + e.getX(), component.getY() + e.getY(), e.getClickCount(), true, e
                                                .getButton()));
                    }
                    e.consume();
                    invalidate();
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    if (table.isEditing()) {
                        TableCellEditor editor = table.getCellEditor();
                        editor.cancelCellEditing();
                    }
                }
            });
            System.out.println("FWAbstractJPanelTableCellRenderer.initEventForwarding() done.");
        }
    }

    protected abstract void updateUIData(Object dataHolder, JTable table, int row);

    protected boolean mouseIsOverRow(JTable table, int row) {
        boolean mouseOver = false;

        try {
            TableUI ui = table.getUI();
            if (ui instanceof SkinTableUI) {
                mouseOver = ((SkinTableUI) ui).getRowAtMouse() == row;
            }
        } catch (Throwable e) {
            // ignore
        }
        return mouseOver;
    }
    
    protected void syncFont(JTable table, JComponent c) {
        Font tableFont = table.getFont();
        if (tableFont != null && !tableFont.equals(c.getFont())) {
            c.setFont(tableFont);
        }
    }
    
    @Override
    public void revalidate() {
        //do nothing by the JDK's documentation recomendation
    }
    
    //@Override
    public void repaint() {
        //do nothing by the JDK's documentation recomendation
    }
    
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if (propertyName=="text"
                || propertyName == "labelFor"
                || propertyName == "displayedMnemonic"
                || ((propertyName == "font" || propertyName == "foreground")
                    && oldValue != newValue
                    && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    @Override
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
        //do nothing by the JDK's documentation recomendation
    }
    
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        //do nothing by the JDK's documentation recomendation
    }

    @Override
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
        //do nothing by the JDK's documentation recomendation
    }

    @Override
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {
        //do nothing by the JDK's documentation recomendation
    }

    @Override
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {
        //do nothing by the JDK's documentation recomendation
    }

    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        //do nothing by the JDK's documentation recomendation
    }

    @Override
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {
        //do nothing by the JDK's documentation recomendation
    }

    @Override
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {
        //do nothing by the JDK's documentation recomendation
    }
}