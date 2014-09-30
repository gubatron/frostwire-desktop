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

import com.frostwire.bittorrent.BTDownloadItem;
import com.frostwire.bittorrent.BTEngine;
import com.frostwire.bittorrent.BTEngineFactory;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.transfers.TransferItem;
import com.limegroup.gnutella.settings.SharingSettings;
import org.gudy.azureus2.core3.torrent.TOTorrentException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author gubatron
 * @author aldenml
 */
public final class BTDownloadCreator {

    public static BTDownload createDownload(File torrentFile, boolean[] filesSelection, File saveDir) throws IOException {
        BTEngine engine = BTEngineFactory.getInstance();

        if (saveDir == null) {
            saveDir = SharingSettings.TORRENT_DATA_DIR_SETTING.getValue();
        }

        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        TorrentInfo tinfo = new TorrentInfo(torrentFile);
        String hash = tinfo.getHash();

        BittorrentDownload bittorrentDownload = TorrentUtil.getDownloadManager(hash);

        if (bittorrentDownload == null) {

            //Complete torrent download.
            if (filesSelection == null) {
                engine.download(torrentFile, saveDir);
            } else {
                //Partial torrent download.
                engine.download(torrentFile, saveDir, filesSelection);
            }
        } else { //the download manager was there...
            if (filesSelection != null) { //I want to download partial files.
                boolean[] prevSelection = getPreviousFileSelections(bittorrentDownload.getDl());

                if (prevSelection.length != filesSelection.length) {
                    //wtf
                    System.out.println("BTDownloadCreator::constructor warning: inconsistency between file selection count, from old state to new state. Is this the same torrent?");
                    return bittorrentDownload;
                }

                //he was already downloading the whole torrent, you'll get the file eventually when it finishes.
                if (isDownloadingEntireContents(prevSelection)) {
                    return bittorrentDownload;
                }

                //let the new _fileSelection know about the older files that were selected for download
                //(union)
                for (int i = 0; i < filesSelection.length; i++) {
                    if (prevSelection[i]) {
                        filesSelection[i] = true;
                    }
                }

            } else { // I want to download the whole thing
                boolean[] prevSelection = getPreviousFileSelections(bittorrentDownload.getDl());
                if (isDownloadingEntireContents(prevSelection)) {
                    //oh, it was already downloading the whole thing
                    return bittorrentDownload;
                }

                filesSelection = prevSelection;
                for (int i = 0; i < filesSelection.length; i++) {
                    filesSelection[i] = true;
                }

            }

            bittorrentDownload.getDl().setFilesSelection(filesSelection);
            bittorrentDownload.refresh();
        }

        return bittorrentDownload;
    }

    public static BTDownload createDownload(File torrentFile, boolean[] filesSelection) throws IOException {
        return createDownload(torrentFile, filesSelection, null);
    }

    public static BTDownload createDownload(File torrentFile, File saveDir) throws IOException {
        return createDownload(torrentFile, null, saveDir);
    }

    public static BTDownload createDownload(File torrentFile) throws TOTorrentException, IOException {
        return createDownload(torrentFile, null, null);
    }

    private static boolean isDownloadingEntireContents(boolean[] prevSelection) {
        for (int i = 0; i < prevSelection.length; i++) {
            if (!prevSelection[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean[] getPreviousFileSelections(com.frostwire.bittorrent.BTDownload dm) {
        List<TransferItem> items = dm.getItems();

        boolean[] fileSelections = new boolean[items.size()];
        for (int i = 0; i < items.size(); i++) {
            fileSelections[i] = !items.get(i).isSkipped();
        }

        return fileSelections;
    }

    public static BTDownload modifyDownload(String hash, String relativePath) {
        BittorrentDownload bittorrentDownload = TorrentUtil.getDownloadManager(hash);

        if (bittorrentDownload != null) {
            boolean[] selection = getPreviousFileSelections(bittorrentDownload.getDl());
            List<TransferItem> items = bittorrentDownload.getDl().getItems();
            for (int i = 0; i < items.size(); i++) {
                BTDownloadItem item = (BTDownloadItem) items.get(i);
                if (item.getFile().getAbsolutePath().contains(relativePath)) {
                    selection[item.getIndex()] = true;
                }
            }

            bittorrentDownload.getDl().setFilesSelection(selection);
        }

        return bittorrentDownload;
    }
}
