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
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

final class TOTorrentCreatorImpl implements TOTorrentCreator {

    private final File torrent_base;
    private final URI announce_url;
    private final boolean add_other_hashes;
    private final long piece_length;
    private final long piece_min_size;
    private final long piece_max_size;
    private final long piece_num_lower;
    private final long piece_num_upper;
    private final TOTorrentProgressListener listener;

    public TOTorrentCreatorImpl(File _torrent_base) {
        this(_torrent_base, null, false, 0);
    }

    public TOTorrentCreatorImpl(File _torrent_base, URI _announce_url, boolean _add_other_hashes, long _piece_length) {
        this(_torrent_base, _announce_url, _add_other_hashes, _piece_length, 0, 0, 0, 0, null);
    }

    public TOTorrentCreatorImpl(File _torrent_base, URI _announce_url, boolean _add_other_hashes, long piece_length, long _piece_min_size, long _piece_max_size, long _piece_num_lower, long _piece_num_upper, TOTorrentProgressListener listener) {
        torrent_base = _torrent_base;
        announce_url = _announce_url;
        add_other_hashes = _add_other_hashes;
        this.piece_length = piece_length;
        piece_min_size = _piece_min_size;
        piece_max_size = _piece_max_size;
        piece_num_lower = _piece_num_lower;
        piece_num_upper = _piece_num_upper;
        this.listener = listener;
    }

    public TOTorrent create() throws TOTorrentException {
        if (announce_url == null) {
            throw new TOTorrentException("Skeleton creator", TOTorrentException.RT_WRITE_FAILS);
        }

        File base_to_use = torrent_base;

        Map<String, File> linkage_map = new HashMap<String, File>();
        TOTorrentCreateImpl torrent;

        if (piece_length > 0) {
            torrent = new TOTorrentCreateImpl(linkage_map, base_to_use, announce_url, add_other_hashes, piece_length);
        } else {
            torrent = new TOTorrentCreateImpl(linkage_map, base_to_use, announce_url, add_other_hashes, piece_min_size, piece_max_size, piece_num_lower, piece_num_upper);
        }

        torrent.addListener(listener);

        torrent.create();

        return torrent;
    }

    public long getTorrentDataSizeFromFileOrDir() {
        return getTorrentDataSizeFromFileOrDir(torrent_base);
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
}
