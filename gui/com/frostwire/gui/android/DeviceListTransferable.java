package com.frostwire.gui.android;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

public class DeviceListTransferable implements Transferable {
    
    public static final DataFlavor FILE_DESCRIPTOR_ARRAY = new DataFlavor(FileDescriptor[].class, "FileDescriptor Array");
    
    private List<FileDescriptor> _fileDescriptors;
    
    public DeviceListTransferable(List<FileDescriptor> fileDescriptors) {
        _fileDescriptors = fileDescriptors;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { FILE_DESCRIPTOR_ARRAY };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(FILE_DESCRIPTOR_ARRAY);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return _fileDescriptors.toArray(new FileDescriptor[0]);
    }
}
