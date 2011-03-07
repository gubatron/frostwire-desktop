package com.limegroup.gnutella.gui.tables;

import javax.swing.JLabel;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

/**
 * Simple renderer that centers the data.
 */
public final class CenteredRenderer extends SubstanceDefaultTableCellRenderer {
	/**
     * 
     */
    private static final long serialVersionUID = 4600574816511326644L;

    public CenteredRenderer() {
	    super();
	    setHorizontalAlignment(JLabel.CENTER);
	}
}