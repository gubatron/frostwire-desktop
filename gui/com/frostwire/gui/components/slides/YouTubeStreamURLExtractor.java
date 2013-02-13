package com.frostwire.gui.components.slides;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import jd.http.Browser;
import jd.http.Request;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.parser.html.Form.MethodType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.util.FileUtils;

public class YouTubeStreamURLExtractor {
    private static Log LOG = LogFactory.getLog(YouTubeStreamURLExtractor.class);
    
    static public final Pattern YT_FILENAME_PATTERN = Pattern.compile("<meta name=\"title\" content=\"(.*?)\">", Pattern.CASE_INSENSITIVE);
    private HashMap<DestinationFormat, ArrayList<Info>> possibleconverts;

    private final String youtubePageUrl;
    
    public YouTubeStreamURLExtractor(String youtubePageUrl) {
        this.youtubePageUrl = youtubePageUrl;
    }
    
    private HashMap<Integer, String[]> getLinks(final String video, final boolean prem, Browser br, int retrycount) throws Exception {
        if (retrycount > 2) {
            // do not retry more often than 2 time
            return null;
        }
        //        if (br == null) {
        //            br = this.br;
        //        }
        br.setFollowRedirects(true);
        /* this cookie makes html5 available and skip controversy check */
        br.setCookie("youtube.com", "PREF", "f2=40100000");
        br.getHeaders().put("User-Agent", "Wget/1.12");
        br.getPage(video);
        if (br.containsHTML("id=\"unavailable-submessage\" class=\"watch-unavailable-submessage\"")) {
            return null;
        }
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
            //            if (url.toLowerCase(Locale.ENGLISH).indexOf("youtube.com/verify_age?next_url=") != -1) {
            //                verifyAge = true;
            //            }
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
                if (br.getCookie("http://www.youtube.com", "is_adult") == null) {
                    return null;
                }
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
        /* html5_fmt_map */
        if (br.getRegex(YT_FILENAME_PATTERN).count() != 0 && fileNameFound == false) {
            YT_FILENAME = Encoding.htmlDecode(br.getRegex(YT_FILENAME_PATTERN).getMatch(0).trim());
            fileNameFound = true;
        }
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
            if (html5_fmt_map != null && !html5_fmt_map.contains("signature") && !html5_fmt_map.contains("sig")) {
                Thread.sleep(5000);
                br.clearCookies("youtube.com");
                return getLinks(video, prem, br, retrycount + 1);
            }
            if (html5_fmt_map != null) {
                String[] html5_hits = new Regex(html5_fmt_map, "(.*?)(,|$)").getColumn(0);
                if (html5_hits != null) {
                    for (String hit : html5_hits) {
                        hit = unescape(hit);
                        String hitUrl = new Regex(hit, "url=(http.*?)(\\&|$)").getMatch(0);
                        String sig = new Regex(hit, "url=http.*?(\\&|$)(sig|signature)=(.*?)(\\&|$)").getMatch(2);
                        String hitFmt = new Regex(hit, "itag=(\\d+)").getMatch(0);
                        String hitQ = new Regex(hit, "quality=(.*?)(\\&|$)").getMatch(0);
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
                if (fmt == 40) {
                    Videoq = "240p Light";
                } else if (q > 1080) {
                    Videoq = "Original";
                } else if (q > 720) {
                    Videoq = "1080p";
                } else if (q > 576) {
                    Videoq = "720p";
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
        if (YT_FILENAME != null) {
            links.put(-1, new String[] { YT_FILENAME });
        }
        return links;
    }
    
    public String getYoutubeStreamURL() throws Exception {
        this.possibleconverts = new HashMap<DestinationFormat, ArrayList<Info>>();
        String decryptedLink = null;

        String param = youtubePageUrl;

        String parameter = param.toString().replace("watch#!v", "watch?v");
        parameter = parameter.replaceFirst("(verify_age\\?next_url=\\/?)", "");
        parameter = parameter.replaceFirst("(%3Fv%3D)", "?v=");
        parameter = parameter.replaceFirst("(watch\\?.*?v)", "watch?v");
        parameter = parameter.replaceFirst("/embed/", "/watch?v=");
        parameter = parameter.replaceFirst("https", "http");

        Browser br = new Browser();

        br.setFollowRedirects(true);
        br.setCookiesExclusive(true);
        br.clearCookies("youtube.com");

        if (parameter.contains("watch#")) {
            parameter = parameter.replace("watch#", "watch?");
        }
        if (parameter.contains("v/")) {
            String id = new Regex(parameter, "v/([a-z\\-_A-Z0-9]+)").getMatch(0);
            if (id != null)
                parameter = "http://www.youtube.com/watch?v=" + id;
        }

        boolean prem = false;

        try {
            final HashMap<Integer, String[]> LinksFound = this.getLinks(parameter, prem, br, 0);
            String error = br.getRegex("<div id=\"unavailable\\-message\" class=\"\">[\t\n\r ]+<span class=\"yt\\-alert\\-vertical\\-trick\"></span>[\t\n\r ]+<div class=\"yt\\-alert\\-message\">([^<>\"]*?)</div>").getMatch(0);
            if (error == null)
                error = br.getRegex("<div class=\"yt\\-alert\\-message\">(.*?)</div>").getMatch(0);
            if ((LinksFound == null || LinksFound.isEmpty()) && error != null) {
                //logger.info("Video unavailable: " + parameter);
                //logger.info("Reason: " + error.trim());
                return decryptedLink;
            }
            if (LinksFound == null || LinksFound.isEmpty()) {
                if (br.getURL().toLowerCase(Locale.getDefault()).indexOf("youtube.com/get_video_info?") != -1 && !prem) {
                    throw new IOException("DecrypterException.ACCOUNT");
                }
                throw new IOException("Video no longer available");
            }

            /* First get the filename */
            String YT_FILENAME = "";
            if (LinksFound.containsKey(-1)) {
                YT_FILENAME = LinksFound.get(-1)[0];
                LinksFound.remove(-1);
            }

            final boolean fast = false;//cfg.getBooleanProperty("FAST_CHECK2", false);
            //final boolean mp3 = cfg.getBooleanProperty("ALLOW_MP3", true);
            final boolean mp4 = true;//cfg.getBooleanProperty("ALLOW_MP4", true);
            //final boolean webm = cfg.getBooleanProperty("ALLOW_WEBM", true);
            //final boolean flv = cfg.getBooleanProperty("ALLOW_FLV", true);
            //final boolean threegp = cfg.getBooleanProperty("ALLOW_3GP", true);

            /* http://en.wikipedia.org/wiki/YouTube */
            final HashMap<Integer, Object[]> ytVideo = new HashMap<Integer, Object[]>() {
                /**
                 * 
                 */
                private static final long serialVersionUID = -3028718522449785181L;

                {
                    // **** FLV *****
                    //                if (mp3) {
                    //                    this.put(0, new Object[] { DestinationFormat.VIDEOFLV, "H.263", "MP3", "Mono" });
                    //                    this.put(5, new Object[] { DestinationFormat.VIDEOFLV, "H.263", "MP3", "Stereo" });
                    //                    this.put(6, new Object[] { DestinationFormat.VIDEOFLV, "H.263", "MP3", "Mono" });
                    //                }
                    //                if (flv) {
                    //                    this.put(34, new Object[] { DestinationFormat.VIDEOFLV, "H.264", "AAC", "Stereo" });
                    //                    this.put(35, new Object[] { DestinationFormat.VIDEOFLV, "H.264", "AAC", "Stereo" });
                    //                }

                    // **** 3GP *****
                    //                if (threegp) {
                    //                    this.put(13, new Object[] { DestinationFormat.VIDEO3GP, "H.263", "AMR", "Mono" });
                    //                    this.put(17, new Object[] { DestinationFormat.VIDEO3GP, "H.264", "AAC", "Stereo" });
                    //                }
                    // **** MP4 *****
                    if (mp4) {
                        this.put(18, new Object[] { DestinationFormat.VIDEOMP4, "H.264", "AAC", "Stereo" });
                        this.put(22, new Object[] { DestinationFormat.VIDEOMP4, "H.264", "AAC", "Stereo" });
                        this.put(37, new Object[] { DestinationFormat.VIDEOMP4, "H.264", "AAC", "Stereo" });
                        //this.put(38, new Object[] { DestinationFormat.VIDEOMP4, "H.264", "AAC", "Stereo" });
                    }
                    // **** WebM *****
                    //                if (webm) {
                    //                    this.put(43, new Object[] { DestinationFormat.VIDEOWEBM, "VP8", "Vorbis", "Stereo" });
                    //                    this.put(45, new Object[] { DestinationFormat.VIDEOWEBM, "VP8", "Vorbis", "Stereo" });
                    //                }
                }
            };

            /* check for wished formats first */
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
                try {
                    if (fast) {
                        this.addtopos(cMode, dlLink, 0, vQuality, format);
                    } else if (br.openGetConnection(dlLink).getResponseCode() == 200) {
                        this.addtopos(cMode, dlLink, br.getHttpConnection().getLongContentLength(), vQuality, format);
                    }
                } catch (final Throwable e) {
                    LOG.error("Error in youtube decrypt logic", e);
                } finally {
                    try {
                        br.getHttpConnection().disconnect();
                    } catch (final Throwable e) {
                    }
                }
            }

            int lastFmt = 0;
            for (final Entry<DestinationFormat, ArrayList<Info>> next : this.possibleconverts.entrySet()) {
                final DestinationFormat convertTo = next.getKey();
                for (final Info info : next.getValue()) {
                    final HttpDownloadLink thislink = new HttpDownloadLink(info.link);
                    //thislink.setBrowserUrl(parameter);
                    //thislink.setFinalFileName(YT_FILENAME + info.desc + convertTo.getExtFirst());
                    thislink.setSize(info.size);
                    String name = null;
                    if (convertTo != DestinationFormat.AUDIOMP3) {
                        name = YT_FILENAME + info.desc + convertTo.getExtFirst();
                        name = FileUtils.getValidFileName(name);
                        thislink.setFileName(name);
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

                    if (lastFmt < info.fmt) {
                        decryptedLink = thislink.getUrl();
                    }
                }
            }
        } catch (final IOException e) {
            br.getHttpConnection().disconnect();
            //logger.log(java.util.logging.Level.SEVERE, "Exception occurred", e);
            return null;
        }

        return decryptedLink;
    }
    
    private static String unescape(final String s) {
        char ch;
        char ch2;
        final StringBuilder sb = new StringBuilder();
        int ii;
        int i;
        for (i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
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
            sb.append(ch);
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
    
    public class HttpDownloadLink {

        private String url;
        private long size;
        private String filename;
        private String displayName;
        private boolean compressed;

        public HttpDownloadLink(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getFileName() {
            return filename;
        }

        public void setFileName(String name) {
            this.filename = name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public boolean isCompressed() {
            return compressed;
        }

        public void setCompressed(boolean compressed) {
            this.compressed = compressed;
        }
    }

    
    static class Info {
        public String link;
        public long size;
        public int fmt;
        public String desc;
    }

    public static enum DestinationFormat {
        AUDIOMP3("Audio (MP3)", new String[] { ".mp3" }), VIDEOFLV("Video (FLV)", new String[] { ".flv" }), VIDEOMP4("Video (MP4)", new String[] { ".mp4" }), VIDEOWEBM("Video (Webm)", new String[] { ".webm" }), VIDEO3GP("Video (3GP)", new String[] { ".3gp" }), UNKNOWN("Unknown (unk)",
                new String[] { ".unk" }),

        VIDEOIPHONE("Video (IPhone)", new String[] { ".mp4" });

        private String text;
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
}
