package com.frostwire.bittorrent;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gudy.azureus2.core3.config.impl.ConfigurationManager;
import org.gudy.azureus2.core3.download.DownloadManager;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreComponent;
import com.aelitis.azureus.core.AzureusCoreException;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreLifecycleListener;
import com.limegroup.bittorrent.settings.BittorrentSettings;
import com.limegroup.gnutella.util.LimeWireUtils;

/**
 * Class to initialize the azureus core. Keeps a static reference to the initialized core
 * @author gubatron
 *
 */
public final class AzureusStarter {
	 
	private static final Log LOG = LogFactory.getLog(AzureusStarter.class);
	
	private static AzureusCore AZUREUS_CORE;

	public final static void start() {
		
		azureusInit();
	}
	
	/**
	 * Returns the azureus core singleton
	 * @return
	 */
	public static AzureusCore getAzureusCore() {
		if (AZUREUS_CORE != null && AZUREUS_CORE.isStarted())
			return AZUREUS_CORE;
		else azureusInit();
				
		return AZUREUS_CORE;
	}
	
	/*
	 * Initializes synchronously the azureus core
	 */
	private static void azureusInit() {
		if (AZUREUS_CORE != null && AZUREUS_CORE.isStarted()) {
			LOG.debug("azureusInit(): core already started. skipping.");
			return;
		}
		
		if (!AzureusCoreFactory.isCoreAvailable()) {
			//This does work
			org.gudy.azureus2.core3.util.SystemProperties.APPLICATION_NAME = "azureus";
			org.gudy.azureus2.core3.util.SystemProperties.setUserPath(LimeWireUtils.getRequestedUserSettingsLocation() + File.separator + "azureus" + File.separator);
			
			AZUREUS_CORE = AzureusCoreFactory.create();
			
			//to guarantee a synchronous start
			final CountDownLatch signal = new CountDownLatch(1);
			
			AZUREUS_CORE
					.addLifecycleListener(new AzureusCoreLifecycleListener() {

						@Override	

						public boolean syncInvokeRequired() {
							return false;
						}

						@Override
						public void stopping(AzureusCore core) {
							core.getGlobalManager().pauseDownloads();
						}

						@Override
						public void stopped(AzureusCore core) {
						}

						@Override
						public boolean stopRequested(AzureusCore core)
								throws AzureusCoreException {
							return false;
						}

						@Override
						public void started(AzureusCore core) {
							signal.countDown();
						}

						@Override
						public boolean restartRequested(AzureusCore core)
								throws AzureusCoreException {
							return false;
						}

						@Override
						public boolean requiresPluginInitCompleteBeforeStartedEvent() {
							return false;
						}

						@Override
						public void componentCreated(AzureusCore core,
								AzureusCoreComponent component) {
						}
					});			

			AZUREUS_CORE.start();
			
			AZUREUS_CORE.getGlobalManager().resumeDownloads();
			LOG.debug("azureusInit(): core.start() waiting...");
			try {
				signal.await(); LOG.debug("azureusInit(): core started...");
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	} //azureusInit

	public static float getLastMeasuredUploadBandwidth() {
		if (AZUREUS_CORE == null || !AZUREUS_CORE.isStarted()|| AZUREUS_CORE.getGlobalManager() == null || AZUREUS_CORE.getGlobalManager().getStats() == null)
			return 0;
		
		return (float) (AZUREUS_CORE.getGlobalManager().getStats().getDataSendRate()/1000);
	}
	
	public ConfigurationManager getConfigManager() {
		return ConfigurationManager.getInstance();
	}

	/**
	 * Checks how many allowable active torrents exist.
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static boolean allowNewTorrent() {
		List downloadManagers = AZUREUS_CORE.getGlobalManager().getDownloadManagers();
		
		int size = downloadManagers.size();
		int activeDownloads=0;
		for (int i=0 ; i < size; i++) {
			DownloadManager manager = (DownloadManager) downloadManagers.get(i);
			
			int state = manager.getState();
			if (state == DownloadManager.STATE_WAITING ||
				state == DownloadManager.STATE_INITIALIZING ||
				state == DownloadManager.STATE_INITIALIZED ||
				state == DownloadManager.STATE_ALLOCATING ||
				state == DownloadManager.STATE_CHECKING ||
				state == DownloadManager.STATE_READY ||
				state == DownloadManager.STATE_DOWNLOADING ||
				state == DownloadManager.STATE_STOPPING)
				activeDownloads++;
		}
		
		return activeDownloads < BittorrentSettings.TORRENT_MAX_ACTIVE_DOWNLOADS.getValue();
	}
	
}
