package com.limegroup.gnutella.gui.options.panes;

import java.io.IOException;

import javax.swing.JCheckBox;

import org.limewire.i18n.I18nMarker;

import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.settings.SearchSettings;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class DetailsPaneItem extends AbstractPaneItem {

    public final static String TITLE = I18n.tr("Details Page");

    public final static String DETAILS = I18n.tr("Show details web page after a download starts.");

    private final JCheckBox DETAILS_CHECK_BOX = new JCheckBox();

    public DetailsPaneItem() {
        super(TITLE, "");

        BoxPanel panel = new BoxPanel();
        LabeledComponent comp = new LabeledComponent(I18nMarker.marktr(DETAILS), DETAILS_CHECK_BOX, LabeledComponent.LEFT_GLUE, LabeledComponent.LEFT);
        panel.add(comp.getComponent());

        add(panel);
    }

    @Override
    public boolean applyOptions() throws IOException {
        SearchSettings.SHOW_DETAIL_PAGE_AFTER_DOWNLOAD_START.setValue(DETAILS_CHECK_BOX.isSelected());
        return false;
    }

    @Override
    public void initOptions() {
        DETAILS_CHECK_BOX.setSelected(SearchSettings.SHOW_DETAIL_PAGE_AFTER_DOWNLOAD_START.getValue());
    }

    public boolean isDirty() {
        if (SearchSettings.SHOW_DETAIL_PAGE_AFTER_DOWNLOAD_START.getValue() != DETAILS_CHECK_BOX.isSelected()) {
            return true;
        }

        return false;
    }
}
