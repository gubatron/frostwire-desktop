/*
 * Created on 07-Nov-2004
 * Created by Paul Gardner
 * Copyright (C) 2004, 2005, 2006 Aelitis, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * AELITIS, SAS au capital de 46,603.30 euros
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 *
 */

package com.frostwire.torrent;

/**
 * @author parg
 *
 */

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TOTorrentCreatorImpl implements TOTorrentCreator {

    private File torrent_base;
    private URL announce_url;
    private boolean add_other_hashes;
    private long piece_length;
    private long piece_min_size;
    private long piece_max_size;
    private long piece_num_lower;
    private long piece_num_upper;

    private Map<String, File> linkage_map = new HashMap<String, File>();
    private TOTorrentCreateImpl torrent;

    private List<TOTorrentProgressListener> listeners = new ArrayList<TOTorrentProgressListener>();

    public TOTorrentCreatorImpl(File _torrent_base) {
        torrent_base = _torrent_base;
    }

    public TOTorrentCreatorImpl(File _torrent_base, URL _announce_url, boolean _add_other_hashes, long _piece_length) {
        torrent_base = _torrent_base;
        announce_url = _announce_url;
        add_other_hashes = _add_other_hashes;
        piece_length = _piece_length;
    }

    public TOTorrentCreatorImpl(File _torrent_base, URL _announce_url, boolean _add_other_hashes, long _piece_min_size, long _piece_max_size, long _piece_num_lower, long _piece_num_upper) {
        torrent_base = _torrent_base;
        announce_url = _announce_url;
        add_other_hashes = _add_other_hashes;
        piece_min_size = _piece_min_size;
        piece_max_size = _piece_max_size;
        piece_num_lower = _piece_num_lower;
        piece_num_upper = _piece_num_upper;
    }

    public TOTorrent create() throws TOTorrentException {
        if (announce_url == null) {
            throw (new TOTorrentException("Skeleton creator", TOTorrentException.RT_WRITE_FAILS));
        }

        File base_to_use;

        base_to_use = torrent_base;

        if (piece_length > 0) {
            torrent = new TOTorrentCreateImpl(linkage_map, base_to_use, announce_url, add_other_hashes, piece_length);
        } else {
            torrent = new TOTorrentCreateImpl(linkage_map, base_to_use, announce_url, add_other_hashes, piece_min_size, piece_max_size, piece_num_lower, piece_num_upper);
        }

        for (TOTorrentProgressListener l : listeners) {
            torrent.addListener(l);
        }

        torrent.create();

        return (torrent);
    }

    public long getTorrentDataSizeFromFileOrDir() {
        return (getTorrentDataSizeFromFileOrDir(torrent_base));
    }

    private long getTorrentDataSizeFromFileOrDir(File file) {
        String name = file.getName();

        if (name.equals(".") || name.equals("..")) {
            return (0);
        }

        if (!file.exists()) {
            return (0);
        }

        if (file.isFile()) {
            return (file.length());
        } else {

            File[] dir_files = file.listFiles();

            long length = 0;

            for (int i = 0; i < dir_files.length; i++) {

                length += getTorrentDataSizeFromFileOrDir(dir_files[i]);
            }

            return (length);
        }
    }

    public void cancel() {
        if (torrent != null) {
            torrent.cancel();
        }
    }

    public void addListener(TOTorrentProgressListener listener) {
        if (torrent == null) {

            listeners.add(listener);

        } else {

            torrent.addListener(listener);
        }
    }

    public void removeListener(TOTorrentProgressListener listener) {
        if (torrent == null) {

            listeners.remove(listener);

        } else {

            torrent.removeListener(listener);
        }
    }
}
