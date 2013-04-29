/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.theme;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.synth.SynthProgressBarUI;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class SkinProgressBarUI extends SynthProgressBarUI {

    public static ComponentUI createUI(JComponent comp) {
        return new SkinProgressBarUI();
    }

//    @Override
//    protected void installListeners() {
//        super.installListeners();
//
//        // fix to remove animation listener
//        this.progressBar.removeChangeListener(this.substanceValueChangeListener);
//
//        this.substanceValueChangeListener = new NoAnimatedSubstanceChangeListener();
//        this.progressBar.addChangeListener(this.substanceValueChangeListener);
//    }
//
//    private final class NoAnimatedSubstanceChangeListener implements ChangeListener {
//        public void stateChanged(ChangeEvent e) {
//            SubstanceCoreUtilities.testComponentStateChangeThreadingViolation(progressBar);
//
//            int currValue = progressBar.getValue();
//
//            displayedValue = currValue;
//            progressBar.repaint();
//        }
//    }
//    
//    @Override
//    public void paintDeterminate(Graphics g, JComponent c) {
//
//    	// fix inner substance issue related to java 7
//    	int barRectHeight = progressBar.getHeight() - 2 * margin;
//    	if (barRectHeight <= 0) {
//    		return;
//    	}
//
//    	super.paintDeterminate(g, c);
//    }
}
