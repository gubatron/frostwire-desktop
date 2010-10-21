package com.limegroup.bittorrent.gui;

import com.limegroup.bittorrent.ManagedTorrent;
import com.limegroup.bittorrent.TorrentEvent;
import com.limegroup.bittorrent.TorrentEventListener;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.DialogOption;
import com.limegroup.gnutella.settings.QuestionsHandler;
import com.limegroup.gnutella.util.EventDispatcher;

public class TorrentUploadCanceller implements TorrentEventListener {

	private final EventDispatcher<TorrentEvent, TorrentEventListener> dispatcher;
	
	public static void createAndRegister(
			EventDispatcher<TorrentEvent, TorrentEventListener> dispatcher) {
		TorrentUploadCanceller canceller = new TorrentUploadCanceller(dispatcher);
		dispatcher.addEventListener(canceller);
	}
	
	private TorrentUploadCanceller(EventDispatcher<TorrentEvent, TorrentEventListener> dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	public void handleTorrentEvent(TorrentEvent evt) {
		if (evt.getType() != TorrentEvent.Type.STOP_REQUESTED)
			return;
		
		ManagedTorrent t = evt.getTorrent();
        if (!t.isActive())
            return;
		boolean approve = true;
		if (!t.isComplete()) {
			approve = GUIMediator.showYesNoMessage(I18n.tr("If you stop this upload, the torrent download will stop. Are you sure you want to do this?"), 
					QuestionsHandler.TORRENT_STOP_UPLOAD, DialogOption.NO) == DialogOption.YES;
		} else if (t.getRatio() < 1.0f) {
			approve = GUIMediator.showYesNoMessage(I18n.tr("This upload is a torrent and it hasn\'t seeded enough. You should let it upload some more. Are you sure you want to stop it?"), 
					QuestionsHandler.TORRENT_SEED_MORE, DialogOption.NO) == DialogOption.YES;
		}
		
		if (approve && t.isActive())
			dispatcher.dispatchEvent(new TorrentEvent(this,TorrentEvent.Type.STOP_APPROVED, t));
	}

}
