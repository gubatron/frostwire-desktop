package com.frostwire.gui.player;

public interface MPlayerUIEventListener {

	public void onUIVolumeChanged(float volume);
    public void onUISeekToTime(float seconds);
    public void onUIPlayPressed();
    public void onUIPausePressed();
    public void onUIFastForwardPressed();
    public void onUIRewindPressed();
    public void onUIToggleFullscreenPressed();
	
}
