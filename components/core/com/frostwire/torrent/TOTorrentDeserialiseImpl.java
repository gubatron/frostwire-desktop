/*
 * File    : TOTorrentDeserialiseImpl.java
 * Created : 5 Oct. 2003
 * By      : Parg 
 * 
 * Azureus - a Java Bittorrent client
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
 */

package com.frostwire.torrent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

final class TOTorrentDeserialiseImpl extends TOTorrentImpl {

    public TOTorrentDeserialiseImpl(InputStream is) throws TOTorrentException {
        construct(is);
    }

    public TOTorrentDeserialiseImpl(byte[] bytes) throws TOTorrentException {
        construct(bytes);
    }

    public TOTorrentDeserialiseImpl(Map<String, Object> map) throws TOTorrentException {
        construct(map);
    }

    protected void construct(InputStream is)

    throws TOTorrentException {
        ByteArrayOutputStream metaInfo = new ByteArrayOutputStream();

        try {
            byte[] buf = new byte[32 * 1024]; // raised this limit as 2k was rather too small

            // do a check to see if it's a BEncode file.
            int iFirstByte = is.read();

            if (iFirstByte != 'd' && iFirstByte != 'e' && iFirstByte != 'i' && !(iFirstByte >= '0' && iFirstByte <= '9')) {

                // often people download an HTML file by accident - if it looks like HTML
                // then produce a more informative error

                try {
                    metaInfo.write(iFirstByte);

                    int nbRead;

                    while ((nbRead = is.read(buf)) > 0 && metaInfo.size() < 32000) {

                        metaInfo.write(buf, 0, nbRead);
                    }

                    String char_data = new String(metaInfo.toByteArray());

                    if (char_data.toLowerCase().indexOf("html") != -1) {

                        char_data = HTMLUtils.convertHTMLToText2(char_data);

                        char_data = HTMLUtils.splitWithLineLength(char_data, 80);

                        if (char_data.length() > 400) {

                            char_data = char_data.substring(0, 400) + "...";
                        }

                        throw (new TOTorrentException("Contents maybe HTML:\n" + char_data, TOTorrentException.RT_DECODE_FAILS));
                    }
                } catch (Throwable e) {

                    if (e instanceof TOTorrentException) {

                        throw ((TOTorrentException) e);
                    }

                    // ignore this
                }

                throw (new TOTorrentException("Contents invalid - bad header", TOTorrentException.RT_DECODE_FAILS));

            }

            metaInfo.write(iFirstByte);

            int nbRead;

            while ((nbRead = is.read(buf)) > 0) {

                metaInfo.write(buf, 0, nbRead);
            }
        } catch (Throwable e) {

            throw (new TOTorrentException("Error reading torrent: " + Debug.getNestedExceptionMessage(e), TOTorrentException.RT_READ_FAILS));
        }

        construct(metaInfo.toByteArray());
    }

    protected void construct(byte[] bytes) throws TOTorrentException {
        try {
            BDecoder decoder = new BDecoder();

            decoder.setVerifyMapOrder(true);

            Map<String, Object> meta_data = decoder.decodeByteArray(bytes);

            // print( "", "", meta_data );

            construct(meta_data);

        } catch (IOException e) {

            throw (new TOTorrentException("Error reading torrent: " + Debug.getNestedExceptionMessage(e), TOTorrentException.RT_DECODE_FAILS, e));
        }
    }

    @SuppressWarnings("unchecked")
    protected void construct(Map<String, Object> meta_data) throws TOTorrentException {
        try {

            String announce_url = null;

            boolean got_announce = false;
            boolean got_announce_list = false;

            boolean bad_announce = false;

            // decode the stuff

            Iterator<String> root_it = meta_data.keySet().iterator();

            while (root_it.hasNext()) {

                String key = (String) root_it.next();

                if (key.equalsIgnoreCase(TK_ANNOUNCE)) {

                    got_announce = true;

                    announce_url = readStringFromMetaData(meta_data, TK_ANNOUNCE);

                    if (announce_url == null) {

                        bad_announce = true;

                    } else {

                        announce_url = announce_url.replaceAll(" ", "");

                        setAnnounceURL(new URI(announce_url));
                    }

                } else if (key.equalsIgnoreCase(TK_ANNOUNCE_LIST)) {

                    got_announce_list = true;

                    List<Object> announce_list = null;

                    Object ann_list = meta_data.get(TK_ANNOUNCE_LIST);

                    if (ann_list instanceof List) { //some malformed torrents have this key as a zero-sized string instead of a zero-sized list

                        announce_list = (List<Object>) ann_list;
                    }

                    if (announce_list != null && announce_list.size() > 0) {

                        announce_url = readStringFromMetaData(meta_data, TK_ANNOUNCE);

                        if (announce_url != null) {

                            announce_url = announce_url.replaceAll(" ", "");
                        }

                        boolean announce_url_found = false;

                        for (int i = 0; i < announce_list.size(); i++) {

                            Object temp = announce_list.get(i);

                            // sometimes we just get a byte[]! turn into a list

                            if (temp instanceof byte[]) {

                                List<byte[]> l = new ArrayList<byte[]>();

                                l.add((byte[]) temp);

                                temp = l;
                            }

                            if (temp instanceof List) {

                                List<Object> set = (List<Object>) temp;

                                Vector<URI> urls = new Vector<URI>();

                                for (int j = 0; j < set.size(); j++) {

                                    String url_str = readStringFromMetaData((byte[]) set.get(j));

                                    url_str = url_str.replaceAll(" ", "");

                                    //check to see if the announce url is somewhere in the announce-list

                                    //urls.add(new URL(StringInterner.intern(url_str)));
                                    urls.add(new URI(url_str));

                                    if (url_str.equalsIgnoreCase(announce_url)) {

                                        announce_url_found = true;
                                    }
                                }

                                if (urls.size() > 0) {

                                    URI[] url_array = new URI[urls.size()];

                                    urls.copyInto(url_array);

                                    addTorrentAnnounceURLSet(url_array);
                                }
                            } else {

                                Debug.out("Torrent has invalid url-list entry (" + temp + ") - ignoring: meta=" + meta_data);
                            }
                        }

                        // if the original announce url isn't found, add it to the list
                        // watch out for those invalid torrents with announce url missing

                        if (!announce_url_found && announce_url != null && announce_url.length() > 0) {
                            try {
                                Vector<URI> urls = new Vector<URI>();
                                //urls.add(new URL(StringInterner.intern(announce_url)));
                                urls.add(new URI(announce_url));
                                URI[] url_array = new URI[urls.size()];
                                urls.copyInto(url_array);
                                addTorrentAnnounceURLSet(url_array);
                            } catch (Exception e) {
                                Debug.out("Invalid URL '" + announce_url + "' - meta=" + meta_data, e);
                            }
                        }
                    }
                } else if (key.equalsIgnoreCase(TK_COMMENT)) {

                    setComment((byte[]) meta_data.get(TK_COMMENT));

                } else if (key.equalsIgnoreCase(TK_CREATED_BY)) {

                    setCreatedBy((byte[]) meta_data.get(TK_CREATED_BY));

                } else if (key.equalsIgnoreCase(TK_CREATION_DATE)) {

                    // non standard, don't fail if format wrong

                    try {

                        Long creation_date = (Long) meta_data.get(TK_CREATION_DATE);

                        if (creation_date != null) {

                            setCreationDate(creation_date.longValue());
                        }
                    } catch (Exception e) {

                        System.out.println("creation_date extraction fails, ignoring");
                    }

                } else if (key.equalsIgnoreCase(TK_INFO)) {

                    // processed later

                } else {

                    Object prop = meta_data.get(key);

                    if (prop instanceof byte[]) {

                        setAdditionalByteArrayProperty(key, (byte[]) prop);

                    } else if (prop instanceof Long) {

                        setAdditionalLongProperty(key, (Long) prop);

                    } else if (prop instanceof List) {

                        setAdditionalListProperty(key, (List<Object>) prop);

                    } else {

                        setAdditionalMapProperty(key, (Map<String, Object>) prop);
                    }
                }
            }

            if (bad_announce) {

                if (got_announce_list) {

                    TOTorrentAnnounceURLSet[] sets = getAnnounceURLGroup().getAnnounceURLSets();

                    if (sets.length > 0) {

                        setAnnounceURL(sets[0].getAnnounceURLs()[0]);
                    } else {

                        throw (new TOTorrentException("ANNOUNCE_URL malformed ('" + announce_url + "' and no usable announce list)", TOTorrentException.RT_DECODE_FAILS));

                    }

                } else {

                    throw (new TOTorrentException("ANNOUNCE_URL malformed ('" + announce_url + "'", TOTorrentException.RT_DECODE_FAILS));
                }
            }

            if (!(got_announce_list || got_announce)) {

                setAnnounceURL(TorrentUtils.getDecentralisedEmptyURL());
            }

            Map<String, Object> info = (Map<String, Object>) meta_data.get(TK_INFO);

            if (info == null) {

                throw (new TOTorrentException("Decode fails, 'info' element not found'", TOTorrentException.RT_DECODE_FAILS));
            }

            boolean hasUTF8Keys = info.containsKey(TK_NAME_UTF8);

            setName((byte[]) info.get(TK_NAME));

            long piece_length = ((Long) info.get(TK_PIECE_LENGTH)).longValue();

            if (piece_length <= 0) {

                throw (new TOTorrentException("Decode fails, piece-length is invalid", TOTorrentException.RT_DECODE_FAILS));
            }

            setPieceLength(piece_length);

            setHashFromInfo(info);

            Long simple_file_length = (Long) info.get(TK_LENGTH);

            long total_length = 0;

            String encoding = getAdditionalStringProperty("encoding");
            hasUTF8Keys &= encoding == null || encoding.equals(ENCODING_ACTUALLY_UTF8_KEYS);

            if (simple_file_length != null) {

                setSimpleTorrent(true);

                total_length = simple_file_length.longValue();

                if (hasUTF8Keys) {
                    setNameUTF8((byte[]) info.get(TK_NAME_UTF8));
                    setAdditionalStringProperty("encoding", ENCODING_ACTUALLY_UTF8_KEYS);
                }

                setFiles(new TOTorrentFileImpl[] { new TOTorrentFileImpl(this, 0, total_length, new byte[][] { getName() }) });

            } else {

                setSimpleTorrent(false);

                List<Object> meta_files = (List<Object>) info.get(TK_FILES);

                TOTorrentFileImpl[] files = new TOTorrentFileImpl[meta_files.size()];

                if (hasUTF8Keys) {
                    for (int i = 0; i < files.length; i++) {
                        Map<String, Object> file_map = (Map<String, Object>) meta_files.get(i);

                        hasUTF8Keys &= file_map.containsKey(TK_PATH_UTF8);
                        if (!hasUTF8Keys) {
                            break;
                        }
                    }

                    if (hasUTF8Keys) {
                        setNameUTF8((byte[]) info.get(TK_NAME_UTF8));
                        setAdditionalStringProperty("encoding", ENCODING_ACTUALLY_UTF8_KEYS);
                    }
                }

                for (int i = 0; i < files.length; i++) {

                    Map<String, Object> file_map = (Map<String, Object>) meta_files.get(i);

                    long len = ((Long) file_map.get(TK_LENGTH)).longValue();

                    List<byte[]> paths = (List<byte[]>) file_map.get(TK_PATH);
                    List<byte[]> paths8 = (List<byte[]>) file_map.get(TK_PATH_UTF8);

                    byte[][] path_comps = null;
                    if (paths != null) {
                        path_comps = new byte[paths.size()][];

                        for (int j = 0; j < paths.size(); j++) {

                            path_comps[j] = (byte[]) paths.get(j);
                        }
                    }

                    TOTorrentFileImpl file;

                    if (hasUTF8Keys) {
                        byte[][] path_comps8 = new byte[paths8.size()][];

                        for (int j = 0; j < paths8.size(); j++) {

                            path_comps8[j] = (byte[]) paths8.get(j);
                        }

                        file = files[i] = new TOTorrentFileImpl(this, total_length, len, path_comps, path_comps8);
                    } else {
                        file = files[i] = new TOTorrentFileImpl(this, total_length, len, path_comps);
                    }

                    total_length += len;

                    // preserve any non-standard attributes

                    Iterator<String> file_it = file_map.keySet().iterator();

                    while (file_it.hasNext()) {

                        String key = (String) file_it.next();

                        if (key.equals(TK_LENGTH) || key.equals(TK_PATH)) {

                            // standard
                            // we don't skip TK_PATH_UTF8 because some code might assume getAdditionalProperty can get it
                        } else {

                            file.setAdditionalProperty(key, file_map.get(key));
                        }
                    }
                }

                setFiles(files);
            }

            byte[] flat_pieces = (byte[]) info.get(TK_PIECES);

            // work out how many pieces we require for the torrent

            int pieces_required = (int) ((total_length + (piece_length - 1)) / piece_length);

            int pieces_supplied = flat_pieces.length / 20;

            if (pieces_supplied < pieces_required) {

                throw (new TOTorrentException("Decode fails, insufficient pieces supplied", TOTorrentException.RT_DECODE_FAILS));
            }

            if (pieces_supplied > pieces_required) {

                Debug.out("Torrent '" + new String(getName()) + "' has too many pieces (required=" + pieces_required + ",supplied=" + pieces_supplied + ") - ignoring excess");
            }

            byte[][] pieces = new byte[pieces_supplied][20];

            for (int i = 0; i < pieces.length; i++) {

                System.arraycopy(flat_pieces, i * 20, pieces[i], 0, 20);
            }

            setPieces(pieces);

            // extract and additional info elements

            Iterator<String> info_it = info.keySet().iterator();

            while (info_it.hasNext()) {

                String key = (String) info_it.next();

                if (key.equals(TK_NAME) || key.equals(TK_LENGTH) || key.equals(TK_FILES) || key.equals(TK_PIECE_LENGTH) || key.equals(TK_PIECES)) {

                    // standard attributes

                } else {

                    addAdditionalInfoProperty(key, info.get(key));
                }
            }

            try {
                byte[] ho = (byte[]) info.get(TK_HASH_OVERRIDE);

                if (ho != null) {

                    setHashOverride(ho);

                } else {

                    if (info instanceof HashMapEx) {

                        HashMapEx info_ex = (HashMapEx) info;

                        if (info_ex.getFlag(HashMapEx.FL_MAP_ORDER_INCORRECT)) {

                            String name = getUTF8Name();

                            if (name == null) {

                                name = new String(getName());
                            }

                            String message = "torrent.decode.info.order.bad:" + name;
                            System.out.println(message);
                        }
                    }
                }
            } catch (Throwable e) {

                Debug.printStackTrace(e);
            }
        } catch (Throwable e) {

            if (e instanceof TOTorrentException) {

                throw ((TOTorrentException) e);
            }

            throw (new TOTorrentException("Torrent decode fails '" + Debug.getNestedExceptionMessageAndStack(e) + "'", TOTorrentException.RT_DECODE_FAILS, e));
        }
    }

    public void printMap() {
        try {

            print("", "root", serialiseToMap());

        } catch (TOTorrentException e) {

            Debug.printStackTrace(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void print(String indent, String name, Map<String, Object> map) {
        System.out.println(indent + name + "{map}");

        Iterator<String> it = map.keySet().iterator();

        while (it.hasNext()) {

            String key = (String) it.next();

            Object value = map.get(key);

            if (value instanceof Map) {

                print(indent + "  ", key, (Map<String, Object>) value);

            } else if (value instanceof List) {

                print(indent + "  ", key, (List<Object>) value);

            } else if (value instanceof Long) {

                print(indent + "  ", key, (Long) value);

            } else {

                print(indent + "  ", key, (byte[]) value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void print(String indent, String name, List<Object> list) {
        System.out.println(indent + name + "{list}");

        Iterator<Object> it = list.iterator();

        int index = 0;

        while (it.hasNext()) {

            Object value = it.next();

            if (value instanceof Map) {

                print(indent + "  ", "[" + index + "]", (Map<String, Object>) value);

            } else if (value instanceof List) {

                print(indent + "  ", "[" + index + "]", (List<Object>) value);

            } else if (value instanceof Long) {

                print(indent + "  ", "[" + index + "]", (Long) value);

            } else {

                print(indent + "  ", "[" + index + "]", (byte[]) value);
            }

            index++;
        }
    }

    protected void print(String indent, String name, Long value) {
        System.out.println(indent + name + "{long} = " + value.longValue());
    }

    protected void print(String indent, String name, byte[] value) {
        String x = new String(value);

        boolean print = true;

        for (int i = 0; i < x.length(); i++) {

            char c = x.charAt(i);

            if (c < 128) {

            } else {

                print = false;

                break;
            }
        }

        if (print) {

            System.out.println(indent + name + "{byte[]} = " + x);

        } else {

            System.out.println(indent + name + "{byte[], length " + value.length + "}");
        }
    }

    private static class HTMLUtils {

        public static String convertHTMLToText2(String content) {
            int pos = 0;

            String res = "";

            content = removeTagPairs(content, "script");

            content = content.replaceAll("&nbsp;", " ");

            content = content.replaceAll("[\\s]+", " ");

            while (true) {

                int p1 = content.indexOf("<", pos);

                if (p1 == -1) {

                    res += content.substring(pos);

                    break;
                }

                int p2 = content.indexOf(">", p1);

                if (p2 == -1) {

                    res += content.substring(pos);

                    break;
                }

                String tag = content.substring(p1 + 1, p2).toLowerCase(Locale.US);

                res += content.substring(pos, p1);

                if (tag.equals("p") || tag.equals("br")) {

                    if (res.length() > 0 && res.charAt(res.length() - 1) != '\n') {

                        res += "\n";
                    }
                }

                pos = p2 + 1;
            }

            res = res.replaceAll("[ \\t\\x0B\\f\\r]+", " ");
            res = res.replaceAll("[ \\t\\x0B\\f\\r]+\\n", "\n");
            res = res.replaceAll("\\n[ \\t\\x0B\\f\\r]+", "\n");

            if (res.length() > 0 && Character.isWhitespace(res.charAt(0))) {

                res = res.substring(1);
            }

            return (res);
        }

        public static String splitWithLineLength(String str, int length) {
            String res = "";

            StringTokenizer tok = new StringTokenizer(str, "\n");

            while (tok.hasMoreTokens()) {

                String line = tok.nextToken();

                while (line.length() > length) {

                    if (res.length() > 0) {

                        res += "\n";
                    }

                    boolean done = false;

                    for (int i = length - 1; i >= 0; i--) {

                        if (Character.isWhitespace(line.charAt(i))) {

                            done = true;

                            res += line.substring(0, i);

                            line = line.substring(i + 1);

                            break;
                        }
                    }

                    if (!done) {

                        res += line.substring(0, length);

                        line = line.substring(length);
                    }
                }

                if (res.length() > 0 && line.length() > 0) {

                    res += "\n";

                    res += line;
                }
            }

            return (res);
        }

        public static String removeTagPairs(String content, String tag_name) {
            tag_name = tag_name.toLowerCase(Locale.US);

            String lc_content = content.toLowerCase(Locale.US);

            int pos = 0;

            String res = "";

            int level = 0;
            int start_pos = -1;

            while (true) {

                int start_tag_start = lc_content.indexOf("<" + tag_name, pos);
                int end_tag_start = lc_content.indexOf("</" + tag_name, pos);

                if (level == 0) {

                    if (start_tag_start == -1) {

                        res += content.substring(pos);

                        break;
                    }

                    res += content.substring(pos, start_tag_start);

                    start_pos = start_tag_start;

                    level = 1;

                    pos = start_pos + 1;

                } else {

                    if (end_tag_start == -1) {

                        res += content.substring(pos);

                        break;
                    }

                    if (start_tag_start == -1 || end_tag_start < start_tag_start) {

                        level--;

                        int end_end = lc_content.indexOf('>', end_tag_start);

                        if (end_end == -1) {

                            break;
                        }

                        pos = end_end + 1;

                    } else {

                        if (start_tag_start == -1) {

                            res += content.substring(pos);

                            break;
                        }

                        level++;

                        pos = start_tag_start + 1;
                    }
                }
            }

            return (res);
        }
    }

}