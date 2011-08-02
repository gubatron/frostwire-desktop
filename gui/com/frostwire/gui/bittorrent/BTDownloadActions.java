package com.frostwire.gui.bittorrent;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.limegroup.gnutella.gui.DialogOption;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.tables.LimeJTable;
import com.limegroup.gnutella.settings.ApplicationSettings;
import com.limegroup.gnutella.settings.SharingSettings;

final class BTDownloadActions {

    static final ShowDetailsAction SHOW_DETAILS_ACTION = new ShowDetailsAction();
    static final ExploreAction EXPLORE_ACTION = new ExploreAction();
    static final ResumeAction RESUME_ACTION = new ResumeAction();
    static final PauseAction PAUSE_ACTION = new PauseAction();
    static final RemoveAction REMOVE_ACTION = new RemoveAction(false, false);
    static final RemoveAction REMOVE_TORRENT_ACTION = new RemoveAction(true, false);
    static final RemoveAction REMOVE_TORRENT_AND_DATA_ACTION = new RemoveAction(true, true);
    static final CopyMagnetAction COPY_MAGNET_ACTION = new CopyMagnetAction();
    static final CopyInfoHashAction COPY_HASH_ACTION = new CopyInfoHashAction();
    public static final ToggleSeedsVisibilityAction TOGGLE_SEEDS_VISIBILITY_ACTION = new ToggleSeedsVisibilityAction();
	public static final Action SHARE_TORRENT_ACTION = new ShareTorrentAction();

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
            	String message1 = (downloaders.length > 1) ? "One of the transfers is complete and resuming will cause it to start seeding" : "This transfer is already complete, resuming it will cause it to start seeding";
            	String message2 = "Do you want to enable torrent seeding?";
            	answer = GUIMediator.showYesNoMessage(I18n.tr(message1 + "\n\n" + message2),DialogOption.YES);
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
        	if (_deleteData) {
				int result = JOptionPane
						.showConfirmDialog(
								GUIMediator.getAppFrame(),
								I18n.tr("Are you sure you want to remove the data files from your computer?\n\nYou won't be able to recover the files."),
								I18n.tr("Are you sure?"),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				
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
                str += TorrentUtil.getMagnet(downloaders[i].getHash());
                str += "\n";
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
                str += "\n";
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
}
