package com.frostwire.gnutella.gui.android;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

public class DeviceListTransferHandler extends TransferHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2277397432089499656L;
	
	@Override
	public boolean canImport(TransferSupport support) {
	    
	    Device device = AndroidMediator.instance().getDeviceBar().getSelectedDevice();
        if (device == null) {
            return false;
        }
        
	    if (support.isDataFlavorSupported(DesktopListTransferable.LOCAL_FILE_ARRAY)) {
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

		Transferable transferable = support.getTransferable();
		LocalFile[] localFiles = null;
		try {
			localFiles = (LocalFile[]) transferable.getTransferData(DesktopListTransferable.LOCAL_FILE_ARRAY);
		} catch (Exception e) {
			return false;
		}
		
		int fileType = AndroidMediator.instance().getDeviceExplorer().getSelectedFileType();
		AndroidMediator.addTask(new CopyToDeviceTask(device, localFiles, fileType));
		
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
        
        ArrayList<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
        
        for (Object value : selectedValues) {
            fileDescriptors.add((FileDescriptor) value);
        }
        
        return new DeviceListTransferable(fileDescriptors);
    }
}
