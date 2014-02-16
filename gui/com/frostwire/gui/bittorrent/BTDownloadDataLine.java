/*
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
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.limewire.util.OSUtils;

import com.frostwire.gui.library.LibraryMediator;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconManager;
import com.limegroup.gnutella.gui.iTunesMediator;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.notify.Notification;
import com.limegroup.gnutella.gui.notify.NotifyUserProxy;
import com.limegroup.gnutella.gui.search.BTDownloadPaymentOptionsHolder;
import com.limegroup.gnutella.gui.tables.AbstractDataLine;
import com.limegroup.gnutella.gui.tables.IconAndNameHolderImpl;
import com.limegroup.gnutella.gui.tables.IconAndNameRenderer;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.ProgressBarHolder;
import com.limegroup.gnutella.gui.tables.SeedsHolder;
import com.limegroup.gnutella.gui.tables.SizeHolder;
import com.limegroup.gnutella.gui.tables.SpeedRenderer;
import com.limegroup.gnutella.gui.tables.TimeRemainingHolder;
import com.limegroup.gnutella.settings.iTunesSettings;

/**
 * This class handles all of the data for a single download, representing
 * one "line" in the download window.  It continually updates the
 * displayed data for the download from the contained <tt>Downloader</tt>
 * instance.
 */
final class BTDownloadDataLine extends AbstractDataLine<BTDownload> {

    private static final String PARTIAL_DOWNLOAD_TEXT = I18n.tr(" (Handpicked)");

    /**
     * Variable for the status of the download.
     */
    private String _status;

    /**
     * Variable for the amount of the file that has been read.
     */
    private long _download = 0;

    private long _upload;

    /**
     * Variable for the progress made in the progressbar.
     */
    private int _progress;

    /**
     * Variable for the size of the download.
     */
    private long _size = -1;

    /**
     * Variable for the speed of the download.
     */
    private double _downloadSpeed;

    private double _uploadSpeed;

    /**
     * Variable for how much time is left.
     */
    private long _timeLeft;

    private String _seeds;

    private String _peers;

    private String _shareRatio;

    private String _seedToPeerRatio;

    private Date dateCreated;

    private boolean _notification;
    /**
     * Column index for the file name.
     */
    static final int FILE_INDEX = 0;
    private static final LimeTableColumn FILE_COLUMN = new LimeTableColumn(FILE_INDEX, "DOWNLOAD_NAME_COLUMN", I18n.tr("Name"), 201, true, IconAndNameRenderer.class);

    /** Column index for name-your-price/tips/donations */
    static final int PAYMENT_OPTIONS_INDEX = 1;
    private static final LimeTableColumn PAYMENT_OPTIONS_COLUMN = new LimeTableColumn(PAYMENT_OPTIONS_INDEX, "PAYMENT_OPTIONS_COLUMN", I18n.tr("Tips/Donations"), 65, true, BTDownloadPaymentOptionsHolder.class );
    
    /**
     * Column index for the file size.
     */
    static final int SIZE_INDEX = 2;
    private static final LimeTableColumn SIZE_COLUMN = new LimeTableColumn(SIZE_INDEX, "DOWNLOAD_SIZE_COLUMN", I18n.tr("Size"), 65, true, SizeHolder.class);

    /**
     * Column index for the file download status.
     */
    static final int STATUS_INDEX = 3;
    private static final LimeTableColumn STATUS_COLUMN = new LimeTableColumn(STATUS_INDEX, "DOWNLOAD_STATUS_COLUMN", I18n.tr("Status"), 152, true, String.class);

    /**
     * Column index for the progress of the download.
     */
    static final int PROGRESS_INDEX = 4;
    private static final LimeTableColumn PROGRESS_COLUMN = new LimeTableColumn(PROGRESS_INDEX, "DOWNLOAD_PROGRESS_COLUMN", I18n.tr("Progress"), 71, true, ProgressBarHolder.class);

    /**
     * Column index for actual amount of bytes downloaded.
     */
    static final int BYTES_DOWNLOADED_INDEX = 5;
    private static final LimeTableColumn BYTES_DOWNLOADED_COLUMN = new LimeTableColumn(BYTES_DOWNLOADED_INDEX, "DOWNLOAD_BYTES_DOWNLOADED_COLUMN", I18n.tr("Downloaded"), 20, true, SizeHolder.class);

    static final int BYTES_UPLOADED_INDEX = 6;
    private static final LimeTableColumn BYTES_UPLOADED_COLUMN = new LimeTableColumn(BYTES_UPLOADED_INDEX, "DOWNLOAD_BYTES_UPLOADED_COLUMN", I18n.tr("Uploaded"), 20, false, SizeHolder.class);

    /**
     * Column index for the download speed.
     */
    static final int DOWNLOAD_SPEED_INDEX = 7;
    private static final LimeTableColumn DOWNLOAD_SPEED_COLUMN = new LimeTableColumn(DOWNLOAD_SPEED_INDEX, "DOWNLOAD_SPEED_COLUMN", I18n.tr("Down Speed"), 58, true, SpeedRenderer.class);

    static final int UPLOAD_SPEED_INDEX = 8;
    private static final LimeTableColumn UPLOAD_SPEED_COLUMN = new LimeTableColumn(UPLOAD_SPEED_INDEX, "UPLOAD_SPEED_COLUMN", I18n.tr("Up Speed"), 58, true, SpeedRenderer.class);

    /**
     * Column index for the download time remaining.
     */
    static final int TIME_INDEX = 9;
    private static final LimeTableColumn TIME_COLUMN = new LimeTableColumn(TIME_INDEX, "DOWNLOAD_TIME_REMAINING_COLUMN", I18n.tr("Time"), 49, true, TimeRemainingHolder.class);

    static final int SEEDS_INDEX = 10;
    private static final LimeTableColumn SEEDS_COLUMN = new LimeTableColumn(SEEDS_INDEX, "SEEDS_STATUS_COLUMN", I18n.tr("Seeds"), 80, true, String.class);

    static final int PEERS_INDEX = 11;
    private static final LimeTableColumn PEERS_COLUMN = new LimeTableColumn(PEERS_INDEX, "PEERS_STATUS_COLUMN", I18n.tr("Peers"), 80, false, String.class);

    static final int SHARE_RATIO_INDEX = 12;
    private static final LimeTableColumn SHARE_RATIO_COLUMN = new LimeTableColumn(SHARE_RATIO_INDEX, "SHARE_RATIO_COLUMN", I18n.tr("Share Ratio"), 80, false, String.class);

    static final int SEED_TO_PEER_RATIO_INDEX = 13;
    private static final LimeTableColumn SEED_TO_PEER_RATIO_COLUMN = new LimeTableColumn(SEED_TO_PEER_RATIO_INDEX, "SEED_TO_PEER_RATIO_COLUMN", I18n.tr("Seeds/Peers"), 80, false, String.class);

    static final int DATE_CREATED_INDEX = 14;
    static final LimeTableColumn DATE_CREATED_COLUMN = new LimeTableColumn(DATE_CREATED_INDEX, "DATE_CREATED_COLUMN", I18n.tr("Started On"), 80, false, Date.class);

    /**
     * Number of columns to display
     */
    static final int NUMBER_OF_COLUMNS = 15;

    // Implements DataLine interface
    public int getColumnCount() {
        return NUMBER_OF_COLUMNS;
    }

    /**
     * Must initialize data.
     *
     * @param downloader the <tt>Downloader</tt>
     *  that provides access to
     *  information about the download
     */
    public void initialize(BTDownload downloader) {
        super.initialize(downloader);
        _notification = downloader.isCompleted();
        update();
    }

    public boolean isSeeding() {
        if (initializer == null) {
            return false;
        }

        return initializer.getState() == DownloadManager.STATE_SEEDING;
    }

    /**
     * Returns the <tt>Object</tt> stored at the specified column in this
     * line of data.
     *
     * @param index the index of the column to retrieve data from
     * @return the <tt>Object</tt> stored at that index
     * @implements DataLine interface
     */
    public Object getValueAt(int index) {
        switch (index) {
        case FILE_INDEX:
            return new IconAndNameHolderImpl(getIcon(), initializer.getDisplayName());
        case PAYMENT_OPTIONS_INDEX:
            return new BTDownloadPaymentOptionsHolder(initializer);
        case SIZE_INDEX:
            if (initializer.isPartialDownload()) {
                return new SizeHolder(_size, PARTIAL_DOWNLOAD_TEXT);
            } else {
                return new SizeHolder(_size);
            }
        case STATUS_INDEX:
            return _status;
        case PROGRESS_INDEX:
            return Integer.valueOf(_progress);
        case BYTES_DOWNLOADED_INDEX:
            return new SizeHolder(_download);
        case BYTES_UPLOADED_INDEX:
            return new SizeHolder(_upload);
        case DOWNLOAD_SPEED_INDEX:
            return new Double(_downloadSpeed);
        case UPLOAD_SPEED_INDEX:
            return new Double(_uploadSpeed);
        case TIME_INDEX:
            if (initializer.isCompleted()) {
                return new TimeRemainingHolder(0);
            } else if (_downloadSpeed < 0.001 && !(initializer instanceof BTPeerHttpUpload)) {
                return new TimeRemainingHolder(-1);
            } else {
                return new TimeRemainingHolder(_timeLeft);
            }
        case SEEDS_INDEX:
            return new SeedsHolder(_seeds);
        case PEERS_INDEX:
            return _peers;
        case SHARE_RATIO_INDEX:
            return _shareRatio;
        case SEED_TO_PEER_RATIO_INDEX:
            return _seedToPeerRatio;
        case DATE_CREATED_INDEX:
            return dateCreated;
        }
        return null;
    }

    /**
     * @implements DataLine interface
     */
    public LimeTableColumn getColumn(int idx) {
        return staticGetColumn(idx);
    }

    static LimeTableColumn staticGetColumn(int idx) {
        switch (idx) {
        case FILE_INDEX:
            return FILE_COLUMN;
        case PAYMENT_OPTIONS_INDEX:
            return PAYMENT_OPTIONS_COLUMN;
        case SIZE_INDEX:
            return SIZE_COLUMN;
        case STATUS_INDEX:
            return STATUS_COLUMN;
        case PROGRESS_INDEX:
            return PROGRESS_COLUMN;
        case BYTES_DOWNLOADED_INDEX:
            return BYTES_DOWNLOADED_COLUMN;
        case BYTES_UPLOADED_INDEX:
            return BYTES_UPLOADED_COLUMN;
        case DOWNLOAD_SPEED_INDEX:
            return DOWNLOAD_SPEED_COLUMN;
        case UPLOAD_SPEED_INDEX:
            return UPLOAD_SPEED_COLUMN;
        case TIME_INDEX:
            return TIME_COLUMN;
        case SEEDS_INDEX:
            return SEEDS_COLUMN;
        case PEERS_INDEX:
            return PEERS_COLUMN;
        case SHARE_RATIO_INDEX:
            return SHARE_RATIO_COLUMN;
        case SEED_TO_PEER_RATIO_INDEX:
            return SEED_TO_PEER_RATIO_COLUMN;
        case DATE_CREATED_INDEX:
            return DATE_CREATED_COLUMN;
        }
        return null;
    }

    public int getTypeAheadColumn() {
        return FILE_INDEX;
    }

    public String[] getToolTipArray(int col) {
        String[] info = new String[11];
        String name = getInitializeObject().getDisplayName();
        String status = I18n.tr("Status") + ": " + getInitializeObject().getStateString();
        String progress = I18n.tr("Progress") + ": " + getInitializeObject().getProgress() + "%";
        String downSpeed = I18n.tr("Down Speed") + ": " + GUIUtils.rate2speed(getInitializeObject().getDownloadSpeed());
        String upSpeed = I18n.tr("Up Speed") + ": " + GUIUtils.rate2speed(getInitializeObject().getUploadSpeed());
        String downloaded = I18n.tr("Downloaded") + ": " + new SizeHolder(getInitializeObject().getBytesReceived());
        String uploaded = I18n.tr("Uploaded") + ": " + new SizeHolder(getInitializeObject().getBytesSent());
        String peers = I18n.tr("Peers") + ": " + getInitializeObject().getPeersString();
        String seeds = I18n.tr("Seeds") + ": " + getInitializeObject().getSeedsString();
        String size = I18n.tr("Size") + ": " + new SizeHolder(getInitializeObject().getSize());
        String time = I18n.tr("ETA") + ": " + (getInitializeObject().isCompleted() ? new TimeRemainingHolder(0) : (getInitializeObject().getDownloadSpeed() < 0.001 ? new TimeRemainingHolder(-1) : new TimeRemainingHolder(getInitializeObject().getETA())));

        info[0] = name;
        info[1] = status;
        info[2] = progress;
        info[3] = downSpeed;
        info[4] = upSpeed;
        info[5] = downloaded;
        info[6] = uploaded;
        info[7] = peers;
        info[8] = seeds;
        info[9] = size;
        info[10] = time;

        return info;
    }

    private Icon getIcon() {
        if (initializer.isPartialDownload()) {
            try {
                return IconManager.instance().getIconForFile(new File(initializer.getDisplayName()));
            } catch (Exception e) {
                // ignore error
                return IconManager.instance().getIconForFile(initializer.getSaveLocation());
            }
        } else if (initializer instanceof YouTubeDownload || initializer instanceof SoundcloudDownload) {
            return IconManager.instance().getIconForFile(initializer.getSaveLocation());
        } else {
            return IconManager.instance().getIconForFile(initializer.getSaveLocation());
        }
    }

    /**
     * Updates all of the data for this download, obtaining fresh information
     * from the contained <tt>Downloader</tt> instance.
     *
     * @implements DataLine interface
     */
    public void update() {
        _status = initializer.getStateString();
        _progress = initializer.getProgress();
        _download = initializer.getBytesReceived();
        _upload = initializer.getBytesSent();
        _downloadSpeed = initializer.getDownloadSpeed();
        _uploadSpeed = initializer.getUploadSpeed();
        _timeLeft = initializer.getETA();
        _seeds = initializer.getSeedsString();
        _peers = initializer.getPeersString();
        _shareRatio = initializer.getShareRatio();
        _seedToPeerRatio = initializer.getSeedToPeerRatio();
        _size = initializer.getSize();
        dateCreated = initializer.getDateCreated();

        if (getInitializeObject().isCompleted()) {
            showNotification();
        }
    }

    private void showNotification() {
        if (!_notification) {
            _notification = true;
            Notification notification = null;
            BTDownload theDownload = getInitializeObject();
            if (theDownload.isCompleted()) {
                Action[] actions = null;
                File file = getInitializeObject().getSaveLocation();
                if (file != null) {
                    actions = new Action[] { new LaunchAction(file), new ShowInLibraryAction(file) };
                }
                notification = new Notification(theDownload.getDisplayName(), getIcon(), actions);
                LibraryMediator.instance().getLibraryExplorer().clearDirectoryHolderCaches();

                iTunesScanIfNecessaryForNonTorrentDownloadItem(theDownload, file);

            } else {
                return;
            }

            if (notification != null) {
                NotifyUserProxy.instance().showMessage(notification);
            }
        }
    }

    /**
     * Gubatron: iTunes auto scanning hack for non-torrents.
     * This logic was taken from BTDownloadCreator::createDownload(DownloadManager downloadManager, final boolean triggerFilter)
     * in that method, the DownloadManager (azureus) sets up a listener that takes care of this when the torrent download is finished.
     * 
     * With YouTubeItemDownload and SoundcloudTrackDownload I've yet to find the equivalent listeners to move this logic at the end of the
     * download, for now, showNotification is the hack that I have...
     * Suggestion: Use BTDownloadCreator to normalize the initialization of all downloads, and setup there the behavior of the
     * equivalent download managers if they exist, if not we could create listeners, and so all this logic will be maintained
     * in one place.
     * 
     * @param theDownload
     * @param file
     */
    private void iTunesScanIfNecessaryForNonTorrentDownloadItem(BTDownload theDownload, File file) {
        if ((OSUtils.isMacOSX() || OSUtils.isWindows()) && (theDownload instanceof YouTubeDownload || theDownload instanceof SoundcloudDownload) && iTunesSettings.ITUNES_SUPPORT_ENABLED.getValue() && !iTunesMediator.instance().isScanned(file)) {
            iTunesMediator.instance().scanForSongs(file);
        }
    }

    private final class LaunchAction extends AbstractAction {

        private static final long serialVersionUID = 4020797972200661119L;

        private File file;

        public LaunchAction(File file) {
            this.file = file;

            putValue(Action.NAME, I18n.tr("Launch"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Launch Selected Files"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_LAUNCH");
        }

        public void actionPerformed(ActionEvent ae) {
            // It adds and plays the current file to the media library when user clicks from the "Launch" notification Window.        
            GUIUtils.launchOrEnqueueFile(file, false);
        }
    }

    private final class ShowInLibraryAction extends AbstractAction {

        private static final long serialVersionUID = -6177511216279954853L;

        private File file;

        public ShowInLibraryAction(File file) {
            this.file = file;

            putValue(Action.NAME, I18n.tr("Show in Library"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Show Download in Library"));
        }

        public void actionPerformed(ActionEvent ae) {
            GUIMediator.instance().setWindow(GUIMediator.Tabs.LIBRARY);
            LibraryMediator.instance().setSelectedFile(file);
        }
    }

    @Override
    public boolean isDynamic(int col) {
        return false;
    }

    @Override
    public boolean isClippable(int col) {
        return false;
    }
}