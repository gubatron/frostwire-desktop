package com.limegroup.gnutella.gui.options.panes;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.limewire.setting.BooleanSetting;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.search.SearchEngine;

public final class SearchEnginesPaneItem extends AbstractPaneItem {

    public final static String TITLE = I18n.tr("Search Engines");
    
    public final static String LABEL = I18n.tr("Select which search engines you want FrostWire to use.");

    private boolean dirty;

	/**
	 * The constructor constructs all of the elements of this
	 * <tt>AbstractPaneItem</tt>.
	 * 
	 * @param key the key for this <tt>AbstractPaneItem</tt> that the
	 *        superclass uses to generate locale-specific keys
	 */
	public SearchEnginesPaneItem() {
	    super(TITLE, LABEL);
		add(createSearchEnginesCheckboxPanel());
	}

	/**
	 * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
	 *
	 * Sets the options for the fields in this <tt>PaneItem</tt> when the 
	 * window is shown.
	 */
	public void initOptions() {
	}

    private JComponent createSearchEnginesCheckboxPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("insets 2, wrap 4, gap 8"));
        List<SearchEngine> searchEngines = SearchEngine.getEngines();
        setupCheckboxes(searchEngines, panel);
        return panel;
    }

    private void setupCheckboxes(List<SearchEngine> searchEngines, JPanel parent) {

        final Map<JCheckBox, BooleanSetting> cBoxes = new HashMap<JCheckBox, BooleanSetting>();

        ItemListener listener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean allDeSelected = true;

                for (JCheckBox cBox : cBoxes.keySet()) {
                    if (cBox.isSelected()) {
                        allDeSelected = false;
                        break;
                    }
                }

                if (allDeSelected) {
                    ((JCheckBox) e.getItemSelectable()).setSelected(true);
                }

                for (JCheckBox cBox : cBoxes.keySet()) {
                    cBoxes.get(cBox).setValue(cBox.isSelected());
                }
                
                dirty = true;
            }
        };

        for (SearchEngine se : searchEngines) {
            JCheckBox cBox = new JCheckBox(se.getName());
            cBox.setSelected(se.isEnabled());
            cBox.setEnabled(true);
            parent.add(cBox);
            cBoxes.put(cBox, se.getEnabledSetting());
            cBox.addItemListener(listener);
        }
    }
    public boolean applyOptions() throws IOException {
        return false;
	}
	
    public boolean isDirty() {
        return dirty;
    }
}