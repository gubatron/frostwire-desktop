/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.bittorrent;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import com.frostwire.alexandria.Playlist;
import com.frostwire.gui.library.LibraryMediator;
import com.frostwire.gui.library.LibraryUtils;
import com.frostwire.gui.player.MediaSource;
import com.frostwire.gui.player.MediaPlayer;
import com.frostwire.uxstats.UXAction;
import com.frostwire.uxstats.UXStats;
import com.limegroup.gnutella.gui.DialogOption;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.iTunesMediator;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.settings.ApplicationSettings;
import com.limegroup.gnutella.settings.SharingSettings;

/**
 * @author gubatron
 * @author aldenml
 *
 */
final class BTDownloadActions {

    static final ShowDetailsAction SHOW_DETAILS_ACTION = new ShowDetailsAction();
    static final ExploreAction EXPLORE_ACTION = new ExploreAction();
    static final ShowInLibraryAction SHOW_IN_LIBRARY_ACTION = new ShowInLibraryAction();
    static final ResumeAction RESUME_ACTION = new ResumeAction();
    static final PauseAction PAUSE_ACTION = new PauseAction();
    static final ClearInactiveAction CLEAR_INACTIVE_ACTION = new ClearInactiveAction();
    static final RemoveAction REMOVE_ACTION = new RemoveAction(false, false);
    static final RemoveAction REMOVE_YOUTUBE_ACTION = new RemoveYouTubeAction();
    static final RemoveAction REMOVE_TORRENT_ACTION = new RemoveAction(true, false);
    static final RemoveAction REMOVE_TORRENT_AND_DATA_ACTION = new RemoveAction(true, true);
    static final CopyMagnetAction COPY_MAGNET_ACTION = new CopyMagnetAction();
    static final CopyInfoHashAction COPY_HASH_ACTION = new CopyInfoHashAction();
    static final SendBTDownloaderAudioFilesToiTunes SEND_TO_ITUNES_ACTION = new SendBTDownloaderAudioFilesToiTunes();
    static final ToggleSeedsVisibilityAction TOGGLE_SEEDS_VISIBILITY_ACTION = new ToggleSeedsVisibilityAction();
    static final ShareTorrentAction SHARE_TORRENT_ACTION = new ShareTorrentAction();
    static final PlaySingleMediaFileAction PLAY_SINGLE_AUDIO_FILE_ACTION = new PlaySingleMediaFileAction();

    private static class SendBTDownloaderAudioFilesToiTunes extends AbstractAction {

        private static final long serialVersionUID = 8230574519252660781L;

        public SendBTDownloaderAudioFilesToiTunes() {
            putValue(Action.NAME, I18n.tr("Send to iTunes"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Send files to iTunes"));
            //putValue(LimeAction.ICON_NAME, "LIBRARY_LAUNCH")
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BTDownload[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();

            if (downloaders != null && downloaders.length > 0) {
                try {
                    System.out.println("Sending to iTunes " + downloaders[0].getSaveLocation());
                    iTunesMediator.instance().scanForSongs(downloaders[0].getSaveLocation());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }
    
    private static abstract class RefreshingAction extends AbstractAction {

        private static final long serialVersionUID = -937688457597255711L;

        public final void actionPerformed(ActionEvent e) {
            performAction(e);
            BTDownloadMediator.instance().doRefresh();
        }

        protected abstract void performAction(ActionEvent e);

    }

	
    
    private static class ShowDetailsAction extends RefreshingAction {

        /**
         * 
         */
        private static final long serialVersionUID = 6100070262538050091L;

        public ShowDetailsAction() {
            putValue(Action.NAME, I18n.tr("Details"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Show Details"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Show Torrent Details"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_EXPLORE");
        }

        protected void performAction(ActionEvent e) {
            System.out.println("Pending implementation");
        }
    }

    private static class ExploreAction extends RefreshingAction {
        /**
         * 
         */
        private static final long serialVersionUID = -4648558721588938475L;

        public ExploreAction() {
            putValue(Action.NAME, I18n.tr("Explore"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Explore"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Open Folder Containing the File"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_EXPLORE");
        }

        public void performAction(ActionEvent e) {
            BTDownload[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            if (downloaders.length > 0) {
                File toExplore = downloaders[0].getSaveLocation();

                if (toExplore == null) {
                    return;
                }

                GUIMediator.launchExplorer(toExplore);
            }
        }
    }

    private static class ShowInLibraryAction extends RefreshingAction {
        /**
         * 
         */
        private static final long serialVersionUID = -4648558721588938475L;

        public ShowInLibraryAction() {
            putValue(Action.NAME, I18n.tr("Show"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Show"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Shows the contents of this transfer in the Library Tab"));
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_SHOW_IN_LIBRARY");
        }

        public void performAction(ActionEvent e) {
            BTDownload[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            if (downloaders.length > 0) {
                final String toExplore = downloaders[0].getDisplayName();

                if (toExplore == null) {
                    return;
                }

                LibraryMediator.instance().getLibrarySearch().searchFor(toExplore.replace("_", " ").replace("-", " ").replace(".", " "), false);
            }
        }
    }

    private static class ResumeAction extends RefreshingAction {

        /**
         * 
         */
        private static final long serialVersionUID = -4449981369424872994L;

        public ResumeAction() {
            putValue(Action.NAME, I18n.tr("Resume Download"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Resume"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Reattempt Selected Downloads"));
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_FILE_MORE_SOURCES");
        }

        public void performAction(ActionEvent e) {
            boolean oneIsCompleted = false;

            BTDownload[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();

            for (int i = 0; i < downloaders.length; i++) {
                if (downloaders[i].isCompleted()) {
                    oneIsCompleted = true;
                    break;
                }
            }

            boolean allowedToResume = true;
            DialogOption answer = null;
            if (oneIsCompleted && !SharingSettings.SEED_FINISHED_TORRENTS.getValue()) {
                String message1 = (downloaders.length > 1) ? I18n.tr("One of the transfers is complete and resuming will cause it to start seeding") : I18n.tr("This transfer is already complete, resuming it will cause it to start seeding");
                String message2 = I18n.tr("Do you want to enable torrent seeding?");
                answer = GUIMediator.showYesNoMessage(message1 + "\n\n" + message2, DialogOption.YES);
                allowedToResume = answer.equals(DialogOption.YES);

                if (allowedToResume) {
                    SharingSettings.SEED_FINISHED_TORRENTS.setValue(true);
                }
            }

            if (allowedToResume) {
                for (int i = 0; i < downloaders.length; i++) {
                    downloaders[i].resume();
                }
            }
            
            UXStats.instance().log(UXAction.DOWNLOAD_RESUME);
        }
    }

    private static class PauseAction extends RefreshingAction {

        /**
         * 
         */
        private static final long serialVersionUID = 4682149704934484393L;

        public PauseAction() {
            putValue(Action.NAME, I18n.tr("Pause Download"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Pause"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Pause Selected Downloads"));
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_PAUSE");
        }

        public void performAction(ActionEvent e) {
            BTDownload[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            for (int i = 0; i < downloaders.length; i++) {
                downloaders[i].pause();
            }
            UXStats.instance().log(UXAction.DOWNLOAD_PAUSE);
        }
    }

    public static class RemoveAction extends RefreshingAction {

        /**
         * 
         */
        private static final long serialVersionUID = -1742554445891016991L;

        private final boolean _deleteTorrent;
        private final boolean _deleteData;

        public RemoveAction(boolean deleteTorrent, boolean deleteData) {
            if (deleteTorrent && deleteData) {
                putValue(Action.NAME, I18n.tr("Remove Torrent and Data"));
                putValue(LimeAction.SHORT_NAME, I18n.tr("Remove Torrent and Data"));
                putValue(Action.SHORT_DESCRIPTION, I18n.tr("Remove Torrent and Data from selected downloads"));
            } else if (deleteTorrent) {
                putValue(Action.NAME, I18n.tr("Remove Torrent"));
                putValue(LimeAction.SHORT_NAME, I18n.tr("Remove Torrent"));
                putValue(Action.SHORT_DESCRIPTION, I18n.tr("Remove Torrent from selected downloads"));
            } else {
                putValue(Action.NAME, I18n.tr("Remove Download"));
                putValue(LimeAction.SHORT_NAME, I18n.tr("Remove"));
                putValue(Action.SHORT_DESCRIPTION, I18n.tr("Remove Selected Downloads"));
            }
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_KILL");

            _deleteTorrent = deleteTorrent;
            _deleteData = deleteData;
        }

        public void performAction(ActionEvent e) {
            if (_deleteData) {
                int result = JOptionPane.showConfirmDialog(GUIMediator.getAppFrame(), I18n.tr("Are you sure you want to remove the data files from your computer?\n\nYou won't be able to recover the files."), I18n.tr("Are you sure?"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            BTDownload[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            for (int i = 0; i < downloaders.length; i++) {
                downloaders[i].setDeleteTorrentWhenRemove(_deleteTorrent);
                downloaders[i].setDeleteDataWhenRemove(_deleteData);
            }
            BTDownloadMediator.instance().removeSelection();
            UXStats.instance().log(UXAction.DOWNLOAD_REMOVE);
        }
    }

    public static class RemoveYouTubeAction extends RemoveAction {

        private static final long serialVersionUID = 4101890173830827703L;

        public RemoveYouTubeAction() {
            super(true, true);
            putValue(Action.NAME, I18n.tr("Remove Download and Data"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Remove Download and Data"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Remove Download and Data from selected downloads"));
        }
    }
    
    private static class ClearInactiveAction extends RefreshingAction {

        private static final long serialVersionUID = 808308856961429212L;

        public ClearInactiveAction() {
            putValue(Action.NAME, I18n.tr("Clear Inactive"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Clear Inactive"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Clear Inactive (completed) transfers from the Transfers list."));
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_CLEAR_INACTIVE");
        }
        
        @Override
        protected void performAction(ActionEvent e) {
            BTDownloadMediator.instance().removeCompleted();
        }

    }

    private static class CopyMagnetAction extends RefreshingAction {

        /**
         * 
         */
        private static final long serialVersionUID = 1602974659454016547L;

        public CopyMagnetAction() {
            putValue(Action.NAME, I18n.tr("Copy Magnet"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Copy Magnet"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Copy Magnet"));
            putValue(LimeAction.ICON_NAME, "COPY_MAGNET");
        }

        public void performAction(ActionEvent e) {
            BTDownload[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            String str = "";
            for (int i = 0; i < downloaders.length; i++) {
                str += TorrentUtil.getMagnet(downloaders[i].getHash()) + "&" + TorrentUtil.getMagnetURLParameters(downloaders[i].getDownloadManager().getTorrent());
                if (i < downloaders.length - 1) {
                    str += "\n";
                }
            }

            GUIMediator.setClipboardContent(str);
        }
    }

    private static class CopyInfoHashAction extends RefreshingAction {

        /**
         * 
         */
        private static final long serialVersionUID = 1138409323772464985L;

        public CopyInfoHashAction() {
            putValue(Action.NAME, I18n.tr("Copy Infohash"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Copy Infohash"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Copy Infohash"));
            putValue(LimeAction.ICON_NAME, "COPY_HASH");
        }

        public void performAction(ActionEvent e) {
            BTDownload[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            String str = "";
            for (int i = 0; i < downloaders.length; i++) {
                str += downloaders[i].getHash();
                if (i < downloaders.length - 1) {
                    str += "\n";
                }
            }
            GUIMediator.setClipboardContent(str);
        }
    }

    private static class ShareTorrentAction extends RefreshingAction {

        /**
         * 
         */
        private static final long serialVersionUID = 1138409323772464985L;

        public ShareTorrentAction() {
            putValue(Action.NAME, I18n.tr("Send to friend"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Send to friend"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Send to friend"));
            putValue(LimeAction.ICON_NAME, "SEND_HASH");
            //putValue(Action.SMALL_ICON, GUIMediator.getThemeImage("share"));
        }

        public void performAction(ActionEvent e) {
            BTDownload[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            if (downloaders.length != 1) {
                return;
            }

            BTDownload btDownload = downloaders[0];

            new ShareTorrentDialog(btDownload.getDownloadManager().getTorrent()).setVisible(true);
        }
    }

    static class ToggleSeedsVisibilityAction extends RefreshingAction {
        private static final long serialVersionUID = -1632629016830943795L;

        public ToggleSeedsVisibilityAction() {
            updateName();
            putValue(LimeAction.SHORT_NAME, I18n.tr("Show Details"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Show Torrent Details"));
        }

        private void updateName() {
            if (ApplicationSettings.SHOW_SEEDING_TRANSFERS.getValue()) {
                putValue(Action.NAME, I18n.tr("Hide Seeding Transfers"));
                putValue(LimeAction.SHORT_NAME, I18n.tr("Hide Seeding Transfers"));
                putValue(Action.SHORT_DESCRIPTION, I18n.tr("Don't show Seeding Transfers"));
            } else {
                putValue(Action.NAME, I18n.tr("Show Seeding Transfers"));
                putValue(LimeAction.SHORT_NAME, I18n.tr("Show Seeding Transfers"));
                putValue(Action.SHORT_DESCRIPTION, I18n.tr("Show Seeding Transfers"));
            }

        }

        @Override
        protected void performAction(ActionEvent e) {
            //toggle the setting
            ApplicationSettings.SHOW_SEEDING_TRANSFERS.setValue(!ApplicationSettings.SHOW_SEEDING_TRANSFERS.getValue());
            updateName();
            BTDownloadMediator.instance().updateTableFilters();
        }
    }

    static class CreateNewPlaylistAction extends AbstractAction {

        private static final long serialVersionUID = 3460908036485828909L;

        public CreateNewPlaylistAction() {
            super(I18n.tr("Create New Playlist"));
            putValue(Action.LONG_DESCRIPTION, I18n.tr("Create and add to a new playlist"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BTDownload[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();

            List<File> playlistFiles = new ArrayList<File>(downloaders.length);

            for (BTDownload d : downloaders) {
                if (!d.isCompleted()) {
                    return;
                }

                playlistFiles.add(d.getSaveLocation());
            }

            LibraryUtils.createNewPlaylist(playlistFiles.toArray(new File[0]));

        }
    }
    
    static final class PlaySingleMediaFileAction extends AbstractAction {
        
        private static final long serialVersionUID = -3628469680044329612L;

        public PlaySingleMediaFileAction() {
            super(I18n.tr("Play file"));
            putValue(Action.LONG_DESCRIPTION,I18n.tr("Play media file"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            File file = BTDownloadMediator.instance().getSelectedDownloaders()[0].getSaveLocation();
            
            if (MediaPlayer.isPlayableFile(file)) {
            	    MediaPlayer.instance().loadMedia(new MediaSource(file),true,false);
            }
        }
    }

    /**
     * NOTE: Make sure to check out AbstractLibraryTableMediator.AddToPlaylistAction, which is a similar action to this one.
     * @author gubatron
     *
     */
    static class AddToPlaylistAction extends AbstractAction {
        private static final int MAX_VISIBLE_PLAYLIST_NAME_LENGTH_IN_MENU = 80;
        private Playlist playlist; 

        public AddToPlaylistAction(Playlist playlist) {
            super(getTruncatedString(playlist.getName(),MAX_VISIBLE_PLAYLIST_NAME_LENGTH_IN_MENU));
            putValue(Action.LONG_DESCRIPTION, I18n.tr("Add to playlist ") + "\"" + getValue(Action.NAME) + "\"");
            System.out.println("Truncated playlist name was: " + getValue(Action.NAME));
            this.playlist = playlist;
        }
        
        private static String getTruncatedString(String string, int MAX_LENGTH) {
            return string.length() > MAX_LENGTH ? (string.substring(0, MAX_LENGTH-1) + "...") : string;            
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            BTDownload[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();

            List<File> playlistFiles = new ArrayList<File>(downloaders.length);

            for (BTDownload d : downloaders) {
                if (!d.isCompleted()) {
                    return;
                }

                playlistFiles.add(d.getSaveLocation());
            }

            LibraryUtils.asyncAddToPlaylist(playlist, playlistFiles.toArray(new File[0]));
            GUIMediator.instance().setWindow(GUIMediator.Tabs.LIBRARY);
        }
    }
}
