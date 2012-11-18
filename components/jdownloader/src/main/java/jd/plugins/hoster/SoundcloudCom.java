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

import jd.PluginWrapper;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.locale.JDL;

import org.appwork.utils.formatter.SizeFormatter;

import com.frostwire.mp3.ID3Wrapper;
import com.frostwire.mp3.ID3v1Tag;
import com.frostwire.mp3.ID3v23Tag;
import com.frostwire.mp3.Mp3File;

@HostPlugin(revision = "$Revision: 15419 $", interfaceVersion = 2, names = { "soundcloud.com" }, urls = { "http://(www\\.)?soundclouddecrypted\\.com/[a-z\\-_0-9]+/[a-z\\-_0-9]+" }, flags = { 0 })
public class SoundcloudCom extends PluginForHost {

    private static final long MAX_ACCEPTABLE_SOUNDCLOUD_FILESIZE_FOR_COVERART_FETCH = 10485760; //10MB

    private String url;

    public SoundcloudCom(PluginWrapper wrapper) {
        super(wrapper);
    }

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
    public void handleFree(DownloadLink link) throws Exception {
        requestFileInformation(link);
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, url, true, 0);
        if (dl.startDownload()) {
            this.postprocess(link);
        }
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink parameter) throws Exception {
        this.setBrowserExclusive();
        br.getPage(parameter.getDownloadURL());
        if (br.containsHTML("Oops, looks like we can\\'t find that page"))
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("<em>(.*?)</em>").getMatch(0);
        br.setFollowRedirects(true);
        if (filename == null)
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        String username = br.getRegex("\"username\":\"(.*?)\"").getMatch(0);
        filename = Encoding.htmlDecode(filename.trim());
        String type = br.getRegex("title=\"Uploaded format\">(.*?)<").getMatch(0);
        if (type == null) {
            type = br.getRegex("class=\"file\\-type\">(.*?)</span>").getMatch(0);
            if (type == null)
                type = "mp3";
        }
        username = username.trim();
        if (username != null && !filename.contains(username))
            filename += " - " + username;
        filename += "." + type;
        if (!br.containsHTML("class=\"download pl\\-button\"")) {
            String[] data = br.getRegex("\"uid\":\"(.*?)\".*?\"token\":\"(.*?)\"").getRow(0);
            url = "http://media.soundcloud.com/stream/" + data[0] + "?stream_token=" + data[1];
            URLConnectionAdapter con = br.openGetConnection(url);
            if (!con.getContentType().contains("html"))
                parameter.setDownloadSize(con.getLongContentLength());
            else
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            con.disconnect();
            parameter.getLinkStatus().setStatusText(JDL.L("plugins.hoster.SoundCloudCom.status.previewavailable", "Preview is downloadable"));
        } else {
            String filesize = br.getRegex("The file you're about to download has a size of (.*?)\"").getMatch(0);
            if (filesize != null)
                parameter.setDownloadSize(SizeFormatter.getSize(filesize));
            url = parameter.getDownloadURL() + "/download";
            parameter.getLinkStatus().setStatusText(JDL.L("plugins.hoster.SoundCloudCom.status.downloadavailable", "Original file is downloadable"));
        }
        parameter.setFinalFileName(filename);
        return AvailableStatus.TRUE;
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
                    mp3.delete();
                    temp.renameTo(mp3);
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
            Mp3File mp3 = new Mp3File(mp3Filename);

            ID3Wrapper newId3Wrapper = new ID3Wrapper(new ID3v1Tag(), new ID3v23Tag());

            newId3Wrapper.setAlbum(username + ": " + title + " via SoundCloud.com");
            newId3Wrapper.setArtist(username);
            newId3Wrapper.setTitle(title);
            newId3Wrapper.setAlbumImage(imageBytes, "image/jpg");
            newId3Wrapper.setUrl(detailsUrl);
            newId3Wrapper.getId3v2Tag().setPadding(true);

            mp3.setId3v1Tag(newId3Wrapper.getId3v1Tag());
            mp3.setId3v2Tag(newId3Wrapper.getId3v2Tag());

            mp3.save(mp3outputFilename);

            return true;
        } catch (Throwable e) {
            return false;
        }
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