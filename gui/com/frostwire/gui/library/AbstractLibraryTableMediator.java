package com.frostwire.gui.library;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import com.frostwire.alexandria.Library;
import com.frostwire.alexandria.Playlist;
import com.frostwire.gui.bittorrent.SendFileProgressDialog;
import com.frostwire.gui.player.AudioPlayer;
import com.frostwire.gui.player.AudioSource;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;
import com.limegroup.gnutella.gui.tables.DataLineModel;
import com.limegroup.gnutella.gui.themes.SkinMenu;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;

abstract class AbstractLibraryTableMediator<T extends DataLineModel<E, I>, E extends AbstractLibraryTableDataLine<I>, I> extends AbstractTableMediator<T, E, I> {

    private MediaType mediaType;

    protected Action SEND_TO_FRIEND_ACTION;

    private int needToScrollTo;
    
    private AdjustmentListener adjustmentListener;

    protected AbstractLibraryTableMediator(String id) {
        super(id);
        GUIMediator.addRefreshListener(this);
        mediaType = MediaType.getAnyTypeMediaType();
        needToScrollTo = -1;
    }

    public List<AbstractLibraryTableDataLine<I>> getSelectedLines() {
        int[] selected = TABLE.getSelectedRows();
        List<AbstractLibraryTableDataLine<I>> lines = new ArrayList<AbstractLibraryTableDataLine<I>>(selected.length);
        for (int i = 0; i < selected.length; i++) {
            lines.add(DATA_MODEL.get(selected[i]));
        }
        return lines;
    }
    
    public I getItemAt(int row) {
    	try {
    		return DATA_MODEL.get(row).getInitializeObject();
    	} catch (Exception e) {
    		return null;
    	}
    }
    
    /**
     * This method selects the given item and ensures that it's visible (scrolls to it)
     * @param item
     * @return
     */
    public boolean setItemSelected(I item) {
        int i = DATA_MODEL.getRow(item);

        if (i != -1) {
            TABLE.setSelectedRow(i);
            TABLE.ensureSelectionVisible();
            return true;
        }
        return false;
    }
    
    /**
     * Convenience method to select an item at the given row.
     * 
     * @param row
     * @return
     */
    public boolean selectItemAt(int row) {
    	return setItemSelected(getItemAt(row));
    }

    @Override
    protected JComponent getScrolledTablePane() {
        JComponent comp = super.getScrolledTablePane();

        if (adjustmentListener == null) {
            adjustmentListener = new AdjustmentListener() {
                public void adjustmentValueChanged(AdjustmentEvent e) {
                    adjustmentListener_adjustmentValueChanged(e);
                }
            };
            SCROLL_PANE.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);
        }

        return comp;
    }

    public abstract List<AudioSource> getFileView();

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    protected void buildListeners() {
        super.buildListeners();
        SEND_TO_FRIEND_ACTION = new SendToFriendAction();
    }

    protected SkinMenu createAddToPlaylistSubMenu() {
        SkinMenu menu = new SkinMenu(I18n.tr("Add to playlist"));

        menu.add(new SkinMenuItem(new CreateNewPlaylistAction()));

        Library library = LibraryMediator.getLibrary();
        List<Playlist> playlists = library.getPlaylists();
        Playlist currentPlaylist = LibraryMediator.instance().getSelectedPlaylist();

        if (playlists.size() > 0) {
            menu.addSeparator();

            for (Playlist playlist : library.getPlaylists()) {

                if (currentPlaylist != null && currentPlaylist.equals(playlist)) {
                    continue;
                }

                menu.add(new SkinMenuItem(new AddToPlaylistAction(playlist)));
            }
        }

        return menu;
    }
    
    private void adjustmentListener_adjustmentValueChanged(AdjustmentEvent e) {
        try {
            int value = needToScrollTo;
            if (value >= 0) {
                if (SCROLL_PANE.getVerticalScrollBar().getMaximum() >= value) {
                    if (value >= 0) {
                        SCROLL_PANE.getVerticalScrollBar().setValue(value);
                        Toolkit.getDefaultToolkit().sync();
                    }
                    needToScrollTo = -1;
                }
            }
        } catch (Exception ex) {
            needToScrollTo = -1;
        }
    }

    private class CreateNewPlaylistAction extends AbstractAction {

        private static final long serialVersionUID = 3460908036485828909L;

        public CreateNewPlaylistAction() {
            super(I18n.tr("Create New Playlist"));
            putValue(Action.LONG_DESCRIPTION, I18n.tr("Create and add to a new playlist"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            LibraryUtils.createNewPlaylist(getSelectedLines());
        }
    }

    private final class AddToPlaylistAction extends AbstractAction {

        private static final long serialVersionUID = 4658698262279334616L;

        private Playlist playlist;

        public AddToPlaylistAction(Playlist playlist) {
            super(playlist.getName());
            putValue(Action.LONG_DESCRIPTION, I18n.tr("Add to playlist ") + "\"" + playlist.getName() + "\"");
            this.playlist = playlist;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            LibraryUtils.asyncAddToPlaylist(playlist, getSelectedLines());
        }
    }

    static class SendToFriendAction extends AbstractAction {

        private static final long serialVersionUID = 1329472129818371471L;

        public SendToFriendAction() {
            super(I18n.tr("Send to friend"));
            putValue(Action.LONG_DESCRIPTION, I18n.tr("Send to friend"));
            putValue(Action.SMALL_ICON, GUIMediator.getThemeImage("share"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_SEND");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
                File file = LibraryMediator.instance().getSelectedFile();
                
                if (file == null) {
                	return;
                }
                
                int result = JOptionPane.showConfirmDialog(GUIMediator.getAppFrame(), I18n.tr("Do you want to send this file to a friend?") + "\n\n\"" + file.getName() + "\"", I18n.tr("Send files with FrostWire"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    new SendFileProgressDialog(GUIMediator.getAppFrame(), file).setVisible(true);
                    GUIMediator.instance().setWindow(GUIMediator.Tabs.SEARCH);
                }
        }
    }
    
    static class ExploreAction extends AbstractAction {

		private static final long serialVersionUID = 8992145937511990033L;

		public ExploreAction() {
            putValue(Action.NAME, I18n.tr("Explore"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Explore"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Open Folder Containing the File"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_EXPLORE");
        }

		@Override
		public void actionPerformed(ActionEvent e) {
			File toExplore = LibraryMediator.instance().getSelectedFile();
			
			if (toExplore != null) {
				GUIMediator.launchExplorer(toExplore);
			} else {
				System.out.println("LibraryMediator.ExploreAction.actionPerformed() - Had nothing to launch.");
			}
		}
    }

    void scrollTo(int value) {
        needToScrollTo = value;
    }

    int getScrollbarValue() {
        if (SCROLL_PANE != null && SCROLL_PANE.getVerticalScrollBar() != null) {
            return SCROLL_PANE.getVerticalScrollBar().getValue();
        }
        return 0;
    }

    public void playCurrentSelection() {
        E line = DATA_MODEL.get(TABLE.getSelectedRow());
        if (line == null) {
            return;
        }

        try {
            AudioSource audioSource = createAudioSource(line);
            if (audioSource != null) {
                AudioPlayer.instance().asyncLoadSong(audioSource, true, false, null, getFileView());
            }
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }
    
    protected abstract AudioSource createAudioSource(E line);
}
