package com.limegroup.gnutella;

import java.io.File;
import java.net.Socket;
import java.util.Collection;
import java.util.List;

import com.limegroup.gnutella.browser.MagnetOptions;
import com.limegroup.gnutella.downloader.CantResumeException;
import com.limegroup.gnutella.downloader.CoreDownloader;
import com.limegroup.gnutella.downloader.PushedSocketHandler;
import com.limegroup.gnutella.messages.QueryReply;
import com.limegroup.gnutella.messages.QueryRequest;
import com.limegroup.gnutella.version.DownloadInformation;


/** 
 * The list of all downloads in progress.  DownloadManager has a fixed number 
 * of download slots given by the MAX_SIM_DOWNLOADS property.  It is
 * responsible for starting downloads and scheduling and queueing them as 
 * needed.  This class is thread safe.<p>
 *
 * As with other classes in this package, a DownloadManager instance may not be
 * used until initialize(..) is called.  The arguments to this are not passed
 * in to the constructor in case there are circular dependencies.<p>
 *
 * DownloadManager provides ways to serialize download state to disk.  Reads 
 * are initiated by RouterService, since we have to wait until the GUI is
 * initiated.  Writes are initiated by this, since we need to be notified of
 * completed downloads.  Downloads in the COULDNT_DOWNLOAD state are not 
 * serialized.  
 */
public interface DownloadManager extends BandwidthTracker, SaveLocationManager, PushedSocketHandler {

    /** 
     * Initializes this manager. <b>This method must be called before any other
     * methods are used.</b> 
     *     @uses RouterService.getCallback for the UI callback 
     *       to notify of download changes
     *     @uses RouterService.getMessageRouter for the message 
     *       router to use for sending push requests
     *     @uses RouterService.getFileManager for the FileManager
     *       to check if files exist
     */
    public void initialize();

    /**
     * Performs the slow, low-priority initialization tasks: reading in
     * snapshots and scheduling snapshot checkpointing.
     */
    public void loadSavedDownloadsAndScheduleWriting();

    /**
     * Kills all in-network downloaders that are not present in the list of URNs
     * @param urns a current set of urns that we are downloading in-network.
     */
    public void killDownloadersNotListed(Collection<? extends DownloadInformation> updates);

    public boolean acceptPushedSocket(String file, int index, byte[] clientGUID, Socket socket);

    /**
     * Determines if the given URN has an incomplete file.
     */
    public boolean isIncomplete(URN urn);

    /**
     * Returns whether or not we are actively downloading this file.
     */
    public boolean isActivelyDownloading(URN urn);

    public int downloadsInProgress();

    public int getNumIndividualDownloaders();

    /**
     * Inner network traffic don't count towards overall download activity.
     */
    public int getNumActiveDownloads();

    public int getNumWaitingDownloads();

    public Downloader getDownloaderForURN(URN sha1);

    /**
     * Returns the active or waiting downloader that uses or will use 
     * <code>file</code> as incomplete file.
     * @param file the incomplete file candidate
     * @return <code>null</code> if no downloader for the file is found
     */
    public Downloader getDownloaderForIncompleteFile(File file);

    public boolean isGuidForQueryDownloading(GUID guid);


    /**
     * Returns <code>true</code> if there already is a download with the same urn. 
     * @param urn may be <code>null</code>, then a check based on the fileName
     * and the fileSize is performed
     * @return
     */
    public boolean conflicts(URN urn, long fileSize, File... fileName);

    /**
     * Returns <code>true</code> if there already is a download that is or
     * will be saving to this file location.
     * @param candidateFile the final file location.
     * @return
     */
    public boolean isSaveLocationTaken(File candidateFile);

    /** 
     * Adds all responses (and alternates) in qr to any downloaders, if
     * appropriate.
     */
    public void handleQueryReply(QueryReply qr);

    /**
     * Removes downloader entirely from the list of current downloads.
     * Notifies callback of the change in status.
     * If completed is true, finishes the download completely.  Otherwise,
     * puts the download back in the waiting list to be finished later.
     *     @modifies this, callback
     */
    public void remove(CoreDownloader downloader, boolean completed);

    /**
     * Bumps the priority of an inactive download either up or down
     * by amt (if amt==0, bump to start/end of list).
     */
    public void bumpPriority(Downloader downl, boolean up, int amt);

    /** 
     * Attempts to send the given requery to provide the given downloader with 
     * more sources to download.  May not actually send the requery if it doing
     * so would exceed the maximum requery rate.
     * @param query the requery to send, which should have a marked GUID.
     *  Queries are subjected to global rate limiting iff they have marked 
     *  requery GUIDs.
     */
    public void sendQuery(QueryRequest query);

    /** Calls measureBandwidth on each uploader. */
    public void measureBandwidth();

    /** Returns the total upload throughput, i.e., the sum over all uploads. */
    public float getMeasuredBandwidth();

    /**
     * returns the summed average of the downloads
     */
    public float getAverageBandwidth();
}
