package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.plaf.TableUI;
import javax.swing.table.TableCellRenderer;

import com.frostwire.gui.theme.SkinTableUI;
import com.frostwire.gui.theme.ThemeMediator;

abstract public class FWAbstractJPanelTableCellRenderer extends JPanel implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        updateUIData(value, table, row);
        resetMouseListeners();
        setOpaque(true);
        setEnabled(table.isEnabled());

        if (isSelected) {
            setBackground(ThemeMediator.TABLE_SELECTED_BACKGROUND_ROW_COLOR);
        } else {
            setBackground(row % 2 == 1 ? ThemeMediator.TABLE_ALTERNATE_ROW_COLOR : Color.WHITE);
        }

        return this;
    }
    
    protected abstract void updateUIData(Object dataHolder, JTable table, int row);

    /** Remove old mouse listeners and add new ones with the received data */
    protected abstract void resetMouseListeners();

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