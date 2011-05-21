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
import org.limewire.util.Version;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreComponent;
import com.aelitis.azureus.core.AzureusCoreException;
import com.aelitis.azureus.core.AzureusCoreLifecycleListener;
import com.frostwire.bittorrent.AzureusStarter;
import com.frostwire.guice.FrostWireCoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.limegroup.gnutella.http.HttpExecutor;
import com.limegroup.gnutella.library.SharingUtils;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.util.FrostWireUtils;

public class CoreFrostWireUtils {
	
	private static Injector INJECTOR;
	
	public final static File getPreferencesFolder() {
	    return FrostWireUtils.getRequestedUserSettingsLocation();
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
			return new HttpFetcher(new URI(url)).fetch();
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
	
	
	public static Injector getInjector() {
		if (INJECTOR == null) {
			INJECTOR = Guice.createInjector(new FrostWireCoreModule());
		}
		
		return INJECTOR;
	}

	public static HttpExecutor getHTTPExecutor() {
		return getInjector().getInstance(HttpExecutor.class);
	}
	
	/** 
	 * Quick and dirty java version comparator 
	 * 
	 * If you're in java 1.6.xxx and you just ass "1.6" and it should always return true.
	 * 
	 * */
	public static boolean isJavaMinorVersionEqualOrGreaterThan(String version) {
		if (version == null)
			return false;
		
		Version asking = null;
		Version javaVersion = null;
			
		try {
			asking = new Version(version);
			javaVersion = new Version(System.getProperty("java.version"));
		} catch (Exception e) {
			return false;
		}
		
		return javaVersion.compareMajorMinorTo(asking) >= 0;
	}
	
	public final static boolean canShareTorrentMetaFiles() {
		if (!SharingSettings.DEFAULT_DOT_TORRENTS_DIR.exists()) {
			SharingSettings.DEFAULT_DOT_TORRENTS_DIR.mkdir();
		}

		return SharingSettings.SHARE_TORRENT_META_FILES.getValue()
				&& SharingSettings.DEFAULT_DOT_TORRENTS_DIR.exists()
				&& SharingSettings.DEFAULT_DOT_TORRENTS_DIR.isDirectory()
				&& SharingSettings.DEFAULT_DOT_TORRENTS_DIR.canWrite();
	}

	public static boolean isInternetReachable() {
		AzureusCore azureusCore = AzureusStarter.getAzureusCore();
		
		if (azureusCore != null) {
			int rate = azureusCore.getGlobalManager().getStats().getDataReceiveRate() + azureusCore.getGlobalManager().getStats().getDataSendRate();
			return (rate > 0);
		}
		
        return false;
    }
}
