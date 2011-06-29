package com.limegroup.gnutella.gui.options.panes;

import java.io.IOException;

import com.frostwire.gui.bittorrent.BTDownloadMediator;
import com.frostwire.gui.bittorrent.SeedingSettingComponent;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.SharingSettings;

public class TorrentSeedingSettingPaneItem extends AbstractPaneItem {


	public final static String TITLE = I18n.tr("Seeding Settings");
	
	private SeedingSettingComponent COMPONENT;

	public TorrentSeedingSettingPaneItem() {
		super(TITLE, I18n.tr("Seeding is the process of connecting to a torrent when you have a complete file(s). Pieces of the seeded file(s) will be available to everybody. While downloading pieces are always available to other peers in the swarm."));
		
		COMPONENT = new SeedingSettingComponent(true, false);
        add(COMPONENT);
	}
	
	@Override
	public boolean isDirty() {
		// nothing the component does it.
		return false;
	}

	@Override
	public void initOptions() {
		// nothing the component does it.
	}

	@Override
	public boolean applyOptions() throws IOException {
		SharingSettings.SEED_FINISHED_TORRENTS.setValue(COMPONENT.wantsSeeding());		

		if (!COMPONENT.wantsSeeding()) {
    		BTDownloadMediator.instance().stopCompleted();
        }

		return false;
	}

}
