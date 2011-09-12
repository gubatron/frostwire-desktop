package com.frostwire.gui.player;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.limewire.concurrent.ExecutorsHelper;
import org.limewire.service.ErrorService;
import org.limewire.util.OSUtils;

import com.frostwire.mplayer.MediaPlaybackState;
import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.GUIConstants;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.MediaButton;
import com.limegroup.gnutella.gui.MediaSlider;
import com.limegroup.gnutella.gui.RefreshListener;
import com.limegroup.gnutella.gui.playlist.PlaylistMediator;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;
import com.limegroup.gnutella.util.FrostWireUtils;
import com.limegroup.gnutella.util.Tagged;
import com.limegroup.gnutella.util.URLDecoder;

/**
 * This class sets up JPanel with MediaPlayer on it, and takes care of GUI
 * MediaPlayer events.
 */
public final class AudioPlayerComponent implements AudioPlayerListener, RefreshListener, ThemeObserver {


    public static final String STREAMING_AUDIO = "Streaming Audio";

    /**
     * How fast to scroll the song title if it is too long in milliseconds
     */
    private static final long SCROLL_RATE = 200;

    /**
     * The maximum characters to show in the progress bar.
     */
    private static final int STRING_SIZE_TO_SHOW = 20;

    /**
     * Width needed to fully display everything in the media player. If this width
     * isn't available, the progress and volume bar collapse
     */
    public final int fullSizeWidth = 351;

    /**
     *  Minimum width needed to display just the buttons
     */
    public final int minWidth = 240;

    /**
     * Constant for the play button.
     */
    private final MediaButton PLAY_BUTTON = new MediaButton(I18n.tr("Play"), "play_up", "play_dn");

    /**
     * Constant for the pause button.
     */
    private final MediaButton PAUSE_BUTTON = new MediaButton(I18n.tr("Pause"), "pause_up", "pause_dn");

    /**
     * Constant for the stop button.
     */
    private final MediaButton STOP_BUTTON = new MediaButton(I18n.tr("Stop"), "stop_up", "stop_dn");

    /**
     * Constant for the forward button.
     */
    private final MediaButton NEXT_BUTTON = new MediaButton(I18n.tr("Next"), "forward_up", "forward_dn");

    /**
     * Constant for the rewind button.
     */
    private final MediaButton PREV_BUTTON = new MediaButton(I18n.tr("Previous"), "rewind_up", "rewind_dn");

    /**
     * Constant for the volume control
     */
    private final MediaSlider VOLUME = new MediaSlider("volume_track_left", "volume_track_center", "volume_track_right", "volume_thumb_up",
            "volume_thumb_dn");

    /**
     * Constant for the progress bar
     */
    private final SongProgressBar PROGRESS = new SongProgressBar("progress_track_left", "progress_track_center", "progress_track_right",
            "progress_thumb_up", "progress_thumb_dn", "progress_bar");

    /**
     * Executor to ensure all thread creation on the frostwireplayer is called from
     * a single thread
     */
    private final ExecutorService SONG_QUEUE = ExecutorsHelper.newProcessingQueue("SongProcessor");

    /**
     * The MP3 player.
     */
    private final AudioPlayer PLAYER;

    /**
     * The ProgressBar dimensions for showing the name & play progress.
     */
    private final Dimension progressBarDimension = new Dimension(129, 19);

    /**
     * Volume slider dimensions for adjusting the audio level of a song
     */
    private final Dimension volumeSliderDimension = new Dimension(63, 19);

    /**
     * Index of where to display the name in the progress bar.
     */
    private volatile int currBeginIndex = -1;

    /**
     * Current properties of the song being played
     */
    private AudioMetaData audioProperties;

    /**
     * The last time the scrolling song was shifted
     */
    private long lastScroll = System.currentTimeMillis();

    /**
     * The current song that is playing
     */
    private AudioSource currentPlayListItem;

    /**
     * The lazily constructed media panel.
     */
    private JPanel myMediaPanel = null;

    /**
     * Variable for the name of the current file being played.
     */
    private String currentFileName;

    /**
     * If true, will only play current song and stop, regradless of position
     * in the playlist or value of continous or random. If false, continous
     * and random control the play feature
     */
    private boolean playOneTime = false;

    /**
     * Lock for access to the above String.
     */
    private final Object cfnLock = new Object();

    private float _progress;

    /**
     * Constructs a new <tt>MediaPlayerComponent</tt>.
     */
    public AudioPlayerComponent() {
        PLAYER = AudioPlayer.instance();
        PLAYER.addAudioPlayerListener(this);

        GUIMediator.addRefreshListener(this);
        ThemeMediator.addThemeObserver(this);
    }

    /**
     * Gets the media panel, constructing it if necessary.
     */
    public JPanel getMediaPanel() {
        if (myMediaPanel == null)
            myMediaPanel = constructMediaPanel();
        return myMediaPanel;
    }

    /**
     * Constructs the media panel.
     */
    private JPanel constructMediaPanel() {
        int tempWidth = 0, tempHeight = 0;
        tempHeight += PLAY_BUTTON.getIcon().getIconHeight() + 2;
        tempWidth += PLAY_BUTTON.getIcon().getIconWidth() + 2 + PAUSE_BUTTON.getIcon().getIconWidth() + 2 + STOP_BUTTON.getIcon().getIconWidth() + 2
                + NEXT_BUTTON.getIcon().getIconWidth() + 2 + PREV_BUTTON.getIcon().getIconWidth() + 2;

        // create sliders
        PROGRESS.setMinimumSize(progressBarDimension);
        PROGRESS.setMaximumSize(progressBarDimension);
        PROGRESS.setPreferredSize(progressBarDimension);
        PROGRESS.setString(I18n.tr("FrostWire Media Player"));
        PROGRESS.setMaximum(3600);
        PROGRESS.setEnabled(false);

        VOLUME.setMaximumSize(volumeSliderDimension);
        VOLUME.setPreferredSize(volumeSliderDimension);
        VOLUME.setMinimum(0);
        VOLUME.setValue(50);
        VOLUME.setMaximum(100);
        VOLUME.setEnabled(true);

        // setup buttons
        registerListeners();

        // add everything
        JPanel buttonPanel = new BoxPanel(BoxPanel.X_AXIS);
        buttonPanel.setMaximumSize(new Dimension(tempWidth + PROGRESS.getWidth() + VOLUME.getWidth(), tempHeight));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(VOLUME);
        buttonPanel.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 3));
        buttonPanel.add(PREV_BUTTON);
        buttonPanel.add(PLAY_BUTTON);
        buttonPanel.add(PAUSE_BUTTON);
        buttonPanel.add(STOP_BUTTON);
        buttonPanel.add(NEXT_BUTTON);
        buttonPanel.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 3));
        buttonPanel.add(PROGRESS);
        if (OSUtils.isMacOSX())
            buttonPanel.add(Box.createHorizontalStrut(16));
        buttonPanel.add(Box.createHorizontalGlue());

        return buttonPanel;
    }

    public void registerListeners() {
        PLAY_BUTTON.addActionListener(new PlayListener());
        PAUSE_BUTTON.addActionListener(new PauseListener());
        STOP_BUTTON.addActionListener(new StopListener());
        NEXT_BUTTON.addActionListener(new NextListener());
        PREV_BUTTON.addActionListener(new BackListener());
        VOLUME.addChangeListener(new VolumeSliderListener());
        PROGRESS.addMouseListener(new ProgressBarMouseAdapter());

    }

    public void unregisterListeners() {
        PLAY_BUTTON.removeActionListener(new PlayListener());
        PAUSE_BUTTON.removeActionListener(new PauseListener());
        STOP_BUTTON.removeActionListener(new StopListener());
        NEXT_BUTTON.removeActionListener(new NextListener());
        PREV_BUTTON.removeActionListener(new BackListener());
        VOLUME.removeChangeListener(new VolumeSliderListener());
        PROGRESS.removeMouseListener(new ProgressBarMouseAdapter());
    }

    /**
     * Updates the audio player.
     */
    public void refresh() {
        PLAYER.refresh();

//        if (getMediaPanel().getSize().width < fullSizeWidth) {
//            GUIMediator.safeInvokeLater(new Runnable() {
//                public void run() {
//                    //PROGRESS.setVisible(false);
//                    //VOLUME.setVisible(false);
//                }
//            });
//        } else {
//            GUIMediator.safeInvokeLater(new Runnable() {
//                public void run() {
//                    PROGRESS.setVisible(true);
//                    VOLUME.setVisible(true);
//                }
//            });
//        }
    }

    // inherit doc comment
    public void updateTheme() {
        PLAY_BUTTON.updateTheme();
        PAUSE_BUTTON.updateTheme();
        STOP_BUTTON.updateTheme();
        NEXT_BUTTON.updateTheme();
        PREV_BUTTON.updateTheme();
        PROGRESS.updateTheme();
        PROGRESS.setString(I18n.tr("FrostWire Media Player"));
        VOLUME.updateTheme();
    }

    /**
     * Updates the displayed String on the progress bar, on the Swing thread.
     */
    private void setProgressString(final String update) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                PROGRESS.setString(update);
            }
        });
    }

    /**
     * Updates the current progress of the progress bar, on the Swing thread.
     */
    private void setProgressValue(final int update) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                PROGRESS.setValue(update);
            }
        });
    }

    /**
     * Enables or disables the skipping action on the progress bar
     * safely from the swing event queue
     * 
     * @param enabled - true to allow skipping, false otherwise
     */
    private void setProgressEnabled(final boolean enabled) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                PROGRESS.setEnabled(enabled);
            }
        });
    }

    /**
     * Updates the volume based on the position of the volume slider
     */
    private void setVolumeValue() {
        VOLUME.repaint();
        PLAYER.setVolume(((float) VOLUME.getValue()) / VOLUME.getMaximum());
    }

    /**
     * Public accessor for loading a song to be played. 
     */
    public void loadSong(AudioSource item) {
        loadSong(item, false);
    }

    /**
     * Public accessor for loading a song to be played,
     * @playOnce - if true, play song one time regardless of continous
     *			and random values and stop the player after completing, 
     *			if false, observe the continous and	random control 
     */
    public void loadSong(AudioSource item, boolean playOnce) {
        // fail silently if there's nothing to play
        if (item == null)
            return;
        currentPlayListItem = item;
        playOneTime = playOnce;

        loadSong(currentPlayListItem, "");
    }

    /**
     * Loads an audiosource to be played. 
     */
    private void loadSong(final AudioSource audioSource, String displayName) {
        if (audioSource == null)
            return;
        
        audioProperties = null;//audioSource.getMetaData();
        
        if (audioProperties == null) {
        	loadAudioProperties();
        }

        // load song on Executor thread
        SONG_QUEUE.execute(new SongLoader(audioSource));

        // try using the name passed in to the function, if not fallback on
        //  the a default string
        if (displayName != null && displayName.length() > 0)
            currentFileName = generateNameDisplay(displayName);
        else
            currentFileName = generateNameDisplay(STREAMING_AUDIO);
    }

    /**
     * Begins playing the loaded song
     */
    public void play() {
        if (PLAYER.getStatus() == MediaPlaybackState.Paused || PLAYER.getStatus() == MediaPlaybackState.Playing)
            PLAYER.unpause();
        else {
            loadSong(currentPlayListItem, playOneTime);
        }
    }

    /**
     * Pauses the currently playing audio file.
     */
    public void pauseSong() {
        if (PLAYER.getStatus() == MediaPlaybackState.Paused)
            PLAYER.unpause();
        else
            PLAYER.pause();
    }

    /**
     * Stops the currently playing audio file.
     */
    public void stopSong() {
        PLAYER.stop();
    }

    public void seek(float percent) {
        
        if (audioProperties.getLength() == -1) {
            return;
        }
        
        if (audioProperties != null && audioProperties.isSeekable()) {
            float timeInSecs = audioProperties.getLength() * percent;
            PLAYER.seek(timeInSecs);
        }
    }

    /**
     * @return the current song that is playing, null if there is no song loaded
     *         or the song is streaming audio
     */
    public AudioSource getCurrentSong() {
        return currentPlayListItem;
    }

    /**
     * Modifies the filename that is displayed in the progress bar. If the name
     * is too large to display in one string, *** are added to the end of the
     * name and it rotates like a ticker display
     */
    private String generateNameDisplay(String filename) {
        if (filename.length() > STRING_SIZE_TO_SHOW) {
            filename += " *** " + filename + " *** ";
        }
        currBeginIndex = -1;
        return filename;
    }

    /**
     * This event is thrown everytime a new song is opened and is ready to be
     * played.
     */
    public void songOpened(AudioPlayer audioPlayer, AudioSource audioSource) {
        //audioProperties = properties;
        
        if (audioProperties == null) {
        	loadAudioProperties();
        }
        
        setVolumeValue();
//        if (properties.isSeekable()) {
//            setProgressEnabled(true);
//        } else {
//            setProgressEnabled(false);
//        }

        // notify the playlist to repaint since a new song has started playing
        GUIMediator.getPlayList().playStarted();
    }

    /**
     * This event is thrown a number of times a second. It updates the current
     * frames that have been read, along with position and bytes read
     */
    public void progressChange(AudioPlayer audioPlayer, float currentTimeInSecs) {
        
        if (audioProperties.getLength() == -1) {
            return;
        }
        
        _progress = currentTimeInSecs;
        
        //I wasn't ready
        if (audioProperties == null) {
        	loadAudioProperties();
        }

        float progressUpdate = ((PROGRESS.getMaximum() * currentTimeInSecs) / audioProperties.getLength());
        setProgressValue((int) progressUpdate);
        
        // if the display name is too long, increment it
        // TODO: this should be replaced by the TimingFramework Animator
        if (currentFileName.length() <= STRING_SIZE_TO_SHOW) {
            setProgressString(currentFileName);
        } else if ((System.currentTimeMillis() - lastScroll) > SCROLL_RATE) {

            synchronized (cfnLock) {
                lastScroll = System.currentTimeMillis();
                if (currentFileName == null)
                    return;

                currBeginIndex = currBeginIndex + 1;
                if (currBeginIndex > currentFileName.length() / 2)
                    currBeginIndex = 0;
                setProgressString(currentFileName.substring(currBeginIndex, currBeginIndex + STRING_SIZE_TO_SHOW));
            }
        }
    }

    private void loadAudioProperties() {
    	if (currentPlayListItem == null) {
    		return;
    	}
    	
    	//currentPlayListItem.initMetaData();
    	//audioProperties = currentPlayListItem.getMetaData();
	}

	/**
     * This event is generated everytime the song state changes. ie.
     * OPENED->PLAYING->PAUSED->PLAYING->STOPPED->EOF, etc..
     */
    public void stateChange(AudioPlayer audioPlayer, MediaPlaybackState state) {
    	
    	if (audioProperties == null) {
    		loadAudioProperties();
    	}
    	
    	if (audioProperties != null && audioProperties.getLength() == -1) {
            return;
        }
    	
        if (state == MediaPlaybackState.Failed || state == MediaPlaybackState.Uninitialized) {
            //setProgressEnabled(false);
        } else if (state == MediaPlaybackState.Opening) {
            setVolumeValue();
        } else if (state == MediaPlaybackState.Stopped) {
            setProgressValue(PROGRESS.getMinimum());
        } else if (Math.abs(_progress - audioProperties.getLength()) < 5)  {
            
            PlaylistMediator playlist = GUIMediator.getPlayList();
            if (playlist == null)
                return;
            // inform the GUI on whether or not we're going to continue playing
            // if the end of the playlist has been reached, stop even if continous play is selected
            if (playOneTime || !playlist.isContinuous() || playlist.getSize() <= 0 || playlist.isEndOfList() && playlist.isContinuous() == false) {
                playlist.playComplete();
                PLAYER.stop();
            } else {
                playlist.playComplete();
                // if we don't already have another song to play,
                // get one.
                //loadSong(playlist.getNextSong());
            }
        }
    }

//    /**
//     * @return [ <song> '\t' <url> '\t' <length> '\t' <isStorePreview> '|' ]*
//     */
//    String getSongs() {
//        PlaylistMediator pl = GUIMediator.getPlayList();
//        List<PlayListItem> songs = pl.getSongs();
//        StringBuffer res = new StringBuffer();
//        if (songs != null) {
//            for (PlayListItem s : songs) {
//                res.append(s.getName()).append('\t').append(s.getURI()).append('\t').append(s.getProperty(PlayListItem.LENGTH)).append('\t')
//                        .append(s.isStorePreview()).append('|');
//
//            }
//        }
//        System.out.println("songs:" + res);
//        return res.toString();
//    }

    /**
     * Begins playing the loaded song in url of args.
     */
    String playSong(Map<String, String> args) {

//        Tagged<String> urlString = FrostWireUtils.getArg(args, "url", "AddToPlaylist");
//        if (!urlString.isValid())
//            return urlString.getValue();
//        String url = urlString.getValue();
//
//        // Find the song with this url
//        PlaylistMediator pl = GUIMediator.getPlayList();
//        List<PlayListItem> songs = pl.getSongs();
//        PlayListItem targetTrack = null;
//        for (PlayListItem it : songs) {
//            try {
//                String thatOne = URLDecoder.decode(it.getURI().toString());
//                String thisOne = URLDecoder.decode(url);
//                if (thatOne.equals(thisOne)) {
//                    targetTrack = it;
//                    break;
//                }
//            } catch (IOException e) {
//                // ignore
//            }
//        }
//
//        if (targetTrack != null) {
//            loadSong(targetTrack);
//            return "ok";
//        }
//
//        if (PLAYER.getStatus() == MediaPlaybackState.Paused || PLAYER.getStatus() == MediaPlaybackState.Playing)
//            PLAYER.unpause();
//        else {
//            loadSong(currentPlayListItem);
//        }

        return "ok";
    }

    /**
     * Returns "ok" on success and a
     * failure message on failure after taking an index into the playlist and
     * remove it.
     * 
     * @param index index of the item to remove
     * @return "ok" on success and a
     *         failure message on failure after taking an index into the
     *         playlist and remove it;
     */
    String removeFromPlaylist(int index) {
        PlaylistMediator pl = GUIMediator.getPlayList();
        if (pl.removeFileFromPlaylist(index)) {
            return "ok";
        }
        return "invalid.index: " + index;
    }

    /**
     * Returns "ok" on success and a
     * failure message on failure after taking an index into the playlist and
     * remove it.
     * 
     * @param index index of the item to remove
     * @return "ok" on success and a
     *         failure message on failure after taking an index into the
     *         playlist and remove it;
     */
    String playIndexInPlaylist(int index) {
        PlaylistMediator pl = GUIMediator.getPlayList();
        if (pl.removeFileFromPlaylist(index)) {
            return "ok";
        }
        return "invalid.index: " + index;
    }

    /**
     * @return <code>PROGRESS.getValue() + "\t" + PROGRESS.getMaximum()</code>
     *         or <code>"stopped"</code> if we're not playing
     */
    String getProgress() {
        String res;
        if (isPlaying()) {
            int secs = PROGRESS.getValue();
            int length = PROGRESS.getMaximum();
            System.out.println(secs + ":" + length);
            res = secs + "\t" + length;
        } else {
            res = "stopped";
        }
        return res.toString();
    }

    String addToPlaylist(Map<String, String> args) {

        Tagged<String> urlString = FrostWireUtils.getArg(args, "url", "AddToPlaylist");
        if (!urlString.isValid())
            return urlString.getValue();

        Tagged<String> nameString = FrostWireUtils.getArg(args, "name", "AddtoPlaylist");
        if (!nameString.isValid())
            return nameString.getValue();

        Tagged<String> lengthString = FrostWireUtils.getArg(args, "length", "AddtoPlaylist");
        if (!lengthString.isValid())
            return lengthString.getValue();

        Tagged<String> artistString = FrostWireUtils.getArg(args, "artist", "AddtoPlaylist");
        if (!artistString.isValid())
            return artistString.getValue();

        Tagged<String> albumString = FrostWireUtils.getArg(args, "album", "AddtoPlaylist");
        if (!albumString.isValid())
            return albumString.getValue();

        // We won't accept full URLs
        String baseDir = "http://riaa.com";
        int port = 0;
        if (port > 0) {
            baseDir += ":" + port;
        }

//        String url = baseDir + urlString.getValue();
//        try {
//            String decodedURL = URLDecoder.decode(url);
//            URL u = new URL(decodedURL);
//            PlayListItem song = new PlayListItem(u.toURI(), new AudioSource(u), nameString.getValue(), false);
//            GUIMediator.instance().launchAudio(song);
//        } catch (IOException e) {
//            ErrorService.error(e, "invalid URL:" + url);
//            return "ERROR:invalid.url:" + url;
//        } catch (URISyntaxException e) {
//            ErrorService.error(e, "invalid URL:" + url);
//            return "ERROR:invalid.url:" + url;
//        }
        return "ok";
    }

    String playURL(Map<String, String> args) {

        Tagged<String> urlString = FrostWireUtils.getArg(args, "url", "PlayURL");
        if (!urlString.isValid())
            return urlString.getValue();

        // We won't accept full URLs
        String baseDir = "http://riaa.com";
        int port = 0;
        if (port > 0) {
            baseDir += ":" + port;
        }

//        String url = baseDir + urlString.getValue();
//        String name = getName(url);
//        try {
//            String decodedURL = URLDecoder.decode(url);
//            URL u = new URL(decodedURL);
//            PlayListItem song = new PlayListItem(u.toURI(), new AudioSource(u), name, false);
//            GUIMediator.instance().launchAudio(song);
//        } catch (IOException e) {
//            ErrorService.error(e, "invalid URL:" + url);
//            return "ERROR:invalid.url:" + url;
//        } catch (URISyntaxException e) {
//            ErrorService.error(e, "invalid URL:" + url);
//            return "ERRORinvalid.url:" + url;
//        }
        return "ok";
    }

    private String getName(String url) {
        int ilast = url.lastIndexOf('/');
        if (ilast == -1) {
            ilast = url.lastIndexOf('\\');
        }
        if (ilast == -1) {
            return url;
        }
        return url.substring(ilast + 1);
    }

    private boolean isPlaying() {
        return !(PLAYER.getStatus() == MediaPlaybackState.Stopped || PLAYER.getStatus() == MediaPlaybackState.Uninitialized || PLAYER.getStatus() == MediaPlaybackState.Paused || PLAYER.getStatus() == MediaPlaybackState.Failed);
    }

    /** Attempts to stop a song if its playing any song
     *
     * Returns true if it actually stopped, false if there was no need to do so.
     *
     * */
    public boolean attemptStop() {

        if (PLAYER.getStatus() != MediaPlaybackState.Stopped) {
            PLAYER.stop();
            return true;
        }

        return false;
    }

    /**
     * Disables the Volume Slider, And Pause Button
     */
    public void disableControls() {
        VOLUME.setEnabled(false);
        PAUSE_BUTTON.setEnabled(false);
    }

    /** 
     * Enables the Volume Slider, And Pause Button
     * 
     */
    public void enableControls() {
        VOLUME.setEnabled(true);
        PAUSE_BUTTON.setEnabled(true);
    }

    /**
     * Listens for the play button being pressed.
     */
    private class PlayListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            play();
        }
    }

    /**
     * Listens for the stopped button being pressed.
     */
    private class StopListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            stopSong();
        }
    }

    /**
     * Listens for the next button being pressed.
     */
    private class NextListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            next();
        }
    }

    private void next() {
        stopSong();
        //loadSong(GUIMediator.getPlayList().getNextSong());
        play();
    }

    /**
     * Listens for the back button being pressed.
     */
    private class BackListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            back();
        }
    }

    private void back() {
        //loadSong(GUIMediator.getPlayList().getPrevSong());
    }

    /**
     * Listens for the pause button being pressed.
     */
    private class PauseListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            pauseSong();
        }
    }

    /**
     * This listener is added to the progressbar to process when the user has
     * skipped to a new part of the song with a mouse
     */
    private class ProgressBarMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            seek(e.getX() * 1.0f / ((Component)e.getSource()).getWidth());
        }
    }

    /**
     * This listener is added to the volume slider to process whene the user has
     * adjusted the volume of the audio player
     */
    private class VolumeSliderListener implements ChangeListener {
        /**
         * If the user moved the thumb, adjust the volume of the player
         */
        public void stateChanged(ChangeEvent e) {
            setVolumeValue();
        }
    }

    /**
     * Ensures that all songs will be loaded/played from the same thread.
     */
    private class SongLoader implements Runnable {

        /**
         * Audio source to load
         */
        private final AudioSource audio;

        public SongLoader(AudioSource audio) {
            this.audio = audio;
        }

        public void run() {
            if (PLAYER == null) {
                System.err.println("SongLoader.run(): There's no PLAYER to load the Song to");
                return;
            }

            if (audio != null)
                PLAYER.loadSong(audio);

            if (PLAYER.getStatus() != MediaPlaybackState.Playing)
                PLAYER.stop();

            try {
                PLAYER.playSong();
            } catch (Exception e) {
                PLAYER.stop();
                //System.out.println("Could not play song " + audio.getURL().toString());
                e.printStackTrace();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    //e1.printStackTrace();
                }

                try {
                    synchronized (PLAYER) {
                        PLAYER.notifyAll();
                    }
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
        }
    }

}