/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
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

package com.frostwire.gui.library;

import java.util.List;

import com.frostwire.gui.player.DeviceMediaSource;
import com.frostwire.gui.player.InternetRadioAudioSource;
import com.frostwire.gui.player.MediaPlayer;
import com.frostwire.gui.player.MediaSource;
import com.frostwire.uxstats.UXAction;
import com.frostwire.uxstats.UXStats;
import com.limegroup.gnutella.gui.tables.AbstractActionsRenderer;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public final class LibraryActionsRenderer extends AbstractActionsRenderer {

    @Override
    protected void onPlayAction() {
        if (actionsHolder != null && actionsHolder.getDataLine() != null) {

            MediaSource mediaSource = null;
            List<MediaSource> filesView = null;
            boolean playNextSong = false;
            Object dataLine = actionsHolder.getDataLine();

            if (dataLine instanceof LibraryFilesTableDataLine) {
                mediaSource = new MediaSource(((LibraryFilesTableDataLine) dataLine).getFile());
                filesView = LibraryFilesTableMediator.instance().getFilesView();
                playNextSong = true;
            } else if (dataLine instanceof LibraryPlaylistsTableDataLine) {
                mediaSource = new MediaSource(((LibraryPlaylistsTableDataLine) dataLine).getPlayListItem());
                filesView = LibraryPlaylistsTableMediator.instance().getFilesView();
                playNextSong = true;
            } else if (dataLine instanceof LibraryInternetRadioTableDataLine) {
                LibraryInternetRadioTableDataLine irDataLine = (LibraryInternetRadioTableDataLine) dataLine;
                mediaSource = new InternetRadioAudioSource(irDataLine.getInitializeObject().getUrl(), irDataLine.getInitializeObject());
                filesView = LibraryInternetRadioTableMediator.instance().getFilesView();
                playNextSong = false;
            } else if (dataLine instanceof LibraryDeviceTableDataLine) {
                LibraryDeviceTableDataLine dl = (LibraryDeviceTableDataLine) dataLine;
                Device device = LibraryMediator.instance().getLibraryExplorer().getSelectedDeviceFiles();
                if (device != null) {
                    String url = device.getDownloadURL(dl.getInitializeObject());
                    mediaSource = new DeviceMediaSource(url, device, dl.getInitializeObject());
                    filesView = LibraryDeviceTableMediator.instance().getFilesView();
                    playNextSong = true;
                }
                UXStats.instance().log(UXAction.WIFI_SHARING_PREVIEW);
            }

            if (mediaSource != null && !actionsHolder.isPlaying()) {
                MediaPlayer.instance().asyncLoadMedia(mediaSource, true, playNextSong, null, filesView);
            }
        }        
    }

    @Override
    protected void onDownloadAction() {
        if (actionsHolder != null && actionsHolder.getDataLine() != null) {
            Object dataLine = actionsHolder.getDataLine();

            if (dataLine instanceof LibraryDeviceTableDataLine) {
                Device device = LibraryMediator.instance().getLibraryExplorer().getSelectedDeviceFiles();
                if (device != null) {
                    LibraryDeviceTableMediator.instance().downloadSelectedItems();
                    UXStats.instance().log(UXAction.WIFI_SHARING_DOWNLOAD);
                }
            }
        }
    }
}