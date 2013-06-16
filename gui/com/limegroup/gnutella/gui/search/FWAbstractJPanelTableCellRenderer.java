/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.plaf.TableUI;
import javax.swing.table.TableCellRenderer;

import com.frostwire.gui.theme.SkinTableUI;
import com.frostwire.gui.theme.ThemeMediator;

abstract public class FWAbstractJPanelTableCellRenderer extends JPanel implements TableCellRenderer {

    private boolean foundLabelsOnFirstPass = true;
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        updateUIData(value, table, row, column);
        setOpaque(true);
        setEnabled(table.isEnabled());

        if (isSelected) {
            setBackground(ThemeMediator.TABLE_SELECTED_BACKGROUND_ROW_COLOR);
        } else {
            setBackground(row % 2 == 1 ? ThemeMediator.TABLE_ALTERNATE_ROW_COLOR : Color.WHITE);
        }

        //fix labels if you have any
        if (!foundLabelsOnFirstPass) {
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
    
    protected abstract void updateUIData(Object dataHolder, JTable table, int row, int column);

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
        System.out.println("firePropertyChange 1 - propertyName=" + propertyName);
        System.out.println("Old: " + oldValue);
        System.out.println("New: " + newValue);
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
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        //do nothing by the JDK's documentation recomendation
        System.out.println("firePropertyChange 2 - propertyName=" + propertyName);
        System.out.println("Old: " + oldValue);
        System.out.println("New: " + newValue);

    }


 
}