package com.limegroup.gnutella.gui.options.panes;

import java.io.IOException;

import javax.swing.JCheckBox;

import org.limewire.i18n.I18nMarker;

import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.settings.BittorrentSettings;

/**
 * Defines options for BitTorrent settings.
 */
public class TorrentDetailsPaneItem extends AbstractPaneItem {
	
    public final static String TITLE = I18n.tr("Torrent Details Page");
    
    public final static String DETAILS = I18n.tr("Show torrent details web page after a download starts. (Recommended)");
    
	private final JCheckBox DETAILS_CHECK_BOX = new JCheckBox();
	
	public TorrentDetailsPaneItem() {
	    super(TITLE, "");

		BoxPanel panel = new BoxPanel();
		LabeledComponent comp = new LabeledComponent(
				I18nMarker
                .marktr(DETAILS), DETAILS_CHECK_BOX,
				LabeledComponent.LEFT_GLUE, LabeledComponent.LEFT);
		panel.add(comp.getComponent());

		add(panel);
	}

	@Override
	public boolean applyOptions() throws IOException {
		BittorrentSettings.TORRENT_DETAIL_PAGE_SHOWN_AFTER_DOWNLOAD.setValue(DETAILS_CHECK_BOX.isSelected());
		return false;
	}

	@Override
	public void initOptions() {
		DETAILS_CHECK_BOX.setSelected(BittorrentSettings.TORRENT_DETAIL_PAGE_SHOWN_AFTER_DOWNLOAD.getValue());
	}

	public boolean isDirty() {
		if (BittorrentSettings.TORRENT_DETAIL_PAGE_SHOWN_AFTER_DOWNLOAD.getValue() != DETAILS_CHECK_BOX.isSelected())
			return true;
		
		return false;
	}

}
