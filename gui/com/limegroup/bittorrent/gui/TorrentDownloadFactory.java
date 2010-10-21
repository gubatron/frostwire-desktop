package com.limegroup.bittorrent.gui;

import java.io.File;
import java.io.IOException;

import org.limewire.util.FileUtils;

import com.limegroup.bittorrent.BTMetaInfo;
import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.SaveLocationException;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.download.GuiDownloaderFactory;

import com.frostwire.bittorrent.BTFilesManagerFactory;


public class TorrentDownloadFactory implements GuiDownloaderFactory {

	private BTMetaInfo info; // removed final
	private boolean useBTFilesManager = false; // FTA: Flag to enable/disable BT file's manager

	public TorrentDownloadFactory(File f) throws IOException {
		byte [] b = FileUtils.readFileFully(f);
		if (b == null)
			throw new IOException();
		if (useBTFilesManager) {
			BTFilesManagerFactory myBTFM = new BTFilesManagerFactory(b,info,f); // BTFileManager
			myBTFM.loadFiles();
			info = myBTFM.info;
		} else {
			info = BTMetaInfo.readFromBytes(b); 
		}
	}
	
	public TorrentDownloadFactory(BTMetaInfo info) {
		this.info = info;
	}
	
	public Downloader createDownloader(boolean overwrite) 
	throws SaveLocationException {
		return GuiCoreMediator.getDownloadServices().downloadTorrent(info, overwrite);
	}

	public long getFileSize() {
		return info.getFileSystem().getTotalSize();
	}

	public File getSaveFile() {
		return info.getFileSystem().getCompleteFile();
	}

	public URN getURN() {
		if (info != null)
		   return info.getURN(); // URN Must have a value but some torrents didn't have this data causing an unhandled exception
		return null;
	}
    
    public BTMetaInfo getBTMetaInfo() {
        return info;
    }

	public void setSaveFile(File saveFile) {
		info.getFileSystem().setCompleteFile(saveFile);
	}

}
