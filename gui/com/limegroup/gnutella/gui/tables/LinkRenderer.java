package com.limegroup.gnutella.gui.tables;

import java.awt.Component;

import javax.swing.JTable;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

import com.limegroup.gnutella.gui.GUIUtils;


/** Renderer that can display {@link Linkable} objects in HTML. */
public class LinkRenderer extends SubstanceDefaultTableCellRenderer {
    
    /**
     * 
     */
    private static final long serialVersionUID = 648136389396954869L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        
        if(value instanceof Linkable && ((Linkable)value).isLink()) {
            StringBuilder sb = new StringBuilder(30);
            sb.append("<html><a href=\"")
              .append(((Linkable)value).getLinkUrl())
              .append("\"");
            if(isSelected) {
                sb.append("color=\"")
                  .append(GUIUtils.colorToHex(table.getSelectionForeground()))
                  .append("\"");
            }
            sb.append(">")
              .append(value)
              .append("</a></html>");
            value = sb.toString();
        }
        
        if (value != null) {
            String strValue = value.toString();
            strValue = strValue.replace("<html>", "<html><div width=\"1000000px\">");
            strValue = strValue.replace("</html>", "</div></html>");
            value = strValue;
        }
        
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

}
