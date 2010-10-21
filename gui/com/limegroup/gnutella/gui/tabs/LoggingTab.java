package com.limegroup.gnutella.gui.tabs;

import javax.swing.JComponent;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.logging.LoggingMediator;
import com.limegroup.gnutella.settings.ApplicationSettings;

public class LoggingTab extends AbstractTab {
    
    /** visible component. */
    private final JComponent COMPONENT;

    /**
     * Constructs the tab for the console.
     * 
     * @param CONSOLE
     *            the <tt>Console</tt> instance containing all component for
     *            the console display and handling
     */
    public LoggingTab(final LoggingMediator logger) {
        super(I18n.tr("Logging"),
                I18n.tr("View Logging Messages"), "logging_tab");
        COMPONENT = logger.getComponent();
    }

    public void storeState(boolean visible) {
        ApplicationSettings.LOGGING_VIEW_ENABLED.setValue(visible);
    }

    public JComponent getComponent() {
        return COMPONENT;
    }
}
