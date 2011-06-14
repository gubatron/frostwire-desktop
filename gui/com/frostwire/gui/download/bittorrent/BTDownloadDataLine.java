package com.frostwire.gui.download.bittorrent;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import com.frostwire.bittorrent.BTDownloader;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconManager;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.library.LibraryMediator;
import com.limegroup.gnutella.gui.notify.Notification;
import com.limegroup.gnutella.gui.notify.NotifyUserProxy;
import com.limegroup.gnutella.gui.tables.AbstractDataLine;
import com.limegroup.gnutella.gui.tables.IconAndNameHolder;
import com.limegroup.gnutella.gui.tables.IconAndNameHolderImpl;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.ProgressBarHolder;
import com.limegroup.gnutella.gui.tables.SizeHolder;
import com.limegroup.gnutella.gui.tables.SpeedRenderer;
import com.limegroup.gnutella.gui.tables.TimeRemainingHolder;

/**
 * This class handles all of the data for a single download, representing
 * one "line" in the download window.  It continually updates the
 * displayed data for the download from the contained <tt>Downloader</tt>
 * instance.
 */
final class BTDownloadDataLine extends AbstractDataLine<BTDownloader> {

    /**
     * Variable for the status of the download.
     */
    private String _status;

    /**
     * Variable for the amount of the file that has been read.
     */
    private long _amountRead = 0;

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

    private boolean _notification;
    /**
     * Column index for the file name.
     */
    static final int FILE_INDEX = 0;
    private static final LimeTableColumn FILE_COLUMN = new LimeTableColumn(FILE_INDEX, "DOWNLOAD_NAME_COLUMN", I18n.tr("Name"), 201, true,
            IconAndNameHolder.class);

    /**
     * Column index for the file size.
     */
    static final int SIZE_INDEX = 1;
    private static final LimeTableColumn SIZE_COLUMN = new LimeTableColumn(SIZE_INDEX, "DOWNLOAD_SIZE_COLUMN", I18n.tr("Size"), 65, true, SizeHolder.class);

    /**
     * Column index for the file download status.
     */
    static final int STATUS_INDEX = 2;
    private static final LimeTableColumn STATUS_COLUMN = new LimeTableColumn(STATUS_INDEX, "DOWNLOAD_STATUS_COLUMN", I18n.tr("Status"), 152, true, String.class);

    /**
     * Column index for the progress of the download.
     */
    static final int PROGRESS_INDEX = 3;
    private static final LimeTableColumn PROGRESS_COLUMN = new LimeTableColumn(PROGRESS_INDEX, "DOWNLOAD_PROGRESS_COLUMN", I18n.tr("Progress"), 71, true,
            ProgressBarHolder.class);

    /**
     * Column index for actual amount of bytes downloaded.
     */
    static final int BYTES_DOWNLOADED_INDEX = 4;
    private static final LimeTableColumn BYTES_DOWNLOADED_COLUMN = new LimeTableColumn(BYTES_DOWNLOADED_INDEX, "DOWNLOAD_BYTES_DOWNLOADED_COLUMN",
            I18n.tr("Downloaded"), 20, true, SizeHolder.class);

    /**
     * Column index for the download speed.
     */
    static final int DOWNLOAD_SPEED_INDEX = 5;
    private static final LimeTableColumn DOWNLOAD_SPEED_COLUMN = new LimeTableColumn(DOWNLOAD_SPEED_INDEX, "DOWNLOAD_SPEED_COLUMN", I18n.tr("Down Speed"), 58,
            true, SpeedRenderer.class);

    static final int UPLOAD_SPEED_INDEX = 6;
    private static final LimeTableColumn UPLOAD_SPEED_COLUMN = new LimeTableColumn(UPLOAD_SPEED_INDEX, "UPLOAD_SPEED_COLUMN", I18n.tr("Up Speed"), 58, true,
            SpeedRenderer.class);

    /**
     * Column index for the download time remaining.
     */
    static final int TIME_INDEX = 7;
    private static final LimeTableColumn TIME_COLUMN = new LimeTableColumn(TIME_INDEX, "DOWNLOAD_TIME_REMAINING_COLUMN", I18n.tr("Time"), 49, true,
            TimeRemainingHolder.class);

    static final int SEEDS_INDEX = 8;
    private static final LimeTableColumn SEEDS_COLUMN = new LimeTableColumn(SEEDS_INDEX, "SEEDS_STATUS_COLUMN", I18n.tr("Seeds"), 80, true, String.class);

    static final int PEERS_INDEX = 9;
    private static final LimeTableColumn PEERS_COLUMN = new LimeTableColumn(PEERS_INDEX, "PEERS_STATUS_COLUMN", I18n.tr("Peers"), 80, false, String.class);

    /**
     * Number of columns to display
     */
    static final int NUMBER_OF_COLUMNS = 10;

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
    public void initialize(BTDownloader downloader) {
        super.initialize(downloader);
        _size = initializer.getSize();
        _notification = downloader.isCompleted();
        update();
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
        case SIZE_INDEX:
            return new SizeHolder(_size);
        case STATUS_INDEX:
            return _status;
        case PROGRESS_INDEX:
            return Integer.valueOf(_progress);
        case BYTES_DOWNLOADED_INDEX:
            return new SizeHolder(_amountRead);
        case DOWNLOAD_SPEED_INDEX:
            return new Double(_downloadSpeed);
        case UPLOAD_SPEED_INDEX:
            return new Double(_uploadSpeed);
        case TIME_INDEX:
            if (initializer.isCompleted()) {
                return new TimeRemainingHolder(0);
            } else if (_downloadSpeed < 0.001) {
                return new TimeRemainingHolder(-1);
            } else {
                return new TimeRemainingHolder(_timeLeft);
            }
        case SEEDS_INDEX:
            return _seeds;
        case PEERS_INDEX:
            return _peers;
        }
        return null;
    }

    /**
     * @implements DataLine interface
     */
    public LimeTableColumn getColumn(int idx) {
        switch (idx) {
        case FILE_INDEX:
            return FILE_COLUMN;
        case SIZE_INDEX:
            return SIZE_COLUMN;
        case STATUS_INDEX:
            return STATUS_COLUMN;
        case PROGRESS_INDEX:
            return PROGRESS_COLUMN;
        case BYTES_DOWNLOADED_INDEX:
            return BYTES_DOWNLOADED_COLUMN;
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
        return IconManager.instance().getIconForFile(initializer.getSaveLocation());
    }

    /**
     * Updates all of the data for this download, obtaining fresh information
     * from the contained <tt>Downloader</tt> instance.
     *
     * @implements DataLine interface
     */
    public void update() {
        _status = getInitializeObject().getStateString();
        _progress = getInitializeObject().getProgress();
        _amountRead = getInitializeObject().getBytesReceived();
        _downloadSpeed = getInitializeObject().getDownloadSpeed();
        _uploadSpeed = getInitializeObject().getUploadSpeed();
        _timeLeft = getInitializeObject().getETA();
        _seeds = getInitializeObject().getSeedsString();
        _peers = getInitializeObject().getPeersString();

        if (getInitializeObject().isCompleted()) {
            showNotification();
        }
    }

    private void showNotification() {
        if (!_notification) {
            _notification = true;
            Notification notification = null;
            if (getInitializeObject().isCompleted()) {
                Action[] actions = null;
                File file = getInitializeObject().getSaveLocation();
                if (file != null) {
                    actions = new Action[] { new LaunchAction(file), new ShowInLibraryAction(file) };
                }
                notification = new Notification(getInitializeObject().getDisplayName(), getIcon(), actions);
            } else {
                return;
            }

            if (notification != null) {
                NotifyUserProxy.instance().showMessage(notification);
            }
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
            LibraryMediator.setSelectedFile(file);
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
