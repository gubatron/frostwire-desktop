/*
 * Created by  Alden Torres (aldenml)
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

package org.gudy.azureus2.core3.torrent.impl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentAnnounceURLGroup;
import org.gudy.azureus2.core3.torrent.TOTorrentAnnounceURLSet;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.torrent.TOTorrentListener;
import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.core3.util.BEncoder;
import org.gudy.azureus2.core3.util.ByteFormatter;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.DirectByteBuffer;
import org.gudy.azureus2.core3.util.FileUtil;
import org.gudy.azureus2.core3.util.HashWrapper;
import org.gudy.azureus2.core3.util.SHA1Hasher;
import org.gudy.azureus2.core3.util.TorrentUtils;

import com.aelitis.azureus.core.peermanager.messaging.MessagingUtil;

public class TOTorrentMetadata implements TOTorrent {

    private final byte[] hash;
    private final String displayName;
    private final String saveLocation;
    private final Map<Integer, byte[]> metadataPieces;

    private URL announceURL;
    private TOTorrentAnnounceURLGroup announceURLGroup;
    private List<TOTorrentAnnounceURLSet> announceURLSet;

    private byte[] infoBytes;

    public TOTorrentMetadata(byte[] hash, String displayName, URL[] trackers) {
        this.hash = hash;
        if (displayName != null) {
            this.displayName = displayName;
        } else {
            this.displayName = ByteFormatter.encodeString(hash);
        }
        String filename = "metadata_" + ByteFormatter.encodeString(hash) + ".torrent";
        this.saveLocation = new File(COConfigurationManager.getStringParameter("General_sDefaultTorrent_Directory"), filename).getAbsolutePath();
        this.metadataPieces = new HashMap<Integer, byte[]>();

        this.announceURL = trackers[0];
        setAnnounceUrlGroup(trackers);
    }

    public void setAnnounceUrlGroup(URL[] trackers) {
        this.announceURLSet = new ArrayList<TOTorrentAnnounceURLSet>();
        this.announceURLGroup = new TOTorrentAnnounceURLGroup() {

            @Override
            public TOTorrentAnnounceURLSet[] getAnnounceURLSets() {
                return announceURLSet.toArray(new TOTorrentAnnounceURLSet[0]);
            }

            @Override
            public void setAnnounceURLSets(TOTorrentAnnounceURLSet[] sets) {
                announceURLSet = Arrays.asList(sets);
            }

            @Override
            public TOTorrentAnnounceURLSet createAnnounceURLSet(final URL[] urls) {
                TOTorrentAnnounceURLSet s = new TOTorrentAnnounceURLSet() {
                    @Override
                    public URL[] getAnnounceURLs() {
                        return urls;
                    }

                    @Override
                    public void setAnnounceURLs(URL[] urls) {
                    }
                };
                announceURLSet.add(s);
                return s;
            }
        };
        this.announceURLGroup.createAnnounceURLSet(trackers);
    }

    @Override
    public void setPrivate(boolean _private) throws TOTorrentException {
    }

    @Override
    public void setPieces(byte[][] pieces) throws TOTorrentException {
    }

    @Override
    public void setHashOverride(byte[] hash) throws TOTorrentException {
    }

    @Override
    public void setCreationDate(long date) {
    }

    @Override
    public void setCreatedBy(byte[] cb) {
    }

    @Override
    public void setComment(String comment) {
    }

    @Override
    public boolean setAnnounceURL(URL url) {
        this.announceURL = url;
        return true;
    }

    @Override
    public void setAdditionalStringProperty(String name, String value) {
    }

    @Override
    public void setAdditionalProperty(String name, Object value) {
    }

    @Override
    public void setAdditionalMapProperty(String name, Map value) {
    }

    @Override
    public void setAdditionalLongProperty(String name, Long value) {
    }

    @Override
    public void setAdditionalListProperty(String name, List value) {
    }

    @Override
    public void setAdditionalByteArrayProperty(String name, byte[] value) {
    }

    @Override
    public void serialiseToXMLFile(File file) throws TOTorrentException {
    }

    @Override
    public Map serialiseToMap() throws TOTorrentException {
        try {
            Integer[] keys = metadataPieces.keySet().toArray(new Integer[0]);
            Arrays.sort(keys);

            ByteArrayOutputStream infoStream = new ByteArrayOutputStream();
            for (Integer p : keys) {
                byte[] pieceMetadata = metadataPieces.get(p);
                infoStream.write(pieceMetadata);
            }
            infoStream.close();

            DirectByteBuffer buffer = new DirectByteBuffer(ByteBuffer.wrap(infoStream.toByteArray()));

            Map map = new HashMap();
            map.put("info", MessagingUtil.convertBencodedByteStreamToPayload(buffer, 2, "info"));
            map.put("announce", getAnnounceURL().toString().getBytes(Constants.DEFAULT_ENCODING));

            TOTorrentAnnounceURLSet[] sets = getAnnounceURLGroup().getAnnounceURLSets();
            if (sets.length > 0) {
                List announce_list = new ArrayList();

                for (int i = 0; i < sets.length; i++) {
                    TOTorrentAnnounceURLSet set = sets[i];
                    URL[] urls = set.getAnnounceURLs();

                    if (urls.length == 0) {
                        continue;
                    }

                    List sub_list = new ArrayList();
                    announce_list.add(sub_list);

                    for (int j = 0; j < urls.length; j++) {
                        sub_list.add(urls[j].toString().getBytes(Constants.DEFAULT_ENCODING));
                    }
                }

                if (announce_list.size() > 0) {
                    map.put("announce-list", announce_list);
                }
            }

            return map;
        } catch (Throwable e) {
            throw new TOTorrentException("Error serialising torrent", TOTorrentException.RT_WRITE_FAILS, e);
        }
    }

    @Override
    public void serialiseToBEncodedFile(File output_file) throws TOTorrentException {
        byte[] res = serialiseToByteArray();

        BufferedOutputStream bos = null;

        try {
            File parent = output_file.getParentFile();
            if (parent == null) {
                throw new TOTorrentException("Path '" + output_file + "' is invalid", TOTorrentException.RT_WRITE_FAILS);
            }

            // We would expect this to be normally true most of the time.
            if (!parent.isDirectory()) {

                // Try to create a directory.
                boolean dir_created = FileUtil.mkdirs(parent);

                // Something strange going on...
                if (!dir_created) {

                    // Does it exist already?
                    if (parent.exists()) {

                        // And it really isn't a directory?
                        if (!parent.isDirectory()) {

                            // How strange.
                            throw new TOTorrentException("Path '" + output_file + "' is invalid", TOTorrentException.RT_WRITE_FAILS);

                        }

                        // It is a directory which does exist. But we tested for that earlier. Perhaps it has been created in the
                        // meantime.
                        else {
                            /* do nothing */
                        }
                    }

                    // It doesn't exist, and we couldn't create it.
                    else {
                        throw new TOTorrentException("Failed to create directory '" + parent + "'", TOTorrentException.RT_WRITE_FAILS);
                    }
                } // end if (!dir_created)

            } // end if (!parent.isDirectory)

            File temp = new File(parent, output_file.getName() + ".saving");

            if (temp.exists()) {

                if (!temp.delete()) {

                    throw (new TOTorrentException("Insufficient permissions to delete '" + temp + "'", TOTorrentException.RT_WRITE_FAILS));
                }
            } else {

                boolean ok = false;

                try {
                    ok = temp.createNewFile();

                } catch (Throwable e) {
                }

                if (!ok) {

                    throw (new TOTorrentException("Insufficient permissions to write '" + temp + "'", TOTorrentException.RT_WRITE_FAILS));

                }
            }

            FileOutputStream fos = new FileOutputStream(temp, false);

            bos = new BufferedOutputStream(fos, 8192);

            bos.write(res);

            bos.flush();

            // thinking about removing this - just do so for CVS for the moment

            if (!Constants.isCVSVersion()) {

                fos.getFD().sync();
            }

            bos.close();

            bos = null;

            //only use newly saved file if it got this far, i.e. it was written successfully

            if (temp.length() > 1L) {
                output_file.delete(); // Will fail silently if it doesn't exist.
                temp.renameTo(output_file);
            }

        } catch (TOTorrentException e) {

            throw (e);

        } catch (Throwable e) {

            throw (new TOTorrentException("Failed to serialise torrent: " + Debug.getNestedExceptionMessage(e), TOTorrentException.RT_WRITE_FAILS));

        } finally {

            if (bos != null) {

                try {
                    bos.close();

                } catch (IOException e) {

                    Debug.printStackTrace(e);
                }
            }
        }
    }

    @Override
    public void removeListener(TOTorrentListener l) {
    }

    @Override
    public void removeAdditionalProperty(String name) {
    }

    @Override
    public void removeAdditionalProperties() {
    }

    @Override
    public void print() {
    }

    @Override
    public boolean isSimpleTorrent() {
        return false;
    }

    @Override
    public boolean isCreated() {
        return false;
    }

    @Override
    public boolean hasSameHashAs(TOTorrent other) {
        try {
            return Arrays.equals(hash, other.getHash());
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public String getUTF8Name() {
        return displayName;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public boolean getPrivate() {
        return false;
    }

    @Override
    public byte[][] getPieces() throws TOTorrentException {
        return null;
    }

    @Override
    public long getPieceLength() {
        return 0;
    }

    @Override
    public int getNumberOfPieces() {
        return 0;
    }

    @Override
    public byte[] getName() {
        return getUTF8Name().getBytes();
    }

    @Override
    public AEMonitor getMonitor() {
        return null;
    }

    @Override
    public HashWrapper getHashWrapper() throws TOTorrentException {
        return new HashWrapper(hash);
    }

    @Override
    public byte[] getHash() throws TOTorrentException {
        return hash;
    }

    @Override
    public TOTorrentFile[] getFiles() {
        return null;
    }

    @Override
    public long getCreationDate() {
        return 0;
    }

    @Override
    public byte[] getCreatedBy() {
        return null;
    }

    @Override
    public byte[] getComment() {
        return null;
    }

    @Override
    public TOTorrentAnnounceURLGroup getAnnounceURLGroup() {
        return announceURLGroup;
    }

    @Override
    public URL getAnnounceURL() {
        return announceURL;
    }

    @Override
    public String getAdditionalStringProperty(String name) {
        return null;
    }

    @Override
    public Object getAdditionalProperty(String name) {
        return null;
    }

    @Override
    public Map getAdditionalMapProperty(String name) {
        return null;
    }

    @Override
    public Long getAdditionalLongProperty(String name) {
        return null;
    }

    @Override
    public List getAdditionalListProperty(String name) {
        return null;
    }

    @Override
    public byte[] getAdditionalByteArrayProperty(String name) {
        return null;
    }

    @Override
    public void addListener(TOTorrentListener l) {
    }

    public String getSaveLocation() {
        return saveLocation;
    }

    public void notifyComplete() {
    }

    public void updateMetadataPieces(Map<Integer, byte[]> metadataPieces, int totalSize) {
        for (Entry<Integer, byte[]> kv : metadataPieces.entrySet()) {
            if (!this.metadataPieces.containsKey(kv.getKey())) {
                this.metadataPieces.put(kv.getKey(), kv.getValue());
            }
        }

        int currentSize = 0;
        for (byte[] piece : this.metadataPieces.values()) {
            currentSize += piece.length;
        }

        if (currentSize == totalSize) {
            notifyComplete();
        }
    }

    public Map<Integer, byte[]> getMetadataPieces() {
        return metadataPieces;
    }

    public byte[] getInfoBytes() {
        if (infoBytes == null) {
            try {
                Map map = serialiseToMap();
                infoBytes = BEncoder.encode((Map) map.get("info"));
            } catch (Throwable e) {
                infoBytes = new byte[0];
            }
        }

        return infoBytes;
    }

    public boolean validHash() {
        SHA1Hasher sha1 = new SHA1Hasher();
        return Arrays.equals(hash, sha1.calculateHash(getInfoBytes()));
    }

    public boolean save() {
        try {
            serialiseToBEncodedFile(new File(getSaveLocation()));
            return true;
        } catch (TOTorrentException e) {
            return false;
        }
    }

    protected byte[] serialiseToByteArray() throws TOTorrentException {
        Map root = serialiseToMap();

        try {
            return BEncoder.encode(root);
        } catch (IOException e) {
            throw new TOTorrentException("Failed to serialise torrent: " + Debug.getNestedExceptionMessage(e), TOTorrentException.RT_WRITE_FAILS);
        }
    }

    public static class TorrentWrapper {

        private static final Map<String, byte[]> INFOS = new HashMap<String, byte[]>();

        private final TOTorrent torrent;
        private String key;

        public TorrentWrapper(TOTorrent torrent) {
            this.torrent = torrent;
            try {
                this.key = ByteFormatter.encodeString(torrent.getHash());
            } catch (TOTorrentException e) { // ignore for now
                this.key = null;
            }
        }

        public byte[] getInfoBytes() {
            if (!INFOS.containsKey(key)) {
                byte[] bytes = serializeInfo();
                INFOS.put(key, bytes);
            }

            return INFOS.get(key);
        }

        private byte[] serializeInfo() {
            byte[] bytes = null;

            try {
                Map map = torrent.serialiseToMap();

                Map info = (Map) map.get("info");

                bytes = BEncoder.encode(info);
            } catch (Throwable e) {
                // ignore
                bytes = new byte[0];
            }

            return bytes;
        }
    }
}
