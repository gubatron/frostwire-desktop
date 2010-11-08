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
		
		if (!support.isDataFlavorSupported(DesktopListTransferable.LOCAL_FILE_ARRAY)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean importData(TransferSupport support) {
		
		Device device = AndroidMediator.instance().getDeviceBar().getSelectedDevice();
		if (device == null) {
			return false;
		}
		
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
		
		AndroidMediator.addAcitivy(new CopyToDeviceActivity(device, localFiles));
		
		return true;
	}
}
