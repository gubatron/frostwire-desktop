package com.frostwire.gui.library;

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
import com.frostwire.gui.player.AudioSource;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;
import com.limegroup.gnutella.gui.tables.DataLineModel;
import com.limegroup.gnutella.gui.themes.SkinMenu;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;

abstract class AbstractLibraryTableMediator<T extends DataLineModel<E, I>, E extends AbstractLibraryTableDataLine<I>, I> extends AbstractTableMediator<T, E, I> {

    private MediaType mediaType;

    protected Action SEND_TO_FRIEND_ACTION;

    private int needToScrollTo;

    protected static boolean dragging;

    protected AbstractLibraryTableMediator(String id) {
        super(id);
        GUIMediator.addRefreshListener(this);
        mediaType = MediaType.getAnyTypeMediaType();
    }

    public List<AbstractLibraryTableDataLine<I>> getSelectedLines() {
        int[] selected = TABLE.getSelectedRows();
        List<AbstractLibraryTableDataLine<I>> lines = new ArrayList<AbstractLibraryTableDataLine<I>>(selected.length);
        for (int i = 0; i < selected.length; i++)
            lines.add(DATA_MODEL.get(selected[i]));
        return lines;
    }

    @Override
    protected JComponent getScrolledTablePane() {
        JComponent comp = super.getScrolledTablePane();
        SCROLL_PANE.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (needToScrollTo > 0) {
                    scrollTo(needToScrollTo);
                }
            }
        });
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

    private final class SendToFriendAction extends AbstractAction {

        private static final long serialVersionUID = 1329472129818371471L;

        public SendToFriendAction() {
            super(I18n.tr("Send to friend"));
            putValue(Action.LONG_DESCRIPTION, I18n.tr("Send to friend"));
            putValue(Action.SMALL_ICON, GUIMediator.getThemeImage("share"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<AbstractLibraryTableDataLine<I>> lines = getSelectedLines();
            if (lines.size() == 1) {
                File file = lines.get(0).getFile();
                String fileFolder = file.isFile() ? I18n.tr("file") : I18n.tr("folder");
                int result = JOptionPane.showConfirmDialog(GUIMediator.getAppFrame(), I18n.tr("Do you want to send this {0} to a friend?", fileFolder) + "\n\n\"" + file.getName() + "\"", I18n.tr("Send files with FrostWire"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    new SendFileProgressDialog(GUIMediator.getAppFrame(), file).setVisible(true);
                    GUIMediator.instance().setWindow(GUIMediator.Tabs.SEARCH);
                }
            }
        }
    }

    void scrollTo(int value) {
        if (SCROLL_PANE != null && SCROLL_PANE.getVerticalScrollBar() != null && SCROLL_PANE.getVerticalScrollBar().getMaximum() >= value) {
            try {
                SCROLL_PANE.getVerticalScrollBar().setValue(value);
            } catch (Exception e) {
                //let it be, let it beeee...
            }
            needToScrollTo = -1;
        } else {
            needToScrollTo = value;
        }
    }

    int getScrollbarValue() {
        if (SCROLL_PANE != null && SCROLL_PANE.getVerticalScrollBar() != null) {
            return SCROLL_PANE.getVerticalScrollBar().getValue();
        }
        return 0;
    }
}
