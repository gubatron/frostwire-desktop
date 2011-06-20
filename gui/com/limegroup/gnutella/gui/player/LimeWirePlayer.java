
package com.limegroup.gnutella.gui.player;


import static com.limegroup.gnutella.gui.player.PlayerState.PAUSED;
import static com.limegroup.gnutella.gui.player.PlayerState.PLAYING;
import static com.limegroup.gnutella.gui.player.PlayerState.SEEKING;
import static com.limegroup.gnutella.gui.player.PlayerState.SEEKING_PAUSED;
import static com.limegroup.gnutella.gui.player.PlayerState.SEEKING_PLAY;
import static com.limegroup.gnutella.gui.player.PlayerState.STOPPED;
import static com.limegroup.gnutella.gui.player.PlayerState.UNKNOWN;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

import com.frostwire.gui.mplayer.MPlayer;
import com.limegroup.gnutella.gui.RefreshListener;

/**
 *  An audio player to play compressed and uncompressed music.
 */
public class LimeWirePlayer implements AudioPlayer, RefreshListener {
   
    /**
     * Sleep time is for when the song is loaded or paused but not playing.
     */
    private static final long SLEEP_NONPLAYING = 100;
    
    /**
     * Sleep time for when the song is playing but the sourceLine is full and 
     * no reading/writing occurs
     */
    private static final long SLEEP_PLAYING = 150;

    /**
     *  Maximum size of the buffer to read/write from
     */
    public static final int EXTERNAL_BUFFER_SIZE = 4096 * 4;

    /**
     * Our list of AudioPlayerListeners that are currently listening for events
     * from this player
     */
    private List<AudioPlayerListener> listenerList = new CopyOnWriteArrayList<AudioPlayerListener>();
    
    /**
     * main thread that does the audio IO
     */
    private Thread playerthread;
    
    /**
     * Used in playerThread to sleep when player is paused
     */
    private final Object threadLock = new Object();
       
    /**
     * Current state of the player
     */
    private volatile PlayerState playerState = UNKNOWN;
    
    
    private Object seekLock = new Object();
    /**
     * byte location to skip to in file
     */
    private long seekValue = -1;
    
    /**
     * true==the thread should close the current song and load the next song
     */
    private volatile boolean loadSong = false;
    
    
    private Object volumeLock = new Object();
    /**
     * true== the thread should update the volume on the sourceDataLine 
     */
    private boolean setVolume = false;
    
    /**
     * the current volume of the player
     */
    private double volume = 0;
    
    /**
     * The source that the thread is currently reading from
     */
    private AudioSource currentSong;
    
    /**
     * buffer for reading from input stream/ writing to the data line
     */
    private final byte[] buffer = new byte[EXTERNAL_BUFFER_SIZE];
    
    /**
     * bytes read from the input stream
     */
    private int readBytes = 0;
    
    /**
     * available bytes that can be written to the sourceDataLine
     */
    private int avail;
    
    /**
     * Keeps track of wether or not we're using the AuxMP3Player to play the current
     * song.
     */
    private boolean usingAuxMP3Player = false;
    
    private MPlayer _mplayer;

    public LimeWirePlayer() {
        MPlayer.initialise(new File("/usr/bin/mplayer"));
        _mplayer = new MPlayer();
    }

    /**
     * Adds the specified AudioPlayer listener to the list
     */
    public void addAudioPlayerListener(AudioPlayerListener listener) {
        listenerList.add(listener);
    }

    /**
     * Removes the specified AudioPlayer listener from the list
     */
    public void removeAudioPlayerListener(AudioPlayerListener listener) {
        listenerList.remove(listener);
    }

    /**
     * Converts the playerstate from ints to PlayerState enums
     */
    public PlayerState getStatus() {
        return playerState;
    }
    
    /**
     * Loads a AudioSource into the player to play next
     */
    public void loadSong(AudioSource source) {
        currentSong = source;
    }

    /**
     * Begins playing a song
     */
    public void playSong() {
        _mplayer.stop();
        _mplayer.open(currentSong.getFile().getAbsolutePath());
        notifyEvent(PLAYING, -1);
    }

    /**
     * Pausing the current song
     */
    public void pause() {
        _mplayer.pause();
        if( !(playerState == UNKNOWN || playerState == STOPPED)){
            playerState = PAUSED;
            notifyEvent(PAUSED, -1);
        }
    }

    /**
     * Unpauses the current song
     */
    public void unpause() {
        _mplayer.play();
        if( !(playerState == UNKNOWN || playerState == STOPPED)){
            playerState = PLAYING;
            notifyEvent(PLAYING, -1);
        }
    }

    /**
     * Stops the current song
     */
    public void stop() {
        _mplayer.stop();
    }
    
    /**
     * Seeks to a new location in the current song
     */
    public long seekLocation(long value) {
        //_mplayer.seek(timeInSecs)
        if( !(playerState == UNKNOWN || playerState == STOPPED) ) {
            if( playerState == PAUSED || playerState == SEEKING_PAUSED )
                playerState = SEEKING_PAUSED;
            else
                playerState = SEEKING_PLAY;
            synchronized (seekLock) {
                seekValue = value;
            }
            notifyEvent(SEEKING,value);
        }
        return value;
    }
    
    /**
     * Sets the gain(volume) for the outputline
     * 
     * @param gain - [0.0 <-> 1.0]
     * @throws IOException - thrown when the soundcard does not support this
     *         operation
     */
    public void setVolume(double fGain) {
        //_mplayer.setv
        synchronized (volumeLock) {
            volume = fGain;
            setVolume = true;
        }
    }  
    
    
    
    /**
     * Notify listeners when a new audio source has been opened. 
     * 
     * @param properties - any properties about the source that we extracted
     */
    protected void notifyOpened(final Map<String,Object> properties){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                fireOpened(properties);
            }
        });
    }

    /**
     * Notify listeners about an AudioPlayerEvent. This creates general state
     * modifications to the player such as the transition from opened to 
     * playing to paused to end of song.
     * 
     * @param code - the type of player event.
     * @param position in the stream when the event occurs.
     * @param value if the event was a modification such as a volume update,
     *        list the new value
     */
    protected void notifyEvent(final PlayerState state, final double value) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireStateUpdated(new AudioPlayerEvent(state,value));
            }
        });
    }

    /**
     * fires a progress event off a new thread. This lets us safely fire events
     * off of the player thread while using a lock on the input stream
     */
    protected void notifyProgress(final int bytesread) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireProgress(bytesread);
            }
        });
    }

    /**
     * This is fired everytime a new song is loaded and ready to play. The
     * properties map contains information about the type of song such as bit
     * rate, sample rate, media type(MPEG, Streaming,etc..), etc..
     */
    protected void fireOpened(Map<String, Object> properties) {
        for (AudioPlayerListener listener : listenerList)
            listener.songOpened(properties);
    }

    /**
     * Fired everytime a byte stream is written to the sound card. This lets 
     * listeners be aware of what point in the entire file is song is currnetly
     * playing. This also returns a copy of the written byte[] so it can get
     * passed along to objects such as a FFT for visual feedback of the song
     */
    protected void fireProgress(int bytesread) {
        for (AudioPlayerListener listener : listenerList)
            listener.progressChange(bytesread);
    }

    /**
     * Fired everytime the state of the player changes. This allows a listener
     * to be aware of state transitions such as from OPENED -> PLAYING ->
     * STOPPED -> EOF
     */
    protected void fireStateUpdated(AudioPlayerEvent event) {
        for (AudioPlayerListener listener : listenerList)
            listener.stateChange(event);
    }


    /**
     * returns the current state of the player and position of the song being
     * played
     */
    public void refresh() {
        notifyEvent(getStatus(), -1);
    }

    
    /**
     * Holds a reference to the next song to be played
     */
    private class LoadSongBuffer {
        
        private AudioSource nextItem;
        
        public synchronized void setSong(AudioSource song){ 
            nextItem = song;
        }
        
        /**
         * @return the next song to be played, returns null if no new song is 
         * awaiting play
         */
        public synchronized AudioSource getSong(){
            AudioSource next = nextItem; 
            nextItem = null;
            return next;
        }
        
        public synchronized boolean hasSong(){
            return nextItem != null;
        }
    }
    
}