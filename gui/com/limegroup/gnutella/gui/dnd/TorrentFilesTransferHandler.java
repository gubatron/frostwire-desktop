package com.limegroup.gnutella.gui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;

import com.limegroup.gnutella.gui.GUIMediator;

/**
 * FileTransferHandler that imports drags of local torrent files into frostwire
 * by starting downloads for them if all files of the drag are torrent files.
 */
public class TorrentFilesTransferHandler extends LimeTransferHandler {

	/**
     * 
     */
    private static final long serialVersionUID = 5478003116391589602L;

    @Override
	public boolean canImport(JComponent c, DataFlavor[] flavors, DropInfo ddi) {
		return canImport(c, flavors);
	}
	
	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		if (DNDUtils.contains(transferFlavors, DataFlavor.javaFileListFlavor)
				|| DNDUtils.contains(transferFlavors, FileTransferable.URIFlavor)) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean importData(JComponent c, Transferable t, DropInfo ddi) {
		return importData(c, t);
	}
	
	@Override
	public boolean importData(JComponent comp, Transferable t) {
		if (!canImport(comp, t.getTransferDataFlavors()))
			return false;

		try {
			File[] files = DNDUtils.getFiles(t);
			if (areAllTorrentFiles(files)) {
			    if (files.length == 1) {
			        GUIMediator.instance().openTorrentFile(files[0], true);
			    } else {
			        for (File file : files) {
	                    GUIMediator.instance().openTorrentFile(file, false);
	                }
			    }
			}
		} catch (UnsupportedFlavorException e) {
		} catch (IOException e) {
		}
		return false;
	}		
	
	// made package private for tests
	boolean areAllTorrentFiles(File[] files) {
		for (File file : files) {
			if (!file.isFile() ||!file.getName().toLowerCase().endsWith(".torrent")) {
				return false;
			}
		}
		return true;
	}
}
