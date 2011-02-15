package com.frostwire.gnutella.gui.skin;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.UIDefaults;

public class SkinPopupMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 182325604729450397L;
	
	@Override
	public void addSeparator() {
		add( new SkinPopupMenu.Separator() );
	}

	/**
	 * A popup menu-specific separator.
	 */
	static public class Separator extends JSeparator {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2601567679361541634L;

		public Separator() {
			super(JSeparator.HORIZONTAL);
		}

		/**
		 * Returns the name of the L&F class that renders this component.
		 * 
		 * @return the string "PopupMenuSeparatorUI"
		 * @see JComponent#getUIClassID
		 * @see UIDefaults#getUI
		 */
		public String getUIClassID() {
			return "PopupMenuSeparatorUI";

		}
	}
}
