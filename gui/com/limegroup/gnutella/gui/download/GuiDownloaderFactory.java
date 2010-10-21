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
	/**
	 * Tries to create a new downloader object for the given parameters, hiding
	 * which kind of downloader is created, e.g.
	 * {@link com.limegroup.gnutella.downloader.ManagedDownloader}
	 * or {@link com.limegroup.gnutella.downloader.MagnetDownloader}.
	 * @param saveDir can be <code>null</code>
	 * @param fileName the final filename
	 * @param overwrite whether or not to overwrite an existing file at the
	 * given location
	 * @return
	 * @throws SaveLocationException when the file could not be saved there
	 */
	Downloader createDownloader(boolean overwrite) 
		throws SaveLocationException;
	
}

