package com.limegroup.gnutella.gui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.Action;

import org.limewire.i18n.I18nMarker;

import com.limegroup.gnutella.FileEventListener;
import com.limegroup.gnutella.FileManagerEvent;
import com.limegroup.gnutella.gui.FileChooserHandler;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.MessageService;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;

/**
 * Opens a file chooser dialog centerened on {@link
 * MessageService#getParentComponent()} and adds the selected file to the
 * specially shared files if it is not being shared already.
 */
public class ShareFileSpeciallyAction extends AbstractAction {

	public ShareFileSpeciallyAction() {
		putValue(Action.NAME, I18n.tr
				("Share New File..."));
		putValue(Action.SHORT_DESCRIPTION, "Opens a Dialog and Lets You Choose a File to Share");
	}
	
	public void actionPerformed(ActionEvent e) {
		final List<File> toShare = FileChooserHandler.getMultiInputFile(MessageService.getParentComponent(), 
				I18nMarker.marktr("Share New File..."),
				I18nMarker.marktr("Share"),
				null);
		if (toShare != null) {
		    BackgroundExecutorService.schedule(new Runnable() {
		        public void run() {
		            for(File f : toShare)
		                GuiCoreMediator.getFileManager().addFileAlways(f, new Listener());
                }
            });
        }
	}
	
	private static class Listener implements FileEventListener {
	    public void handleFileEvent(final FileManagerEvent fev) {
	    	GUIMediator.safeInvokeLater(new Runnable() {
	    		public void run() {
	    			if (fev.isAlreadySharedEvent())
	    				GUIMediator.showError(I18n.tr("The file \"{0}\" is already shared.", fev.getFiles()[0]));
	    			else if (!fev.isAddEvent() && !fev.isAddStoreEvent()) // like FailedEvent, but potentially others too.
	    				GUIMediator.showError(I18n.tr("FrostWire was unable to share the file \"{0}\".", fev.getFiles()[0]));
	    		}
	    	});
        }
    }
	            
}
