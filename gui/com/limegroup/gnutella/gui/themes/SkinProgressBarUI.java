package com.limegroup.gnutella.gui.themes;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;

import org.pushingpixels.substance.internal.ui.SubstanceProgressBarUI;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;

public class SkinProgressBarUI extends SubstanceProgressBarUI {

    public static ComponentUI createUI(JComponent comp) {
        return ThemeMediator.CURRENT_THEME.createProgressBarUI(comp);
    }

    @Override
    protected void installListeners() {
        super.installListeners();

        // fix to remove animation listener
        this.progressBar.removeChangeListener(this.substanceValueChangeListener);

        this.substanceValueChangeListener = new NoAnimatedSubstanceChangeListener();
        this.progressBar.addChangeListener(this.substanceValueChangeListener);
    }

    private final class NoAnimatedSubstanceChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            SubstanceCoreUtilities.testComponentStateChangeThreadingViolation(progressBar);

            int currValue = progressBar.getValue();

            displayedValue = currValue;
            progressBar.repaint();
        }
    }
}
