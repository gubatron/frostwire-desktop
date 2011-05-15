package com.frostwire.gui.download.bittorrent;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.library.LibraryMediator;
import com.limegroup.gnutella.gui.notify.Notification;
import com.limegroup.gnutella.gui.notify.NotifyUserProxy;
import com.limegroup.gnutella.gui.tables.AbstractDataLine;
import com.limegroup.gnutella.gui.tables.ColoredCell;
import com.limegroup.gnutella.gui.tables.ColoredCellImpl;
import com.limegroup.gnutella.gui.tables.IconAndNameHolder;
import com.limegroup.gnutella.gui.tables.IconAndNameHolderImpl;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.ProgressBarHolder;
import com.limegroup.gnutella.gui.tables.SizeHolder;
import com.limegroup.gnutella.gui.tables.SpeedRenderer;
import com.limegroup.gnutella.gui.tables.TimeRemainingHolder;
import com.limegroup.gnutella.gui.themes.SkinHandler;

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

    /**
     * Stores the current state of this download, as of the last update.
     * This is the state the everything should work off of to avoid the
     * <tt>Downloader</tt> instance being in a different state than
     * this data line.
     */
    private int _state;

    /**
     * Whether or not we've cleaned up this line.
     */
    private boolean _cleaned = false;

    /**
     * The colors for cells.
     */
    private Color _cellColor;
    private Color _othercellColor;

    private int lastState = -1;

    private Notification lastNotification;

    /**
     * Column index for the file name.
     */
    static final int FILE_INDEX = 0;
    private static final LimeTableColumn FILE_COLUMN = new LimeTableColumn(FILE_INDEX, "DOWNLOAD_NAME_COLUMN", I18n.tr("Name"), 201, true, ColoredCell.class);

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

    /**
     * Number of columns to display
     */
    static final int NUMBER_OF_COLUMNS = 8;

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
        _status = "";
        initColors();
        update();
    }

    /**
     * Tell the downloader to close its sockets.
     */
    public void cleanup() {
        //	    BackgroundExecutorService.schedule(new Runnable() {
        //	        public void run() {
        //	        	if (initializer.getClass().equals(BTDownloaderImpl.class)) {
        //	        		((BTDownloaderImpl) initializer).setCancelled(true);
        //	        	}
        //	        	
        //	            initializer.stop();
        //            }
        //        });
        _cleaned = true;
    }

    /**
     * Determines if this was cleaned up.
     */
    public boolean isCleaned() {
        return _cleaned;
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
            Color color = getColor(false);
            return new ColoredCellImpl(new IconAndNameHolderImpl(getIcon(), initializer.getDisplayName()), color, IconAndNameHolder.class);
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
            return new TimeRemainingHolder(_timeLeft);
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
        }
        return null;
    }

    public int getTypeAheadColumn() {
        return FILE_INDEX;
    }

    //    public boolean isTooltipRequired(int col) {
    //        return _state == DownloadStatus.INVALID;
    //    }

    //	public String[] getToolTipArray(int col) {
    //	    // give a new message if we gave up
    //	    if( _state == DownloadStatus.GAVE_UP )
    //	        return GAVE_UP_MESSAGE;
    //        
    //        if(_state == DownloadStatus.INVALID )
    //            return INVALID_MESSAGE;
    //        
    //        if (_state == DownloadStatus.WAITING_FOR_USER) {
    //            String custom = (String)initializer.getAttribute(Downloader.CUSTOM_INACTIVITY_KEY);
    //            if (custom != null)
    //                return new String[]{TRACKER_FAILURE_REASON,custom};
    //        }
    //	    	    
    //	    String[] info = new String[11];
    //	    String bandwidth = AVERAGE_BANDWIDTH + ": " + GUIUtils.rate2speed(
    //	        initializer.getAverageBandwidth()
    //	    );
    //	    String numHosts = POSSIBLE_HOSTS + ": " + 
    //	                     initializer.getPossibleHostCount();
    //        String busyHosts = BUSY_HOSTS + ": " +initializer.getBusyHostCount();
    //        String queuedHosts=QUEUED_HOSTS + ": "+initializer.getQueuedHostCount();
    //	    String numLocs = ALTERNATE_LOCATIONS + ": " +
    //	                     initializer.getNumberOfAlternateLocations();
    //        String numInvalidLocs = INVALID_ALTERNATE_LOCATIONS + ": " +
    //                         initializer.getNumberOfInvalidAlternateLocations();
    //		int chunkSize = 0;
    //		String numChunks = null;
    //		String lost;
    //		// DPINJ: pass in the shared disk controller!!!
    //        int totalPending = GuiCoreMediator.getDiskController().getNumPendingItems();
    //		synchronized(initializer) {
    //			if (_endTime == -1) {
    //				chunkSize = initializer.getChunkSize();
    //				numChunks = CHUNKS + ": "+initializer.getAmountVerified() / chunkSize +"/"+
    //				initializer.getAmountRead() / chunkSize+ "["+ 
    //				initializer.getAmountPending()+"|"+totalPending+"]"+ 
    //				"/"+
    //				initializer.getContentLength() / chunkSize+
    //				", "+chunkSize/1024+KB;
    //		
    //			}
    //		 	lost = LOST+": "+initializer.getAmountLost()/1024+KB;
    //		}
    //
    //        info[0] = STARTED_ON + " " + GUIUtils.msec2DateTime( _startTime );
    //	    if( _endTime != -1 ) {
    //	        info[1] = FINISHED_ON + " " + GUIUtils.msec2DateTime( _endTime );
    //	        info[2] = TIME_SPENT + ": " + CommonUtils.seconds2time(
    //	            (int)((_endTime - _startTime) / 1000 ) );
    //	        info[3] = "";
    //	        info[4] = bandwidth;
    //	        info[5] = numHosts;
    //            info[6] = busyHosts;
    //            info[7] = queuedHosts;
    //	        info[8] = numLocs;
    //            info[9] = numInvalidLocs;
    //			info[10] = lost;
    //	    } else {
    //	        info[1] = TIME_SPENT + ": " + CommonUtils.seconds2time(
    //	            (int) ((System.currentTimeMillis() - _startTime) / 1000 ) );
    //	        info[2] = "";
    //	        info[3] = bandwidth;
    //	        info[4] = numHosts;
    //            info[5] = busyHosts;
    //            info[6] = queuedHosts;
    //	        info[7] = numLocs;
    //            info[8] = numInvalidLocs;
    //			info[9] = numChunks;
    //			info[10] = lost;}
    //
    //	    return info;
    //	}

    /**
     * Returns the total size in bytes of the file being downloaded.
     *
     * @return the total size in bytes of the file being downloaded
     */
    long getLength() {
        return _size;
    }

    /**
     * Returns whether or not the <tt>Downloader</tt> for this download
     * is equal to the one passed in.
     *
     * @return <tt>true</tt> if the passed-in downloader is equal to the
     *  <tt>Downloader</tt> for this download, <tt>false</tt> otherwise
     */
    boolean containsDownloader(BTDownloader downloader) {
        return initializer.equals(downloader);
    }

    /**
     * Return the state of the Downloader
     *
     * @return the state of the downloader
     */
    int getState() {
        return _state;
    }

    /**
     * Returns whether or not the download has completed.
     *
     * @return <tt>true</tt> if the download is complete, <tt>false</tt> otherwise
     */
    boolean isCompleted() {
        return false;//_state == DownloadStatus.COMPLETE;
    }

    private Icon getIcon() {
        //	    if (initializer.getCustomIconDescriptor() == Downloader.BITTORRENT_DOWNLOAD)
        //	        return GUIMediator.getThemeImage("bittorrent_download");
        //	    else
        //	        return IconManager.instance().getIconForFile(initializer.getFile());
        return null;
    }

    /**
     * Updates all of the data for this download, obtaining fresh information
     * from the contained <tt>Downloader</tt> instance.
     *
     * @implements DataLine interface
     */
    public void update() {
        //		synchronized(initializer) {
        //		// always get new file name it might have changed
        //		_fileName = initializer.getSaveFile().getName();
        //	    _speed = -1;
        //	    _size = initializer.getContentLength();
        //		_amountRead = initializer.getAmountRead();
        //		_chatEnabled = initializer.hasChatEnabledHost();
        //        _browseEnabled = initializer.hasBrowseEnabledHost();
        //        _timeLeft = 0;
        //        //note: we *always* want to update progress
        //        // specifically for when the user has downloaded stuff,
        //        // closed the app, and then re-opened the app.
        //        //previously, because progress was only set while downloading
        //        //or corrupted, the GUI would display 0 progress, even
        //        //though it actually had progress.
        //		double d = (double)_amountRead/(double)_size;
        //		_progress = (int)(d*100);
        //		if (_progress > 100) {
        //			_progress = 100;
        //		}
        //		if (_progress < 0) {
        //			_progress = 0;
        //		}
        //		this.updateStatus();
        //		// downloads can go from inactive to active through resuming.
        //		if ( !this.isInactive() ) _endTime = -1;
        //	}

        _status = getInitializeObject().getStateString();
        _progress = getInitializeObject().getProgress();
        _amountRead = getInitializeObject().getBytesReceived();
        _downloadSpeed = getInitializeObject().getDownloadSpeed();
        _uploadSpeed = getInitializeObject().getUploadSpeed();
        _timeLeft = getInitializeObject().getETA();
    }

    /**
     * Updates the status of the download based on the state stored in the
     * <tt>Downloader</tt> instance for this <tt>DownloadDataLine</tt>.
     */
    private void updateStatus() {
        //	    final String lastVendor = _vendor;
        //	    _vendor = "";
        //		_state = initializer.getState();
        //		boolean paused = initializer.isPaused();
        //		if(paused && _state != DownloadStatus.PAUSED && !initializer.isCompleted()) {
        //		    _status = PAUSING_STATE;
        //		    return;
        //		}
        //		
        //		switch (_state) {
        //		case QUEUED:
        //			_status = QUEUED_STATE;
        //			break;
        //		case CONNECTING:
        //			int triedCount = initializer.getTriedHostCount();
        //			if (triedCount < 15) {
        //				
        //				_status = CONNECTING_STATE;
        //
        //			}
        //			else { 
        //				_status = MessageFormat.format(CONNECTING_STATE_TRIED_COUNT, 
        //						new Object[] { triedCount });
        //			}
        //			break;
        //		case BUSY:
        //			_status = WAITING_STATE;
        //			break;
        //	    case HASHING:
        //	        _status = HASHING_STATE;
        //	        break;
        //	    case SAVING:
        //	        _status = SAVING_STATE;
        //	        break;
        //		case COMPLETE:
        //            _status = COMPLETE_STATE;
        //            
        //            if (getDownloader() instanceof BTDownloaderImpl ||
        //            	getDownloader() instanceof TorrentFileFetcher) {
        //            	
        //            	if (SharingSettings.SEED_FINISHED_TORRENTS.getValue()) {
        //            		_status = COMPLETE_SEEDING_STATE;
        //            	} else {
        //            		_status = COMPLETE_NOT_SEEDING_STATE;
        //            	}
        //            }
        //            
        //			_progress = 100;
        //			break;
        //		case ABORTED:
        //			_status = ABORTED_STATE;
        //			break;
        //		case GAVE_UP:
        //			_status = FAILED_STATE;
        //			break;
        //        case IDENTIFY_CORRUPTION:
        //			_status = IDENTIFY_CORRUPTION_STATE;
        // 			break;
        //        case RECOVERY_FAILED:
        //            _status = "Recovery Failed";
        //            break;
        //		case DOWNLOADING:
        //		    _vendor = lastVendor;
        //		    updateHostCount(initializer);
        //            try {
        //                _speed = initializer.getMeasuredBandwidth();
        //                
        //                if (initializer instanceof BTDownloader && _speed < 0.1) {
        //					
        //					int numConnections = ((BTDownloader)initializer).getNumSeeds();
        //					
        //					if (numConnections <=1 )
        //						_status = CONNECTING_STATE;
        //					else
        //						_status = MessageFormat.format(BT_CONNECTED_STATE, 
        //								new Object[] { numConnections });
        //				}
        //            } catch(InsufficientDataException ide) {
        //                _speed = 0;
        //            }
        //            // If we have a valid rate (can't compute if rate is 0),
        //            // then determine how much time (in seconds) is remaining.
        //            if ( _speed > 0) {
        //                double kbLeft = ((_size/1024.0) -
        //								 (_amountRead/1024.0));
        //                _timeLeft = (int)(kbLeft / _speed);
        //            }
        //			break;
        //		case DISK_PROBLEM:
        //			_status = LIBRARY_MOVE_FAILED_STATE;
        //			_progress = 100;
        //			break;
        //        case CORRUPT_FILE:
        //            _status = CORRUPT_FILE_STATE;
        //            break;
        //        case WAITING_FOR_GNET_RESULTS:
        //			int stateTime=initializer.getRemainingStateTime();
        //			_status = MessageFormat.format(REQUERY_WAITING_STATE_START, stateTime);
        //            break;
        //        case ITERATIVE_GUESSING:
        //        case QUERYING_DHT:
        //            _status = DOWNLOAD_SEARCHING;
        //            break;
        //        case WAITING_FOR_USER:
        //            _status = REQUERY_WAITING_FOR_USER;
        //            break;
        //        case WAITING_FOR_CONNECTIONS:
        //            _status = WAITING_FOR_CONNECTIONS_STATE;
        //            break;
        //        case REMOTE_QUEUED:
        //            _status = REMOTE_QUEUED_STATE+" "+initializer.getQueuePosition();
        //            _vendor = initializer.getVendor();  
        //            updateVendor();
        //            break;
        //        case PAUSED:
        //            _status = PAUSED_STATE;
        //            break;
        //        case INVALID:
        //            _status = INVALID_STATE;
        //            break;
        //        case RESUMING:
        //        	_status = RESUMING_STATE;
        //        	break;
        //        case FETCHING:
        //        	_status = FETCHING_STATE;
        //        	break;
        //		default:
        //		    throw new IllegalStateException("Unknown status "+initializer.getState()+" of downloader");
        //		}
        //		
        //		showNotification();
    }

    private void showNotification() {
        if (lastState != _state) {
            Notification notification = null;
            if (isCompleted()) {
                Action[] actions = null;
                //	            File file = getFile();
                //	            if (file != null) {
                //	                actions = new Action[] { new LaunchAction(file), new ShowInLibraryAction(file) };
                //	            }
                //                if (file == null || !(file.getName().endsWith(".torrent") &&
                //                        BittorrentSettings.TORRENT_AUTO_START.getValue()))
                //                    notification = new Notification(getFileName() + ": " + _status, getIcon(), actions);
                //	        } else if (isDownloading() || isInactive() || lastState == -1) {
                //	            notification =  new Notification(getFileName() + ": " + _status, getIcon());
            } else {
                return;
            }

            if (notification != null) {
                if (lastNotification != null) {
                    NotifyUserProxy.instance().hideMessage(lastNotification);
                }
                NotifyUserProxy.instance().showMessage(notification);
                lastNotification = notification;
            }

            lastState = _state;
        }
    }

    /**
     * Returns a human-readable description of the address(es) from
     * which d is downloading.
     */
    private void updateHostCount(BTDownloader d) {
        //        int count = d.getNumHosts();
        //
        //        // we are in between chunks with this host,
        //        // use the previous count so-as not to confuse
        //        // the user.
        //        if (count == 0) {
        //            // don't change anything.
        //            return;
        //        }
        //        
        //        if (count==1) {
        //            _status = DOWNLOADING_STATE + " " + count + " "+ HOST_LABEL; 
        //            _vendor = d.getVendor();
        //        } else {
        //            _status = DOWNLOADING_STATE + " " +  count + " " + HOSTS_LABEL;
        //            _vendor = d.getVendor();
        //        }
        //        updateVendor();
    }

    private void updateVendor() {
        //    	if (_vendor == Downloader.BITTORRENT_DOWNLOAD)
        //        	_vendor = I18n.tr("BitTorrent");
    }

    /**
     * Returns whether or not this download is in what
     * is considered an "inactive"
     * state, such as completeed, aborted, failed, etc.
     *
     * @return <tt>true</tt> if this download is in an inactive state,
     *  <tt>false</tt> otherwise
     */
    boolean isInactive() {
        //		return (_state == DownloadStatus.COMPLETE ||
        //				_state == DownloadStatus.ABORTED ||
        //				_state == DownloadStatus.GAVE_UP ||
        //				_state == DownloadStatus.DISK_PROBLEM ||
        //                _state == DownloadStatus.CORRUPT_FILE);
        return false;
    }

    /**
     * Returns whether or not the
     * download for this line is currently downloading
     *
     * @return <tt>true</tt> if this download is currently downloading,
     *  <tt>false</tt> otherwise
     */
    boolean isDownloading() {
        return false;// _state == DownloadStatus.DOWNLOADING;
    }

    private void initColors() {
        _cellColor = SkinHandler.getWindow8Color();
        _othercellColor = SkinHandler.getSearchResultSpeedColor();
    }

    private Color getColor(boolean playing) {
        return playing ? _othercellColor : _cellColor;
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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isClippable(int col) {
        // TODO Auto-generated method stub
        return false;
    }
}
