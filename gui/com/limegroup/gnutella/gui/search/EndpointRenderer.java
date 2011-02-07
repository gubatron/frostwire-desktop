package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

import com.limegroup.gnutella.gui.themes.SkinHandler;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;

/** Draws EndpointHolder's appropriately colorized */
class EndpointRenderer extends SubstanceDefaultTableCellRenderer 
                                                     implements ThemeObserver {

	/**
     * 
     */
    private static final long serialVersionUID = -1874962715412607083L;

    private static Color _nonPrivateColor;

	private static Color _privateColor;
    
    private static Color _selectedPrivateColor;


    public EndpointRenderer() {
        updateTheme();
        ThemeMediator.addThemeObserver(this);
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, 
                                                   boolean isSel, 
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        EndpointHolder e = (EndpointHolder)value;
        
        Component ret = super.getTableCellRendererComponent(
            table, e, isSel, hasFocus, row, column);

        //Render private IP addresses in red, leave the others alone.
        if (e != null && e.isPrivateAddress()) {
            if (!isSel)
                ret.setForeground(_privateColor);
            else
                ret.setForeground(_selectedPrivateColor);            
        } else if(!isSel) {
            // leave selected cells alone.
            ret.setForeground(_nonPrivateColor);
        }

        return ret;
    }

    public void updateTheme() {
        _nonPrivateColor = SkinHandler.getWindow8Color();
        _privateColor = SkinHandler.getSearchPrivateIPColor();
        _selectedPrivateColor = SkinHandler.getSearchSelectedPrivateIPColor();
    }
}
