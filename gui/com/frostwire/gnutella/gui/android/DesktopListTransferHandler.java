package com.frostwire.gnutella.gui.android;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

public class DesktopListTransferHandler extends TransferHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7212838569016925166L;
	
	@Override
    public boolean canImport(TransferSupport support) {
        
        if (support.isDataFlavorSupported(DeviceListTransferable.FILE_DESCRIPTOR_ARRAY)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean importData(TransferSupport support) {
        
        if (!canImport(support)) {
            return false;
        }
        
        Device device = AndroidMediator.instance().getDeviceBar().getSelectedDevice();
        if (device == null) {
            return false;
        }
        
        File path = AndroidMediator.instance().getDesktopExplorer().getSelectedFolder();
        if (path == null) {
            path = AndroidMediator.instance().getDesktopExplorer().getRootFolder();
        }
        if (path == null || !path.exists() || !path.isDirectory()) {
            return false;
        }

        Transferable transferable = support.getTransferable();
        FileDescriptor[] fileDescriptors = null;
        try {
            fileDescriptors = (FileDescriptor[]) transferable.getTransferData(DeviceListTransferable.FILE_DESCRIPTOR_ARRAY);
        } catch (Exception e) {
            return false;
        }
        
        AndroidMediator.addActivity(new CopyToDesktopTask(device, path, fileDescriptors));
        
        return true;
    }
	
	@Override
	public int getSourceActions(JComponent c) {
		return COPY;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		
		JList list = (JList) c;
		
		Object[] selectedValues = list.getSelectedValues();
		
		ArrayList<LocalFile> localFiles = new ArrayList<LocalFile>();
		
		for (Object value : selectedValues) {
			localFiles.add((LocalFile) value);
		}
		
		return new DesktopListTransferable(localFiles);
	}
}
