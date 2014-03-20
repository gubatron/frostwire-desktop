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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.limewire.util.FilenameUtils;

import com.frostwire.torrent.CopyrightLicenseBroker;
import com.frostwire.torrent.PaymentOptions;
import com.frostwire.util.DigestUtils;
import com.frostwire.util.DigestUtils.DigestProgressListener;
import com.frostwire.util.HttpClient;
import com.frostwire.util.HttpClient.HttpClientListener;
import com.frostwire.util.HttpClient.RangeNotSupportedException;
import com.frostwire.util.HttpClientFactory;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.SharingSettings;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class HttpDownload implements BTDownload {

    /** TODO: Make this configurable */
    private static final Executor HTTP_THREAD_POOL = Executors.newFixedThreadPool(6);

    private static final String STATE_DOWNLOADING = I18n.tr("Downloading");
    private static final String STATE_ERROR = I18n.tr("Error");
    private static final String STATE_ERROR_MD5 = I18n.tr("Error - corrupted file");
    private static final String STATE_ERROR_MOVING_INCOMPLETE = I18n.tr("Error - can't save");
    private static final String STATE_PAUSING = I18n.tr("Pausing");
    private static final String STATE_PAUSED = I18n.tr("Paused");
    private static final String STATE_CANCELING = I18n.tr("Canceling");
    private static final String STATE_CANCELED = I18n.tr("Canceled");
    private static final String STATE_WAITING = I18n.tr("Waiting");
    private static final String STATE_CHECKING = I18n.tr("Checking");
    private static final String STATE_FINISHED = I18n.tr("Finished");

    private static final int SPEED_AVERAGE_CALCULATION_INTERVAL_MILLISECONDS = 1000;

    private final String url;
    private final String title;
    private final String saveAs;

    private File saveFile;
    private final File completeFile;
    private final File incompleteFile;

    private final String md5; //optional
    private final HttpClient httpClient;
    private final HttpClientListener httpClientListener;
    private final Date dateCreated;

    /** If false it should delete any temporary data and start from the beginning. */
    private final boolean deleteDataWhenCancelled;

    private long size;
    private long bytesReceived;
    protected String state;
    private long averageSpeed; // in bytes

    // variables to keep the download rate of file transfer
    private long speedMarkTimestamp;
    private long totalReceivedSinceLastSpeedStamp;

    private int md5CheckingProgress;

    private boolean isResumable;

    public HttpDownload(String theURL, String theTitle, String saveFileAs, long fileSize, String md5hash, boolean shouldResume, boolean deleteFileWhenTransferCancelled) {
        url = theURL;
        title = theTitle;
        saveAs = saveFileAs;

        size = fileSize;
        md5 = md5hash;
        deleteDataWhenCancelled = deleteFileWhenTransferCancelled;

        completeFile = buildFile(SharingSettings.TORRENT_DATA_DIR_SETTING.getValue(), saveAs);
        incompleteFile = buildIncompleteFile(completeFile);

        bytesReceived = 0;
        dateCreated = new Date();

        httpClientListener = new HttpDownloadListenerImpl();

        httpClient = HttpClientFactory.newInstance();
        httpClient.setListener(httpClientListener);

        isResumable = shouldResume;
        start(shouldResume);
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getSize(boolean update) {
        return size;
    }

    @Override
    public String getDisplayName() {
        return title;
    }

    @Override
    public boolean isResumable() {
        return isResumable && state == STATE_PAUSED && size > 0;
    }

    @Override
    public boolean isPausable() {
        return isResumable && state == STATE_DOWNLOADING && size > 0;
    }

    @Override
    public boolean isCompleted() {
        return isComplete();
    }

    @Override
    public int getState() {
        if (state == STATE_DOWNLOADING) {
            return DownloadManager.STATE_DOWNLOADING;
        }

        return DownloadManager.STATE_STOPPED;
    }

    @Override
    public void remove() {
        if (state != STATE_FINISHED) {
            state = STATE_CANCELING;
            httpClient.cancel();
        }
    }

    private void cleanup() {
        cleanupIncomplete();
        cleanupComplete();
    }

    @Override
    public void pause() {
        state = STATE_PAUSING;
        httpClient.cancel();
    }

    @Override
    public File getSaveLocation() {
        return saveFile;
    }

    @Override
    public void resume() {
        start(true);
    }

    @Override
    public int getProgress() {
        if (state == STATE_CHECKING) {
            return md5CheckingProgress;
        }

        if (size <= 0) {
            return -1;
        }

        int progress = (int) ((bytesReceived * 100) / size);

        return Math.min(100, progress);
    }

    @Override
    public String getStateString() {
        return state;
    }

    @Override
    public long getBytesReceived() {
        return bytesReceived;
    }

    @Override
    public long getBytesSent() {
        return 0;
    }

    @Override
    public double getDownloadSpeed() {
        double result = 0;
        if (state == STATE_DOWNLOADING) {
            result = averageSpeed / 1000;
        }
        return result;
    }

    @Override
    public double getUploadSpeed() {
        return 0;
    }

    @Override
    public long getETA() {
        if (size > 0) {
            long speed = averageSpeed;
            return speed > 0 ? (size - getBytesReceived()) / speed : -1;
        } else {
            return -1;
        }
    }

    @Override
    public DownloadManager getDownloadManager() {
        return null;
    }

    @Override
    public String getPeersString() {
        return "";
    }

    @Override
    public String getSeedsString() {
        return "";
    }

    @Override
    public boolean isDeleteTorrentWhenRemove() {
        return false;
    }

    @Override
    public boolean isDeleteDataWhenRemove() {
        return deleteDataWhenCancelled;
    }

    @Override
    public String getHash() {
        return md5;
    }

    @Override
    public String getSeedToPeerRatio() {
        return "";
    }

    @Override
    public String getShareRatio() {
        return "";
    }

    @Override
    public boolean isPartialDownload() {
        return false;
    }

    @Override
    public void updateDownloadManager(DownloadManager downloadManager) {

    }

    @Override
    public Date getDateCreated() {
        return dateCreated;
    }

    private void start(final boolean resume) {
        state = STATE_WAITING;

        saveFile = completeFile;

        HTTP_THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File expectedFile = new File(SharingSettings.TORRENT_DATA_DIR_SETTING.getValue(), saveAs);
                    if (md5 != null &&
                        expectedFile.length() == size &&
                        checkMD5(expectedFile)) {
                        saveFile = expectedFile;
                        bytesReceived = expectedFile.length();
                        state = STATE_FINISHED;
                        onComplete();
                        return;
                    }

                    if (resume) {
                        if (incompleteFile.exists()) {
                            bytesReceived = incompleteFile.length();
                        }
                    }

                    httpClient.save(url, incompleteFile, resume);
                } catch (IOException e) {
                    e.printStackTrace();
                    httpClientListener.onError(httpClient, e);
                }
            }
        });
    }

    private void cleanupFile(File f) {
        if (f.exists()) {
            boolean delete = f.delete();
            if (!delete) {
                f.deleteOnExit();
            }
        }
    }

    private void cleanupIncomplete() {
        cleanupFile(incompleteFile);
    }

    private void cleanupComplete() {
        cleanupFile(completeFile);
    }

    private boolean checkMD5(File file) {
        state = STATE_CHECKING;
        md5CheckingProgress = 0;
        return file.exists() && DigestUtils.checkMD5(file, md5, new DigestProgressListener() {

            @Override
            public void onProgress(int progressPercentage) {
                md5CheckingProgress = progressPercentage;
            }

            @Override
            public boolean stopDigesting() {
                return httpClient.isCanceled();
            }
        });
    }

    /** files are saved with (1), (2),... if there's one with the same name already. */
    private static File buildFile(File savePath, String name) {
        String baseName = FilenameUtils.getBaseName(name);
        String ext = FilenameUtils.getExtension(name);

        File f = new File(savePath, name);
        int i = 1;
        while (f.exists() && i < Integer.MAX_VALUE) {
            f = new File(savePath, baseName + " (" + i + ")." + ext);
            i++;
        }
        return f;
    }

    private static File getIncompleteFolder() {
        File incompleteFolder = new File(SharingSettings.TORRENT_DATA_DIR_SETTING.getValue().getParentFile(), "Incomplete");
        if (!incompleteFolder.exists()) {
            incompleteFolder.mkdirs();
        }
        return incompleteFolder;
    }

    private static File buildIncompleteFile(File file) {
        String prefix = FilenameUtils.getBaseName(file.getName());
        String ext = FilenameUtils.getExtension(file.getAbsolutePath());
        return new File(getIncompleteFolder(), prefix + ".incomplete." + ext);
    }

    public boolean isComplete() {
        if (bytesReceived > 0) {
            return bytesReceived == size || state == STATE_FINISHED;
        } else {
            return false;
        }
    }

    private void updateAverageDownloadSpeed() {
        long now = System.currentTimeMillis();

        if (isComplete()) {
            averageSpeed = 0;
            speedMarkTimestamp = now;
            totalReceivedSinceLastSpeedStamp = 0;
        } else if (now - speedMarkTimestamp > SPEED_AVERAGE_CALCULATION_INTERVAL_MILLISECONDS) {
            averageSpeed = ((bytesReceived - totalReceivedSinceLastSpeedStamp) * 1000) / (now - speedMarkTimestamp);
            speedMarkTimestamp = now;
            totalReceivedSinceLastSpeedStamp = bytesReceived;
        }
    }

    private final class HttpDownloadListenerImpl implements HttpClientListener {
        @Override
        public void onError(HttpClient client, Exception e) {
            if (e instanceof RangeNotSupportedException) {
                isResumable = false;
                start(false);
            } else {
                state = STATE_ERROR;
                cleanup();
            }
        }

        @Override
        public void onData(HttpClient client, byte[] buffer, int offset, int length) {
            if (!state.equals(STATE_PAUSING) && !state.equals(STATE_CANCELING)) {
                bytesReceived += length;
                updateAverageDownloadSpeed();
                state = STATE_DOWNLOADING;
            }
        }

        @Override
        public void onComplete(HttpClient client) {
            if (md5 != null && !checkMD5(incompleteFile)) {
                state = STATE_ERROR_MD5;
                cleanupIncomplete();
                return;
            }

            boolean renameTo = incompleteFile.renameTo(completeFile);

            if (!renameTo) {
                state = STATE_ERROR_MOVING_INCOMPLETE;
            } else {
                state = STATE_FINISHED;
                cleanupIncomplete();
                HttpDownload.this.onComplete();
            }
        }

        @Override
        public void onCancel(HttpClient client) {
            if (state.equals(STATE_CANCELING)) {
                if (deleteDataWhenCancelled) {
                    cleanup();
                }
                state = STATE_CANCELED;
            } else if (state.equals(STATE_PAUSING)) {
                state = STATE_PAUSED;
            } else {
                state = STATE_CANCELED;
            }
        }

        @Override
        public void onHeaders(HttpClient httpClient, Map<String, List<String>> headerFields) {
            if (headerFields.containsKey("Accept-Ranges")) {
                isResumable = headerFields.get("Accept-Ranges").contains("bytes");
            } else if (headerFields.containsKey("Content-Range")) {
                isResumable = true;
            } else {
                isResumable = false;
            }
            
            //try figuring out file size from HTTP headers depending on the response.
            if (size < 0) {
                String responseCodeStr = headerFields.get(null).get(0);
                
                if (responseCodeStr.contains(String.valueOf(HttpURLConnection.HTTP_OK))) {
                    if (headerFields.containsKey("Content-Length")) {
                        try {
                            size = Long.valueOf(headerFields.get("Content-Length").get(0));
                        } catch (Exception e) {}
                    }
                } 
            } 
        }
    }

    /** Meant to be overwritten by children classes that want to do something special
     * after the download is completed. */
    protected void onComplete() {

    }

    @Override
    public void setDeleteTorrentWhenRemove(boolean deleteTorrentWhenRemove) {
    }

    @Override
    public void setDeleteDataWhenRemove(boolean deleteDataWhenRemove) {

    }

    @Override
    public PaymentOptions getPaymentOptions() {
        return null;
    }

    @Override
    public CopyrightLicenseBroker getCopyrightLicenseBroker() {
        return null;
    }
}