package com.frostwire.bittorrent;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.limegroup.bittorrent.BTData;
import com.limegroup.bittorrent.BTDataImpl;
import com.limegroup.bittorrent.BTMetaInfo;
import com.limegroup.bittorrent.ValueException;
import com.limegroup.bittorrent.bencoding.Token;

//import com.limegroup.bittorrent.BTDataImplTM;

/**
 * This class is used to get and download the selected files only
 * 
 * Gubatron: Can you please explain what the name of the class stands for? BT =
 * BitTorrent FM = ?
 * 
 * 
 * @author FTA
 */

public class BTMetaInfoBTFM {
	
	public BTData _torrentData = null;

	public BTMetaInfoBTFM() {
		
	}
	
	/**
	 * Reads a BTMetaInfo from byte [] using BT with Torrent Manager THIS IS A
	 * SIMILAR PROCEDURE, IN ORDER TO KEEP THE SAME METHOD A SIMILAR METHOD HAS
	 * BEEN USED WITHOUT TOUCH "readfromBytes"
	 * 
	 * Gubatron: refactored to put the torrentData inside this object.
	 * 
	 * @see BTMetaInfoBTFM.getFiles(), BTMetaInfoBTFM.getBTData()
	 * @see getBTData()
	 * 
	 * @param torrent
	 *            byte array with the contents of .torrent
	 * @throws IOException
	 *             if parsing or reading failed.
	 */
	// Try to load the files inside
	public void readFromBytesFullList(byte[] torrent) throws IOException {
		try {
			Object metaInfo = Token.parse(torrent);

			if (!(metaInfo instanceof Map))
				throw new ValueException("metaInfo not a Map!");
			_torrentData = new BTDataImpl((Map<?, ?>) metaInfo); // full list of files
															// inside the
															// torrent

		} catch (IOException bad) {
			System.out
					.println("BTMetaInfo.readFromBytesFullList() - read failed!: "
							+ bad);
			throw bad;
		}
	}

	/**
	 * Returns the list of files inside the torrent. Only to be used after
	 * readFromBytesFullList has been invoked otherwise it'll return null
	 * 
	 * @return
	 */
	public List<BTData.BTFileData> getFiles() {
		if (_torrentData != null)
			return _torrentData.getFiles();
		return null;
	}

	/**
	 * Returns the entire BTData object if available. Use readFromBytesFullList
	 * first
	 * 
	 * @return
	 */
	public BTData getBTData() {
		return _torrentData;
	}

	// Tries to download the selected files only
	public static BTMetaInfo readFromBytesSelected(
			List<com.limegroup.bittorrent.BTData.BTFileData> selectedfiles,
			Set<String> selectedfolders, BTData torrentData) throws IOException {
		try {
			System.out
					.println("*****START THE DOWNLOADING PROCESS OF SELECTED FILES******");
			BTData validlist = new BTDataImpl(selectedfiles, selectedfolders,
					torrentData);
			BTMetaInfo tempo = new BTMetaInfo(validlist); // Bit torrent
			// handling using
			// the FrostWire's
			// Torrent Manager
			// *temporary under
			// limegroup*
			return tempo;
		} catch (IOException badm) {
			System.out
					.println("BTMetaInfo.readFromBytesSelected() - read failed!: "
							+ badm);
			throw badm;
		}
	}

}