package com.limegroup.gnutella.gui;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * This class contains the logo and the searching icon for the application.
 */
final class LogoPanel extends BoxPanel /* implements ThemeObserver */ {

	/**
     * 
     */
    private static final long serialVersionUID = 222666494852328516L;

    /**
	 * Icon for the when we're searching.
	 */
	private ImageIcon _searchingIcon;

	/**
	 * Icon for not searching.
	 */
	private ImageIcon _notSearchingIcon;

	/**
	 * Constant for the <tt>JLabel</tt> used for displaying the lime/spinning
	 * lime search status indicator.
	 */
	private final JLabel ICON_LABEL = new JLabel();

	private final JLabel LOGO_LABEL = new JLabel();

	private boolean _searching;

	/**
	 * Constructs a new panel containing the logo and the search icon.
	 */
	LogoPanel() {
		super(BoxPanel.X_AXIS);
		updateTheme();

		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				GUIMediator.openURL("http://www.frostwire.com");
			}

			public void mouseEntered(MouseEvent me){
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
		});

	}

	// inherit doc comment
	public void updateTheme() {
		_searchingIcon = GUIMediator.getThemeImage("searching");
		_notSearchingIcon = GUIMediator.getThemeImage("notsearching");
		
		if(_searching) {
			ICON_LABEL.setIcon(_searchingIcon);
		} else {
			ICON_LABEL.setIcon(_notSearchingIcon);
		}
		ImageIcon logoIcon = GUIMediator.getThemeImage("logo");
		LOGO_LABEL.setIcon(logoIcon);
		
		
		LOGO_LABEL.setSize(logoIcon.getIconWidth(),
						   logoIcon.getIconHeight());
		ICON_LABEL.setSize(_searchingIcon.getIconWidth(),
						   _searchingIcon.getIconHeight());
		
		GUIUtils.setOpaque(false, this);

		buildPanel();
	}
	
	private void buildPanel() {
	    removeAll();
	    add(Box.createHorizontalGlue());
        add(ICON_LABEL);
        add(LOGO_LABEL);
        add(Box.createHorizontalGlue());
	}       

	/**
	 * Sets the searching or not searching status of the application.
	 *
	 * @param searching the searching status of the application
	 */
	void setSearching(boolean searching) {
		_searching = searching;
		if(searching) {
			ICON_LABEL.setIcon(_searchingIcon);
		} else {
			ICON_LABEL.setIcon(_notSearchingIcon);
		}
	}
}
