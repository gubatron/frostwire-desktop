package com.frostwire.gui.download.bittorrent;

import com.limegroup.gnutella.gui.ButtonRow;

/**
 * This class contains the buttons in the download window, allowing
 * classes in this package to enable or disable buttons at specific
 * indexes in the row.
 */
final class BTDownloadButtons {

    /**
     * The row of buttons for the download window.
     */
    private ButtonRow BUTTONS;

    BTDownloadButtons(final BTDownloadMediator dm) {
        BUTTONS = new ButtonRow(dm.getActions(), ButtonRow.X_AXIS, ButtonRow.NO_GLUE);
    }

    ButtonRow getComponent() {
        return BUTTONS;
    }
}
