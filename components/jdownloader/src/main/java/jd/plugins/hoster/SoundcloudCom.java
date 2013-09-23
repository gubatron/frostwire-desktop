//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
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

package jd.plugins.hoster;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jd.PluginWrapper;
import jd.config.Property;
import jd.config.SubConfiguration;
import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.locale.JDL;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "soundcloud.com" }, urls = { "https://(www\\.)?soundclouddecrypted\\.com/[a-z\\-_0-9]+/[a-z\\-_0-9]+" }, flags = { 0 })
public class SoundcloudCom extends PluginForHost {
    
    private static final String CUSTOM_DATE        = "CUSTOM_DATE";
    private static final String CUSTOM_FILENAME    = "CUSTOM_FILENAME";
    
    private final static String defaultCustomFilename    = "*songtitle* - *channelname**ext*";
    private final static String defaultCustomPackagename = "*channelname* - *playlistname*";

    private static final long MAX_ACCEPTABLE_SOUNDCLOUD_FILESIZE_FOR_COVERART_FETCH = 10485760; //10MB

    private String url;

    public SoundcloudCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    public final static String CLIENTID = "b45b1aa10f1ac2941910a7f0d10f8e28";

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("soundclouddecrypted", "soundcloud"));
    }

    @Override
    public String getAGBLink() {
        return "http://soundcloud.com/terms-of-use";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(final DownloadLink link) throws Exception {
        requestFileInformation(link);
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, url, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            logger.warning("The final dllink seems not to be a file!");
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (dl.startDownload()) {
            this.postprocess(link);
        }
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink parameter) throws Exception {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        url = parameter.getStringProperty("directlink");
        if (url != null) {
            checkDirectLink(parameter, url);
            if (url != null)
                return AvailableStatus.TRUE;
        }
        br.getPage("https://api.sndcdn.com/resolve?url=" + Encoding.urlEncode(parameter.getDownloadURL()) + "&_status_code_map%5B302%5D=200&_status_format=json&client_id=" + CLIENTID);
        if (br.containsHTML("\"404 \\- Not Found\""))
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        AvailableStatus status = checkStatus(parameter, this.br.toString());
        if (status.equals(AvailableStatus.FALSE))
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        if (url == null)
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        checkDirectLink(parameter, url);
        return AvailableStatus.TRUE;
    }

    public AvailableStatus checkStatus(final DownloadLink parameter, final String source) throws ParseException {
        String filename = getXML("title", source);
        if (filename == null) {
            parameter.getLinkStatus().setStatusText(JDL.L("plugins.hoster.SoundCloudCom.status.pluginBroken", "The host plugin is broken!"));
            return AvailableStatus.FALSE;
        }
        filename = Encoding.htmlDecode(filename.trim().replace("\"", "'"));
        final String filesize = getXML("original-content-size", source);
        if (filesize != null) parameter.setDownloadSize(Long.parseLong(filesize));
        final String description = getXML("description", source);
        if (description != null) {
            try {
                parameter.setComment(description);
            } catch (Throwable e) {
            }
        }
        String date = new Regex(source, "<created\\-at type=\"datetime\">([^<>\"]*?)</created-at>").getMatch(0);
        String username = getXML("username", source);
        String type = null;//getXML("original-format", source);
        if (type == null) type = "mp3";
        username = Encoding.htmlDecode(username.trim());
        url = getXML("download-url", source);
        if (url != null) {
            parameter.getLinkStatus().setStatusText(JDL.L("plugins.hoster.SoundCloudCom.status.downloadavailable", "Original file is downloadable"));
        } else {
            url = getXML("stream-url", source);
            parameter.getLinkStatus().setStatusText(JDL.L("plugins.hoster.SoundCloudCom.status.previewavailable", "Preview (Stream) is downloadable"));
        }
        if (url == null) {
            parameter.getLinkStatus().setStatusText(JDL.L("plugins.hoster.SoundCloudCom.status.pluginBroken", "The host plugin is broken!"));
            return AvailableStatus.FALSE;
        }

        url = url + "?client_id=" + CLIENTID;
        parameter.setProperty("directlink", url);
        parameter.setProperty("channel", username);
        parameter.setProperty("plainfilename", filename);
        parameter.setProperty("originaldate", date);
        parameter.setProperty("type", type);
        final String formattedfilename = getFormattedFilename(parameter);
        parameter.setFinalFileName(formattedfilename);
        return AvailableStatus.TRUE;
    }
    
    public String getFormattedFilename(final DownloadLink downloadLink) throws ParseException {
        String songTitle = downloadLink.getStringProperty("plainfilename", null);
        final SubConfiguration cfg = SubConfiguration.getConfig("soundcloud.com");
        String formattedFilename = cfg.getStringProperty(CUSTOM_FILENAME, defaultCustomFilename);
        if (formattedFilename == null || formattedFilename.equals("")) formattedFilename = defaultCustomFilename;
        if (!formattedFilename.contains("*songtitle*") || !formattedFilename.contains("*ext*")) formattedFilename = defaultCustomFilename;
        String ext = downloadLink.getStringProperty("type", null);
        if (ext != null)
            ext = "." + ext;
        else
            ext = ".mp3";

        String date = downloadLink.getStringProperty("originaldate", null);
        final String channelName = downloadLink.getStringProperty("channel", null);

        String formattedDate = null;
        if (date != null && formattedFilename.contains("*date*")) {
            // 2011-08-10T22:50:49Z
            date = date.replace("T", ":");
            final String userDefinedDateFormat = cfg.getStringProperty(CUSTOM_DATE, "dd.MM.yyyy_HH-mm-ss");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
            Date dateStr = formatter.parse(date);

            formattedDate = formatter.format(dateStr);
            Date theDate = formatter.parse(formattedDate);

            if (userDefinedDateFormat != null) {
                try {
                    formatter = new SimpleDateFormat(userDefinedDateFormat);
                    formattedDate = formatter.format(theDate);
                } catch (Exception e) {
                    // prevent user error killing plugin.
                    formattedDate = "";
                }
            }
            if (formattedDate != null)
                formattedFilename = formattedFilename.replace("*date*", formattedDate);
            else
                formattedFilename = formattedFilename.replace("*date*", "");
        }
        if (formattedFilename.contains("*channelname*") && channelName != null) {
            formattedFilename = formattedFilename.replace("*channelname*", channelName);
        }
        formattedFilename = formattedFilename.replace("*ext*", ext);
        // Insert filename at the end to prevent errors with tags
        formattedFilename = formattedFilename.replace("*songtitle*", songTitle);

        return formattedFilename;
    }

    private void checkDirectLink(final DownloadLink downloadLink, final String property) {
        URLConnectionAdapter con = null;
        try {
            Browser br2 = br.cloneBrowser();
            con = br2.openGetConnection(url);
            if (con.getContentType().contains("html") || con.getLongContentLength() == -1 || con.getResponseCode() == 401) {
                downloadLink.setProperty(property, Property.NULL);
                url = null;
                return;
            }
            downloadLink.setDownloadSize(con.getLongContentLength());

        } catch (Exception e) {
            downloadLink.setProperty(property, Property.NULL);
            url = null;
        } finally {
            try {
                con.disconnect();
            } catch (final Throwable e) {
            }
        }
    }

    private String getJson(final String parameter) {
        return br.getRegex("\"" + parameter + "\":\"([^<>\"]*?)\"").getMatch(0);
    }

    public String getXML(final String parameter, final String source) {
        return new Regex(source, "<" + parameter + "( type=\"[^<>\"/]*?\")?>([^<>]*?)</" + parameter + ">").getMatch(1);
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    private void postprocess(DownloadLink link) {
        try {
            if ((Boolean) link.getProperty("setid3tag", "false")) {
                String thumbnailUrl = (String) link.getProperty("thumbnailUrl", null);
                String username = (String) link.getProperty("username", null);
                String title = (String) link.getProperty("title", null);
                String detailsUrl = (String) link.getProperty("detailsUrl", null);

                File mp3 = new File(link.getFileOutput());
                File temp = new File(link.getFileOutput().replace(".mp3", "_id3.mp3"));

                downloadAndUpdateCoverArt(mp3, temp, thumbnailUrl, username, title, detailsUrl);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void downloadAndUpdateCoverArt(File mp3, File temp, String thumbnailUrl, String username, String title, String detailsUrl) {
        //abort if file is too large.
        if (mp3 != null && mp3.exists() && mp3.length() <= MAX_ACCEPTABLE_SOUNDCLOUD_FILESIZE_FOR_COVERART_FETCH) {

            byte[] coverArtBytes = downloadCoverArt(thumbnailUrl);

            if (coverArtBytes != null && coverArtBytes.length > 0) {
                if (setAlbumArt(coverArtBytes, mp3.getAbsolutePath(), temp.getAbsolutePath(), username, title, detailsUrl)) {
                    //mp3.delete();
                    //temp.renameTo(mp3);
                } else {
                    if (temp.exists()) {
                        temp.delete();
                    }
                }
            }
        }
    }

    private byte[] downloadCoverArt(String thumbnailUrl) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            simpleHTTP(thumbnailUrl, baos, 3000);
            return baos.toByteArray();
        } catch (Throwable e) {
            // ignore
        }
        return null;
    }

    private boolean setAlbumArt(byte[] imageBytes, String mp3Filename, String mp3outputFilename, String username, String title, String detailsUrl) {
        try {
            AudioFile f = AudioFileIO.read(new File(mp3Filename));
            Tag tag = f.getTagOrCreateAndSetDefault();

            tag.setField(FieldKey.ALBUM, username + ": " + title + " via SoundCloud.com");
            tag.setField(FieldKey.ARTIST, username);
            tag.setField(FieldKey.TITLE, title);
            tag.setField(FieldKey.URL_OFFICIAL_RELEASE_SITE, detailsUrl);

            Artwork artwork = ArtworkFactory.getNew();
            artwork.setBinaryData(imageBytes);
            artwork.setMimeType("image/jpg");
            
            tag.addField(artwork);

            f.commit();

            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private String cleanupFilename(String filename) {
        filename = filename.replace("&#8482;", "TM");

        // bug in jdownloader?
        if (filename.endsWith(".m4a")) {
            filename = filename.replace(".m4a", ".mp3");
        }

        return filename;
    }

    static void simpleHTTP(String url, OutputStream out, int timeout) throws Throwable {
        URL u = new URL(url);
        URLConnection con = u.openConnection();
        con.setConnectTimeout(timeout);
        con.setReadTimeout(timeout);
        InputStream in = con.getInputStream();
        try {

            byte[] b = new byte[1024];
            int n = 0;
            while ((n = in.read(b, 0, b.length)) != -1) {
                out.write(b, 0, n);
            }
        } finally {
            try {
                out.close();
            } catch (Throwable e) {
                // ignore   
            }
            try {
                in.close();
            } catch (Throwable e) {
                // ignore   
            }
        }
    }
}