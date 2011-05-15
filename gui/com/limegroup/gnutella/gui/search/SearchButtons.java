package com.limegroup.gnutella.gui.search;

import java.awt.event.ActionListener;

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

	/**
	 * The index of the WishList / Download Button.
	 */
	static final int DOWNLOAD_BUTTON_INDEX = 0;

	/**
	 * The index of the stop button in the button row.
	 */
	static final int STOP_BUTTON_INDEX = 1;

	/**
	 * The constructor creates the row of buttons with their associated
	 * listeners.
	 */
    SearchButtons(ResultPanel rp) {
        String[] buttonLabelKeys = {
			I18nMarker.marktr("Download"),
            I18nMarker.marktr("Stop Search")
		};
        String[] buttonTipKeys = {
			I18nMarker.marktr("Download All Selected Files"),
            I18nMarker.marktr("Stop Collecting Search Results for Selected Tab")
		};

		ActionListener[] buttonListeners = {
		    rp.DOWNLOAD_LISTENER,
		    rp.STOP_LISTENER
		};
		
		String[] iconNames =  {
		    "SEARCH_DOWNLOAD",
		    "SEARCH_STOP"
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