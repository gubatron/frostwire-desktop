package com.frostwire.gui.download.bittorrent;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.frostwire.bittorrent.BTDownloader;
import com.frostwire.bittorrent.TorrentUtil;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.util.GUILauncher;
import com.limegroup.gnutella.gui.util.GUILauncher.LaunchableProvider;

final class BTDownloadActions {

    static final ShowDetailsAction SHOW_DETAILS_ACTION = new ShowDetailsAction();
    static final ExploreAction EXPLORE_ACTION = new ExploreAction();
    static final LaunchAction LAUNCH_ACTION = new LaunchAction();
    static final ResumeAction RESUME_ACTION = new ResumeAction();
    static final PauseAction PAUSE_ACTION = new PauseAction();
    static final RemoveAction REMOVE_ACTION = new RemoveAction(false, false);
    static final RemoveAction REMOVE_TORRENT_ACTION = new RemoveAction(true, false);
    static final RemoveAction REMOVE_TORRENT_AND_DATA_ACTION = new RemoveAction(true, true);
    static final CopyMagnetAction COPY_MAGNET_ACTION = new CopyMagnetAction();
    static final CopyHashAction COPY_HASH_ACTION = new CopyHashAction();

    private static abstract class RefreshingAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = -937688457597255711L;

        public final void actionPerformed(ActionEvent e) {
            performAction(e);
            BTDownloadMediator.instance().doRefresh();
        }

        protected abstract void performAction(ActionEvent e);
    }

    final static class ShowDetailsAction extends RefreshingAction {

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
            BTDownloader[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            if (downloaders.length > 0) {
                File toExplore = downloaders[0].getSaveLocation();

                if (toExplore == null) {
                    return;
                }

                GUIMediator.launchExplorer(toExplore);
            }
        }
    }

    private static class LaunchAction extends RefreshingAction {

        /**
         * 
         */
        private static final long serialVersionUID = -567893064454697074L;

        public LaunchAction() {
            putValue(Action.NAME, I18n.tr("Preview Download"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Preview"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Preview Selected Downloads"));
            putValue(LimeAction.ICON_NAME, "DOWNLOAD_LAUNCH");
        }

        public void performAction(ActionEvent e) {
            BTDownloader[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            if (downloaders.length > 0) {

                LaunchableProvider[] providers = new LaunchableProvider[downloaders.length];

                for (int i = 0; i < downloaders.length; i++) {
                    providers[i] = new DownloaderProvider(downloaders[i]);
                }
                GUILauncher.launch(providers);
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
            BTDownloader[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            for (int i = 0; i < downloaders.length; i++) {
                downloaders[i].resume();
            }
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
            BTDownloader[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            for (int i = 0; i < downloaders.length; i++) {
                downloaders[i].pause();
            }
        }
    }

    private static class RemoveAction extends RefreshingAction {

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
            BTDownloader[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            for (int i = 0; i < downloaders.length; i++) {
                downloaders[i].setDeleteTorrentWhenRemove(_deleteTorrent);
                downloaders[i].setDeleteDataWhenRemove(_deleteData);
            }
            BTDownloadMediator.instance().removeSelection();
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
            BTDownloader[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            String str = "";
            for (int i = 0; i < downloaders.length; i++) {
                str += TorrentUtil.getMagnet(downloaders[i].getHash());
                str += "\n";
            }
            GUIMediator.setClipboardContent(str);
        }
    }
    
    private static class CopyHashAction extends RefreshingAction {

        /**
         * 
         */
        private static final long serialVersionUID = 1138409323772464985L;

        public CopyHashAction() {
            putValue(Action.NAME, I18n.tr("Copy Hash"));
            putValue(LimeAction.SHORT_NAME, I18n.tr("Copy Hash"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Copy Hash"));
            putValue(LimeAction.ICON_NAME, "COPY_HASH");
        }

        public void performAction(ActionEvent e) {
            BTDownloader[] downloaders = BTDownloadMediator.instance().getSelectedDownloaders();
            String str = "";
            for (int i = 0; i < downloaders.length; i++) {
                str += TorrentUtil.hashToString(downloaders[i].getHash());
                str += "\n";
            }
            GUIMediator.setClipboardContent(str);
        }
    }

    private static class DownloaderProvider implements LaunchableProvider {

        private final BTDownloader downloader;

        public DownloaderProvider(BTDownloader downloader) {
            this.downloader = downloader;
        }

        public BTDownloader getDownloader() {
            return downloader;
        }

        public File getFile() {
            return null;
        }

    }
}
