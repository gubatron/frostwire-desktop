//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.frostwire.search.youtube;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import jd.http.Browser;
import jd.http.Request;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.parser.html.Form.MethodType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class YouTubeDecrypter {
    
    private static final Logger LOG = LoggerFactory.getLogger(YouTubeDecrypter.class);
    
    private Browser br = new Browser();
    
    HashMap<DestinationFormat, ArrayList<Info>> possibleconverts    = null;

    private boolean verifyAge = false;
    private static final String                 UNSUPPORTEDRTMP     = "itag%2Crtmpe%2";
    static public final Pattern                 YT_FILENAME_PATTERN = Pattern.compile("<meta name=\"title\" content=\"(.*?)\">", Pattern.CASE_INSENSITIVE);

    public List<YouTubeDownloadLink> decrypt(String videoUrl) throws Exception {
        this.possibleconverts = new HashMap<DestinationFormat, ArrayList<Info>>();
        List<YouTubeDownloadLink> decryptedLinks = new LinkedList<YouTubeDownloadLink>();
        String param = videoUrl;
        String parameter = param.toString().replace("watch#!v", "watch?v");
        parameter = parameter.replaceFirst("(verify_age\\?next_url=\\/?)", "");
        parameter = parameter.replaceFirst("(%3Fv%3D)", "?v=");
        parameter = parameter.replaceFirst("(watch\\?.*?v)", "watch?v");
        parameter = parameter.replaceFirst("/embed/", "/watch?v=");
        parameter = parameter.replaceFirst("https", "http");

        this.br.setFollowRedirects(true);
        this.br.setCookiesExclusive(true);
        this.br.clearCookies("youtube.com");
        br.setCookie("http://youtube.com", "PREF", "hl=en-GB");
        if (parameter.contains("watch#")) {
            parameter = parameter.replace("watch#", "watch?");
        }
        if (parameter.contains("v/")) {
            String id = new Regex(parameter, "v/([a-z\\-_A-Z0-9]+)").getMatch(0);
            if (id != null) parameter = "http://www.youtube.com/watch?v=" + id;
        }

        ArrayList<String> linkstodecrypt = new ArrayList<String>();

        boolean prem = false;
//        boolean multiple_videos = false;
//        parameter = new Regex(parameter, "(http://www\\.youtube\\.com/watch\\?v=[a-z\\-_A-Z0-9]+).*?").getMatch(0);
        
        // Handle single video
        linkstodecrypt.add(parameter);
//        multiple_videos = false;
        

        boolean fast = false;
        final boolean best = false;
        final AtomicBoolean mp3 = new AtomicBoolean(false);
        final AtomicBoolean flv = new AtomicBoolean(false);
        
        
        /* http://en.wikipedia.org/wiki/YouTube */
        final HashMap<Integer, Object[]> ytVideo = new HashMap<Integer, Object[]>() {
            private static final long serialVersionUID = -3028718522449785181L;

            {
                boolean mp4 = true;
                boolean webm = true;
                boolean threegp = false;
                
                if (mp3.get() == false && mp4 == false && webm == false && flv.get() == false && threegp == false) {
                    /* if no container is selected, then everything is enabled */
                    mp3.set(true);
                    mp4 = true;
                    webm = true;
                    flv.set(true);
                    threegp = true;
                }

                boolean q240p = true;
                boolean q360p = true;
                boolean q480p = true;
                boolean q720p = true;
                boolean q1080p = true;
                boolean qOriginal = false;
                
                // **** FLV *****
                if (mp3.get()) {
                    this.put(0, new Object[] { DestinationFormat.AUDIOMP3, "H.263", "MP3", "Mono" });
                    this.put(5, new Object[] { DestinationFormat.AUDIOMP3, "H.263", "MP3", "Stereo" });
                    this.put(6, new Object[] { DestinationFormat.AUDIOMP3, "H.263", "MP3", "Mono" });
                }
                
                if (flv.get() || best) {
                    if (q240p || best) {
                        // video bit rate @ 0.25Mbit/second
                        this.put(5, new Object[] { DestinationFormat.VIDEOFLV, "H.263", "MP3", "Stereo", "240p" });
                        // video bit rate @ 0.8Mbit/second
                        this.put(6, new Object[] { DestinationFormat.VIDEOFLV, "H.263", "MP3", "Stereo", "240p" });
                    }
                    if (q360p || best) {
                        this.put(34, new Object[] { DestinationFormat.VIDEOFLV, "H.264", "AAC", "Stereo", "360p" });
                    }
                    if (q480p || best) {
                        this.put(35, new Object[] { DestinationFormat.VIDEOFLV, "H.264", "AAC", "Stereo", "480p" });
                    }
                }

                // **** 3GP *****
                if ((threegp && q240p) || best) {
                    this.put(13, new Object[] { DestinationFormat.VIDEO3GP, "H.263", "AAC", "Mono", "240p" });
                    this.put(36, new Object[] { DestinationFormat.VIDEO3GP, "H.264", "AAC", "Stereo", "240p" });
                }

                // **** MP4 *****
                if (mp4 || best) {
                    if (q240p || best) {
                        this.put(133, new Object[] { DestinationFormat.VIDEOMP4, "H.264", "AAC", "Stereo", "240p" });
                    }
                    if (q480p || best) {
                        this.put(135, new Object[] { DestinationFormat.VIDEOMP4, "H.264", "AAC", "Stereo", "480p" });
                    }
                    if (q360p || best) {
                        // 270p / 360p
                        this.put(18, new Object[] { DestinationFormat.VIDEOMP4, "H.264", "AAC", "Stereo", "360p" });
                        this.put(134, new Object[] { DestinationFormat.VIDEOMP4, "H.264", "AAC", "Stereo", "360p" });
                    }
                    if (q720p || best) {
                        this.put(136, new Object[] { DestinationFormat.VIDEOMP4, "H.264", "AAC", "Stereo", "720p" });
                        this.put(22, new Object[] { DestinationFormat.VIDEOMP4, "H.264", "AAC", "Stereo", "720p" });
                    }
                    if (q1080p || best) {
                        this.put(137, new Object[] { DestinationFormat.VIDEOMP4, "H.264", "AAC", "Stereo", "1080p" });
                        this.put(37, new Object[] { DestinationFormat.VIDEOMP4, "H.264", "AAC", "Stereo", "1080" });
                    }
                    if (qOriginal || best) {
                        this.put(38, new Object[] { DestinationFormat.VIDEOMP4, "H.264", "AAC", "Stereo", "Original" });
                    }
                }

                // **** WebM *****
                if (webm || best) {
                    if (q360p || best) {
                        this.put(43, new Object[] { DestinationFormat.VIDEOWEBM, "VP8", "Vorbis", "Stereo", "360p" });
                    }
                    if (q480p || best) {
                        this.put(44, new Object[] { DestinationFormat.VIDEOWEBM, "VP8", "Vorbis", "Stereo", "480p" });
                    }
                    if (q720p || best) {
                        this.put(45, new Object[] { DestinationFormat.VIDEOWEBM, "VP8", "Vorbis", "Stereo", "720p" });
                    }
                    if (q1080p || best) {
                        this.put(46, new Object[] { DestinationFormat.VIDEOWEBM, "VP8", "Vorbis", "Stereo", "1080p" });
                    }
                }
            }
        };

        // Force fast linkcheck if there are more then 20 videos in queue.
        if (linkstodecrypt.size() > 20) fast = true;

        for (String url : linkstodecrypt) {
            // Make an little sleep to prevent DDoS
            Thread.sleep(25);

            try {
                this.possibleconverts.clear();

                verifyAge  = false;
                final HashMap<Integer, String[]> LinksFound = this.getLinks(url, prem, this.br, 0);
                String error = br.getRegex("<div id=\"unavailable\\-message\" class=\"\">[\t\n\r ]+<span class=\"yt\\-alert\\-vertical\\-trick\"></span>[\t\n\r ]+<div class=\"yt\\-alert\\-message\">([^<>\"]*?)</div>").getMatch(0);
                // Removed due wrong offline detection
                // if (error == null) error =
                // br.getRegex("<div class=\"yt\\-alert\\-message\">(.*?)</div>").getMatch(0);
                if (error == null) error = br.getRegex("\\&reason=([^<>\"/]*?)\\&").getMatch(0);
                if (br.containsHTML(UNSUPPORTEDRTMP)) error = "RTMP video download isn't supported yet!";
                if ((LinksFound == null || LinksFound.isEmpty()) && error != null) {
                    error = Encoding.urlDecode(error, false);
                    LOG.info("Video unavailable: " + url);
                    LOG.info("Reason: " + error.trim());
                    continue;
                }
                if (LinksFound == null || LinksFound.isEmpty()) {
                    if (linkstodecrypt.size() == 1) {
                        if (verifyAge || this.br.getURL().toLowerCase(Locale.US).indexOf("youtube.com/get_video_info?") != -1 && !prem) { 
                            throw new IOException("Can't download this video with FrostWire, age verification required from youtube."); 
                        }
                        LOG.info("Video unavailable: " + url);
                        continue;
                    } else {
                        continue;
                    }
                }

                /* First get the filename */
                String YT_FILENAME = "";
                if (LinksFound.containsKey(-1)) {
                    YT_FILENAME = LinksFound.get(-1)[0];
                    LinksFound.remove(-1);
                }

                /* check for wished formats first */
                if (best) {
                    // 1080p
                    if (LinksFound.get(37) != null) {
                        String[] temp = LinksFound.get(37);
                        LinksFound.clear();
                        LinksFound.put(37, temp);
                        // 720p
                    } else if (LinksFound.get(45) != null || LinksFound.get(22) != null) {
                        String[] temp1 = LinksFound.get(45);
                        String[] temp2 = LinksFound.get(22);

                        LinksFound.clear();

                        if (temp1 != null) LinksFound.put(45, temp1);
                        if (temp2 != null) LinksFound.put(22, temp2);
                        // 480p
                    } else if (LinksFound.get(35) != null) {
                        String[] temp = LinksFound.get(35);
                        LinksFound.clear();
                        LinksFound.put(35, temp);
                        // 360p
                    } else if (LinksFound.get(43) != null || LinksFound.get(18) != null || LinksFound.get(34) != null) {
                        String[] temp1 = LinksFound.get(43);
                        String[] temp2 = LinksFound.get(18);
                        String[] temp3 = LinksFound.get(34);

                        LinksFound.clear();

                        if (temp1 != null) LinksFound.put(43, temp1);
                        if (temp2 != null) LinksFound.put(18, temp2);
                        if (temp3 != null) LinksFound.put(34, temp3);
                        // 240p
                    } else if (LinksFound.get(13) != null || LinksFound.get(17) != null || LinksFound.get(5) != null) {
                        String[] temp1 = LinksFound.get(13);
                        String[] temp2 = LinksFound.get(17);
                        String[] temp3 = LinksFound.get(5);

                        LinksFound.clear();

                        if (temp1 != null) LinksFound.put(13, temp1);
                        if (temp2 != null) LinksFound.put(17, temp2);
                        if (temp3 != null) LinksFound.put(5, temp3);
                    } else {
                        // Original
                        String[] temp = LinksFound.get(38);
                        LinksFound.clear();
                        LinksFound.put(38, temp);
                    }
                }

                String dlLink = "";
                String vQuality = "";
                DestinationFormat cMode = null;

                for (final Integer format : LinksFound.keySet()) {
                    if (ytVideo.containsKey(format)) {
                        cMode = (DestinationFormat) ytVideo.get(format)[0];
                        vQuality = "(" + LinksFound.get(format)[1] + "_" + ytVideo.get(format)[1] + "-" + ytVideo.get(format)[2] + ")";
                    } else {
                        cMode = DestinationFormat.UNKNOWN;
                        vQuality = "(" + LinksFound.get(format)[1] + "_" + format + ")";
                        /*
                         * we do not want to download unknown formats at the
                         * moment
                         */
                        continue;
                    }
                    dlLink = LinksFound.get(format)[0];
                    // Skip MP3 but handle 240p flv
                    if (!(format == 5 && mp3.get() && !flv.get())) {
                        try {
                            if (fast) {
                                this.addtopos(cMode, dlLink, 0, vQuality, format);
                            } else if (this.br.openGetConnection(dlLink).getResponseCode() == 200) {
                                Thread.sleep(200);
                                this.addtopos(cMode, dlLink, this.br.getHttpConnection().getLongContentLength(), vQuality, format);
                            }
                        } catch (final Throwable e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                this.br.getHttpConnection().disconnect();
                            } catch (final Throwable e) {
                            }
                        }
                    }
                    // Handle MP3
                    if ((format == 0 || format == 5 || format == 6) && mp3.get()) {
                        try {
                            if (fast) {
                                this.addtopos(DestinationFormat.AUDIOMP3, dlLink, 0, "", format);
                            } else if (this.br.openGetConnection(dlLink).getResponseCode() == 200) {
                                Thread.sleep(200);
                                this.addtopos(DestinationFormat.AUDIOMP3, dlLink, this.br.getHttpConnection().getLongContentLength(), "", format);
                            }
                        } catch (final Throwable e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                this.br.getHttpConnection().disconnect();
                            } catch (final Throwable e) {
                            }
                        }
                    }
                }

                
                
                for (final Entry<DestinationFormat, ArrayList<Info>> next : this.possibleconverts.entrySet()) {
                    final DestinationFormat convertTo = next.getKey();

                    for (final Info info : next.getValue()) {
                        //final DownloadLink thislink = this.createDownloadlink(info.link.replaceFirst("http", "httpJDYoutube"));
                        String link = info.link;
                        //thislink.setBrowserUrl(parameter);
                        //thislink.setFinalFileName(YT_FILENAME + info.desc + convertTo.getExtFirst());
                        long size = info.size;
                        String name = null;
                        if (convertTo != DestinationFormat.AUDIOMP3) {
                            name = YT_FILENAME + info.desc + convertTo.getExtFirst();
                            name = getValidFileName(name);
                        } else {
                            /*
                             * because demuxer will fail when mp3 file already
                             * exists
                             */
                            //name = YT_FILENAME + info.desc + ".tmp";
                            //thislink.setProperty("name", name);
                        }
                        //thislink.setProperty("convertto", convertTo.name());
                        //thislink.setProperty("videolink", parameter);
                        //thislink.setProperty("valid", true);
                        //thislink.setProperty("fmtNew", info.fmt);
                        //thislink.setProperty("LINKDUPEID", name);

                        decryptedLinks.add(new YouTubeDownloadLink(name,size,link,info.fmt));
                    }
                }

               
            } catch (final IOException e) {
                this.br.getHttpConnection().disconnect();
                LOG.warn("Exception occurred", e);
                // return null;
            }
        }

        return decryptedLinks;
    }
    
    private String getVideoID(String URL) {
        return new Regex(URL, "v=([a-z\\-_A-Z0-9]+)").getMatch(0);
    }
    
    private HashMap<Integer, String[]> parseLinks(String html5_fmt_map, boolean allowVideoOnly) {
        final HashMap<Integer, String[]> links = new HashMap<Integer, String[]>();
        if (html5_fmt_map != null) {
            if (html5_fmt_map.contains(UNSUPPORTEDRTMP)) { return links; }
            String[] html5_hits = new Regex(html5_fmt_map, "(.*?)(,|$)").getColumn(0);
            if (html5_hits != null) {
                for (String hit : html5_hits) {
                    hit = unescape(hit);
                    String hitUrl = new Regex(hit, "url=(http.*?)(\\&|$)").getMatch(0);
                    String sig = new Regex(hit, "url=http.*?(\\&|$)(sig|signature)=(.*?)(\\&|$)").getMatch(2);
                    if (sig == null) sig = new Regex(hit, "(sig|signature)=(.*?)(\\&|$)").getMatch(1);
                    if (sig == null) sig = new Regex(hit, "(sig|signature)%3D(.*?)%26").getMatch(1);
                    if (sig == null) sig = decryptSignature(new Regex(hit, "s=(.*?)(\\&|$)").getMatch(0));
                    String hitFmt = new Regex(hit, "itag=(\\d+)").getMatch(0);
                    String hitQ = new Regex(hit, "quality=(.*?)(\\&|$)").getMatch(0);
                    if (hitQ == null && allowVideoOnly) hitQ = "unknown";
                    if (hitUrl != null && hitFmt != null && hitQ != null) {
                        hitUrl = unescape(hitUrl.replaceAll("\\\\/", "/"));
                        if (hitUrl.startsWith("http%253A")) {
                            hitUrl = Encoding.htmlDecode(hitUrl);
                        }
                        String[] inst = null;
                        if (hitUrl.contains("sig")) {
                            inst = new String[] { Encoding.htmlDecode(Encoding.urlDecode(hitUrl, true)), hitQ };
                        } else {
                            inst = new String[] { Encoding.htmlDecode(Encoding.urlDecode(hitUrl, true) + "&signature=" + sig), hitQ };
                        }
                        links.put(Integer.parseInt(hitFmt), inst);
                    }
                }
            }
        }
        return links;
    }
    
    private HashMap<Integer, String[]> parseLinks(Browser br, final String videoURL, String YT_FILENAME, boolean ythack, boolean tryGetDetails) throws InterruptedException, IOException {
        final HashMap<Integer, String[]> links = new HashMap<Integer, String[]>();
        String html5_fmt_map = br.getRegex("\"html5_fmt_map\": \\[(.*?)\\]").getMatch(0);

        if (html5_fmt_map != null) {
            String[] html5_hits = new Regex(html5_fmt_map, "\\{(.*?)\\}").getColumn(0);
            if (html5_hits != null) {
                for (String hit : html5_hits) {
                    String hitUrl = new Regex(hit, "url\": \"(http:.*?)\"").getMatch(0);
                    String hitFmt = new Regex(hit, "itag\": (\\d+)").getMatch(0);
                    String hitQ = new Regex(hit, "quality\": \"(.*?)\"").getMatch(0);
                    if (hitUrl != null && hitFmt != null && hitQ != null) {
                        hitUrl = unescape(hitUrl.replaceAll("\\\\/", "/"));
                        links.put(Integer.parseInt(hitFmt), new String[] { Encoding.htmlDecode(Encoding.urlDecode(hitUrl, true)), hitQ });
                    }
                }
            }
        } else {
            /* new format since ca. 1.8.2011 */
            html5_fmt_map = br.getRegex("\"url_encoded_fmt_stream_map\": \"(.*?)\"").getMatch(0);
            if (html5_fmt_map == null) {
                html5_fmt_map = br.getRegex("url_encoded_fmt_stream_map=(.*?)(&|$)").getMatch(0);
                if (html5_fmt_map != null) {
                    html5_fmt_map = html5_fmt_map.replaceAll("%2C", ",");
                    if (!html5_fmt_map.contains("url=")) {
                        html5_fmt_map = html5_fmt_map.replaceAll("%3D", "=");
                        html5_fmt_map = html5_fmt_map.replaceAll("%26", "&");
                    }
                }
            }
            if (html5_fmt_map != null && !html5_fmt_map.contains("signature") && !html5_fmt_map.contains("sig") && !html5_fmt_map.contains("s=")) {
                Thread.sleep(5000);
                br.clearCookies("youtube.com");
                return null;
            }
            if (html5_fmt_map != null) {
                HashMap<Integer, String[]> ret = parseLinks(html5_fmt_map, false);
                if (ret.size() == 0) return links;
                links.putAll(ret);
//                if (false) {
//                    /* not playable by vlc */
//                    /* check for adaptive fmts */
//                    String adaptive = br.getRegex("\"adaptive_fmts\": \"(.*?)\"").getMatch(0);
//                    ret = parseLinks(adaptive, true);
//                    links.putAll(ret);
//                }
            } else {
                if (br.containsHTML("reason=Unfortunately")) return null;
                if (tryGetDetails == true) {
                    br.getPage("http://www.youtube.com/get_video_info?el=detailpage&video_id=" + getVideoID(videoURL));
                    return parseLinks(br, videoURL, YT_FILENAME, ythack, false);
                } else {
                    return null;
                }
            }
        }

        /* normal links */
        final HashMap<String, String> fmt_list = new HashMap<String, String>();
        String fmt_list_str = "";
        if (ythack) {
            fmt_list_str = (br.getMatch("&fmt_list=(.+?)&") + ",").replaceAll("%2F", "/").replaceAll("%2C", ",");
        } else {
            fmt_list_str = (br.getMatch("\"fmt_list\":\\s+\"(.+?)\",") + ",").replaceAll("\\\\/", "/");
        }
        final String fmt_list_map[][] = new Regex(fmt_list_str, "(\\d+)/(\\d+x\\d+)/\\d+/\\d+/\\d+,").getMatches();
        for (final String[] fmt : fmt_list_map) {
            fmt_list.put(fmt[0], fmt[1]);
        }
        if (links.size() == 0 && ythack) {
            /* try to find fallback links */
            String urls[] = br.getRegex("url%3D(.*?)($|%2C)").getColumn(0);
            int index = 0;
            for (String vurl : urls) {
                String hitUrl = new Regex(vurl, "(.*?)%26").getMatch(0);
                String hitQ = new Regex(vurl, "%26quality%3D(.*?)%").getMatch(0);
                if (hitUrl != null && hitQ != null) {
                    hitUrl = unescape(hitUrl.replaceAll("\\\\/", "/"));
                    if (fmt_list_map.length >= index) {
                        links.put(Integer.parseInt(fmt_list_map[index][0]), new String[] { Encoding.htmlDecode(Encoding.urlDecode(hitUrl, false)), hitQ });
                        index++;
                    }
                }
            }
        }
        for (Integer fmt : links.keySet()) {
            String fmt2 = fmt + "";
            if (fmt_list.containsKey(fmt2)) {
                String Videoq = links.get(fmt)[1];
                final Integer q = Integer.parseInt(fmt_list.get(fmt2).split("x")[1]);
                if (fmt == 17) {
                    Videoq = "144p";
                } else if (fmt == 40) {
                    Videoq = "240p Light";
                } else if (q > 1080) {
                    Videoq = "Original";
                } else if (q > 720) {
                    Videoq = "1080p";
                } else if (q > 576) {
                    Videoq = "720p";
                } else if (q > 480) {
                    Videoq = "520p";
                } else if (q > 360) {
                    Videoq = "480p";
                } else if (q > 240) {
                    Videoq = "360p";
                } else {
                    Videoq = "240p";
                }
                links.get(fmt)[1] = Videoq;
            }
        }
        if (YT_FILENAME != null && links != null && !links.isEmpty()) {
            links.put(-1, new String[] { YT_FILENAME });
        }
        return links;
    }

    public HashMap<Integer, String[]> getLinks(final String video, final boolean prem, Browser br, int retrycount) throws Exception {
        if (retrycount > 2) {
            // do not retry more often than 2 time
            return null;
        }
        if (br == null) {
            br = this.br;
        }

        try {
            //gsProxy(true);
        } catch (Throwable e) {
            /* does not exist in 09581 */
        }
        br.setFollowRedirects(true);
        /* this cookie makes html5 available and skip controversy check */
        br.setCookie("youtube.com", "PREF", "f2=40100000&hl=en-GB");
        br.getHeaders().put("User-Agent", "Wget/1.12");
        br.getPage(video);
        if (br.containsHTML("id=\"unavailable-submessage\" class=\"watch-unavailable-submessage\"")) { return null; }
        final String VIDEOID = new Regex(video, "watch\\?v=([\\w_\\-]+)").getMatch(0);
        boolean fileNameFound = false;
        String YT_FILENAME = VIDEOID;
        if (br.containsHTML("&title=")) {
            YT_FILENAME = Encoding.htmlDecode(br.getRegex("&title=([^&$]+)").getMatch(0).replaceAll("\\+", " ").trim());
            fileNameFound = true;
        }
        final String url = br.getURL();
        boolean ythack = false;
        if (url != null && !url.equals(video)) {
            /* age verify with activated premium? */
            if (url.toLowerCase(Locale.ENGLISH).indexOf("youtube.com/verify_age?next_url=") != -1) {
                verifyAge = true;
            }
            if (url.toLowerCase(Locale.ENGLISH).indexOf("youtube.com/verify_age?next_url=") != -1 && prem) {
                final String session_token = br.getRegex("onLoadFunc.*?gXSRF_token = '(.*?)'").getMatch(0);
                final LinkedHashMap<String, String> p = Request.parseQuery(url);
                final String next = p.get("next_url");
                final Form form = new Form();
                form.setAction(url);
                form.setMethod(MethodType.POST);
                form.put("next_url", "%2F" + next.substring(1));
                form.put("action_confirm", "Confirm+Birth+Date");
                form.put("session_token", Encoding.urlEncode(session_token));
                br.submitForm(form);
                if (br.getCookie("http://www.youtube.com", "is_adult") == null) { return null; }
            } else if (url.toLowerCase(Locale.ENGLISH).indexOf("youtube.com/index?ytsession=") != -1 || url.toLowerCase(Locale.ENGLISH).indexOf("youtube.com/verify_age?next_url=") != -1 && !prem) {
                ythack = true;
                br.getPage("http://www.youtube.com/get_video_info?video_id=" + VIDEOID);
                if (br.containsHTML("&title=") && fileNameFound == false) {
                    YT_FILENAME = Encoding.htmlDecode(br.getRegex("&title=([^&$]+)").getMatch(0).replaceAll("\\+", " ").trim());
                    fileNameFound = true;
                }
            } else if (url.toLowerCase(Locale.ENGLISH).indexOf("google.com/accounts/servicelogin?") != -1) {
                // private videos
                return null;
            }
        }
        Form forms[] = br.getForms();
        if (forms != null) {
            for (Form form : forms) {
                if (form.getAction() != null && form.getAction().contains("verify_age")) {
                    LOG.info("Verify Age");
                    br.submitForm(form);
                    break;
                }
            }
        }
        /* html5_fmt_map */
        if (br.getRegex(YT_FILENAME_PATTERN).count() != 0 && fileNameFound == false) {
            YT_FILENAME = Encoding.htmlDecode(br.getRegex(YT_FILENAME_PATTERN).getMatch(0).trim());
            fileNameFound = true;
        }
        HashMap<Integer, String[]> links = parseLinks(br, video, YT_FILENAME, ythack, false);
        return links;
    }
    
    /**
     * thx to youtube-dl
     * 
     * @param s
     * @return
     */
    private String decryptSignature(String s) {
        if (s == null) return s;
        StringBuilder sb = new StringBuilder();
        LOG.info("SigLength: " + s.length());
        if (s.length() == 92) {
            sb.append(s.charAt(25));
            sb.append(s.substring(3, 25));
            sb.append(s.charAt(0));
            sb.append(s.substring(26, 42));
            sb.append(s.charAt(79));
            sb.append(s.substring(43, 79));
            sb.append(s.charAt(91));
            sb.append(s.substring(80, 83));
        } else if (s.length() == 90) {
            sb.append(s.charAt(25));
            sb.append(s.substring(3, 25));
            sb.append(s.charAt(2));
            sb.append(s.substring(26, 40));
            sb.append(s.charAt(77));
            sb.append(s.substring(41, 77));
            sb.append(s.charAt(89));
            sb.append(s.substring(78, 81));
        } else if (s.length() == 88) {
            sb.append(s.charAt(48));
            sb.append(new StringBuilder(s.substring(68, 82)).reverse());
            sb.append(s.charAt(82));
            sb.append(new StringBuilder(s.substring(63, 67)).reverse());
            sb.append(s.charAt(85));
            sb.append(new StringBuilder(s.substring(49, 62)).reverse());
            sb.append(s.charAt(67));
            sb.append(new StringBuilder(s.substring(13, 48)).reverse());
            sb.append(s.charAt(3));
            sb.append(new StringBuilder(s.substring(4, 12)).reverse());
            sb.append(s.charAt(2));
            sb.append(s.charAt(12));
        } else if (s.length() == 87) {
            //sb.append(s.substring(4, 23));
            //sb.append(s.charAt(86));
            //sb.append(s.substring(24, 85));
            //s[83:53:-1] + s[3] + s[52:40:-1] + s[86] + s[39:10:-1] + s[0] + s[9:3:-1] + s[53]
            sb.append(new StringBuilder(s.substring(54, 84)).reverse());
            sb.append(s.charAt(3));
            sb.append(new StringBuilder(s.substring(41, 53)).reverse());
            sb.append(s.charAt(86));
            sb.append(new StringBuilder(s.substring(11, 40)).reverse());
            sb.append(s.charAt(0));
            sb.append(new StringBuilder(s.substring(4, 10)).reverse());
            sb.append(s.charAt(53));
        } else if (s.length() == 86) {
            sb.append(s.substring(83, 85));
            sb.append(s.charAt(26));
            sb.append(new StringBuilder(s.substring(47, 80)).reverse());
            sb.append(s.charAt(85));
            sb.append(new StringBuilder(s.substring(37, 46)).reverse());
            sb.append(s.charAt(30));
            sb.append(new StringBuilder(s.substring(31, 36)).reverse());
            sb.append(s.charAt(46));
            sb.append(new StringBuilder(s.substring(27, 30)).reverse());
            sb.append(s.charAt(82));
            sb.append(new StringBuilder(s.substring(2, 26)).reverse());
        } else if (s.length() == 85) {
            sb.append(s.substring(2, 8));
            sb.append(s.charAt(0));
            sb.append(s.substring(9, 21));
            sb.append(s.charAt(65));
            sb.append(s.substring(22, 65));
            sb.append(s.charAt(84));
            sb.append(s.substring(66, 82));
            sb.append(s.charAt(21));
        } else if (s.length() == 84) {
            sb.append(new StringBuilder(s.substring(37, 84)).reverse());
            sb.append(s.charAt(2));
            sb.append(new StringBuilder(s.substring(27, 36)).reverse());
            sb.append(s.charAt(3));
            sb.append(new StringBuilder(s.substring(4, 26)).reverse());
            sb.append(s.charAt(26));
        } else if (s.length() == 83) {
            sb.append(s.substring(0, 15));
            sb.append(s.charAt(80));
            sb.append(s.substring(16, 80));
            sb.append(s.charAt(15));
        } else if (s.length() == 82) {
            sb.append(s.charAt(36));
            sb.append(new StringBuilder(s.substring(68, 80)).reverse());
            sb.append(s.charAt(81));
            sb.append(new StringBuilder(s.substring(41, 67)).reverse());
            sb.append(s.charAt(33));
            sb.append(new StringBuilder(s.substring(37, 40)).reverse());
            sb.append(s.charAt(40));
            sb.append(s.charAt(35));
            sb.append(s.charAt(0));
            sb.append(s.charAt(67));
            sb.append(new StringBuilder(s.substring(1, 33)).reverse());
            sb.append(s.charAt(34));
        } else if (s.length() == 81) {
            sb.append(s.charAt(56));
            sb.append(new StringBuilder(s.substring(57, 80)).reverse());
            sb.append(s.charAt(41));
            sb.append(new StringBuilder(s.substring(42, 56)).reverse());
            sb.append(s.charAt(80));
            sb.append(new StringBuilder(s.substring(35, 41)).reverse());
            sb.append(s.charAt(0));
            sb.append(new StringBuilder(s.substring(30, 34)).reverse());
            sb.append(s.charAt(34));
            sb.append(new StringBuilder(s.substring(10, 29)).reverse());
            sb.append(s.charAt(29));
            sb.append(new StringBuilder(s.substring(1, 9)).reverse());
            sb.append(s.charAt(9));
        } else if (s.length() == 79) {
            sb.append(s.charAt(54));
            sb.append(new StringBuilder(s.substring(55, 78)).reverse());
            sb.append(s.charAt(39));
            sb.append(new StringBuilder(s.substring(40, 54)).reverse());
            sb.append(s.charAt(78));
            sb.append(new StringBuilder(s.substring(35, 39)).reverse());
            sb.append(s.charAt(0));
            sb.append(new StringBuilder(s.substring(30, 34)).reverse());
            sb.append(s.charAt(34));
            sb.append(new StringBuilder(s.substring(10, 29)).reverse());
            sb.append(s.charAt(29));
            sb.append(new StringBuilder(s.substring(1, 9)).reverse());
            sb.append(s.charAt(9));
        } else {
            LOG.info("Unsupported SigLength: " + s.length());
            return null;
        }
        return sb.toString();
    }
    
    private void addtopos(final DestinationFormat mode, final String link, final long size, final String desc, final int fmt) {
        ArrayList<Info> info = this.possibleconverts.get(mode);
        if (info == null) {
            info = new ArrayList<Info>();
            this.possibleconverts.put(mode, info);
        }
        final Info tmp = new Info();
        tmp.link = link;
        tmp.size = size;
        tmp.desc = desc;
        tmp.fmt = fmt;
        info.add(tmp);
    }
    
    public static String unescape(final String s) {
        if (s == null) return null;
        char ch;
        char ch2;
        final StringBuilder sb = new StringBuilder();
        int ii;
        int i;
        for (i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
            // prevents StringIndexOutOfBoundsException with ending char equals
            // case trigger
            if (s.length() != i + 1) {
                switch (ch) {
                case '%':
                case '\\':
                    ch2 = ch;
                    ch = s.charAt(++i);
                    StringBuilder sb2 = null;
                    switch (ch) {
                    case 'u':
                        /* unicode */
                        sb2 = new StringBuilder();
                        i++;
                        ii = i + 4;
                        for (; i < ii; i++) {
                            ch = s.charAt(i);
                            if (sb2.length() > 0 || ch != '0') {
                                sb2.append(ch);
                            }
                        }
                        i--;
                        sb.append((char) Long.parseLong(sb2.toString(), 16));
                        continue;
                    case 'x':
                        /* normal hex coding */
                        sb2 = new StringBuilder();
                        i++;
                        ii = i + 2;
                        for (; i < ii; i++) {
                            ch = s.charAt(i);
                            sb2.append(ch);
                        }
                        i--;
                        sb.append((char) Long.parseLong(sb2.toString(), 16));
                        continue;
                    default:
                        if (ch2 == '%') {
                            sb.append(ch2);
                        }
                        sb.append(ch);
                        continue;
                    }
                }
            }
            sb.append(ch);
        }
        return sb.toString();
    }
    
    public static enum DestinationFormat {
        AUDIOMP3("Audio (MP3)", new String[] { ".mp3" }),
        VIDEOFLV("Video (FLV)", new String[] { ".flv" }),
        VIDEOMP4("Video (MP4)", new String[] { ".mp4" }),
        VIDEOWEBM("Video (Webm)", new String[] { ".webm" }),
        VIDEO3GP("Video (3GP)", new String[] { ".3gp" }),
        UNKNOWN("Unknown (unk)", new String[] { ".unk" }),
        VIDEOIPHONE("Video (IPhone)", new String[] { ".mp4" });

        private String   text;
        private String[] ext;

        DestinationFormat(final String text, final String[] ext) {
            this.text = text;
            this.ext = ext;
        }

        public String getExtFirst() {
            return this.ext[0];
        }

        public String getText() {
            return this.text;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }
    
    static class Info {
        public String link;
        public long   size;
        public int    fmt;
        public String desc;
    }
    
    private static String getValidFileName(String fileName) {
        String newFileName = fileName.replaceAll("[\\\\/:*?\"<>|\\[\\]]+", "_");
        return newFileName;
    }
}
