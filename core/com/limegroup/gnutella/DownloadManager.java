package com.limegroup.gnutella;

import java.io.File;
import java.net.Socket;



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
public interface DownloadManager {

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

    public boolean acceptPushedSocket(String file, int index, byte[] clientGUID, Socket socket);

    /**
     * Determines if the given URN has an incomplete file.
     */
    public boolean isIncomplete(URN urn);

    public int downloadsInProgress();

    public int getNumIndividualDownloaders();

    /**
     * Inner network traffic don't count towards overall download activity.
     */
    public int getNumActiveDownloads();

    public int getNumWaitingDownloads();

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


    /** Calls measureBandwidth on each uploader. */
    public void measureBandwidth();

    /** Returns the total upload throughput, i.e., the sum over all uploads. */
    public float getMeasuredBandwidth();

    /**
     * returns the summed average of the downloads
     */
    public float getAverageBandwidth();
}
