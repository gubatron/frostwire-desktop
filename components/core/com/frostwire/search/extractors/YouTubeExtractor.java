package com.frostwire.search.extractors;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import jd.http.Browser;
import jd.http.Request;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.parser.html.Form.MethodType;

public class YouTubeExtractor {

    static class Info {
        public String filename;
        public Date date;
        public String link;
        public long size;
        public int fmt;
        public String desc;
        public ThumbnailLinks thumbnails;
        public String videoId;
        public String user;
        public String channel;
    }

    public static class ThumbnailLinks {
        public String normal;
        public String mq;
        public String hq;
        public String maxres;
    }

    private final Pattern YT_FILENAME_PATTERN = Pattern.compile("<meta name=\"title\" content=\"(.*?)\">", Pattern.CASE_INSENSITIVE);
    private final String UNSUPPORTEDRTMP = "itag%2Crtmpe%2";

    public List<Info> extract(String videoUrl) throws Exception {
        // Make an little sleep to prevent DDoS
        Thread.sleep(200);

        Browser br = new Browser();
        String currentVideoUrl = videoUrl;

        HashMap<Integer, String> LinksFound = this.getLinks(currentVideoUrl, false, br);
        String error = br.getRegex("<div id=\"unavailable\\-message\" class=\"\">[\t\n\r ]+<span class=\"yt\\-alert\\-vertical\\-trick\"></span>[\t\n\r ]+<div class=\"yt\\-alert\\-message\">([^<>\"]*?)</div>").getMatch(0);
        // Removed due wrong offline detection
        // if (error == null) error = br.getRegex("<div class=\"yt\\-alert\\-message\">(.*?)</div>").getMatch(0);
        if (error == null)
            error = br.getRegex("reason=([^<>\"/]*?)(\\&|$)").getMatch(0);
        if (br.containsHTML(UNSUPPORTEDRTMP))
            error = "RTMP video download isn't supported yet!";
        if ((LinksFound == null || LinksFound.isEmpty()) && error != null) {
            error = Encoding.urlDecode(error, false);
            log("Video unavailable: " + currentVideoUrl);
            if (error != null)
                log("Reason: " + error.trim());
            return Collections.emptyList();
        }
        String videoId = getVideoID(currentVideoUrl);

        /** FILENAME PART1 START */
        String YT_FILENAME = "";
        if (LinksFound.containsKey(-1)) {
            YT_FILENAME = LinksFound.get(-1);
            LinksFound.remove(-1);
        }
        // replacing default Locate to be compatible with page language
        Locale locale = Locale.ENGLISH;
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy", locale);
        String date = br.getRegex("id=\"eow-date\" class=\"watch-video-date\" >(\\d{2}\\.\\d{2}\\.\\d{4})</span>").getMatch(0);
        if (date == null) {
            formatter = new SimpleDateFormat("dd MMM yyyy", locale);
            date = br.getRegex("class=\"watch-video-date\" >([ ]+)?(\\d{1,2} [A-Za-z]{3} \\d{4})</span>").getMatch(1);
        }
        final String channelName = br.getRegex("feature=watch\"[^>]+dir=\"ltr[^>]+>(.*?)</a>(\\s+)?<span class=\"yt-user").getMatch(0);
        // userName != channelName
        final String userName = br.getRegex("temprop=\"url\" href=\"http://(www\\.)?youtube\\.com/user/([^<>\"]*?)\"").getMatch(1);

        ThumbnailLinks thumbnailLinks = createThumbnailLink(videoId);

        List<Info> infos = new LinkedList<Info>();

        for (final Integer format : LinksFound.keySet()) {
            String dlLink = LinksFound.get(format);

            try {
                if (br.openGetConnection(dlLink).getResponseCode() == 200) {
                    Thread.sleep(200);
                    final Info tmp = new Info();
                    tmp.filename = YT_FILENAME;
                    tmp.date = date != null ? formatter.parse(date) : null;
                    tmp.link = dlLink;
                    tmp.size = br.getHttpConnection().getLongContentLength();
                    //tmp.desc = desc;
                    tmp.fmt = format;
                    tmp.thumbnails = thumbnailLinks;
                    tmp.videoId = videoId;
                    tmp.user = userName;
                    tmp.channel = channelName;
                    infos.add(tmp);
                }
            } catch (final Throwable e) {
                e.printStackTrace();
            } finally {
                try {
                    br.getHttpConnection().disconnect();
                } catch (final Throwable e) {
                }
            }
        }

        return infos;
    }

    private ThumbnailLinks createThumbnailLink(String videoId) {
        ThumbnailLinks links = new ThumbnailLinks();
        links.normal = "http://img.youtube.com/vi/" + videoId + "/default.jpg";
        links.mq = "http://img.youtube.com/vi/" + videoId + "/mqdefault.jpg";
        links.hq = "http://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
        links.maxres = "http://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
        return links;
    }

    public HashMap<Integer, String> getLinks(final String video, final boolean prem, Browser br) throws Exception {
        br.setFollowRedirects(true);
        /* this cookie makes html5 available and skip controversy check */
        br.setCookie("youtube.com", "PREF", "f2=40100000&hl=en-GB");
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
            if (url.toLowerCase(Locale.ENGLISH).indexOf("youtube.com/verify_age?next_url=") != -1) {
                //verifyAge = true;
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
        Form forms[] = br.getForms();
        if (forms != null) {
            for (Form form : forms) {
                if (form.getAction() != null && form.getAction().contains("verify_age")) {
                    log("Verify Age");
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
        HashMap<Integer, String> links = parseLinks(br, video, YT_FILENAME, ythack, false);
        return links;
    }

    private HashMap<Integer, String> parseLinks(Browser br, final String videoURL, String YT_FILENAME, boolean ythack, boolean tryGetDetails) throws InterruptedException, IOException {
        final HashMap<Integer, String> links = new HashMap<Integer, String>();
        String html5_fmt_map = br.getRegex("\"html5_fmt_map\": \\[(.*?)\\]").getMatch(0);

        if (html5_fmt_map != null) {
            String[] html5_hits = new Regex(html5_fmt_map, "\\{(.*?)\\}").getColumn(0);
            if (html5_hits != null) {
                for (String hit : html5_hits) {
                    String hitUrl = new Regex(hit, "url\": \"(http:.*?)\"").getMatch(0);
                    String hitFmt = new Regex(hit, "itag\": (\\d+)").getMatch(0);
                    if (hitUrl != null && hitFmt != null) {
                        hitUrl = unescape(hitUrl.replaceAll("\\\\/", "/"));
                        links.put(Integer.parseInt(hitFmt), Encoding.htmlDecode(Encoding.urlDecode(hitUrl, true)));
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
                HashMap<Integer, String> ret = parseLinks(html5_fmt_map);
                if (ret.size() == 0)
                    return links;
                links.putAll(ret);
                if (true) {
                    /* not playable by vlc */
                    /* check for adaptive fmts */
                    String adaptive = br.getRegex("\"adaptive_fmts\": \"(.*?)\"").getMatch(0);
                    ret = parseLinks(adaptive);
                    links.putAll(ret);
                }
            } else {
                if (br.containsHTML("reason=Unfortunately"))
                    return null;
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
                        links.put(Integer.parseInt(fmt_list_map[index][0]), Encoding.htmlDecode(Encoding.urlDecode(hitUrl, false)));
                        index++;
                    }
                }
            }
        }
        if (YT_FILENAME != null && links != null && !links.isEmpty()) {
            links.put(-1, YT_FILENAME);
        }
        return links;
    }

    private HashMap<Integer, String> parseLinks(String html5_fmt_map) {
        final HashMap<Integer, String> links = new HashMap<Integer, String>();
        if (html5_fmt_map != null) {
            if (html5_fmt_map.contains(UNSUPPORTEDRTMP)) {
                return links;
            }
            String[] html5_hits = new Regex(html5_fmt_map, "(.*?)(,|$)").getColumn(0);
            if (html5_hits != null) {
                for (String hit : html5_hits) {
                    hit = unescape(hit);
                    String hitUrl = new Regex(hit, "url=(http.*?)(\\&|$)").getMatch(0);
                    String sig = new Regex(hit, "url=http.*?(\\&|$)(sig|signature)=(.*?)(\\&|$)").getMatch(2);
                    if (sig == null)
                        sig = new Regex(hit, "(sig|signature)=(.*?)(\\&|$)").getMatch(1);
                    if (sig == null)
                        sig = new Regex(hit, "(sig|signature)%3D(.*?)%26").getMatch(1);
                    if (sig == null)
                        sig = decryptSignature(new Regex(hit, "s=(.*?)(\\&|$)").getMatch(0));
                    String hitFmt = new Regex(hit, "itag=(\\d+)").getMatch(0);
                    if (hitUrl != null && hitFmt != null) {
                        hitUrl = unescape(hitUrl.replaceAll("\\\\/", "/"));
                        if (hitUrl.startsWith("http%253A")) {
                            hitUrl = Encoding.htmlDecode(hitUrl);
                        }
                        String inst = null;
                        if (hitUrl.contains("sig")) {
                            inst = Encoding.htmlDecode(Encoding.urlDecode(hitUrl, true));
                        } else {
                            inst = Encoding.htmlDecode(Encoding.urlDecode(hitUrl, true) + "&signature=" + sig);
                        }
                        links.put(Integer.parseInt(hitFmt), inst);
                    }
                }
            }
        }
        return links;
    }

    private String getVideoID(String URL) {
        String vuid = new Regex(URL, "v=([A-Za-z0-9\\-_]+)").getMatch(0);
        if (vuid == null) {
            vuid = new Regex(URL, "(v|embed)/([A-Za-z0-9\\-_]+)").getMatch(1);
        }
        return vuid;
    }

    /**
     * thx to youtube-dl
     * 
     * @param s
     * @return
     */
    private String decryptSignature(String s) {
        if (s == null)
            return s;
        StringBuilder sb = new StringBuilder();
        log("SigLength: " + s.length());
        if (s.length() == 93) {
            sb.append(new StringBuilder(s.substring(30, 87)).reverse());
            sb.append(s.charAt(88));
            sb.append(new StringBuilder(s.substring(6, 29)).reverse());
        } else if (s.length() == 92) {
            sb.append(s.charAt(25));
            sb.append(s.substring(3, 25));
            sb.append(s.charAt(0));
            sb.append(s.substring(26, 42));
            sb.append(s.charAt(79));
            sb.append(s.substring(43, 79));
            sb.append(s.charAt(91));
            sb.append(s.substring(80, 83));
        } else if (s.length() == 91) {
            sb.append(new StringBuilder(s.substring(28, 85)).reverse());
            sb.append(s.charAt(86));
            sb.append(new StringBuilder(s.substring(6, 27)).reverse());
        } else if (s.length() == 90) {
            sb.append(s.charAt(25));
            sb.append(s.substring(3, 25));
            sb.append(s.charAt(2));
            sb.append(s.substring(26, 40));
            sb.append(s.charAt(77));
            sb.append(s.substring(41, 77));
            sb.append(s.charAt(89));
            sb.append(s.substring(78, 81));
        } else if (s.length() == 89) {
            sb.append(new StringBuilder(s.substring(79, 85)).reverse());
            sb.append(s.charAt(87));
            sb.append(new StringBuilder(s.substring(61, 78)).reverse());
            sb.append(s.charAt(0));
            sb.append(new StringBuilder(s.substring(4, 60)).reverse());
        } else if (s.length() == 88) {
            sb.append(s.substring(7, 28));
            sb.append(s.charAt(87));
            sb.append(s.substring(29, 45));
            sb.append(s.charAt(55));
            sb.append(s.substring(46, 55));
            sb.append(s.charAt(2));
            sb.append(s.substring(56, 87));
            sb.append(s.charAt(28));
        } else if (s.length() == 87) {
            sb.append(s.substring(6, 27));
            sb.append(s.charAt(4));
            sb.append(s.substring(28, 39));
            sb.append(s.charAt(27));
            sb.append(s.substring(40, 59));
            sb.append(s.charAt(2));
            sb.append(s.substring(60));
        } else if (s.length() == 86) {
            sb.append(new StringBuilder(s.substring(73, 81)).reverse());
            sb.append(s.charAt(16));
            sb.append(new StringBuilder(s.substring(40, 72)).reverse());
            sb.append(s.charAt(72));
            sb.append(new StringBuilder(s.substring(17, 39)).reverse());
            sb.append(s.charAt(82));
            sb.append(new StringBuilder(s.substring(0, 16)).reverse());
        } else if (s.length() == 85) {
            sb.append(s.substring(3, 11));
            sb.append(s.charAt(0));
            sb.append(s.substring(12, 55));
            sb.append(s.charAt(84));
            sb.append(s.substring(56, 84));
        } else if (s.length() == 84) {
            sb.append(new StringBuilder(s.substring(71, 79)).reverse());
            sb.append(s.charAt(14));
            sb.append(new StringBuilder(s.substring(38, 70)).reverse());
            sb.append(s.charAt(70));
            sb.append(new StringBuilder(s.substring(15, 37)).reverse());
            sb.append(s.charAt(80));
            sb.append(new StringBuilder(s.substring(0, 13)).reverse());
        } else if (s.length() == 83) {
            sb.append(new StringBuilder(s.substring(64, 81)).reverse());
            sb.append(s.charAt(0));
            sb.append(new StringBuilder(s.substring(1, 63)).reverse());
            sb.append(s.charAt(63));
        } else if (s.length() == 82) {
            sb.append(new StringBuilder(s.substring(38, 81)).reverse());
            sb.append(s.charAt(7));
            sb.append(new StringBuilder(s.substring(8, 37)).reverse());
            sb.append(s.charAt(0));
            sb.append(new StringBuilder(s.substring(1, 7)).reverse());
            sb.append(s.charAt(37));
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
        } else if (s.length() == 80) {
            sb.append(s.substring(1, 19));
            sb.append(s.charAt(0));
            sb.append(s.substring(20, 68));
            sb.append(s.charAt(19));
            sb.append(s.substring(69, 80));
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
            log("Unsupported SigLength: " + s.length());
            return null;
        }
        return sb.toString();
    }

    public static String unescape(final String s) {
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

    private void log(String message) {
        //System.out.println(message);
    }

    public static void main(String[] args) throws Exception {
        YouTubeExtractor yt = new YouTubeExtractor();
        List<Info> list = yt.extract();

        for (Info inf : list) {
            System.out.println(inf.fmt);
        }
    }
}
