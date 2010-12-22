package com.frostwire.gnutella.gui.android;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import com.limegroup.gnutella.gui.dnd.DNDUtils;

public class DeviceListTransferHandler extends TransferHandler {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2277397432089499656L;
	
	@Override
	public boolean canImport(TransferSupport support) {
        DataFlavor[] flavors = support.getDataFlavors();
        return support.isDataFlavorSupported(DesktopListTransferable.LOCAL_FILE_ARRAY) || DNDUtils.containsFileFlavors(flavors);
    }
	
	@Override
	public boolean importData(TransferSupport support) {
		
	    if (!canImport(support)) {
            return false;
        }
	    
		Device device = AndroidMediator.instance().getDeviceBar().getSelectedDevice();
		if (device == null) {
			
			if (support.getComponent() instanceof DeviceButton) {
				device = ((DeviceButton) support.getComponent()).getDevice();
			} else {
				return false;
			}
		}

		Transferable transferable = support.getTransferable();
		int fileType = AndroidMediator.instance().getDeviceExplorer().getSelectedFileType();
		
		if (DNDUtils.contains(transferable.getTransferDataFlavors(), DesktopListTransferable.LOCAL_FILE_ARRAY)) {
			LocalFile[] localFiles = null;
			try {
				localFiles = (LocalFile[]) transferable.getTransferData(DesktopListTransferable.LOCAL_FILE_ARRAY);
			} catch (Exception e) {
				return false;
			}			
			
			AndroidMediator.addTask(new CopyToDeviceTask(device, localFiles, fileType));
		} else {
			File[] files = null;
			try {
				 files = DNDUtils.getFiles(transferable);
			} catch (Exception e) {
				return false;
			}
			AndroidMediator.addTask(new CopyToDeviceTask(device, files, fileType));
		}
		
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
