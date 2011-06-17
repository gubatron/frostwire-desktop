package com.frostwire.gui.android;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

public class DesktopListTransferable implements Transferable {
	
	public static final DataFlavor LOCAL_FILE_ARRAY = new DataFlavor(LocalFile[].class, "LocalFile Array");
	
	private List<LocalFile> _localFiles;
	
	public DesktopListTransferable(List<LocalFile> localFiles) {
		_localFiles = localFiles;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { LOCAL_FILE_ARRAY };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(LOCAL_FILE_ARRAY);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		return _localFiles.toArray(new LocalFile[0]);
	}
}
