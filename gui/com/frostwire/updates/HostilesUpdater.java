package com.frostwire.updates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.gudy.azureus2.core3.disk.DiskManager;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerListener;
import org.gudy.azureus2.core3.global.GlobalManagerDownloadRemovalVetoException;
import org.limewire.io.InvalidDataException;

import com.aelitis.azureus.core.AzureusCoreException;
import com.frostwire.CoreFrostWireUtils;
import com.frostwire.GuiFrostWireUtils;
import com.frostwire.bittorrent.AzureusStarter;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.library.SharingUtils;

/*
 * Given an update message. It checks if we have already downloaded 
 * the latest hostiles.txt. If not, it'll download the zipped file
 * from the torrent and it'll save on hostiles.dat the following:
 * - version of the hostiles.dat
 * - last timestamp (so that we don't do this twice the same day)
 */
public final class HostilesUpdater implements DownloadManagerListener {
	private static int TORRENT_DOWNLOAD_THREAD_MAX_MINUTES = 10;
	private static HostilesUpdater INSTANCE = null;
	private UpdateMessage _updateMessage = null;
	private HostilesMetaData _hostilesMetaData = null;
	private boolean _alreadyPostProcessed = false;
	private DownloadManager _manager = null;
	private CountDownLatch _processingLatch = new CountDownLatch(1);

	private HostilesUpdater() {

	} // HostilesUpdater

	public static HostilesUpdater getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new HostilesUpdater();
		}

		return INSTANCE;
	} // getInstance()

	/**
	 * This is the main method of this class. The one which controls what must
	 * be done in regards to a <message type="hostiles"> if found.
	 * 
	 * If will decide whether to download or not the hostiles torrent.
	 * 
	 * It won't download if: - It recently downloaded a hostiles file, to avoid
	 * unintentional massive requests in case a download couldn't be completed.
	 * 
	 * The current Thread will wait after this method finishes, so that it
	 * doesn't die. We have to notify it when we're done processing.
	 * 
	 * @see postProcessMessage(...)
	 * 
	 * @param message
	 * @param currentThread
	 */
	public void processMessage(UpdateMessage message) {
		// TODO: Remember to put back on UpdateManager.DEBUGGING_NON_UI_MESSAGES

		// A faulty message will cancel message processing.
		if (isMessageInvalid(message))
			return;

		_updateMessage = message;

		// no need to do anything, we're up to date.
		if (alreadyHaveValidHostilesTxt()) {
			try {
				// clean up just in case
				deleteOlderHostiles();
			} catch (Exception e) {
				System.out.println(e);
			}

			// Try to seed

			return;
		}

		checkHostilesMetaData();

		// starts a bittorrent downloader (creates it's own thread) and waits
		// for our torrent handling routine to do its work once the torrent has
		// been downloaded. Then our thread will come to an end once
		// processMessage
		// has finished or if
		startDownloadAndWait(TORRENT_DOWNLOAD_THREAD_MAX_MINUTES);
	} // processMessage

	/**
	 * Deferred activities to torrent event handling which will invoke
	 * postProcessing() are: - Unzipping the downloaded file. - Calculating the
	 * MD5 of the unzipped file. - If its good, save it as hostiles.txt, and
	 * restart IPFilter. - Seeding the torrent for a while. - Delete older zips
	 * and .torrents
	 */
	private final void postProcessMessage(boolean notifyWhenDone) throws IOException {
		try {
			File hostilesZip = new File(getAppSpecialShareFolder(),
					"hostiles.txt." + _hostilesMetaData.get_version() + ".zip");
	
			if (_hostilesMetaData == null)
				throw new InvalidDataException(
						"No HostilesMedataData available");
	
			File unzippedFile = new File(getHostilesTxtPath());
	
			// If zip exists, check if it has expected MD5,
			// if not deleted and unzip from new torrent.
			boolean keptOldhostilesDotText = true;
			if (unzippedFile.exists()
			    && !CoreFrostWireUtils.checkMD5(unzippedFile, _hostilesMetaData
							.get_md5())) {
				unzippedFile.delete(); //I will be replacing for the new hostiles.txt
				keptOldhostilesDotText = false;
			}
	
			// Delete older .torrents and .zips if there are any
			try {
				deleteOlderHostiles();
			} catch (Exception e) {
				// ignore
			}
	
			// Gotta unzip new one.
			if (!keptOldhostilesDotText || !unzippedFile.exists()) {
				try {
					GuiFrostWireUtils.unzip(hostilesZip, GuiFrostWireUtils
							.getPreferencesFolder());
				} catch (Exception e) {
					//ATTENTION _manager.stopIt(DownloadManager.STATE_ERROR, true, true);
					e.printStackTrace();
					hostilesZip.delete();
					saveHostilesMetaData(_hostilesMetaData,
							HostilesMetaData.STATUS_ERRORED);
	
					if (notifyWhenDone) {
						_alreadyPostProcessed = true;							
	
						notifyHostileUpdaterWorkerThread();
						return;
					}
				}
			}
	
			// make sure new zip exists
			if (!unzippedFile.exists())
				throw new IOException("Zip file "
						+ unzippedFile.getAbsolutePath() + " doesn't exist");
	
			// make sure MD5 is good (this takes a while)
			if (!GuiFrostWireUtils.checkMD5(unzippedFile, _hostilesMetaData
					.get_md5())) {
				hostilesZip.delete();
				unzippedFile.delete();
				//ATTENTION _manager.stopIt(DownloadManager.STATE_ERROR, true, true);
				throw new InvalidDataException(
						"MD5 of unzipped file does not coincide with "
								+ _hostilesMetaData.get_md5());
			}
	
			// if we make it to this point, we do update the hostiles.dat
			saveHostilesMetaData(_hostilesMetaData,
					HostilesMetaData.STATUS_PROCESSED);
	
			// Re-Initialize IPFilter.
			System.out.println("IPFilter has black listed hosts? (before) "
					+ GuiCoreMediator.getIpFilter().hasBlacklistedHosts());
			GuiCoreMediator.getIpFilter().forceRefreshHosts();
			System.out.println("IPFilter has black listed hosts? (after) "
					+ GuiCoreMediator.getIpFilter().hasBlacklistedHosts());
			
			notifyWhenDone = true;
	
		} catch (InvalidDataException e) {
			saveHostilesMetaData(_hostilesMetaData,
					HostilesMetaData.STATUS_ERRORED);
		} catch (IOException e2) {
			saveHostilesMetaData(_hostilesMetaData,
					HostilesMetaData.STATUS_ERRORED);
			System.out.println("Unzip FAIL! " + e2.getMessage());
			throw e2;
		} catch (Exception e3) {
			saveHostilesMetaData(_hostilesMetaData,
					HostilesMetaData.STATUS_ERRORED);
		} finally {
			if (notifyWhenDone) {
				notifyHostileUpdaterWorkerThread();
			}
		}
	} // postProcessMessage

	/**
	 * Check if we already have a hostiles.txt with the MD5 signature of the
	 * latest hostiles.txt specified by update.frostwire.com
	 * 
	 * @return
	 */
	private boolean alreadyHaveValidHostilesTxt() {
		// If we already have a hostiles.txt, check if its the latest.
		try {
			File hostilesTxt = new File(getHostilesTxtPath());

			if (!hostilesTxt.exists()) {
				// never downloaded hostiles.txt or lost it
				throw new Exception(
						"HostilesUpdater.alreadyHaveValidHostilesTxt(): Had no hostiles.txt - start from scratch");
			}

			if (CoreFrostWireUtils.checkMD5(hostilesTxt, _updateMessage
					.getRemoteMD5())) {
				System.out
						.println("HostilesUpdater.alreadyHaveValidHostilesTxt() - Done. We have a good hostiles.txt already");
				return true;
			} else {
				hostilesTxt.delete();
				hostilesTxt.deleteOnExit();
				throw new Exception(
						"HostilesUpdater.alreadyHaveValidHostilesTxt() - hostiles.txt is old or invalid");
			}

		} catch (Exception exception) {
			_hostilesMetaData = initHostilesMetaData(_updateMessage);
			saveHostilesMetaData(_hostilesMetaData,
					HostilesMetaData.STATUS_INITIATED);
		}

		return false;
	}

	/**
	 * Make sure the _hostilesMetaData object is OK. If it's not it creates and
	 * saves a new hostiles.dat and _hostilesMetaData Object.
	 */
	private void checkHostilesMetaData() {
		try {
			if (_hostilesMetaData == null) {
				// First try to fetch from disk.
				_hostilesMetaData = getLastHostilesMetaData();

				if (_hostilesMetaData == null)
					throw new InvalidDataException(
							"HostilesUpdater.checkHostilesMetaData(): We had no hostiles.dat");
			}

			if (_hostilesMetaData.get_status() == HostilesMetaData.STATUS_ERRORED)
				throw new InvalidDataException(
						"HostilesUpdater.checkHostilesMetaData(): Something went wrong the last time");

			if (!_hostilesMetaData.get_md5().equals(
					_updateMessage.getRemoteMD5()))
				throw new InvalidDataException(
						"HostilesUpdater.checkHostilesMetaData(): Had a good metadata but it was out of date");
		} catch (InvalidDataException e) {
			// Re-create a new hostiles data
			System.out.println("HostilesUpdater.processMessage() "
					+ e.getMessage());
			_hostilesMetaData = initHostilesMetaData(_updateMessage);
			saveHostilesMetaData(_hostilesMetaData,
					HostilesMetaData.STATUS_INITIATED);
		}
	} // checkHostilesMetaData

	/**
	 * Will start downloading the torrent and will lock (wait) the current
	 * thread for m minutes. If the download finishes before those m minutes,
	 * the handleTorrentEvent method will call for postprocessing and notify
	 * (unlock,wake up) the thread.
	 */
	private final void startDownloadAndWait(final int m) {
		// if we are here it means we must download the file.
		try {
			downloadHostiles(_hostilesMetaData);

			if (_alreadyPostProcessed)
				return;

			// check in case there's no need to wait for anybody
			if (_manager != null
					&& _manager.getState() == DownloadManager.STATE_STOPPED) {
				return;
			}

			if (_manager.getState() == DownloadManager.STATE_SEEDING) {
				postProcessMessage(true);
				return;
			}
			
			if (_manager.getState() == DownloadManager.STATE_WAITING)
				_manager.startDownload();
			
			_processingLatch.await();
		} catch (IOException e) {
			// Here's probably a good moment to handle all those exceptions.
			e.printStackTrace();
			saveHostilesMetaData(_hostilesMetaData,
					HostilesMetaData.STATUS_ERRORED);
			notifyHostileUpdaterWorkerThread();
			return;
		} catch (InterruptedException e) {
			e.printStackTrace();
			saveHostilesMetaData(_hostilesMetaData,
					HostilesMetaData.STATUS_ERRORED);
			notifyHostileUpdaterWorkerThread();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			saveHostilesMetaData(_hostilesMetaData,
					HostilesMetaData.STATUS_ERRORED);
			notifyHostileUpdaterWorkerThread();
		}
	} // processMessage

	/**
	 * Downloads the file with all the hostile ips.
	 * 
	 * Our current thread must be notified once the Azureus DownloadManagerImpl
	 * is done. In the meantime it should be waiting.
	 * 
	 * @param lastHostiles
	 * @throws Exception
	 */
	private final void downloadHostiles(HostilesMetaData lastHostiles)
			throws Exception {
		File hostilesTorrentsFolder = getAppSpecialShareFolder();

		File torrentFileLocation = new File(getAppSpecialShareFolder()
				+ File.separator + "hostiles.txt." + lastHostiles.get_version()
				+ ".zip.torrent");

		if (!hostilesTorrentsFolder.exists()) {
			hostilesTorrentsFolder.mkdir();
			hostilesTorrentsFolder.setWritable(true);
		}
		
		//We always re-download the torrent just in case.
		CoreFrostWireUtils.downloadTorrentFile(lastHostiles.get_torrentURL(), 
					torrentFileLocation);

		assert (torrentFileLocation.exists());

		String torrent_data_save_loc = getAppSpecialShareFolder()
				.getAbsolutePath();

		_hostilesMetaData.set_torrent_file_save_location(torrentFileLocation
				.getAbsolutePath());
		_hostilesMetaData.set_torrent_data_save_location(torrent_data_save_loc);
		saveHostilesMetaData(_hostilesMetaData,
				HostilesMetaData.STATUS_DOWNLOADING);

		// 3. Maybe we already have the file
		File zip = new File(torrent_data_save_loc + File.separator
				+ "hostiles.txt." + lastHostiles.get_version() + ".zip");
		if (zip.exists() && zip.isFile()) {
			boolean notifyIfPostProcessIsGood = true;
			try {
				postProcessMessage(false); // don't notify waiting threads or
				// mark as processed
			} 
			catch (IOException e) {
				notifyIfPostProcessIsGood = false;
				zip.delete();

				// 3. Start the Download (Torrent Data Download)
				_manager = GuiFrostWireUtils.startTorrentDownload(
						torrentFileLocation.getAbsolutePath(),
						torrent_data_save_loc, (DownloadManagerListener) this); // as a
				// DownloadManagerListener
			}
			if (notifyIfPostProcessIsGood)
				notifyHostileUpdaterWorkerThread();// if we get here, then
			// we're done.

		} else {
			// 3. Start the Download (Torrent Data Download)
			_manager = GuiFrostWireUtils.startTorrentDownload(torrentFileLocation
					.getAbsolutePath(), torrent_data_save_loc,
					(DownloadManagerListener) this); // as a
			// DownloadManagerListener
		}
	}

	private boolean isMessageInvalid(UpdateMessage message) {
		return message == null || message.getVersion() == null
				|| message.getVersion().equals("")
				|| message.getTorrent() == null
				|| message.getTorrent().equals("")
				|| message.getRemoteMD5() == null
				|| message.getRemoteMD5().equals("");
	}

	/**
	 * This is the path of where the final hostiles.txt should be. hostiles.txt
	 * is the file that finally seeds the IP Filter.
	 * 
	 * @return
	 */
	public final String getHostilesTxtPath() {
		return CoreFrostWireUtils.getPreferencesFolder() + File.separator
				+ "hostiles.txt";
	} // getHostilesTxtPath

	/**
	 * Returns the complete file path to the hostiles.dat file
	 * 
	 * @return
	 */
	private final String getHostilesDatPath() throws IOException {
		return CoreFrostWireUtils.getPreferencesFolder().getCanonicalPath()
				+ File.separator + "hostiles.dat";
	}

	private final File getAppSpecialShareFolder() throws IOException {
		return SharingUtils.APPLICATION_SPECIAL_SHARE;
	}

	/**
	 * Checks the last Hostiles file we downloaded. Information about it should
	 * exists on a file called hostiles.dat.
	 * 
	 * If this file doesn't exist, then this method returns null.
	 * 
	 * @return
	 */
	private final HostilesMetaData getLastHostilesMetaData() {
		HostilesMetaData result = null;
		try {
			File hostilesDatFile = new File(getHostilesDatPath());

			if (!hostilesDatFile.exists())
				return null;

			FileInputStream fis = new FileInputStream(hostilesDatFile);
			ObjectInputStream ois = new ObjectInputStream(fis);

			result = (HostilesMetaData) ois.readObject();
			
			fis.close();

			if (result == null)
				return null;

			System.out.println(result);

			// Invalid serialized data (probably first time)
			if (!result.is_valid()) {
				if (hostilesDatFile.exists())
					hostilesDatFile.delete();
				result.set_status(HostilesMetaData.STATUS_ERRORED);
				return result;
			}

		} catch (Exception e) {
			// processMessage will deal with us returning null
			e.printStackTrace();
			return null;
		}

		return result;
	} // getLastHostilesMetaData

	/*
	 * Given a message it'll create a HostilesMetaData object based on the
	 * message's data.
	 */
	private final HostilesMetaData initHostilesMetaData(UpdateMessage message) {
		HostilesMetaData result = new HostilesMetaData();
		result.set_date(Calendar.getInstance().getTimeInMillis());
		result.set_md5(message.getRemoteMD5());
		result.set_status(HostilesMetaData.STATUS_INITIATED);
		result.set_torrentURL(message.getTorrent());
		result.set_version(message.getVersion());

		_hostilesMetaData = result;

		return result;
	}

	/**
	 * Will create or overwrite the contents of hostiles.dat with the data
	 * inside the hostilesData object.
	 * 
	 * @param hostilesData
	 */
	private final void saveHostilesMetaData(HostilesMetaData hostilesMetaData,
			final Byte status) {
		try {
			String hostilesPath = getHostilesDatPath();

			hostilesMetaData.set_status(status);
			hostilesMetaData.set_date(System.currentTimeMillis());

			File f = new File(hostilesPath);

			if (f.exists())
				f.delete();

			f.createNewFile();

			FileOutputStream fos = new FileOutputStream(hostilesPath);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject((HostilesMetaData) hostilesMetaData);
			
			fos.close();

		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	/**
	 * Deletes hostile.x.torrents and zips that we don't need.
	 * 
	 * @throws InvalidDataException
	 * @throws GlobalManagerDownloadRemovalVetoException 
	 * @throws AzureusCoreException 
	 */
	private final void deleteOlderHostiles() throws InvalidDataException,
			IOException, AzureusCoreException, GlobalManagerDownloadRemovalVetoException {
		if (_updateMessage == null)
			throw new InvalidDataException(
					"There no UpdateMessage to know which is the newest hostiles file.");

		if (_hostilesMetaData == null)
			checkHostilesMetaData();

		// 1. Delete old .torrent's
		// Get the folder where all the .torrents should be
		File torrentSaveLocation = getAppSpecialShareFolder();

		File[] torrents = torrentSaveLocation.listFiles();

		// Get rid of .torrents and folders containing old .zips
		for (File t : torrents) {
			if (t.getName().startsWith("hostiles.txt") &&
					!t.getName().endsWith(_hostilesMetaData.get_version() + ".zip.torrent")
					&& !t.getName().equals(
							"hostiles.txt." + _hostilesMetaData.get_version()
									+ ".zip")) {
				if (t.isDirectory())
					CoreFrostWireUtils.deleteFolderRecursively(t);
				else {
					@SuppressWarnings("unchecked")
                    List<DownloadManager> managers = AzureusStarter.getAzureusCore().getGlobalManager().getDownloadManagers();
					for (DownloadManager manager : managers) {
						if (manager.getSaveLocation().equals(t)) {
							manager.stopIt(DownloadManager.STATE_CLOSED, true, true);
							AzureusStarter.getAzureusCore().getGlobalManager().removeDownloadManager(manager);
						}
					}
					t.delete();
				}
				System.out
						.println("HostilesUpdater.deleteOlderHostiles(): Deleted "
								+ t.getName());
			}
		}

	} // deleteOlderHostiles

	private final void notifyHostileUpdaterWorkerThread() {
		_alreadyPostProcessed = true;							

		if (_processingLatch != null)
			_processingLatch.countDown();
	}

	@Override
	public void completionChanged(DownloadManager manager, boolean completed) {

	}
	
	@Override
	public void filePriorityChanged(DownloadManager download,
			DiskManagerFileInfo file) {
	}

	@Override
	public void positionChanged(DownloadManager download, int oldPosition,
			int newPosition) {
	}

	@Override
	public void stateChanged(DownloadManager manager, int state) {
		if (_manager == null && manager != null)
			_manager = manager;

		CoreFrostWireUtils.printDiskManagerPieces(manager.getDiskManager());
		CoreFrostWireUtils.printDownloadManagerStatus(manager);
		
		if (torrentDataDownloadedToDisk()) {
			try {
				postProcessMessage(false);
			} catch (IOException e) {
			}
			return;
		}

		System.out.println("HostilesUpdater.stateChanged() - " + state + " completed: " + manager.isDownloadComplete(false));
		if (state == DownloadManager.STATE_SEEDING) {
			System.out.println("HostilesUpdater.stateChanged() - SEEDING!");
			try {
				postProcessMessage(false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (state == DownloadManager.STATE_ERROR) {
			saveHostilesMetaData(_hostilesMetaData,
					HostilesMetaData.STATUS_ERRORED);
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println(_manager.getErrorDetails());
			System.out.println("HostilesUpdater: ERROR - stopIt, startDownload!");
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

			//try to restart the download. delete torrent and data
			//manager.stopIt(DownloadManager.STATE_READY, false, true);
			try {
				AzureusStarter.getAzureusCore().getGlobalManager().removeDownloadManager(manager, false, true);
				processMessage(_updateMessage);
			} catch (GlobalManagerDownloadRemovalVetoException e) {
				e.printStackTrace();
			}
			
			saveHostilesMetaData(_hostilesMetaData,
					HostilesMetaData.STATUS_INITIATED);
		} else if (state == DownloadManager.STATE_DOWNLOADING) {
			System.out.println("stateChanged(STATE_DOWNLOADING)");
		} else if (state == DownloadManager.STATE_READY) {
			System.out.println("stateChanged(STATE_READY)");
			manager.startDownload();
			saveHostilesMetaData(_hostilesMetaData,
					HostilesMetaData.STATUS_DOWNLOADING);
		}
	}
	
	private boolean torrentDataDownloadedToDisk() {
		if (_manager == null || _manager.getDiskManager() == null)
			return false;
	
		String saveLocation = _hostilesMetaData.get_torrent_data_save_location();
		File f = new File(saveLocation);
		System.out.println(f.length());

		DiskManager dm = _manager.getDiskManager();
		//boolean filesExist = dm.filesExist();		
		int percentDone = dm.getPercentDone();		
		long totalLength = dm.getTotalLength();
		int rechecking = dm.getCompleteRecheckStatus();
		
		return f.exists() && f.length() == totalLength && percentDone == 1000 && rechecking == -1;		
	}

	@Override
	public void downloadComplete(DownloadManager manager) {
		// notifyHostileUpdaterWorkerThread();
		System.out.println("HostilesUpdater.downloadComplete()!!!!");
		CoreFrostWireUtils.printDownloadManagerStatus(_manager);
		File zipFile = null;
		try {
			zipFile = new File(getAppSpecialShareFolder(), "hostiles.txt."
					+ _hostilesMetaData.get_version() + ".zip");
			// Make sure the file is really complete
			if (zipFile.exists()) {
				postProcessMessage(true);
			} 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			saveHostilesMetaData(_hostilesMetaData, HostilesMetaData.STATUS_ERRORED);
		}
	}

} // HostilesUpdater
