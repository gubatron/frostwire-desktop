package com.frostwire;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.limewire.io.IOUtils;
import org.limewire.util.FileUtils;

import com.limegroup.bittorrent.BTMetaInfo;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.settings.SharingSettings;

public final class GuiFrostWireUtils extends CoreFrostWireUtils {
	private final static boolean canShareTorrentMetaFiles() {
		if (!SharingSettings.DEFAULT_SHARED_TORRENTS_DIR.exists()) {
			SharingSettings.DEFAULT_SHARED_TORRENTS_DIR.mkdir();
		}
		
		return SharingSettings.SHARE_TORRENT_META_FILES.getValue() &&
        SharingSettings.DEFAULT_SHARED_TORRENTS_DIR.exists() &&
        SharingSettings.DEFAULT_SHARED_TORRENTS_DIR.isDirectory() &&
        SharingSettings.DEFAULT_SHARED_TORRENTS_DIR.canWrite();
	}

	public final static void shareTorrent(BTMetaInfo bt, byte[] body) {
        if (!canShareTorrentMetaFiles())
        	return;

		BufferedOutputStream bos = null;
		try {
			File newTorrent = new File(
					SharingSettings.DEFAULT_SHARED_TORRENTS_DIR, bt.getName()
							.concat(".torrent"));

			bos = new BufferedOutputStream(new FileOutputStream(newTorrent));
			bos.write(body);
			bos.flush();
			
			verifySharedTorrentFolderCorrecteness();
		} catch (Exception e) {
			// we tried...
		} finally {
			IOUtils.close(bos);
		}	
	} //shareTorrent
	
	public final static void shareTorrent(File f) {
		if (!canShareTorrentMetaFiles())
			return;
		
		//make a copy of the torrent (wherever it is they are opening it from) to our torrents/ folder and share it.
		File newTorrent = new File(SharingSettings.DEFAULT_SHARED_TORRENTS_DIR,f.getName());
		FileUtils.copy(f, newTorrent);
		
		verifySharedTorrentFolderCorrecteness();			
	} //shareTorrent
	
	
	/**
	 * Makes sure the Torrents/ folder exists.
	 * If it's shareable it will make sure the folder is shared.
	 * If not it'll make sure all the torrents inside are not shared.
	 */
	public final static void verifySharedTorrentFolderCorrecteness() {
		canShareTorrentMetaFiles();
		
		if (SharingSettings.SHARE_TORRENT_META_FILES.getValue()) {
			GuiCoreMediator.getFileManager().addSharedFolder(
					SharingSettings.DEFAULT_SHARED_TORRENTS_DIR);
		}
		
		//share/unshare all torrents inside
		File[] torrents = SharingSettings.DEFAULT_SHARED_TORRENTS_DIR.listFiles();
		if (torrents!=null && torrents.length > 0) {
			for (File t : torrents) {
				if (SharingSettings.SHARE_TORRENT_META_FILES.getValue())
					GuiCoreMediator.getFileManager().addFileAlways(t);
				else
					GuiCoreMediator.getFileManager().stopSharingFile(t);
			}
		}
		
	} //verifySharedTorrentFolderCorrecteness

}
