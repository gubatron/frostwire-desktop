package com.frostwire;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.limewire.util.CommonUtils;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreComponent;
import com.aelitis.azureus.core.AzureusCoreException;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.AzureusCoreLifecycleListener;
import com.limegroup.gnutella.settings.SharingSettings;

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
	private static synchronized void azureusInit() {
	    System.setProperty("azureus.loadplugins", "0"); // disable third party azureus plugins
		try {
			if (AZUREUS_CORE != null && AZUREUS_CORE.isStarted()) {
				LOG.debug("azureusInit(): core already started. skipping.");
				return;
			}
		} catch (Exception ignore) {}
		
		if (!AzureusCoreFactory.isCoreAvailable()) {
			//This does work
			org.gudy.azureus2.core3.util.SystemProperties.APPLICATION_NAME = "azureus";
			
			File azureusUserPath = new File(CommonUtils.getUserSettingsDir() + File.separator + "azureus" + File.separator);
			if (!azureusUserPath.exists()) {
			    azureusUserPath.mkdirs();
			}
			
			org.gudy.azureus2.core3.util.SystemProperties.setUserPath(azureusUserPath.getAbsolutePath());
			
			if (!SharingSettings.TORRENTS_DIR_SETTING.getValue().exists()) {
			    SharingSettings.TORRENTS_DIR_SETTING.getValue().mkdirs();
			}
			
			COConfigurationManager.setParameter("General_sDefaultTorrent_Directory", SharingSettings.TORRENTS_DIR_SETTING.getValue().getAbsolutePath());
			
			try {
				AZUREUS_CORE = AzureusCoreFactory.create();
			} catch (AzureusCoreException coreException) {
				//so we already had one eh...
				if (AZUREUS_CORE == null) {
					AZUREUS_CORE = AzureusCoreFactory.getSingleton();
				}
			}
				
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

			if (!AZUREUS_CORE.isStarted() && !AZUREUS_CORE.isRestarting()) {
				AZUREUS_CORE.start();
			}
			
			AZUREUS_CORE.getGlobalManager().resumeDownloads();
			
			
			LOG.debug("azureusInit(): core.start() waiting...");
			try {
				signal.await(); LOG.debug("azureusInit(): core started...");
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	} //azureusInit
}
