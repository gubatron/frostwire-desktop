package com.frostwire;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.gudy.azureus2.core3.disk.DiskManager;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerPiece;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerDiskListener;
import org.gudy.azureus2.core3.download.DownloadManagerListener;
import org.gudy.azureus2.core3.download.DownloadManagerStats;
import org.gudy.azureus2.core3.util.DisplayFormatters;
import org.limewire.util.CommonUtils;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreComponent;
import com.aelitis.azureus.core.AzureusCoreException;
import com.aelitis.azureus.core.AzureusCoreLifecycleListener;
import com.frostwire.bittorrent.AzureusStarter;
import com.frostwire.guice.FrostWireCoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.limegroup.bittorrent.BTMetaInfo;
import com.limegroup.gnutella.http.HttpExecutor;
import com.limegroup.gnutella.library.SharingUtils;
import com.limegroup.gnutella.util.LimeWireUtils;

public class CoreFrostWireUtils {
	
	private static Injector INJECTOR;
	
	public final static File getPreferencesFolder() {
	    return LimeWireUtils.getRequestedUserSettingsLocation();
	}
	
    public final static String getMD5(File f) throws Exception{
        MessageDigest m=MessageDigest.getInstance("MD5");

        //We read the file in buffers so we don't
        //eat all the memory in case we have a huge plugin.
        byte[] buf = new byte[65536];
        int num_read;

        InputStream in = new BufferedInputStream(new FileInputStream(f));

        while ((num_read = in.read(buf)) != -1) {
            m.update(buf, 0, num_read);
        }
        
        in.close();

        String result = new BigInteger(1,m.digest()).toString(16);

        //pad with zeros if until it's 32 chars long.
        if (result.length() < 32) {
            int paddingSize = 32 - result.length();
            for (int i=0; i < paddingSize; i++)
                result = "0" + result;
        }
        
        System.out.println("MD5: "+ result);
        return result;
    }
    
    public final static byte[] getSHA1(File f) {
        MessageDigest m;
		try {
			m = MessageDigest.getInstance("SHA-1");
		

	        //We read the file in buffers so we don't
	        //eat all the memory in case we have a huge plugin.
	        byte[] buf = new byte[65536];
	        int num_read;
	
	        InputStream in = new BufferedInputStream(new FileInputStream(f));
	
	        while ((num_read = in.read(buf)) != -1) {
	            m.update(buf, 0, num_read);
	        }
	        
	        in.close();
	
	        //String result = new BigInteger(1,m.digest()).toString(16);
	
	        //pad with zeros if until it's 40 chars long.
	        //if (result.length() < 40) {
	        //    int paddingSize = 40 - result.length();
	        //   for (int i=0; i < paddingSize; i++)
	        //        result = "0" + result;
	        //}
	        
	        //System.out.println("SHA1: "+ result);
	        return m.digest();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }

	
	/**
	 * Returns true if the MD5 of the file corresponds to the given MD5 string.
	 * It works with lowercase or uppercase, you don't need to worry about that.
	 * 
	 * @param f
	 * @param expectedMD5
	 * @return
	 * @throws Exception
	 */
	public final static boolean checkMD5(File f, String expectedMD5) throws Exception {
	    if (expectedMD5 == null) {
	        throw new Exception("Expected MD5 is null");
	    }
	    
	    if (expectedMD5.length() != 32) {
	        throw new Exception("Invalid Expected MD5, not 32 chars long");
	    }
	    
	    return getMD5(f).trim().equalsIgnoreCase(expectedMD5.trim());
	}
	
	public final static void deleteFolderRecursively(File folder) {
	    if (folder.isFile()) {
	        folder.delete();
	        return;
	    }
	        
	    File[] files = folder.listFiles();
	    
	    if (files.length > 0) {
	        for (File f : files) {
	            if (f.isDirectory())
	                deleteFolderRecursively(f);
	            else
	                f.delete();
	        }
	    }
	    
	    folder.delete();
	} //deleteFolderRecursively
	
	/** Downloads a file from an HTTP Url and returns a byte array.
	 * You do with it as you please, you create a file or you
	 * use the byte[] directly. */
	public final static byte[] downloadHttpFile(String url) {
	    try {
			return new HttpFileFetcher(new URI(url)).fetch();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public final static String getInstallationFolder() {
		Properties props = System.getProperties();
		return props.getProperty("user.dir");
	}
	
	public static void printDiskManagerPieces(DiskManager dm) {
		if (dm == null)
			return;
		DiskManagerPiece[] pieces = dm.getPieces();
		for (DiskManagerPiece piece : pieces) {
			System.out.print(piece.isDone() ? "1":"0");
		}
		System.out.println();
	}
	
	
	public static void printDownloadManagerStatus(DownloadManager manager) {
		if (manager == null)
			return;
		
		StringBuffer buf = new StringBuffer();
		buf.append(" Completed:");
		
		
		DownloadManagerStats stats = manager.getStats();

		int completed = stats.getCompleted();
		buf.append(completed / 10);
		buf.append('.');
		buf.append(completed % 10);
		buf.append('%');
		buf.append(" Seeds:");
		buf.append(manager.getNbSeeds());
		buf.append(" Peers:");
		buf.append(manager.getNbPeers());
		buf.append(" Downloaded:");
		buf.append(DisplayFormatters.formatDownloaded(stats));
		buf.append(" Uploaded:");
		buf.append(DisplayFormatters.formatByteCountToKiBEtc(stats
				.getTotalDataBytesSent()));
		buf.append(" DSpeed:");
		buf.append(DisplayFormatters.formatByteCountToKiBEtcPerSec(stats
				.getDataReceiveRate()));
		buf.append(" USpeed:");
		buf.append(DisplayFormatters.formatByteCountToKiBEtcPerSec(stats
				.getDataSendRate()));
		buf.append(" TrackerStatus:");
		buf.append(manager.getTrackerStatus());
		while (buf.length() < 80) {
			buf.append(' ');
		}
		
		buf.append(" TO:");
		buf.append(manager.getSaveLocation().getAbsolutePath());
		
		System.out.println(buf.toString());							
		
	}

	/**
	 * Returns the path of the unzipped file
	 * @param inFile
	 * @param outFolder
	 * @return
	 */
	public final static void unzip(File inFile, File outFolder) throws Exception {
	        int BUFFER = 2048;
	        BufferedOutputStream out = null;
	        
	        File outputFile = new File(outFolder.getCanonicalPath(),inFile.getName());
	        if (outputFile.exists())
	            outputFile.delete();
	        
	        ZipInputStream  in = new ZipInputStream(
	                new BufferedInputStream(
	                        new FileInputStream(inFile)));
	        ZipEntry entry;
	        while((entry = in.getNextEntry()) != null) {
	            //System.out.println("Extracting: " + entry);
	            int count;
	            byte data[] = new byte[BUFFER];

	            // write the files to the disk
	            out = new BufferedOutputStream(
	                    new FileOutputStream(outFolder.getPath() + "/" + entry.getName()),BUFFER);

	            while ((count = in.read(data,0,BUFFER)) != -1) {
	                out.write(data,0,count);
	            }
	            out.flush();
	            out.close();
	        }
	        in.close();
	} //unzip

	public final static DownloadManager startTorrentDownload(String torrentFile, 
			String saveDataPath,
			DownloadManagerListener listener) throws Exception {

		waitForAzureusCoreToStart();
		
		DownloadManager manager = AzureusStarter.getAzureusCore()
				.getGlobalManager().addDownloadManager(torrentFile, saveDataPath);
		manager.addListener(listener);
		
		//if (manager.isDownloadComplete(false)) {
		//	manager.setForceStart(true);
		//}

		manager.initialize();
		
		return manager;
	}

	public final static void waitForAzureusCoreToStart() throws InterruptedException {
		
		if (AzureusStarter.getAzureusCore().isStarted())
			return;
		
		final CountDownLatch latch = new CountDownLatch(1);
		AzureusStarter.getAzureusCore().addLifecycleListener(new AzureusCoreLifecycleListener() {

			@Override
			public void componentCreated(AzureusCore core,
					AzureusCoreComponent component) {
			}

			@Override
			public boolean requiresPluginInitCompleteBeforeStartedEvent() {
				return false;
			}

			@Override
			public boolean restartRequested(AzureusCore core)
					throws AzureusCoreException {
				return false;
			}

			@Override
			public void started(AzureusCore core) {
				latch.countDown();
			}

			@Override
			public void stopped(AzureusCore core) {
			}

			@Override
			public void stopping(AzureusCore core) {
			}

			@Override
			public boolean stopRequested(AzureusCore core)
					throws AzureusCoreException {
				return false;
			}

			@Override
			public boolean syncInvokeRequired() {
				return false;
			}
		});

		//Put that listener to use if the azureus core is not started yet.
		if (!AzureusStarter.getAzureusCore().isStarted()) {
			//this latch will go to zero when the azureus core announces it's started
			System.out.println("FrostWireUtils.waitForAzureusCoreToStart() - Waiting for azureus core...");
			latch.await();
			System.out.println("FrostWireUtils.waitForAzureusCoreToStart() - Azureus core has started, let's do this.");
		}
		
	}
	
	/**
	 * Returns a BitTorrent Meta Info object from the URL of a torrent file.
	 * If @saveLocation is a valid File, the torrent will be saved there.
	 * 
	 * @param torrentURL
	 * @param saveLocation - Path to a file where you want to save the .torrent locally.
	 * If the file already exists, it's overwritten.
	 * @return
	 * @throws IOException 
	 */
	public final static BTMetaInfo downloadTorrentFile(
	        String torrentURL,
	        File saveLocation) throws IOException {

	    BTMetaInfo result = null;
	    byte[] contents = CoreFrostWireUtils.downloadHttpFile(torrentURL);

	    try {
	        result = BTMetaInfo.readFromBytes(contents);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }

	    // save the torrent locally if you have to
	    if (saveLocation != null && 
	        contents != null &&
	        contents.length > 0) {
	        
	    	if (saveLocation.exists()) {
	    		saveLocation.delete();
	    	}
	    	
	        //Create all the route necessary to save the .torrent file if it does not exit.
            saveLocation.getParentFile().mkdirs();
            saveLocation.createNewFile();
            saveLocation.setWritable(true);
	        
	        FileOutputStream fos = new FileOutputStream(saveLocation,false);
	        fos.write(contents);
	        fos.flush();
	        fos.close();
	    }
	    
	    return result;
	} //downloadTorrentFile
	
	/**
	 * Returns a BitTorrent Meta info object from the URL of a torrent file.
	 * @param torrentURL
	 * @return
	 */
	public final static BTMetaInfo 
	    downloadTorrentFile(String torrentURL) throws IOException {
	    return CoreFrostWireUtils.downloadTorrentFile(torrentURL, null);
	} //downloadTorrentFile
	

	public final static void main(String[] args) throws Exception {
		AzureusStarter.start();
		AzureusCore CORE = AzureusStarter.getAzureusCore();
		waitForAzureusCoreToStart();
		CORE.getGlobalManager().resumeDownloads();
		downloadTorrentFile("http://dl.frostwire.com/torrents/hostiles/hostiles.txt.2.zip.torrent", 
				new File(SharingUtils.APPLICATION_SPECIAL_SHARE,"hostiles.txt.2.zip.torrent"));
		DownloadManager dManager = startTorrentDownload(SharingUtils.APPLICATION_SPECIAL_SHARE+File.separator+"hostiles.txt.2.zip.torrent", 
				CommonUtils.getUserSettingsDir().getAbsolutePath(), new DownloadManagerListener() {
	
					@Override
					public void completionChanged(DownloadManager manager,
							boolean completed) {
						System.out.println("completionChanged: completed:" + completed);
					}
	
					@Override
					public void downloadComplete(DownloadManager manager) {
						// TODO Auto-generated method stub
						System.out.println("downloadComplete()!");
					}
	
					@Override
					public void filePriorityChanged(DownloadManager download,
							DiskManagerFileInfo file) {
						// TODO Auto-generated method stub
						System.out.println("filePriorityChanged() " + file.getIndex());
					}
	
					@Override
					public void positionChanged(DownloadManager download,
							int oldPosition, int newPosition) {
						System.out.println("positionChanged() oldPos:"+oldPosition+" newPos:"+newPosition);
					}
	
					@Override
					public void stateChanged(DownloadManager manager, int state) {
						System.out.println("State Changed ("+state+")");
						// TODO Auto-generated method stub
						if (state == DownloadManager.STATE_SEEDING) {
							System.out.println("SEEDING");
						} else if (state == DownloadManager.STATE_STOPPING)
							System.out.println("STOPPING");
						else if (state == DownloadManager.STATE_STOPPED) {
							System.out.println("STOPPED");
							System.out.println("Trying to restart...");
							manager.startDownload();
						} else if (state == DownloadManager.STATE_ERROR) {
							System.out.println("ERROR");
							System.out.println(manager.getErrorDetails());
						} else if (state == DownloadManager.STATE_READY && manager.getDiskManager().getPercentDone() < 1000) {
							System.out.println("Ready but getPercentage() < 1000 ["+manager.getDiskManager().getPercentDone()+"]");
							manager.startDownload();
						} else if (state == DownloadManager.STATE_READY) {
							System.out.println("Ready - getPercentage()=" + manager.getDiskManager().getPercentDone());
							System.out.println(manager.getSaveLocation().getAbsolutePath());
							manager.startDownload();
						}
					}
			
		});
		
		dManager.addDiskListener(new DownloadManagerDiskListener() {
	
			@Override
			public void diskManagerAdded(DiskManager dm) {
				System.out.print("Added a diskManager: " + dm.getNbPieces() + " pieces -> ");
				printDiskManagerPieces(dm);
			}
	
			@Override
			public void diskManagerRemoved(DiskManager dm) {
				System.out.print("Removed a diskManager: " + dm.getNbPieces() + " pieces -> ");
				printDiskManagerPieces(dm);
			}});
	}
	
	public static Injector getInjector() {
		if (INJECTOR == null) {
			INJECTOR = Guice.createInjector(new FrostWireCoreModule());
		}
		
		return INJECTOR;
	}

	public static HttpExecutor getHTTPExecutor() {
		return getInjector().getInstance(HttpExecutor.class);
	}
}
