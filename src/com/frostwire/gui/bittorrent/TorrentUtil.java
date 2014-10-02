/*
 * Created on 9 Jul 2007
 * Created by Allan Crooks
 * Copyright (C) 2007 Aelitis, All Rights Reserved.
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
 */
/*
 * File    : ManagerUtils.java
 * Created : 7 d�c. 2003}
 * By      : Olivier
 *
 * Copyright (C) 2004, 2005, 2006 Aelitis SAS, All rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * AELITIS, SAS au capital de 46,603.30 euros,
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 */
package com.frostwire.gui.bittorrent;

import com.frostwire.logging.Logger;
import com.frostwire.transfers.TransferItem;
import com.frostwire.util.StringUtils;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentAnnounceURLGroup;
import org.gudy.azureus2.core3.torrent.TOTorrentAnnounceURLSet;
import org.gudy.azureus2.core3.util.UrlUtils;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TorrentUtil {

    private static final Logger LOG = Logger.getLogger(TorrentUtil.class);

    public static Set<File> getSkipedFiles() {
        Set<File> set = new HashSet<File>();
        List<BTDownload> downloads = BTDownloadMediator.instance().getDownloads();

        for (BTDownload d : downloads) {
            if (d instanceof BittorrentDownload) {
                set.addAll(getSkippedFiles(((BittorrentDownload) d).getDl()));
            }
        }

        return set;
    }

    public static Set<File> getSkippedFiles(com.frostwire.bittorrent.BTDownload dm) {
        Set<File> set = new HashSet<File>();
        List<TransferItem> infoSet = dm.getItems();
        for (TransferItem fileInfo : infoSet) {
            try {
                if (fileInfo.isSkipped()) {
                    set.add(fileInfo.getFile());
                }
            } catch (Throwable e) {
                LOG.error("Error getting file information", e);
            }
        }
        return set;
    }

    public static BittorrentDownload getDownloadManager(File f) {
        List<BTDownload> downloads = BTDownloadMediator.instance().getDownloads();
        for (BTDownload d : downloads) {
            if (d instanceof BittorrentDownload) {
                BittorrentDownload bt = (BittorrentDownload) d;
                com.frostwire.bittorrent.BTDownload dl = bt.getDl();

                List<TransferItem> items = dl.getItems();

                for (TransferItem item : items) {
                    if (f.equals(item.getFile())) {
                        return bt;
                    }
                }
            }
        }

        return null;
    }

    public static Set<File> getIncompleteFiles() {
        Set<File> set = new HashSet<File>();

        List<BTDownload> downloads = BTDownloadMediator.instance().getDownloads();
        for (BTDownload d : downloads) {
            if (d instanceof BittorrentDownload) {
                BittorrentDownload bt = (BittorrentDownload) d;
                com.frostwire.bittorrent.BTDownload dl = bt.getDl();

                List<TransferItem> infoSet = dl.getItems();
                for (TransferItem fileInfo : infoSet) {
                    try {
                        if (getDownloadPercent(fileInfo) < 100) {
                            set.add(fileInfo.getFile());
                        }
                    } catch (Throwable e) {
                        LOG.error("Error getting file information", e);
                    }
                }
            }
        }

        return set;
    }

    public static int getDownloadPercent(TransferItem fileInfo) {
        long length = fileInfo.getSize();
        if (length == 0 || fileInfo.getDownloaded() == length) {
            return 100;
        } else {
            return (int) (fileInfo.getDownloaded() * 100 / length);
        }
    }

    public static String getMagnet(String hash) {
        return "magnet:?xt=urn:btih:" + hash;
    }

    public static String getMagnetURLParameters(TOTorrent torrent) {
        StringBuilder sb = new StringBuilder();
        //dn
        if (StringUtils.isNullOrEmpty(torrent.getUTF8Name())) {
            sb.append("dn=" + UrlUtils.encode(new String(torrent.getName())));
        } else {
            sb.append("dn=" + UrlUtils.encode(torrent.getUTF8Name()));
        }

        TOTorrentAnnounceURLGroup announceURLGroup = torrent.getAnnounceURLGroup();
        TOTorrentAnnounceURLSet[] announceURLSets = announceURLGroup.getAnnounceURLSets();

        for (TOTorrentAnnounceURLSet set : announceURLSets) {
            URL[] announceURLs = set.getAnnounceURLs();
            for (URL url : announceURLs) {
                sb.append("&tr=");
                sb.append(UrlUtils.encode(url.toString()));
            }
        }

        if (torrent.getAnnounceURL() != null) {
            sb.append("&tr=");
            sb.append(UrlUtils.encode(torrent.getAnnounceURL().toString()));
        }

        //iipp = internal ip port, for lan
        /*
        try {
            String localAddress = NetworkUtils.getLocalAddress().getHostAddress();
            int localPort = TCPNetworkManager.getSingleton().getTCPListeningPortNumber();

            if (localPort != -1) {
                sb.append("&iipp=");
                sb.append(NetworkUtils.convertIPPortToHex(localAddress, localPort));
            }

        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */

        return sb.toString();
    }

    public static String hashToString(byte[] hash) {
        String hex = "";
        for (int i = 0; i < hash.length; i++) {
            String t = Integer.toHexString(hash[i] & 0xFF);
            if (t.length() < 2) {
                t = "0" + t;
            }
            hex += t;
        }

        return hex;
    }

    public static Set<File> getIgnorableFiles() {
        Set<File> set = TorrentUtil.getIncompleteFiles();
        set.addAll(TorrentUtil.getSkipedFiles());
        return set;
    }
}
