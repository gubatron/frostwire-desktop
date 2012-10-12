package com.frostwire.gui.mplayer;

public class MPlayerJNIHandler {

    public void onVolumeChanged(float volume) {
        System.out.println("MPlayerJNIHandler: onVolumeChanged");
    }

    public void onSeekToTime(float seconds) {
        System.out.println("MPlayerJNIHandler: onSeekToTime");
    }
    
    public void onPlayPressed() {
        System.out.println("MPlayerJNIHandler: onPlayPressed");
    }
    
    public void onPausePressed() {
        System.out.println("MPlayerJNIHandler: onPausePressed");
    }
    
    public void onFastForwardPressed() {
        System.out.println("MPlayerJNIHandler: onFastForwardPressed");
    }
    
    public void onRewindPressed() {
        System.out.println("MPlayerJNIHandler: onRewindPressed");
    }
    
}
