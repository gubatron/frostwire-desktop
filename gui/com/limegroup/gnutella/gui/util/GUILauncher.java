package com.limegroup.gnutella.gui.util;

import java.awt.Cursor;
import java.io.File;

import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.Downloader.DownloadStatus;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.QuestionsHandler;

import foxtrot.Job;
import foxtrot.Worker;

/**
 * Static utility class that handles launching of downloaders and
 * displaying error messages.
 */
public class GUILauncher {

	/**
	 * Provides a downloader or a file that should be launched.
	 */
	public interface LaunchableProvider {
		/**
		 * Can return if only a file is available
		 */
		Downloader getDownloader();
		/**
		 * Can return null if only a downloader is avaialable 
		 */
		File getFile();
	}
	
	/**
	 * Launches an array of <code>providers</code> delegating the time
	 * consuming construction of {@link Downloader#getDownloadFragment()}
	 * into a background threads.
	 */
	public static void launch(LaunchableProvider[] providers) {
		boolean audioLaunched = false;
		GUIMediator.instance().setFrameCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		for (LaunchableProvider provider : providers) {
			final Downloader dl = provider.getDownloader();
			if (dl == null) {
				File file = provider.getFile();
				if (file != null) {
					audioLaunched = GUIUtils.launchOrEnqueueFile(file, audioLaunched);
				}
			}
			else {
				if (dl.getState() == DownloadStatus.INVALID) {
					GUIMediator.openURL("http://www.frostwire.com/?id=faq#Downloading");
				}
				else {
					File fragment = (File) Worker.post(new Job() {
						public Object run() {
							return dl.getDownloadFragment();
						}
					});
					if (fragment != null && dl.getState() == DownloadStatus.COMPLETE) {
						//System.out.println("****launching and adding to playlist: " + dl.getState()); 
						//FTA: any complete file is auto-added to the playlist following the Guba "always listening music" policy
						audioLaunched = GUIUtils.launchOrEnqueueFile(fragment,false);
						/*
						// Since the file is complete, the fragment = file, we don't need to get the file
						File file = provider.getFile();
						if (file != null) {
						System.out.println("****launching the full file and enqueue!");
						audioLauched = GUIUtils.launchAndEnqueueFile(file, audioLaunched);					
						} else {
						audioLaunched = GUIUtils.launchAndEnqueueFile(fragment,false);
						}
						*/
					}
					else if (fragment != null) {						
						//FTA: it launchs a fragment, so, it doesn't adds the file to the playlist
						audioLaunched = GUIUtils.launchOneTimeFile(fragment);
					}
					else {
						GUIMediator.instance().setFrameCursor(Cursor.getDefaultCursor());
						GUIMediator.showMessage(I18n.tr("There is nothing to preview for file {0}.",dl.getSaveFile().getName()), 
						        QuestionsHandler.NO_PREVIEW_REPORT
								);
						GUIMediator.instance().setFrameCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					}
				}
			}
		}
		GUIMediator.instance().setFrameCursor(Cursor.getDefaultCursor());
	}
	
}
