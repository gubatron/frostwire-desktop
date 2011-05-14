package com.frostwire.gui.download.bittorrent;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.limewire.i18n.I18nMarker;
import org.limewire.util.OSUtils;

import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.IncompleteFileDesc;
import com.limegroup.gnutella.SaveLocationException;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.browser.MagnetOptions;
import com.limegroup.gnutella.gui.FileChooserHandler;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.MessageService;
import com.limegroup.gnutella.settings.QuestionsHandler;

/**
 * Static helper class that kicks of downloads handling all the necessary
 * consistency check and showing appropriate error/warning dialogs.
 */
public class BTDownloaderUtils {

	/**
	 * Tries to create a downloader for a factory performing the following
	 * consistency checks beforehand:
	 * <ul>
	 * <li>{@link #isAlreadyDownloading(GuiDownloaderFactory)}
	 * <li>{@link #isSaveLocationTaken(GuiDownloaderFactory)}
	 * <li>if the proposed save location is not taken
	 * {@link #continueWithOrWithoutHashConflict(GuiDownloaderFactory)} is
	 * performed
	 * </ul>
	 * 
	 * @param factory
	 * @return <code>null</code> if there is another download for the same
	 *         hash, or incomplete file, or the user cancelled the download at
	 *         some point.
	 */
	public static BTDownloader createDownloader(BTDownloaderFactory factory) {

		// check for already downloading conflicts
		if (isAlreadyDownloading(factory)) {
			return null;
		}

		// check for file name conflicts
		if (!isSaveLocationTaken(factory)) {

			return null;
		}

		// try to start download
		return createDownloader(factory, false);
	}
	

	/**
	 * Tries to create a downloader from a factory.
	 * <p>
	 * If the {@link GuiDownloaderFactory#createDownloader(boolean)} throws an
	 * exception, {@link BTDownloaderDialog#handle(GuiDownloaderFactory, 
	 * SaveLocationException)} is called to handle it.
	 * <p>
	 * If the process was successful, the final file is shared individually if
	 * it's not in a shared directory.
	 * 
	 * @param factory
	 * @param overwrite
	 * @return <code>null</null> if the user cancelled at some point
	 */
	public static BTDownloader createDownloader(BTDownloaderFactory factory,
											  boolean overwrite) {
		try {
			return factory.createDownloader(overwrite);
		} catch (SaveLocationException sle) {
			return BTDownloaderDialog.handle(factory, sle);
		} catch (TOTorrentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
	}

	/**
	 * Checks if there is a conflicting download already running with the same
	 * hash or incomplete file name, shows a notification dialog and returns
	 * true.
	 * 
	 * @param factory
	 * @return
	 */
	public static boolean isAlreadyDownloading(BTDownloaderFactory factory) {
//		if (GuiCoreMediator.getDownloadManager().conflicts(factory.getURN(),
//				(int)factory.getFileSize(), factory.getSaveFile())) {
//			showIsAlreadyDownloadingWarning(factory);
//			return true;
//		}
		return false;
	}
	
	public static void showIsAlreadyDownloadingWarning(BTDownloaderFactory factory) {
		GUIMediator.showError(
				I18n.tr("You are already downloading this file to \"{0}\".", factory.getSaveFile()),
				QuestionsHandler.ALREADY_DOWNLOADING);
	}

	/**
	 * Returns a non-incomplete FileDesc for the urn or null.
	 * 
	 * @param urn
	 * @return
	 */
	public static FileDesc getFromLibrary(URN urn) {
		if (urn == null) {
			return null;
		}
		FileDesc desc = GuiCoreMediator.getFileManager().getFileDescForUrn(urn);
		return (desc instanceof IncompleteFileDesc) ? null : desc;
	}

	private static boolean showHashConflict(FileDesc desc) {
		String message = MessageFormat.format(I18n
				.tr("You already have the exact same file at {0}"),
				new Object[] { desc.getFile() });
		String question = I18n
				.tr("Do you want to continue downloading anyway?");
		String continueLabel = I18n
				.tr("Continue");

		String[] content = new String[] { message, question };

		JOptionPane pane = new JOptionPane(
				content,
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_OPTION,
				null,
				new String[] {
						continueLabel,
						I18n.tr("Cancel") }
				);
		Dialog dialog = pane.createDialog(MessageService.getParentComponent(),
							I18n.tr("Same File Already In Library"));
		dialog.setVisible(true);
		return continueLabel.equals(pane.getValue());
	}

	/**
	 * Shows a filechooser for selecting a save location for a downloader.
	 * 
	 * @param factory
	 * @param c
	 * @return
	 */
	public static File showFileChooser(BTDownloaderFactory factory, Component c) {
		return FileChooserHandler.getSaveAsFile(c, I18nMarker.marktr("Save Download As"),
				factory.getSaveFile());
	}

	/**
	 * Checks if the final save location already exists or is taken by another
	 * download.
	 * 
	 * @param factory
	 * @return
	 */
	private static boolean isSaveLocationTaken(BTDownloaderFactory factory) {
		return factory.getSaveFile().exists();
	}

}
