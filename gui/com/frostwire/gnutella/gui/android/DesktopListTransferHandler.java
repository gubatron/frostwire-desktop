package com.frostwire.gnutella.gui.android;

import java.awt.datatransfer.Transferable;
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
