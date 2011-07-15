package com.limegroup.gnutella.gui.menu;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.limewire.i18n.I18nMarker;
import org.limewire.util.OSUtils;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.iTunesMediator;
import com.limegroup.gnutella.gui.actions.AbstractAction;

/**
 * Contains all of the menu items for the tools menu.
 */
final class ToolsMenu extends AbstractMenu {
    
    /**
	 * Creates a new <tt>ToolsMenu</tt>, using the <tt>key</tt> 
	 * argument for setting the locale-specific title and 
	 * accessibility text.
	 *
	 * @param key the key for locale-specific string resources unique
	 *            to the menu
	 */
	ToolsMenu() {
	    super(I18n.tr("&Tools"));
	    
	    if (OSUtils.isMacOSX() || OSUtils.isWindows()) {
	    	addMenuItem(new RebuildiTunesPlaylist());
	    }
	    
        addMenuItem(new ShowOptionsAction());
    }
	
	private static class RebuildiTunesPlaylist extends AbstractAction {

		private static final long serialVersionUID = 8348355619323878579L;

		public RebuildiTunesPlaylist() {
			super(I18n.tr("Rebuild iTunes \"FrostWire\" Playlist"));
			putValue(LONG_DESCRIPTION, I18nMarker.marktr("Deletes and re-builds the \"FrostWire\" playlist on iTunes with all the audio files found on your Torrent Data Folder."));
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			int result = JOptionPane.showConfirmDialog(GUIMediator.getAppFrame(), 
					I18n.tr("This will remove your \"FrostWire\" playlist in iTunes and replace\n" +
							"it with one containing all the iTunes compatible files in your \n" +
							"Frostwire \"Torrent Data Folder\"\n\n" +
							"Please note that it will add the files to the iTunes library as well\n" +
							"and this could result in duplicate files on your iTunes library\n\n" +
							"Are you sure you want to continue?"), 
							I18n.tr("Warning"), JOptionPane.WARNING_MESSAGE, JOptionPane.YES_OPTION | JOptionPane.CANCEL_OPTION);
			
			if (result==JOptionPane.YES_OPTION) {
				iTunesMediator.instance().resetFrostWirePlaylist();
			}
		}
	}
	
	private static class ShowOptionsAction extends AbstractAction {
        private static final long serialVersionUID = 6187597973189408647L;

        public ShowOptionsAction() {
	        super(I18n.tr("&Options"));
	        putValue(LONG_DESCRIPTION, I18nMarker.marktr("Display the Options Screen"));
        }
	    
	    public void actionPerformed(ActionEvent e) {
	        GUIMediator.instance().setOptionsVisible(true);
	    }
	}
}