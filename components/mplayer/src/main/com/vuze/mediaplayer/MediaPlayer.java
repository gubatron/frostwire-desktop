package com.vuze.mediaplayer;

public interface MediaPlayer {
	
	public void open(String fileOrUrl);
	public void loadSubtitlesFile(String file);
	public void seek(float time);
	public void setVolume(int volume);
	public void pause();
	public void play();
	public void stop();
	public void togglePause();
	public void mute( boolean on );
	
	public void showMessage(String message,int duration);
	
	public void addMetaDataListener(MetaDataListener listener);
	public void removeMetaDataListener(MetaDataListener listener);
	
	public void addStateListener(StateListener listener);
	public void removeStateListener(StateListener listener);
	
	public void addVolumeListener(VolumeListener listener);
	public void removeVolumeListener(VolumeListener listener);
	
	public void addPositionListener(PositionListener listener);
	public void removePositionListener(PositionListener listener);
	
	public void addTaskListener(TaskListener listener);
	public void removeTaskListener(TaskListener listener);
	
	public String getOpenedFile();
	
	public int getVolume();
	public float getPositionInSecs();
	public float getDurationInSecs();
	public int getVideoWidth();
	public int getVideoHeight();
	public int getDisplayWidth();
	public int getDisplayHeight();
	public MediaPlaybackState getCurrentState();
	
	public Language[] getAudioTracks();
	public Language[] getSubtitles();
	
	public String getActiveAudioTrackId();
	public String getActiveSubtitleId();
	
	public void setAudioTrack(Language language);
	
	/**
	 * 
	 * @param language the subtitle language to enable, or null to disable subtitles
	 */
	public void setSubtitles(Language language);
	
	
}
