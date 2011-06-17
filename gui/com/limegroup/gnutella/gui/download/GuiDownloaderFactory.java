package com.limegroup.gnutella.gui.download;

import java.io.File;

import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.SaveLocationException;
import com.limegroup.gnutella.URN;

/**
 * Defines the callback requirements for creating a download using 
 * {@link com.limegroup.gnutella.gui.download.DownloaderUtils}.
 */
public interface GuiDownloaderFactory {

	/**
	 * Returns the proposed save directory for the download
	 * @return
	 */
	File getSaveFile();
	/**
	 * Sets the save file used in {@link #createDownloader(boolean)}.
	 * @param saveFile
	 */
	void setSaveFile(File saveFile);
	/**
	 * Returns the final filesize of the download if available, otherwise 0.
	 * @return
	 */
	long getFileSize();
	/**
	 * Returns the urn associated with the file that should be downloaded or
	 * <code>null</code>.
	 * @return
	 */
	URN getURN();	
}

