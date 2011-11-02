package com.limegroup.gnutella.gui.search;

import java.util.EventListener;

import org.limewire.i18n.I18nMarker;

import com.limegroup.gnutella.gui.ButtonRow;

/**
 * This class contains the buttons in the download window, allowing
 * classes in this package to enable or disable buttons at specific
 * indeces in the row.
 */
final class SearchButtons {
		
	/**
	 * The row of buttons for the donwload window.
	 */
	private final ButtonRow BUTTONS;
	
	static final int CONFIGURE_SHARING_BUTTON_INDEX = 0;

	/**
	 * The index of the buy button.
	 */
	static final int BUY_BUTTON_INDEX = 1;
	
	/**
	 * The index of the WishList / Download Button.
	 */
	static final int DOWNLOAD_BUTTON_INDEX = 2;

	/**
	 * The index of the torrent details button in the button row.
	 */
	static final int TORRENT_DETAILS_BUTTON_INDEX = 3;

	/**
	 * The constructor creates the row of buttons with their associated
	 * listeners.
	 */
    SearchButtons(SearchResultMediator rp) {
        String[] buttonLabelKeys = {
            I18nMarker.marktr("Options"),
        	I18nMarker.marktr("Buy"),
			I18nMarker.marktr("Download"),
            I18nMarker.marktr("Details")
		};
        String[] buttonTipKeys = {
            I18nMarker.marktr("Open Options dialog"),
        	I18nMarker.marktr("Search for related products on Amazon"),
        	I18nMarker.marktr("Download All Selected Files"),
            I18nMarker.marktr("See detail web page about the selected torrent (Contents, Comments, Seeds)")
		};
        
		EventListener[] buttonListeners = {
		    rp.CONFIGURE_SHARING_LISTENER,
			rp.BUY_LISTENER,
		    rp.DOWNLOAD_LISTENER,
		    rp.TORRENT_DETAILS_LISTENER
		};
		
		String[] iconNames =  {
		    "LIBRARY_SHARING_OPTIONS",
			"BUY",
		    "SEARCH_DOWNLOAD",
		    "TORRENT_DETAILS"
		};

		BUTTONS = new ButtonRow(buttonLabelKeys,buttonTipKeys,buttonListeners, iconNames);
	}

	/**
	 * Returns the <tt>Component</tt> instance containing all of the buttons.
	 *
	 * @return the <tt>Component</tt> instance containing all of the buttons
	 */
	ButtonRow getComponent() {
		return BUTTONS;
	}
}