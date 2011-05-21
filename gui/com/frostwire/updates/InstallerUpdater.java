package com.frostwire.updates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.gudy.azureus2.core3.disk.DiskManager;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerListener;
import org.gudy.azureus2.core3.global.GlobalManagerDownloadRemovalVetoException;
import org.limewire.util.OSUtils;

import com.frostwire.CoreFrostWireUtils;
import com.frostwire.bittorrent.AzureusStarter;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.library.SharingUtils;
import com.limegroup.gnutella.settings.UpdateSettings;

public class InstallerUpdater implements Runnable, DownloadManagerListener {
	
	private DownloadManager _manager = null;
	private UpdateMessage _updateMessage;
	private File _executableFile;

	public InstallerUpdater(UpdateMessage updateMessage) {
		_updateMessage = updateMessage;
	}

	public void start() {
		new Thread(this, "InstallerUpdater").start();	
	}
	
	public void run() {
		if (!UpdateSettings.AUTOMATIC_INSTALLER_DOWNLOAD.getValue()) {
			return;
		}
		
		if (checkIfDownloaded()) {
			showUpdateMessage();
		}
		else {
			
			File torrentFileLocation = downloadDotTorrent();
			
			try {
				CoreFrostWireUtils.waitForAzureusCoreToStart();
				
				_manager = CoreFrostWireUtils.startTorrentDownload(torrentFileLocation
						.getAbsolutePath(), SharingUtils.APPLICATION_SPECIAL_SHARE.getAbsolutePath(),
						(DownloadManagerListener) this);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}

	private void showUpdateMessage() {
		
		if (_executableFile == null)
			return;
		
		int result = JOptionPane.showConfirmDialog(null, 
                _updateMessage.getMessageInstallerReady(),
                I18n.tr("Update"), 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.INFORMATION_MESSAGE);
		
		if (result == JOptionPane.YES_OPTION) {
			try {
				if (OSUtils.isWindows()) {
					String[] commands =  new String[] {
							"CMD.EXE",
							"/C",
							_executableFile.getAbsolutePath()
					};
					
					ProcessBuilder pbuilder = new ProcessBuilder(commands);
					pbuilder.start();					
				}  else if (OSUtils.isLinux() && OSUtils.isUbuntu()) {
					String[] commands = new String[] {
							"gdebi-gtk",
							_executableFile.getAbsolutePath() };
							
					ProcessBuilder pbuilder = new ProcessBuilder(commands);
					pbuilder.start();
					//Runtime.getRuntime().exec("gdebi", new String[] {_executableFile.getAbsolutePath() });
				}
				
				GUIMediator.shutdown();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	private File downloadDotTorrent() {
		
		File appSpecialShareFolder = SharingUtils.APPLICATION_SPECIAL_SHARE;
		
		int index = _updateMessage.getTorrent().lastIndexOf('/');
		File torrentFileLocation = new File(appSpecialShareFolder, _updateMessage.getTorrent().substring(index + 1));

		if (!appSpecialShareFolder.exists()) {
			appSpecialShareFolder.mkdir();
			appSpecialShareFolder.setWritable(true);
		}
		
		//We always re-download the torrent just in case.
//		try {
//			//CoreFrostWireUtils.downloadTorrentFile(_updateMessage.getTorrent(), 
//			//			torrentFileLocation);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		assert (torrentFileLocation.exists());
		
		return torrentFileLocation;
	}
	
	private final InstallerMetaData getLastInstallerMetaData() {
		InstallerMetaData result = null;
		try {
			File installerDatFile = new File(getInstallerDatPath());

			if (!installerDatFile.exists())
				return null;

			FileInputStream fis = new FileInputStream(installerDatFile);
			ObjectInputStream ois = new ObjectInputStream(fis);

			result = (InstallerMetaData) ois.readObject();

			if (result == null)
				return null;

			System.out.println(result);
			
			fis.close();

			return result;

		} catch (Exception e) {
			// processMessage will deal with us returning null
			e.printStackTrace();
			return null;
		}
	}

	private boolean checkIfDownloaded() {
		
		InstallerMetaData md = getLastInstallerMetaData();
		
		if (md == null)
			return false;
		
		if (!md.frostwireVersion.equals(_updateMessage.getVersion()))
			return false;
		
		int indx1 = _updateMessage.getTorrent().lastIndexOf('/') + 1;
		int indx2 = _updateMessage.getTorrent().lastIndexOf(".torrent");
		
		String subStr = _updateMessage.getTorrent().substring(indx1, indx2);
		
		File f = new File(SharingUtils.APPLICATION_SPECIAL_SHARE, subStr);
		
		if (!f.exists())
			return false;
		
		_executableFile = f;
		
		try {
			return CoreFrostWireUtils.checkMD5(f, _updateMessage.getRemoteMD5());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void completionChanged(DownloadManager manager, boolean bCompleted) {		
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
			
			return;
		}

		System.out.println("InstallerUpdater.stateChanged() - " + state + " completed: " + manager.isDownloadComplete(false));
		if (state == DownloadManager.STATE_SEEDING) {
			System.out.println("InstallerUpdater.stateChanged() - SEEDING!");
			
			return;
		}

		if (state == DownloadManager.STATE_ERROR) {
			//saveHostilesMetaData(_hostilesMetaData,
			//		HostilesMetaData.STATUS_ERRORED);
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println(_manager.getErrorDetails());
			System.out.println("InstallerUpdater: ERROR - stopIt, startDownload!");
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

			//try to restart the download. delete torrent and data
			//manager.stopIt(DownloadManager.STATE_READY, false, true);
			try {
				AzureusStarter.getAzureusCore().getGlobalManager().removeDownloadManager(manager, false, true);
				//processMessage(_updateMessage);
			} catch (GlobalManagerDownloadRemovalVetoException e) {
				e.printStackTrace();
			}
			
			//saveHostilesMetaData(_hostilesMetaData,
				//	HostilesMetaData.STATUS_INITIATED);
		} else if (state == DownloadManager.STATE_DOWNLOADING) {
			System.out.println("stateChanged(STATE_DOWNLOADING)");
		} else if (state == DownloadManager.STATE_READY) {
			System.out.println("stateChanged(STATE_READY)");
			manager.startDownload();
			//saveHostilesMetaData(_hostilesMetaData,
				//	HostilesMetaData.STATUS_DOWNLOADING);
		}
	}
	
	@Override
	public void downloadComplete(DownloadManager manager) {
		// notifyHostileUpdaterWorkerThread();
		System.out.println("InstallerUpdater.downloadComplete()!!!!");
		CoreFrostWireUtils.printDownloadManagerStatus(_manager);
		
		saveMetaData();
		cleanupOldUpdates();
	}
	
	private void cleanupOldUpdates() {
		
		final Pattern p = Pattern.compile("^frostwire-([0-9]+[0-9]?\\.[0-9]+[0-9]?\\.[0-9]+[0-9]?)\\.windows\\.exe(\\.torrent)?$");
		
		for (File f : SharingUtils.APPLICATION_SPECIAL_SHARE.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				
				Matcher m = p.matcher(name);
				
				if (m.matches()) {
					return !m.group(1).equals(_updateMessage.getVersion());
				}
				
				return false;
			}
		})) {
			
			f.delete();
		}
	}

	private final String getInstallerDatPath()  {
		return CoreFrostWireUtils.getPreferencesFolder().getAbsolutePath()
				+ File.separator + "installer.dat";
	}
	
	private void saveMetaData() {
		try {
			String installerPath = getInstallerDatPath();

			InstallerMetaData md = new InstallerMetaData();
			md.frostwireVersion = _updateMessage.getVersion();
			
			File f = new File(installerPath);

			if (f.exists())
				f.delete();

			f.createNewFile();

			FileOutputStream fos = new FileOutputStream(installerPath);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject((InstallerMetaData) md);
			
			fos.close();

		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	private boolean torrentDataDownloadedToDisk() {
		
		if (_manager == null || _manager.getDiskManager() == null)
			return false;
	
		String saveLocation = SharingUtils.APPLICATION_SPECIAL_SHARE.getAbsolutePath();
		File f = new File(saveLocation);
		System.out.println(f.length());

		DiskManager dm = _manager.getDiskManager();
		//boolean filesExist = dm.filesExist();		
		int percentDone = dm.getPercentDone();		
		long totalLength = dm.getTotalLength();
		int rechecking = dm.getCompleteRecheckStatus();
		
		return f.exists() && f.length() == totalLength && percentDone == 1000 && rechecking == -1;		
	}
}
