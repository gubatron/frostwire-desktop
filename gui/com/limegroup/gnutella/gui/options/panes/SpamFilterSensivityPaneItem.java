package com.limegroup.gnutella.gui.options.panes;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.GUIUtils.SizePolicy;
import com.limegroup.gnutella.settings.SearchSettings;

/**
 * This class gives the user the option of whether or not to enable FrostWire's
 * internal spam filter
 */
// 2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
public final class SpamFilterSensivityPaneItem extends AbstractPaneItem {
    
    public final static String TITLE = I18n.tr("Sensitivity");
    
    public final static String LABEL = I18n.tr("Adjust the sensitivity of FrostWire\'s junk filter");

    /** The spam threshold slider */
    private JSlider THRESHOLD = new JSlider(0, 50);

    /** Reset the spam filter */
    private JButton RESET = new JButton();
    
    public SpamFilterSensivityPaneItem() {
        super(TITLE, LABEL);

        RESET.setText(I18n.tr("Forget Training Data"));
        RESET.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GuiCoreMediator.getSpamManager().clearFilterData();
            }
        });

        Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
        labels.put(0, new JLabel(I18n.tr("Relaxed"), JLabel.CENTER));
        labels.put(50, new JLabel(I18n.tr("Strict"), JLabel.CENTER));
        
        THRESHOLD.setLabelTable(labels);
        THRESHOLD.setPaintLabels(true);
        add(THRESHOLD);

        add(getVerticalSeparator());
        add(getVerticalSeparator());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(RESET);
        GUIUtils.restrictSize(buttonPanel, SizePolicy.RESTRICT_HEIGHT);
        add(buttonPanel);
    }

    public void initOptions() {
    	// FIXME check 0 <= value <= 100?  
        THRESHOLD.setValue( (int) (100 - 100 * SearchSettings.FILTER_SPAM_RESULTS.getValue()));
    }

    public boolean applyOptions() throws IOException {
        SearchSettings.FILTER_SPAM_RESULTS.setValue((100 - THRESHOLD.getValue()) / 100f);

        return false;
    }

    public boolean isDirty() {
        return SearchSettings.FILTER_SPAM_RESULTS.getValue() != (100 - THRESHOLD.getValue()) / 100f;
    }
}
