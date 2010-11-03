package com.frostwire.gnutella.gui.android;

import java.awt.datatransfer.Transferable;

import javax.swing.TransferHandler;

public class DeviceListTransferHandler extends TransferHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2277397432089499656L;

	@Override
	public boolean canImport(TransferSupport support) {
		return true;
	}
	
	@Override
	public boolean importData(TransferSupport support) {

		Transferable transferable = support.getTransferable();
		
		return true;
	}
}
