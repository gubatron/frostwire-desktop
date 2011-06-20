package com.limegroup.gnutella.gui.player;

import com.frostwire.gui.mplayer.MediaPlaybackState;
import com.limegroup.gnutella.gui.RefreshListener;

/**
 * This interface defines the functionality of an AudioPlayer
 * component.
 */
public interface AudioPlayer extends RefreshListener {

    /**
     * Loads a song wrapped in a AudioSource object
     */
    public void loadSong(AudioSource source);
	
	/**
     * Begins playing the loaded song
     */
    public void playSong();
	
	/**
     * Pauses the current song.
     */
    public void pause();
    
    /**
     * Unpauses the current song.
     */
    public void unpause();

    /**
     * Stops the current song from playing (essentially returns the song to the
     * loaded state).
     */
    public void stop();
    
    public void seek(float timeInSecs);
    
    
    public MediaPlaybackState getStatus();
    
    /**
     * Sets Volume(Gain) value Linear scale 0.0 <--> 1.0
     */
    public void setVolume(double value);
    
    /**
     * Adds a listener to the list of player listeners
     */
    public void addAudioPlayerListener(AudioPlayerListener listener);
    
    /**
     * Removes a listener from the list of player listeners
     */
    public void removeAudioPlayerListener(AudioPlayerListener listener);
}
