package com.limegroup.gnutella.gui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import com.frostwire.gui.bittorrent.SendFileProgressDialog;
import com.frostwire.uxstats.UXAction;
import com.frostwire.uxstats.UXStats;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

/**
 * Handles local files being dropped on limewire by asking the user if
 * s/he wants to share them.
 */
public class SendFileTransferHandler extends LimeTransferHandler {

	private static final long serialVersionUID = 6541019610960958928L;

	@Override
	public boolean canImport(JComponent c, DataFlavor[] flavors, DropInfo ddi) {
		return canImport(c, flavors);
	}
	
	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		return DNDUtils.containsFileFlavors(transferFlavors);
	}
	
	@Override
	public boolean importData(JComponent c, Transferable t, DropInfo ddi) {
		return importData(c, t);
	}
	
	@Override
	public boolean importData(JComponent comp, Transferable t) {
		if (!canImport(comp, t.getTransferDataFlavors()))
			return false;
		
		try {
			File[] files = DNDUtils.getFiles(t);
			
			//We will only send either 1 folder, or 1 file.
			if (files.length == 1) {
				return handleFiles(files);
			}
		} catch (UnsupportedFlavorException e) {
		} catch (IOException e) {
		}
		return false;
	}

	/**
	 * Returns true if files were shared
	 * @param files
	 * @return
	 */
	public static boolean handleFiles(final File[] files) {

		String fileFolder = files[0].isFile() ? I18n.tr("file"):I18n.tr("folder");
		int result = JOptionPane.showConfirmDialog(GUIMediator.getAppFrame(), I18n.tr("Do you want to send this {0} to a friend?",fileFolder)+"\n\n\""+files[0].getName() + "\"",I18n.tr("Send files with FrostWire"),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
		
		if (result == JOptionPane.YES_OPTION) {
			new SendFileProgressDialog(GUIMediator.getAppFrame(), files[0]).setVisible(true);
			GUIMediator.instance().setWindow(GUIMediator.Tabs.SEARCH);
			UXStats.instance().log(UXAction.SHARING_TORRENT_CREATED_WITH_SEND_TO_FRIEND_FROM_DND);
			return true;
		}
		
		return false;
	}
	
	
	
	
	
}
