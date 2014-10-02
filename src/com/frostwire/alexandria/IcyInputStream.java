/*
 * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.alexandria;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author: Denis Tulskiy
 * Date: 4/10/11
 * 
 * Modified by aldenml: Using raw sockets
 */
public class IcyInputStream extends FilterInputStream {
    private static final Logger logger = Logger.getLogger("musique");

    private Track track;
    private int metaInt = 0;
    private int bytesRead = 0;

    public static void create(String streamUrl, Track track) {
        try {
            InputStream is = openStream(streamUrl);
            IcyInputStream icyInputStream = new IcyInputStream(new BufferedInputStream(is));
            icyInputStream.setTrack(track);
            icyInputStream.init();
            icyInputStream.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error opening Icy stream: " + e.getMessage());
        }
    }

    private static InputStream openStream(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        Socket socket = new Socket();
        socket.setSoTimeout(1000);
        socket.connect(new InetSocketAddress(url.getHost(), url.getPort()), 1000);
        OutputStream os = socket.getOutputStream();
        String user_agent = "WinampMPEG/5.09";
        String req = "GET / HTTP/1.0\r\nuser-agent: " + user_agent + "\r\nIcy-MetaData: 1\r\nConnection: keep-alive\r\n\r\n";
        os.write(req.getBytes());
        return socket.getInputStream();
    }

    private IcyInputStream(InputStream in) {
        super(in);
    }

    private void setTrack(Track track) {
        this.track = track;
    }

    public String readLine() {
        try {
            int ch = read();

            StringBuilder sb = new StringBuilder();
            while (ch != '\n' && ch != '\r' && ch >= 0) {
                sb.append((char) ch);
                ch = read();
            }

            if (ch == '\n' || ch == '\r') {
                //noinspection ResultOfMethodCallIgnored
                read();
            }
            return sb.toString();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading Icy stream", e);
        }
        return null;
    }


    private void init() {
        String metaIntString = "0";
            //Java does not parse non-standart headers
            //used by SHOUTCast
            logger.fine("Reading SHOUTCast response");
            String s = readLine();
            if (!s.equals("ICY 200 OK")) {
                logger.warning("SHOUTCast invalid response: " + s);
                return;
            }

            while (true) {
                s = readLine();

                if (s.isEmpty()) {
                    break;
                }

                int index = s.indexOf(":");
                if (index == -1) {
                    break;
                }
                
                String[] ss = new String[2];
                ss[0] = s.substring(0, index);
                ss[1] = s.substring(index + 1);
                
                if (ss[0].equals("icy-metaint")) {
                    metaIntString = ss[1];
                } else if (ss[0].equals("icy-genre")) {
                    track.genre = ss[1];
                } else if (ss[0].equals("icy-name")) {
                    track.name = ss[1];
                } else if (ss[0].equals("content-type")) {
                    track.contentType = ss[1];
                } else if (ss[0].equals("icy-url")) {
                    track.url = ss[1];
                } else if (ss[0].equals("icy-br")) {
                    track.bitrate = ss[1];
                }
            }
            
        try {
            metaInt = Integer.parseInt(metaIntString.trim());
            logger.fine("Reading metadata information every " + metaInt + " bytes");
        } catch (NumberFormatException e) {
            metaInt = 0;
        }
        //logger.fine("Content type is: " + contentType);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (metaInt > 0) {
            int bytesToMeta = metaInt - bytesRead;
            if (bytesToMeta == 0) {
                int size = read() * 16;
                if (size > 1) {
                    byte[] meta = new byte[size];
                    int i = super.read(meta, 0, size);
                    if (i != size) {
                        throw new RuntimeException("WTF");
                    }
                    String metaString = new String(meta, 0, i, "UTF-8");
                    String title = "StreamTitle='";
                    if (metaString.startsWith(title)) {
                        String[] ss = metaString.substring(title.length(), metaString.indexOf(";") - 1).split(" - ");
                        if (ss.length > 0) {
                            if (ss.length > 1) {
                                System.out.println("artist " + ss[0]);
                                System.out.println("title " + ss[1]);
                            } else {
                                System.out.println("title " + ss[0]);
                            }
                        }
                    }
                }
                bytesRead = 0;
            } else if (bytesToMeta > 0 && bytesToMeta < len) {
                len = bytesToMeta;
            }
        }

        int read = super.read(b, off, len);
        bytesRead += read;
        return read;
    }
    
    public static final class Track {
        
        public String url;
        public String genre;
        public String name;
        public String contentType;
        public String bitrate;
    }
}