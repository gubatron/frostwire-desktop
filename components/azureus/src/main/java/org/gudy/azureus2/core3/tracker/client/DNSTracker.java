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

package org.gudy.azureus2.core3.tracker.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class DNSTracker {

    private static final Pattern BT_TXT_PATTERN = Pattern.compile("(UDP:\\d+|TCP:\\d+)");

    public static List<DNSTrackerEntry> dig(String tracker) {
        List<DNSTrackerEntry> entries = null;
        try {
            Record[] records = new Lookup(tracker, Type.TXT).run();
            for (int i = 0; records != null && i < records.length; i++) {
                TXTRecord txt = (TXTRecord) records[i];
                List<?> strings = txt.getStrings();
                if (strings != null && strings.size() > 0) {
                    String s = strings.get(0).toString();
                    if (s.startsWith("BITTORRENT")) {
                        entries = new ArrayList<DNSTrackerEntry>();
                        Matcher m = BT_TXT_PATTERN.matcher(s);
                        while (m.find()) {
                            for (int j = 0; j < m.groupCount(); j++) {
                                String g = m.group(i);
                                if (g.length() > 0) {
                                    DNSTrackerEntry e = new DNSTrackerEntry();
                                    e.udp = g.startsWith("UDP");
                                    e.port = Integer.parseInt(g.split(":")[1]);
                                    entries.add(e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return entries;
    }

    public static URL fixTrackerURL(URL reqUrl) {
        try {
            String protocol = reqUrl.getProtocol();
            String host = reqUrl.getHost();
            int port = reqUrl.getPort();
            String file = reqUrl.getFile();

            List<DNSTrackerEntry> entries = dig(host);
            if (entries == null) {
                return reqUrl;
            }

            DNSTrackerEntry e = entries.get(0);
            protocol = e.udp ? "udp" : "http";
            port = e.port;

            URL url = new URL(protocol, host, port, file);
            //System.out.println("dig txt:" + reqUrl + "->" + url);
            return url;
        } catch (Throwable e) {
            e.printStackTrace();
            return reqUrl;
        }
    }
}
