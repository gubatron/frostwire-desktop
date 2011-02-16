package com.frostwire.bittorrent;


// I/O Handling
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.limegroup.bittorrent.BTMetaInfo;

/**
 *
 * This class reads the files inside the .torrent file and then it shows a Dialog with checkboxes.
 * It uses the same 
 *
 * @author Fernando Toussaint
 *
 **/

public class BTFilesManagerFactory {

	private byte [] b;
	public BTMetaInfo info;
	private File f;

	// Constructor
	public BTFilesManagerFactory(byte [] b, BTMetaInfo info, File f) {
		this.b = b;
		this.info = info;
		this.f = f; // Torrent's file
	}

	// Read the files from torrent
	public void loadFiles() throws IOException {

		com.limegroup.gnutella.gui.GUIMediator
				.showMessage("Please choose which files you would like to download from this torrent."); // FTA
																											// DEBUG
		
		com.limegroup.bittorrent.BTData torrentData = null;
		BTMetaInfo btMeta = new BTMetaInfo();
		btMeta.readFromBytesFullList(b);
		torrentData = btMeta.getBTData();

		// System.out.println("FTA Debug Announce server: " +
		// torrentData.getAnnounce());
		List<com.limegroup.bittorrent.BTData.BTFileData> filesinside = btMeta.getFiles();
		String FileString = "";

		com.frostwire.bittorrent.BTFilesManager ftoask = new com.frostwire.bittorrent.BTFilesManager(
				filesinside, f); // FTA: Call the frame to ask them

		ftoask.setSize(500, 220);
		ftoask.setLocationRelativeTo(null);
		ftoask.setVisible(true);
		if (ftoask._selectedfiles != null) {
			// System.out.print("FILES SELECTED!!"); //FTA DEBUG
			for (com.limegroup.bittorrent.BTData.BTFileData currfile : ftoask._selectedfiles) {
				System.out.println("Selected file: " + currfile.getPath() + " "
						+ currfile.getLength() + " Bytes"); // FTA DEBUG
				FileString = FileString + currfile.getPath() + "\n";
			}
			info = BTMetaInfo.readFromBytesSelected(ftoask._selectedfiles,
					ftoask._selectedfolders, torrentData); // sends torrent data
															// (tracker,
															// announce,
															// private..etc.) //
															// Tries to download
															// only the selected
															// files from
															// torrent.
			return; // Everything OK!
		}
		System.out.println("Saludo al salir fue: " + ftoask.greeting());
		throw new IOException("No files selected by user");
	}
}