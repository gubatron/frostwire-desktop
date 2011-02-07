package com.limegroup.gnutella.gui.tables;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;

/**
 * Renders the column in the search window that displays an icon for
 * whether or not the host returning the result is chattable.
 */
public final class ChatRenderer extends SubstanceDefaultTableCellRenderer 
	implements TableCellRenderer, ThemeObserver {

	/**
     * 
     */
    private static final long serialVersionUID = 2413971606439575328L;
    /**
	 * Constant <tt>Icon</tt> for chatability.
	 */
	private static Icon _chatIcon = 
		GUIMediator.getThemeImage("chat");

	
	/**
	 * The constructor sets this <tt>JLabel</tt> to be opaque and sets the
	 * border.
	 */
	public ChatRenderer() {
		setHorizontalAlignment(SwingConstants.CENTER);
		ThemeMediator.addThemeObserver(this);
	}

	// inherit doc comment
	public void updateTheme() {
		_chatIcon = GUIMediator.getThemeImage("chat");
	}

	/**
	 * Returns the <tt>Component</tt> that displays the stars based
	 * on the number of stars in the <tt>QualityHolder</tt> object.
	 */
	public Component getTableCellRendererComponent
		(JTable table,Object value,boolean isSelected,
		 boolean hasFocus,int row,int column) {
		    
		if(value != null && value.equals(Boolean.TRUE))
			setIcon(_chatIcon);
		else
			setIcon(null);
        return super.getTableCellRendererComponent(
            table, null, isSelected, hasFocus, row, column);		
	}
}
