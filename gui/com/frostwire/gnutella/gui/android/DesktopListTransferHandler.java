package com.frostwire.gnutella.gui.android;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
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
		return new StringSelection("a");
	}
}
