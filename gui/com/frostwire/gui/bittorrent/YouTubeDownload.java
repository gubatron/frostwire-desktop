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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.io.FilenameUtils;
import org.gudy.azureus2.core3.download.DownloadManager;

import com.frostwire.search.extractors.YouTubeExtractor.LinkInfo;
import com.frostwire.search.youtube.YouTubeCrawledSearchResult;
import com.frostwire.util.HttpClient;
import com.frostwire.util.HttpClient.HttpClientListener;
import com.frostwire.util.HttpClientFactory;
import com.frostwire.util.HttpClientType;
import com.frostwire.util.MP4Muxer;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.SharingSettings;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class YouTubeDownload implements BTDownload {

    private static final Executor YOUTUBE_THREAD_POOL = Executors.newFixedThreadPool(6);

    private static final String STATE_DOWNLOADING = I18n.tr("Downloading");
    private static final String STATE_ERROR = I18n.tr("Error");
    private static final String STATE_ERROR_MOVING_INCOMPLETE = I18n.tr("Error - can't save");
    private static final String STATE_PAUSING = I18n.tr("Pausing");
    private static final String STATE_PAUSED = I18n.tr("Paused");
    private static final String STATE_CANCELING = I18n.tr("Canceling");
    private static final String STATE_CANCELED = I18n.tr("Canceled");
    private static final String STATE_WAITING = I18n.tr("Waiting");
    private static final String STATE_FINISHED = I18n.tr("Finished");

    private static final int SPEED_AVERAGE_CALCULATION_INTERVAL_MILLISECONDS = 1000;

    private final YouTubeCrawledSearchResult sr;
    private final DownloadType downloadType;

    private final File completeFile;
    private final File tempVideo;
    private final File tempAudio;

    private final HttpClient httpClient;
    private final HttpClientListener httpClientListener;
    private final Date dateCreated;

    private final long size;
    private long bytesReceived;
    private String state;
    private long averageSpeed; // in bytes

    // variables to keep the download rate of file transfer
    private long speedMarkTimestamp;
    private long totalReceivedSinceLastSpeedStamp;

    public YouTubeDownload(YouTubeCrawledSearchResult sr) {
        this.sr = sr;
        this.downloadType = buildDownloadType(sr);
        this.size = sr.getSize();

        String filename = sr.getFilename();

        completeFile = buildFile(SharingSettings.TORRENT_DATA_DIR_SETTING.getValue(), filename);
        tempVideo = buildTempFile(FilenameUtils.getBaseName(filename), "video");
        tempAudio = buildTempFile(FilenameUtils.getBaseName(filename), "audio");

        bytesReceived = 0;
        dateCreated = new Date();

        httpClientListener = new HttpDownloadListenerImpl();

        httpClient = HttpClientFactory.newInstance(HttpClientType.PureJava);
        httpClient.setListener(httpClientListener);

        start();
    }

    private DownloadType buildDownloadType(YouTubeCrawledSearchResult sr) {
        DownloadType dt;

        if (sr.getVideo() != null && sr.getAudio() == null) {
            dt = DownloadType.VIDEO;
        } else if (sr.getVideo() != null && sr.getAudio() != null) {
            dt = DownloadType.DASH;
        } else if (sr.getVideo() == null && sr.getAudio() != null) {
            dt = DownloadType.DEMUX;
        } else {
            throw new IllegalArgumentException("Not track specified");
        }

        return dt;
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
        return sr.getDisplayName();
    }

    @Override
    public boolean isResumable() {
        return false;
    }

    @Override
    public boolean isPausable() {
        return false;
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
        state = STATE_CANCELING;
        httpClient.cancel();
    }

    @Override
    public File getSaveLocation() {
        return completeFile;
    }

    @Override
    public void resume() {
        start();
    }

    @Override
    public int getProgress() {
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
        return false;
    }

    @Override
    public String getHash() {
        return null;
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

    private void start() {
        if (downloadType == DownloadType.DEMUX) {
            start(sr.getAudio(), tempAudio);
        } else {
            start(sr.getVideo(), tempVideo);
        }
    }

    private void start(final LinkInfo inf, final File temp) {
        state = STATE_WAITING;

        YOUTUBE_THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    httpClient.save(inf.link, temp, false);
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
        cleanupFile(tempVideo);
        cleanupFile(tempAudio);
    }

    private void cleanupComplete() {
        cleanupFile(completeFile);
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

    private static File buildTempFile(String name, String ext) {
        return new File(getIncompleteFolder(), name + "." + ext);
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
            state = STATE_ERROR;
            cleanup();
        }

        @Override
        public void onData(HttpClient client, byte[] buffer, int offset, int length) {
            if (state != STATE_PAUSING && state != STATE_CANCELING) {
                bytesReceived += length;
                updateAverageDownloadSpeed();
                state = STATE_DOWNLOADING;
            }
        }

        @Override
        public void onComplete(HttpClient client) {
            if (downloadType == DownloadType.VIDEO) {
                boolean renameTo = tempVideo.renameTo(completeFile);

                if (!renameTo) {
                    state = STATE_ERROR_MOVING_INCOMPLETE;
                } else {
                    state = STATE_FINISHED;
                    cleanupIncomplete();
                }
            } else if (downloadType == DownloadType.DEMUX) {
                try {
                    new MP4Muxer().demuxAudio(tempAudio.getAbsolutePath(), completeFile.getAbsolutePath());

                    if (!completeFile.exists()) {
                        state = STATE_ERROR_MOVING_INCOMPLETE;
                    } else {
                        state = STATE_FINISHED;
                        cleanupIncomplete();
                    }

                } catch (Exception e) {
                    state = STATE_ERROR_MOVING_INCOMPLETE;
                    cleanupIncomplete();
                }
            } else if (downloadType == DownloadType.DASH) {
                if (tempVideo.exists() && !tempAudio.exists()) {
                    start(sr.getAudio(), tempAudio);
                } else if (tempVideo.exists() && tempAudio.exists()) {
                    try {
                        new MP4Muxer().mux(tempVideo.getAbsolutePath(), tempAudio.getAbsolutePath(), completeFile.getAbsolutePath());

                        if (!completeFile.exists()) {
                            state = STATE_ERROR_MOVING_INCOMPLETE;
                        } else {
                            state = STATE_FINISHED;
                            cleanupIncomplete();
                        }

                    } catch (Exception e) {
                        state = STATE_ERROR_MOVING_INCOMPLETE;
                        cleanupIncomplete();
                    }
                } else {
                    state = STATE_ERROR_MOVING_INCOMPLETE;
                    cleanupIncomplete();
                }
            } else {
                // warning!!! if this point is reached review the logic
                state = STATE_ERROR_MOVING_INCOMPLETE;
                cleanupIncomplete();
            }
        }

        @Override
        public void onCancel(HttpClient client) {
            if (state == STATE_CANCELING) {
                cleanup();
                state = STATE_CANCELED;
            } else if (state == STATE_PAUSING) {
                state = STATE_PAUSED;
            } else {
                state = STATE_CANCELED;
            }
        }

        @Override
        public void onHeaders(HttpClient httpClient, Map<String, List<String>> headerFields) {
        }
    }

    @Override
    public void setDeleteTorrentWhenRemove(boolean deleteTorrentWhenRemove) {
    }

    @Override
    public void setDeleteDataWhenRemove(boolean deleteDataWhenRemove) {
    }

    private static enum DownloadType {
        VIDEO, DASH, DEMUX
    }
}