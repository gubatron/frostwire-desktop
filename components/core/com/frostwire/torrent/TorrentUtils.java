/*
 * File    : TorrentUtils.java
 * Created : 13-Oct-2003
 * By      : stuff
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

/**
 * @author parg
 *
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

public final class TorrentUtils {

    public static final int TORRENT_FLAG_LOW_NOISE = 0x00000001;
    public static final int TORRENT_FLAG_METADATA_TORRENT = 0x00000002;

    private static final String TORRENT_AZ_PROP_DHT_BACKUP_ENABLE = "dht_backup_enable";
    private static final String TORRENT_AZ_PROP_DHT_BACKUP_REQUESTED = "dht_backup_requested";
    private static final String TORRENT_AZ_PROP_TORRENT_FLAGS = "torrent_flags";
    private static final String TORRENT_AZ_PROP_PLUGINS = "plugins";

    public static final String TORRENT_AZ_PROP_OBTAINED_FROM = "obtained_from";
    public static final String TORRENT_AZ_PROP_PEER_CACHE = "peer_cache";
    public static final String TORRENT_AZ_PROP_PEER_CACHE_VALID = "peer_cache_valid";
    public static final String TORRENT_AZ_PROP_INITIAL_LINKAGE = "initial_linkage";
    public static final String TORRENT_AZ_PROP_INITIAL_LINKAGE2 = "initial_linkage2";

    public static TOTorrent readFromBEncodedInputStream(InputStream is) throws TOTorrentException {
        TOTorrent torrent = TOTorrentFactory.deserialiseFromBEncodedInputStream(is);

        // as we've just imported this torrent we want to clear out any possible attributes that we
        // don't want such as "torrent filename"

        torrent.removeAdditionalProperties();

        return torrent;
    }

    public static void writeToFile(final TOTorrent torrent) throws TOTorrentException {
        writeToFile(torrent, false);
    }

    public static void writeToFile(TOTorrent torrent, boolean force_backup) throws TOTorrentException {
        try {
            String str = torrent.getAdditionalStringProperty("torrent filename");

            if (str == null) {
                throw new TOTorrentException("TorrentUtils::writeToFile: no 'torrent filename' attribute defined", TOTorrentException.RT_FILE_NOT_FOUND);
            }

            // save first to temporary file as serialisation may require state to be re-read from
            // the existing file first and if we rename to .bak first then this aint good

            File torrent_file_tmp = new File(str + "._az");

            torrent.serialiseToBEncodedFile(torrent_file_tmp);

            // now backup if required

            File torrent_file = new File(str);

            //	    	if ( 	( force_backup ||COConfigurationManager.getBooleanParameter("Save Torrent Backup")) &&
            //	    			torrent_file.exists()) {
            //	    		
            //	    		File torrent_file_bak = new File(str + ".bak");
            //	    		
            //	    		try{
            //	    			
            //	    				// Will return false if it cannot be deleted (including if the file doesn't exist).
            //	    			
            //	    			torrent_file_bak.delete();
            //	    			
            //	    			torrent_file.renameTo(torrent_file_bak);
            //	    			
            //	    		}catch( SecurityException e){
            //	    			
            //	    			Debug.printStackTrace( e );
            //	    		}
            //	    	}

            // now rename the temp file to required one

            if (torrent_file.exists()) {

                torrent_file.delete();
            }

            torrent_file_tmp.renameTo(torrent_file);

        } finally {

            //torrent.getMonitor().exit();
        }
    }

    public static void writeToFile(TOTorrent torrent, File file) throws TOTorrentException {
        writeToFile(torrent, file, false);
    }

    public static void writeToFile(TOTorrent torrent, File file, boolean force_backup) throws TOTorrentException {
        torrent.setAdditionalStringProperty("torrent filename", file.toString());

        writeToFile(torrent, force_backup);
    }

    public static String getTorrentFileName(TOTorrent torrent) throws TOTorrentException {
        String str = torrent.getAdditionalStringProperty("torrent filename");

        if (str == null) {
            throw new TOTorrentException("TorrentUtils::getTorrentFileName: no 'torrent filename' attribute defined", TOTorrentException.RT_FILE_NOT_FOUND);
        }

        return str;
    }

    public static void copyToFile(TOTorrent torrent, File file) throws TOTorrentException {
        torrent.serialiseToBEncodedFile(file);
    }

    public static String exceptionToText(TOTorrentException e) {
        String errorDetail;

        int reason = e.getReason();

        if (reason == TOTorrentException.RT_FILE_NOT_FOUND) {

            errorDetail = "DownloadManager.error.filenotfound";

        } else if (reason == TOTorrentException.RT_ZERO_LENGTH) {

            errorDetail = "DownloadManager.error.fileempty";

        } else if (reason == TOTorrentException.RT_TOO_BIG) {

            errorDetail = "DownloadManager.error.filetoobig";

        } else if (reason == TOTorrentException.RT_DECODE_FAILS) {

            errorDetail = "DownloadManager.error.filewithouttorrentinfo";

        } else if (reason == TOTorrentException.RT_UNSUPPORTED_ENCODING) {

            errorDetail = "DownloadManager.error.unsupportedencoding";

        } else if (reason == TOTorrentException.RT_READ_FAILS) {

            errorDetail = "DownloadManager.error.ioerror";

        } else if (reason == TOTorrentException.RT_HASH_FAILS) {

            errorDetail = "DownloadManager.error.sha1";

        } else if (reason == TOTorrentException.RT_CANCELLED) {

            errorDetail = "DownloadManager.error.operationcancancelled";

        } else {

            errorDetail = Debug.getNestedExceptionMessage(e);
        }

        String msg = Debug.getNestedExceptionMessage(e);

        if (errorDetail.indexOf(msg) == -1) {

            errorDetail += " (" + msg + ")";
        }

        return (errorDetail);
    }

    public static String announceGroupsToText(TOTorrent torrent) {
        URI announce_url = torrent.getAnnounceURL();

        String announce_url_str = announce_url == null ? "" : announce_url.toString().trim();

        TOTorrentAnnounceURLGroup group = torrent.getAnnounceURLGroup();

        TOTorrentAnnounceURLSet[] sets = group.getAnnounceURLSets();

        if (sets.length == 0) {

            return (announce_url_str);

        } else {

            StringBuffer sb = new StringBuffer(1024);

            boolean announce_found = false;

            for (int i = 0; i < sets.length; i++) {

                TOTorrentAnnounceURLSet set = sets[i];

                URI[] urls = set.getAnnounceURLs();

                if (urls.length > 0) {

                    for (int j = 0; j < urls.length; j++) {

                        String str = urls[j].toString().trim();

                        if (str.equals(announce_url_str)) {

                            announce_found = true;
                        }

                        sb.append(str);
                        sb.append("\r\n");
                    }

                    sb.append("\r\n");
                }
            }

            String result = sb.toString().trim();

            if (!announce_found) {

                if (announce_url_str.length() > 0) {

                    if (result.length() == 0) {

                        result = announce_url_str;

                    } else {

                        result = "\r\n\r\n" + announce_url_str;
                    }
                }
            }

            return (result);
        }
    }

    public static String announceGroupsToText(List<List<String>> group) {
        StringBuffer sb = new StringBuffer(1024);

        for (List<String> urls : group) {

            if (sb.length() > 0) {

                sb.append("\r\n");
            }

            for (String str : urls) {

                sb.append(str);
                sb.append("\r\n");
            }
        }

        return (sb.toString().trim());
    }

    public static List<List<String>> announceTextToGroups(String text) {
        List<List<String>> groups = new ArrayList<List<String>>();

        String[] lines = text.split("\n");

        List<String> current_group = new ArrayList<String>();

        Set<String> hits = new HashSet<String>();

        for (String line : lines) {

            line = line.trim();

            if (line.length() == 0) {

                if (current_group.size() > 0) {

                    groups.add(current_group);

                    current_group = new ArrayList<String>();
                }
            } else {
                String lc_line = line.toLowerCase(Locale.US);

                if (hits.contains(lc_line)) {

                    continue;
                }

                hits.add(lc_line);

                current_group.add(line);
            }
        }

        if (current_group.size() > 0) {

            groups.add(current_group);
        }

        return (groups);
    }

    public static List<List<String>> announceGroupsToList(TOTorrent torrent) {
        List<List<String>> groups = new ArrayList<List<String>>();

        TOTorrentAnnounceURLGroup group = torrent.getAnnounceURLGroup();

        TOTorrentAnnounceURLSet[] sets = group.getAnnounceURLSets();

        if (sets.length == 0) {

            List<String> s = new ArrayList<String>();

            s.add(UrlUtils.getCanonicalString(torrent.getAnnounceURL()));

            groups.add(s);

        } else {

            Set<String> all_urls = new HashSet<String>();

            for (int i = 0; i < sets.length; i++) {

                List<String> s = new ArrayList<String>();

                TOTorrentAnnounceURLSet set = sets[i];

                URI[] urls = set.getAnnounceURLs();

                for (int j = 0; j < urls.length; j++) {

                    String u = UrlUtils.getCanonicalString(urls[j]);

                    s.add(u);

                    all_urls.add(u);
                }

                if (s.size() > 0) {

                    groups.add(s);
                }
            }

            String a = UrlUtils.getCanonicalString(torrent.getAnnounceURL());

            if (!all_urls.contains(a)) {

                List<String> s = new ArrayList<String>();

                s.add(a);

                groups.add(0, s);
            }
        }

        return (groups);
    }

    public static void listToAnnounceGroups(List<List<String>> groups, TOTorrent torrent) {
        try {
            TOTorrentAnnounceURLGroup tg = torrent.getAnnounceURLGroup();

            if (groups.size() == 1) {

                List<String> set = groups.get(0);

                if (set.size() == 1) {

                    torrent.setAnnounceURL(new URI((String) set.get(0)));

                    tg.setAnnounceURLSets(new TOTorrentAnnounceURLSet[0]);

                    return;
                }
            }

            Vector<TOTorrentAnnounceURLSet> g = new Vector<TOTorrentAnnounceURLSet>();

            for (int i = 0; i < groups.size(); i++) {

                List<String> set = groups.get(i);

                URI[] urls = new URI[set.size()];

                for (int j = 0; j < set.size(); j++) {

                    urls[j] = new URI((String) set.get(j));
                }

                if (urls.length > 0) {

                    g.add(tg.createAnnounceURLSet(urls));
                }
            }

            TOTorrentAnnounceURLSet[] sets = new TOTorrentAnnounceURLSet[g.size()];

            g.copyInto(sets);

            tg.setAnnounceURLSets(sets);

            if (sets.length == 0) {

                // hmm, no valid urls at all

                torrent.setAnnounceURL(new URI("http://no.valid.urls.defined/announce"));
            }

        } catch (URISyntaxException e) {

            Debug.printStackTrace(e);
        }
    }

    public static void announceGroupsInsertFirst(TOTorrent torrent, String first_url) {
        try {

            announceGroupsInsertFirst(torrent, new URI(first_url));

        } catch (URISyntaxException e) {

            Debug.printStackTrace(e);
        }
    }

    public static void announceGroupsInsertFirst(TOTorrent torrent, URI first_url) {
        announceGroupsInsertFirst(torrent, new URI[] { first_url });
    }

    public static void announceGroupsInsertFirst(TOTorrent torrent, URI[] first_urls) {
        TOTorrentAnnounceURLGroup group = torrent.getAnnounceURLGroup();

        TOTorrentAnnounceURLSet[] sets = group.getAnnounceURLSets();

        TOTorrentAnnounceURLSet set1 = group.createAnnounceURLSet(first_urls);

        if (sets.length > 0) {

            TOTorrentAnnounceURLSet[] new_sets = new TOTorrentAnnounceURLSet[sets.length + 1];

            new_sets[0] = set1;

            System.arraycopy(sets, 0, new_sets, 1, sets.length);

            group.setAnnounceURLSets(new_sets);

        } else {

            TOTorrentAnnounceURLSet set2 = group.createAnnounceURLSet(new URI[] { torrent.getAnnounceURL() });

            group.setAnnounceURLSets(new TOTorrentAnnounceURLSet[] { set1, set2 });
        }
    }

    public static void announceGroupsInsertLast(TOTorrent torrent, URI[] first_urls) {
        TOTorrentAnnounceURLGroup group = torrent.getAnnounceURLGroup();

        TOTorrentAnnounceURLSet[] sets = group.getAnnounceURLSets();

        TOTorrentAnnounceURLSet set1 = group.createAnnounceURLSet(first_urls);

        if (sets.length > 0) {

            TOTorrentAnnounceURLSet[] new_sets = new TOTorrentAnnounceURLSet[sets.length + 1];

            new_sets[sets.length] = set1;

            System.arraycopy(sets, 0, new_sets, 0, sets.length);

            group.setAnnounceURLSets(new_sets);

        } else {

            TOTorrentAnnounceURLSet set2 = group.createAnnounceURLSet(new URI[] { torrent.getAnnounceURL() });

            group.setAnnounceURLSets(new TOTorrentAnnounceURLSet[] { set2, set1 });
        }
    }

    public static void announceGroupsSetFirst(TOTorrent torrent, String first_url) {
        List<List<String>> groups = announceGroupsToList(torrent);

        boolean found = false;

        outer: for (int i = 0; i < groups.size(); i++) {

            List<String> set = groups.get(i);

            for (int j = 0; j < set.size(); j++) {

                if (first_url.equals(set.get(j))) {

                    set.remove(j);

                    set.add(0, first_url);

                    groups.remove(set);

                    groups.add(0, set);

                    found = true;

                    break outer;
                }
            }
        }

        if (!found) {

            System.out.println("TorrentUtils::announceGroupsSetFirst - failed to find '" + first_url + "'");
        }

        listToAnnounceGroups(groups, torrent);
    }

    public static boolean announceGroupsContainsURL(TOTorrent torrent, String url) {
        List<List<String>> groups = announceGroupsToList(torrent);

        for (int i = 0; i < groups.size(); i++) {

            List<String> set = groups.get(i);

            for (int j = 0; j < set.size(); j++) {

                if (url.equals(set.get(j))) {

                    return (true);
                }
            }
        }

        return (false);
    }

    public static boolean mergeAnnounceURLs(TOTorrent new_torrent, TOTorrent dest_torrent) {
        if (new_torrent == null || dest_torrent == null) {

            return (false);
        }

        List<List<String>> new_groups = announceGroupsToList(new_torrent);
        List<List<String>> dest_groups = announceGroupsToList(dest_torrent);

        List<List<String>> groups_to_add = new ArrayList<List<String>>();

        for (int i = 0; i < new_groups.size(); i++) {

            List<String> new_set = new_groups.get(i);

            boolean match = false;

            for (int j = 0; j < dest_groups.size(); j++) {

                List<String> dest_set = dest_groups.get(j);

                boolean same = new_set.size() == dest_set.size();

                if (same) {

                    for (int k = 0; k < new_set.size(); k++) {

                        String new_url = (String) new_set.get(k);

                        if (!dest_set.contains(new_url)) {

                            same = false;

                            break;
                        }
                    }
                }

                if (same) {

                    match = true;

                    break;
                }
            }

            if (!match) {

                groups_to_add.add(new_set);
            }
        }

        if (groups_to_add.size() == 0) {

            return (false);
        }

        for (int i = 0; i < groups_to_add.size(); i++) {

            dest_groups.add(i, groups_to_add.get(i));
        }

        listToAnnounceGroups(dest_groups, dest_torrent);

        return (true);
    }

    public static boolean replaceAnnounceURL(TOTorrent torrent, URI old_url, URI new_url) {
        boolean found = false;

        String old_str = old_url.toString();
        String new_str = new_url.toString();

        List<List<String>> l = announceGroupsToList(torrent);

        for (int i = 0; i < l.size(); i++) {

            List<String> set = l.get(i);

            for (int j = 0; j < set.size(); j++) {

                if (((String) set.get(j)).equals(old_str)) {

                    found = true;

                    set.set(j, new_str);
                }
            }
        }

        if (found) {

            listToAnnounceGroups(l, torrent);
        }

        if (torrent.getAnnounceURL().toString().equals(old_str)) {

            torrent.setAnnounceURL(new_url);

            found = true;
        }

        if (found) {

            try {
                writeToFile(torrent);

            } catch (Throwable e) {

                Debug.printStackTrace(e);

                return (false);
            }
        }

        return (found);
    }

    public static String getLocalisedName(TOTorrent torrent) {
        if (torrent == null) {
            return "";
        }
        try {
            String utf8Name = torrent.getUTF8Name();
            if (utf8Name != null) {
                return utf8Name;
            }

            LocaleUtilDecoder decoder = LocaleTorrentUtil.getTorrentEncodingIfAvailable(torrent);

            if (decoder == null) {

                return (new String(torrent.getName(), Constants.DEFAULT_ENCODING));
            }

            return (decoder.decodeString(torrent.getName()));

        } catch (Throwable e) {

            Debug.printStackTrace(e);

            return (new String(torrent.getName()));
        }
    }

    public static URI getDecentralisedEmptyURL() {
        try {
            return (new URI("dht://"));

        } catch (Throwable e) {

            Debug.printStackTrace(e);

            return (null);
        }
    }

    public static URI getDecentralisedURL(byte[] hash) {
        try {
            return (new URI("dht://" + ByteFormatter.encodeString(hash) + ".dht/announce"));

        } catch (Throwable e) {

            Debug.out(e);

            return (getDecentralisedEmptyURL());
        }
    }

    public static URI getDecentralisedURL(TOTorrent torrent) {
        try {
            return (new URI("dht://" + ByteFormatter.encodeString(torrent.getHash()) + ".dht/announce"));

        } catch (Throwable e) {

            Debug.out(e);

            return (getDecentralisedEmptyURL());
        }
    }

    public static void setDecentralised(TOTorrent torrent) {
        torrent.setAnnounceURL(getDecentralisedURL(torrent));
    }

    public static boolean isDecentralised(TOTorrent torrent) {
        if (torrent == null) {

            return (false);
        }

        return (torrent.isDecentralised());
    }

    public static boolean isDecentralised(URI url) {
        if (url == null) {

            return (false);
        }

        return (url.getScheme().equalsIgnoreCase("dht"));
    }

    private static Map<String, Object> getAzureusProperties(TOTorrent torrent) {
        Map<String, Object> m = torrent.getAdditionalMapProperty(TOTorrent.AZUREUS_PROPERTIES);

        if (m == null) {

            m = new HashMap<String, Object>();

            torrent.setAdditionalMapProperty(TOTorrent.AZUREUS_PROPERTIES, m);
        }

        return (m);
    }

    private static Map<String, Object> getAzureusPrivateProperties(TOTorrent torrent) {
        Map<String, Object> m = torrent.getAdditionalMapProperty(TOTorrent.AZUREUS_PRIVATE_PROPERTIES);

        if (m == null) {

            m = new HashMap<String, Object>();

            torrent.setAdditionalMapProperty(TOTorrent.AZUREUS_PRIVATE_PROPERTIES, m);
        }

        return (m);
    }

    public static String getObtainedFrom(TOTorrent torrent) {
        Map<String, Object> m = getAzureusPrivateProperties(torrent);

        byte[] from = (byte[]) m.get(TORRENT_AZ_PROP_OBTAINED_FROM);

        if (from != null) {

            try {
                return (new String(from, "UTF-8"));

            } catch (Throwable e) {

                Debug.printStackTrace(e);
            }
        }

        return (null);
    }

    public static void setPeerCache(TOTorrent torrent, Map<String, Object> pc) {
        Map<String, Object> m = getAzureusPrivateProperties(torrent);

        try {
            m.put(TORRENT_AZ_PROP_PEER_CACHE, pc);

        } catch (Throwable e) {

            Debug.printStackTrace(e);
        }
    }

    public static void setFlag(TOTorrent torrent, int flag, boolean value) {
        Map<String, Object> m = getAzureusProperties(torrent);

        Long flags = (Long) m.get(TORRENT_AZ_PROP_TORRENT_FLAGS);

        if (flags == null) {
            flags = Long.valueOf(0);
        }

        m.put(TORRENT_AZ_PROP_TORRENT_FLAGS, Long.valueOf(flags.intValue() | flag));
    }

    public static boolean getFlag(TOTorrent torrent, int flag) {
        Map<String, Object> m = getAzureusProperties(torrent);

        Long flags = (Long) m.get(TORRENT_AZ_PROP_TORRENT_FLAGS);

        if (flags == null) {

            return (false);
        }

        return ((flags.intValue() & flag) != 0);
    }

    @SuppressWarnings("unchecked")
    public static Map<Integer, File> getInitialLinkage(TOTorrent torrent) {
        Map<Integer, File> result = new HashMap<Integer, File>();

        try {
            Map<String, Object> pp = torrent.getAdditionalMapProperty(TOTorrent.AZUREUS_PRIVATE_PROPERTIES);

            if (pp != null) {

                Map<String, Object> links;

                byte[] g_data = (byte[]) pp.get(TorrentUtils.TORRENT_AZ_PROP_INITIAL_LINKAGE2);

                if (g_data == null) {

                    links = (Map<String, Object>) pp.get(TorrentUtils.TORRENT_AZ_PROP_INITIAL_LINKAGE);

                } else {

                    links = (Map<String, Object>) BDecoder.decode(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(g_data))));

                }
                if (links != null) {//&& TorrentUtils.isCreatedTorrent( torrent )){

                    links = BDecoder.decodeStrings(links);

                    for (Map.Entry<String, Object> entry : links.entrySet()) {

                        int file_index = Integer.parseInt(entry.getKey());
                        String file = (String) entry.getValue();

                        result.put(file_index, new File(file));
                    }
                }
            }
        } catch (Throwable e) {

            Debug.out("Failed to read linkage map", e);
        }

        return (result);
    }

    @SuppressWarnings("unchecked")
    public static void setPluginStringProperty(TOTorrent torrent, String name, String value) {
        Map<String, Object> m = getAzureusProperties(torrent);

        Object obj = m.get(TORRENT_AZ_PROP_PLUGINS);

        Map<String, Object> p;

        if (obj instanceof Map) {

            p = (Map<String, Object>) obj;

        } else {

            p = new HashMap<String, Object>();

            m.put(TORRENT_AZ_PROP_PLUGINS, p);
        }

        if (value == null) {

            p.remove(name);

        } else {

            p.put(name, value.getBytes());
        }
    }

    public static String getPluginStringProperty(TOTorrent torrent, String name) {
        Map<String, Object> m = getAzureusProperties(torrent);

        Object obj = m.get(TORRENT_AZ_PROP_PLUGINS);

        if (obj instanceof Map) {

            @SuppressWarnings("unchecked")
            Map<String, Object> p = (Map<String, Object>) obj;

            obj = p.get(name);

            if (obj instanceof byte[]) {

                return (new String((byte[]) obj));
            }
        }

        return (null);
    }

    @SuppressWarnings("unchecked")
    public static void setPluginMapProperty(TOTorrent torrent, String name, Map<String, Object> value) {
        Map<String, Object> m = getAzureusProperties(torrent);

        Object obj = m.get(TORRENT_AZ_PROP_PLUGINS);

        Map<String, Object> p;

        if (obj instanceof Map) {

            p = (Map<String, Object>) obj;

        } else {

            p = new HashMap<String, Object>();

            m.put(TORRENT_AZ_PROP_PLUGINS, p);
        }

        if (value == null) {

            p.remove(name);

        } else {

            p.put(name, value);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getPluginMapProperty(TOTorrent torrent, String name) {
        Map<String, Object> m = getAzureusProperties(torrent);

        Object obj = m.get(TORRENT_AZ_PROP_PLUGINS);

        if (obj instanceof Map) {

            Map<String, Object> p = (Map<String, Object>) obj;

            obj = p.get(name);

            if (obj instanceof Map) {

                return ((Map<String, Object>) obj);
            }
        }

        return (null);
    }

    public static void setDHTBackupEnabled(TOTorrent torrent, boolean enabled) {
        Map<String, Object> m = getAzureusProperties(torrent);

        m.put(TORRENT_AZ_PROP_DHT_BACKUP_ENABLE, Long.valueOf(enabled ? 1 : 0));
    }

    public static boolean getDHTBackupEnabled(TOTorrent torrent) {
        // missing -> true

        Map<String, Object> m = getAzureusProperties(torrent);

        Object obj = m.get(TORRENT_AZ_PROP_DHT_BACKUP_ENABLE);

        if (obj instanceof Long) {

            return (((Long) obj).longValue() == 1);
        }

        return (true);
    }

    public static boolean isDHTBackupRequested(TOTorrent torrent) {
        // missing -> false

        Map<String, Object> m = getAzureusProperties(torrent);

        Object obj = m.get(TORRENT_AZ_PROP_DHT_BACKUP_REQUESTED);

        if (obj instanceof Long) {

            return (((Long) obj).longValue() == 1);
        }

        return (false);
    }

    public static void setDHTBackupRequested(TOTorrent torrent, boolean requested) {
        Map<String, Object> m = getAzureusProperties(torrent);

        m.put(TORRENT_AZ_PROP_DHT_BACKUP_REQUESTED, Long.valueOf(requested ? 1 : 0));
    }

    public static boolean isReallyPrivate(TOTorrent torrent) {
        if (torrent == null) {

            return (false);
        }

        if (UrlUtils.containsPasskey(torrent.getAnnounceURL())) {

            return torrent.getPrivate();
        }

        return false;
    }

    public static boolean getPrivate(TOTorrent torrent) {
        if (torrent == null) {

            return (false);
        }

        return (torrent.getPrivate());
    }

    public static void setPrivate(TOTorrent torrent, boolean _private) {
        if (torrent == null) {

            return;
        }

        try {
            torrent.setPrivate(_private);

        } catch (Throwable e) {

            Debug.printStackTrace(e);
        }
    }

    /**
     * A nice string of a Torrent's hash
     * 
     * @param torrent Torrent to fromat hash of
     * @return Hash string in a nice format
     */
    public static String nicePrintTorrentHash(TOTorrent torrent) {
        return nicePrintTorrentHash(torrent, false);
    }

    /**
     * A nice string of a Torrent's hash
     * 
     * @param torrent Torrent to fromat hash of
     * @param tight No spaces between groups of numbers
     * 
     * @return Hash string in a nice format
     */
    public static String nicePrintTorrentHash(TOTorrent torrent, boolean tight) {
        byte[] hash;

        if (torrent == null) {

            hash = new byte[20];
        } else {
            try {
                hash = torrent.getHash();

            } catch (TOTorrentException e) {

                Debug.printStackTrace(e);

                hash = new byte[20];
            }
        }

        return (ByteFormatter.nicePrint(hash, tight));
    }

    private final static class UrlUtils {

        public static boolean containsPasskey(URI url) {
            String url_str = url.toString();

            return (url_str.matches(".*[0-9a-z]{20,40}.*"));
        }

        public static URI setPort(URI u, int port) {
            if (port == -1) {
                port = u.getPort();
            }
            StringBuffer result = new StringBuffer();
            result.append(u.getScheme());
            result.append(":");
            String authority = u.getAuthority();
            if (authority != null && authority.length() > 0) {
                result.append("//");
                int pos = authority.indexOf('@');
                if (pos != -1) {
                    result.append(authority.substring(0, pos + 1));
                    authority = authority.substring(pos + 1);
                }
                pos = authority.lastIndexOf(':');
                if (pos == -1) {
                    if (port > 0) {
                        result.append(authority + ":" + port);
                    } else {
                        result.append(authority);
                    }
                } else {
                    if (port > 0) {
                        result.append(authority.substring(0, pos + 1) + port);
                    } else {
                        result.append(authority.substring(0, pos));
                    }
                }
            }
            if (u.getPath() != null) {
                result.append(u.getPath());
            }
            if (u.getQuery() != null) {
                result.append('?');
                result.append(u.getQuery());
            }
            //            if (u.getRef() != null) {
            //                result.append("#");
            //                result.append(u.getRef());
            //            }
            try {
                return (new URI(result.toString()));
            } catch (Throwable e) {
                Debug.out(e);
                return (u);
            }
        }

        public static URI setProtocol(URI u, String protocol) {
            String str = u.toString();

            int pos = str.indexOf(":");

            try {
                return (new URI(protocol + str.substring(pos)));

            } catch (Throwable e) {

                Debug.out(e);

                return (u);
            }
        }

        public static String getCanonicalString(URI url) {
            String protocol = url.getScheme();

            if (!protocol.equals(protocol.toLowerCase(Locale.US))) {

                protocol = protocol.toLowerCase(Locale.US);

                url = UrlUtils.setProtocol(url, protocol);
            }

            int port = url.getPort();

            if (protocol.equals("http") || protocol.equals("https")) {

                try {
                    if (port == url.toURL().getDefaultPort()) {

                        url = UrlUtils.setPort(url, 0);
                    }
                } catch (MalformedURLException e) {
                    url = UrlUtils.setPort(url, -1);
                }
            } else {

                if (port == -1) {

                    url = UrlUtils.setPort(url, -1);
                }
            }

            return (url.toString());
        }
    }
}
