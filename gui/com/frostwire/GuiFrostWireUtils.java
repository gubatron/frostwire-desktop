package com.frostwire;

import java.awt.GraphicsEnvironment;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdesktop.jdic.desktop.Desktop;
import org.limewire.collection.SortedList;
import org.limewire.io.IOUtils;
import org.limewire.util.FileUtils;
import org.limewire.util.OSUtils;

import com.limegroup.bittorrent.BTMetaInfo;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.settings.SharingSettings;

public final class GuiFrostWireUtils extends CoreFrostWireUtils {
	private final static boolean canShareTorrentMetaFiles() {
		if (!SharingSettings.DEFAULT_SHARED_TORRENTS_DIR.exists()) {
			SharingSettings.DEFAULT_SHARED_TORRENTS_DIR.mkdir();
		}

		return SharingSettings.SHARE_TORRENT_META_FILES.getValue()
				&& SharingSettings.DEFAULT_SHARED_TORRENTS_DIR.exists()
				&& SharingSettings.DEFAULT_SHARED_TORRENTS_DIR.isDirectory()
				&& SharingSettings.DEFAULT_SHARED_TORRENTS_DIR.canWrite();
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
	} // shareTorrent

	public final static void shareTorrent(File f) {
		if (!canShareTorrentMetaFiles())
			return;

		// make a copy of the torrent (wherever it is they are opening it from)
		// to our torrents/ folder and share it.
		File newTorrent = new File(SharingSettings.DEFAULT_SHARED_TORRENTS_DIR,
				f.getName());
		FileUtils.copy(f, newTorrent);

		verifySharedTorrentFolderCorrecteness();
	} // shareTorrent

	/**
	 * Makes sure the Torrents/ folder exists. If it's shareable it will make
	 * sure the folder is shared. If not it'll make sure all the torrents inside
	 * are not shared.
	 */
	public final static void verifySharedTorrentFolderCorrecteness() {
		canShareTorrentMetaFiles();

		if (SharingSettings.SHARE_TORRENT_META_FILES.getValue()) {
			//GuiCoreMediator.getFileManager().addSharedFolder(
			//		SharingSettings.DEFAULT_SHARED_TORRENTS_DIR);
		}

		// share/unshare all torrents inside
		File[] torrents = SharingSettings.DEFAULT_SHARED_TORRENTS_DIR
				.listFiles();
		if (torrents != null && torrents.length > 0) {
			for (File t : torrents) {
				if (SharingSettings.SHARE_TORRENT_META_FILES.getValue() &&
				    GuiCoreMediator.getFileManager().isFolderShared(SharingSettings.DEFAULT_SHARED_TORRENTS_DIR))
					GuiCoreMediator.getFileManager().addFileAlways(t);
				else
					GuiCoreMediator.getFileManager().stopSharingFile(t);
			}
		}

	} // verifySharedTorrentFolderCorrecteness

	/**
	 * The idea is to find the first of the given options for Font Families.
	 * Inspired on CSS's font-family: Font1, Font2, Font3.
	 * 
	 * If Font1 is found, it'll be returned. If not it'll go to Font2 and so on.
	 * Only that this method has a fallback default font family and you can
	 * specify more than just 3 fonts.
	 * 
	 * Take that CSS.
	 * 
	 * @param defaultFamily
	 * @param prioritizedFamilies (case independent font family names)
	 * @return The name of the highest priority family name found in the system, if nothing is found it'll return the default family value.
	 */
	public final static String getFontFamily(String defaultFamily,
			String... prioritizedFamilies) {
		String[] availableFontFamilyNames = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

		// reverse priority index.
		final Map<String, Integer> reversePriorityIndex = new HashMap<String, Integer>();
		for (int i = 0; i < prioritizedFamilies.length; i++) {
			reversePriorityIndex.put(prioritizedFamilies[i].toLowerCase(), i);
		}

		// If we find a family we'll insert it in order by priority on this
		// list.
		List<String> foundFamilies = new SortedList<String>(
				new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						/**
						 * We compare these guys by their priority. Lower has
						 * higher priority
						 */
						if (o1.equals(o2))
							return 0;

						// compare backwards since lower means higher priority.
						return new Integer(reversePriorityIndex.get(o1
								.toLowerCase())).compareTo(reversePriorityIndex
								.get(o2.toLowerCase()));
					}
				});

		// scan available family names and see if we got any of our candidates.
		for (String fontName : availableFontFamilyNames) {
			if (reversePriorityIndex.containsKey(fontName.toLowerCase())) {
				foundFamilies.add(fontName);
			}
		}

		if (foundFamilies.size() > 0) {
			return foundFamilies.get(0);
		}

		return defaultFamily;
	}
	
	/**
	 * Tries to open a file using java.awt.Desktop, jdic, or GUIMediator.launchFile.
	 * 
	 * @param file
	 */
	public static void launchFile(File file) {
		try {
			boolean isJava16orGreater = CoreFrostWireUtils
					.isJavaMinorVersionEqualOrGreaterThan("1.6");

			if (isJava16orGreater) {
				java.awt.Desktop.getDesktop().open(file);
			} else {
				//try jdic if you're not a mac and you're on below 1.6
				if (!OSUtils.isMacOSX()) {
					Desktop.open(file);
				} else {
					//if you're an old mac, try the old way
					GUIMediator.launchFile(file);
				}
			}
		} catch (Exception e) {
			GUIMediator.launchFile(file);
		}
	}
}
