package com.frostwire.gnutella.gui.android;

import java.awt.datatransfer.Transferable;
import java.io.File;

import javax.swing.TransferHandler;

public class DeviceListTransferHandler extends TransferHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2277397432089499656L;

	@Override
	public boolean canImport(TransferSupport support) {
		
		if (!support.isDataFlavorSupported(DesktopListTransferable.LOCAL_FILE_ARRAY)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean importData(TransferSupport support) {
		
		if (!canImport(support)) {
	        return false;
	    }

		Transferable transferable = support.getTransferable();
		LocalFile[] localFiles = null;
		try {
			localFiles = (LocalFile[]) transferable.getTransferData(DesktopListTransferable.LOCAL_FILE_ARRAY);
		} catch (Exception e) {
			System.out.print(e);
			return false;
		}
		
		for (LocalFile localFile : localFiles) {
			File file = localFile.getFile();
			AndroidMediator.SELECTED_DEVICE.upload(getFileType(file), file);
		}
		
		return true;
	}
	
	public int getFileType(File file) {
		String name = file.getName();
		
		if (name.endsWith(".mp3")) {
			return DeviceConstants.FILE_TYPE_AUDIO;
		} else {
			return DeviceConstants.FILE_TYPE_DOCUMENTS;
		}
	}
}
