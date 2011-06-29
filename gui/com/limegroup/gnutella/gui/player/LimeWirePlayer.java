
package com.limegroup.gnutella.gui.player;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

import org.gudy.azureus2.core3.util.UrlUtils;
import org.limewire.util.OSUtils;

import com.frostwire.gui.mplayer.MPlayer;
import com.frostwire.gui.mplayer.MediaPlaybackState;
import com.frostwire.gui.mplayer.PositionListener;
import com.limegroup.gnutella.gui.RefreshListener;
import com.limegroup.gnutella.util.FrostWireUtils;

/**
 *  An audio player to play compressed and uncompressed music.
 */
public class LimeWirePlayer implements AudioPlayer, RefreshListener {

    /**
     * Our list of AudioPlayerListeners that are currently listening for events
     * from this player
     */
    private List<AudioPlayerListener> listenerList = new CopyOnWriteArrayList<AudioPlayerListener>();
       
    /**
     * The source that the thread is currently reading from
     */
    private AudioSource currentSong;
    
    private MPlayer _mplayer;

    /** Wether or not we're running from source or from a binary distribution */
    private static boolean _isRelease;
    
    static {
    	_isRelease = !FrostWireUtils.getFrostWireJarPath().contains("frostwire.desktop");   	
    }
    
    public LimeWirePlayer() {
    	String playerPath = new String();
    	
    	if (OSUtils.isWindows()) {
    		playerPath = (_isRelease) ? FrostWireUtils.getFrostWireJarPath() + File.separator + "fwplayer.exe" : "lib/native/fwplayer.exe";
    		playerPath = UrlUtils.decode(playerPath);
    	} else if (OSUtils.isMacOSX()) {
    		String macOSFolder = new File(FrostWireUtils.getFrostWireJarPath()).getParentFile().getParent() + File.separator + "MacOS";
    		
    		playerPath = (_isRelease) ?  macOSFolder + File.separator + "fwplayer" : "lib/native/fwplayer";
    	} else {
    		playerPath = "/usr/bin/mplayer";
    	}
    	
    	//System.out.println("LimeWirePlayer - player path: ["+playerPath+"]");
    	
		MPlayer.initialise(new File(playerPath));
        _mplayer = new MPlayer();
        _mplayer.setPositionListener(new PositionListener() {
            public void positionChanged(float currentTimeInSecs) {
                fireProgress(currentTimeInSecs);
            }
        });
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
    public MediaPlaybackState getStatus() {
        return _mplayer.getCurrentState();
    }
    
    /**
     * Loads a AudioSource into the player to play next
     */
    public void loadSong(AudioSource source) {
        currentSong = source;
        notifyOpened(source.getMetaData());
    }

    /**
     * Begins playing a song
     */
    public void playSong() {
        _mplayer.stop();
        
        if (currentSong.getFile()==null) {
        	_mplayer.open(currentSong.getURL().toString());
        } else {        
        	_mplayer.open(currentSong.getFile().getAbsolutePath());
        	
        }
        
        notifyEvent(getStatus(), -1);    
    }

    /**
     * Pausing the current song
     */
    public void pause() {
        _mplayer.togglePause();
        notifyEvent(getStatus(), -1);
    }

    /**
     * Unpauses the current song
     */
    public void unpause() {
        _mplayer.togglePause();
        notifyEvent(getStatus(), -1);
    }

    /**
     * Stops the current song
     */
    public void stop() {
        _mplayer.stop();
        notifyEvent(getStatus(), -1);
    }
    
    /**
     * Seeks to a new location in the current song
     */
    public void seek(float timeInSecs) {
        _mplayer.seek(timeInSecs);
        notifyEvent(getStatus(), -1);
    }
    
    /**
     * Sets the gain(volume) for the outputline
     * 
     * @param gain - [0.0 <-> 1.0]
     * @throws IOException - thrown when the soundcard does not support this
     *         operation
     */
    public void setVolume(double fGain) {
        _mplayer.setVolume((int)(fGain * 200));
    }  
    
    
    
    /**
     * Notify listeners when a new audio source has been opened. 
     * 
     * @param properties - any properties about the source that we extracted
     */
    protected void notifyOpened(final AudioMetaData metaData){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                fireOpened(metaData);
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
    protected void notifyEvent(final MediaPlaybackState state, final double value) {
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
    protected void fireOpened(AudioMetaData metaData) {
        for (AudioPlayerListener listener : listenerList)
            listener.songOpened(metaData);
    }

    /**
     * Fired everytime a byte stream is written to the sound card. This lets 
     * listeners be aware of what point in the entire file is song is currnetly
     * playing. This also returns a copy of the written byte[] so it can get
     * passed along to objects such as a FFT for visual feedback of the song
     */
    protected void fireProgress(float currentTimeInSecs) {
        for (AudioPlayerListener listener : listenerList)
            listener.progressChange(currentTimeInSecs);
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
}