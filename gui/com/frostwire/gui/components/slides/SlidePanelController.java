package com.frostwire.gui.components.slides;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.util.StringUtils;

import com.frostwire.gui.player.MediaSource;
import com.frostwire.gui.player.StreamMediaSource;
import com.limegroup.gnutella.gui.GUIMediator;

public class SlidePanelController {

    private static Log LOG = LogFactory.getLog(SlidePanelController.class);

    private Slide slide;

    private String cachedVideoStreamURL;
    private String cachedAudioStreamURL;
    
    public SlidePanelController(Slide slide) {
        this.slide = slide;
    }

    public Slide getSlide() {
        return slide;
    }

    public void downloadSlide() {

        switch (slide.method) {
        case Slide.SLIDE_DOWNLOAD_METHOD_HTTP:
            if (slide.httpDownloadURL != null) {
                GUIMediator.instance().openSlide(slide);
            }
            break;

        case Slide.SLIDE_DOWNLOAD_METHOD_TORRENT:
            if (slide.torrent != null) {
                if (slide.torrent.toLowerCase().startsWith("http")) {
                    GUIMediator.instance().openTorrentURI(slide.torrent, false);
                } else if (slide.torrent.toLowerCase().startsWith("magnet:?")) {
                    GUIMediator.instance().openTorrentURI(slide.torrent, false);
                }
            }
            break;
        }

        if (slide.hasFlag(Slide.OPEN_CLICK_URL_ON_DOWNLOAD) && slide.clickURL != null) {
            GUIMediator.openURL(slide.clickURL);
        }
    }

    /**
     * Note: only for HTTP downloads
     */
    public void installSlide() {
        if (slide.method == Slide.SLIDE_DOWNLOAD_METHOD_HTTP && slide.hasFlag(Slide.POST_DOWNLOAD_EXECUTE)) {
            downloadSlide();
        }
    }

    private void playInOS(MediaSource source) {
        if (source == null) {
            return;
        }

        if (source.getFile() != null) {
            GUIMediator.launchFile(source.getFile());
        } else if (source.getPlaylistItem() != null) {
            GUIMediator.launchFile(new File(source.getPlaylistItem().getFilePath()));
        } else if (source.getURL() != null) {
            GUIMediator.openURL(source.getURL());
        }
    }

    public void previewVideo() {
        final String mediaURL = slide.videoURL;
        if (mediaURL != null && mediaURL.contains("youtube.com")) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        if (cachedVideoStreamURL == null) {
                            cachedVideoStreamURL = new YouTubeStreamURLExtractor(mediaURL).getYoutubeStreamURL();
                        }
                        previewMedia(cachedVideoStreamURL, true, Slide.PREVIEW_VIDEO_USING_FWPLAYER);
                    } catch (Exception e) {
                        LOG.error("Could not extract/play youtube stream.", e);
                    }
                }
            }.start();
        } else {
            previewMedia(mediaURL, true, Slide.PREVIEW_VIDEO_USING_FWPLAYER);
        }
    }

    public void previewAudio() {
        previewMedia(slide.audioURL, false, Slide.PREVIEW_AUDIO_USING_FWPLAYER);
    }

    private void previewMedia(String mediaURL, boolean showMediaPlayer, int flagUsingFWPlayerForMediaType) {
        if (!StringUtils.isNullOrEmpty(mediaURL)) {
            StreamMediaSource mediaSource = new StreamMediaSource(mediaURL, slide.title, slide.clickURL, showMediaPlayer);
            if (slide.hasFlag(flagUsingFWPlayerForMediaType)) {
                GUIMediator.instance().launchMedia(mediaSource);
            } else {
                playInOS(mediaSource);
            }
        }
    }

    public void openFacebookPage() {
        openSocialMediaPage(slide.facebook);
    }

    public void openTwitterPage() {
        openSocialMediaPage(slide.twitter);
    }

    public void openGooglePlusPage() {
        openSocialMediaPage(slide.gplus);
    }

    private void openSocialMediaPage(String url) {
        if (!StringUtils.isNullOrEmpty(url)) {
            GUIMediator.openURL(url);
        }

    }
}