package com.frostwire.audio;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.Player;

import com.limegroup.gnutella.gui.mp3.PlayerState;
import com.limegroup.gnutella.gui.mp3.AudioSource;

/**
 * @author gubatron
 * @date Aug/2/2009
 * The LimeWirePlayer fails to play some (or most actually) mp3s.
 * When the LimeWirePlayer fails to fetch an audio format, we substitute it with this
 * AuxMP3Player.
 * 
 * I'm not sure if we'll provide "seek" functionality, but at least we need to provide
 * functionality to pause, play, and stop this player, specially if another mp3 or audio file
 * can be played with the LimeWirePlayer.
 * 
 * This AuxMP3Player basically wraps the javazoom jlayer player, which can play everything.
 */
final public class AuxMP3Player implements Runnable {
    private Thread playerThread;
    private Player player;
    private static AuxMP3Player INSTANCE = null;
    private PlayerState state = PlayerState.STOPPED;
    private Object playerLock;
    
    private AuxMP3Player()  {
        playerLock = new Object();
    }
    
    final static public AuxMP3Player getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuxMP3Player();
        }
        
        return INSTANCE;
    }

    /**
     * Makes sure the player knows about the InputStream.
     * Initializes the Player if necessary
     * @param song
     * @return 
     */
    final private void setSong(AudioSource song) throws Exception {
        if (INSTANCE !=null && player != null) {
            player.close();
            player = null;
        }
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(song.getFile()));
            player = new Player(bis,
                    FactoryRegistry.systemRegistry().createAudioDevice());
        } catch (Exception e) {
            player = null;
            throw e;
        }
    }
    
    final public void tryPlaying(AudioSource song) throws Exception {
        state = PlayerState.OPENING;
        setSong(song);
        playerThread = new Thread(this);
        playerThread.start();
    }

    final public void run() {
        if (state == PlayerState.PLAYING || 
            INSTANCE == null || 
            player == null)
            return;
        
        try {
            state = PlayerState.PLAYING;
            player.play(Integer.MAX_VALUE);
            state = PlayerState.STOPPED;
        } catch (Exception e) {
            state = PlayerState.STOPPED;
            player.close();
        } finally {
            player.close();
            player = null;
        }
    }

    
    synchronized final public void stop() {
        //no need to stop something if it's not playing
        if (state != PlayerState.PLAYING)
            return;
        
        synchronized (playerLock) {
            if (player != null) {
                player.close();
                player = null;
                state = PlayerState.STOPPED;
            }
            
        }
    }
}
