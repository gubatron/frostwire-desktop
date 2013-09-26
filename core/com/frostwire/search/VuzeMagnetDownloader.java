/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2013, FrostWire(R). All rights reserved.
 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.search;

import java.io.File;
import java.io.FileInputStream;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloader;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderCallBackInterface;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderFactory;
import org.limewire.concurrent.ExecutorsHelper;

import com.limegroup.gnutella.settings.SharingSettings;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class VuzeMagnetDownloader implements MagnetDownloader {

    private final Random rnd;
    
    public VuzeMagnetDownloader() {
        rnd = new Random(System.currentTimeMillis());
    }

    public byte[] download(String magnet, int timeout) {

        CountDownLatch signal = new CountDownLatch(1);

        TorrentDownloaderListener listener = new TorrentDownloaderListener(signal);

        String saveFile = newSaveFile();

        TorrentDownloader td = TorrentDownloaderFactory.create(listener, magnet, null, saveFile);

        td.start();

        try {
            signal.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ignore
        }

        if (listener.getData() == null) {
            td.cancel();
        }

        new File(saveFile).delete(); // one more intent

        return listener.getData();
    }

    private String newSaveFile() {
        return new File(SharingSettings.TORRENTS_DIR_SETTING.getValue(), rnd.nextInt(Integer.MAX_VALUE) + ".dat").getAbsolutePath();
    }

    private final class TorrentDownloaderListener implements TorrentDownloaderCallBackInterface {

        private final AtomicBoolean finished = new AtomicBoolean(false);
        private final CountDownLatch signal;

        private byte[] data;

        public TorrentDownloaderListener(CountDownLatch signal) {
            this.signal = signal;
        }

        public byte[] getData() {
            return data;
        }

        public void TorrentDownloaderEvent(int state, final TorrentDownloader inf) {
            if (state == TorrentDownloader.STATE_FINISHED && finished.compareAndSet(false, true)) {
                File tempTorrent = inf.getFile();

                try {
                    data = IOUtils.toByteArray(new FileInputStream(tempTorrent));
                } catch (Throwable e) {
                    // ignore
                }
                cleanupTemporaryTorrent(tempTorrent, 5);   
            } else if (state == TorrentDownloader.STATE_ERROR) {
                signal.countDown();
            }
        }
        
        private void cleanupTemporaryTorrent(File torrent,final int attempts) {
        	int attempt = 0;
			while (torrent.exists() && attempt < attempts) {
				if (!torrent.delete()) {
					attempt++;
                	System.out.println("Warning! (attempt " + attempt + "/" + attempts + ") VuzeMagnetDownloader could not delete .torrent -> " + torrent.getAbsolutePath() + "...");
                	try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						
					}
                }
				//exists might not change since files are immutable.
				torrent = new File(torrent.getAbsolutePath());
			}
			
			if (torrent.exists()) {
				System.out.println("Warning! Mission failed, could not delete " + torrent.getAbsolutePath());
				torrent.deleteOnExit();
			} else {
				System.out.println("VuzeMagnetDownloader deleted temporary .torrent " + torrent.getAbsolutePath()); 
			}
			
			signal.countDown();
        }
    }
}
