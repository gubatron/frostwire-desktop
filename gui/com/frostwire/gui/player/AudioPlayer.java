package com.frostwire.gui.player;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

import org.gudy.azureus2.core3.util.UrlUtils;
import org.limewire.util.OSUtils;

import com.frostwire.alexandria.Playlist;
import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.gui.library.LibraryMediator;
import com.frostwire.mplayer.MPlayer;
import com.frostwire.mplayer.MediaPlaybackState;
import com.frostwire.mplayer.PositionListener;
import com.frostwire.mplayer.StateListener;
import com.limegroup.gnutella.gui.RefreshListener;
import com.limegroup.gnutella.settings.PlayerSettings;
import com.limegroup.gnutella.util.FrostWireUtils;

/**
 * An audio player to play compressed and uncompressed music.
 */
public class AudioPlayer implements RefreshListener {

	/**
	 * Our list of AudioPlayerListeners that are currently listening for events
	 * from this player
	 */
	private List<AudioPlayerListener> listenerList = new CopyOnWriteArrayList<AudioPlayerListener>();

	private MPlayer mplayer;
	private AudioSource currentSong;
	private Playlist currentPlaylist;
	private List<AudioSource> playlistFilesView;
	private RepeatMode repeatMode;
	private boolean shuffle;
	private boolean playNextSong;

	private double volume;

	private Queue<AudioSource> lastRandomFiles;
	
	private static AudioPlayer instance;

	public static AudioPlayer instance() {
		if (instance == null) {
			instance = new AudioPlayer();
		}
		return instance;
	}

	private AudioPlayer() {
	    lastRandomFiles = new LinkedList<AudioSource>();
		String playerPath = "";

		// Whether or not we're running from source or from a binary
		// distribution
		boolean isRelease = !FrostWireUtils.getFrostWireJarPath().contains(
				"frostwire.desktop");

		if (OSUtils.isWindows()) {
			playerPath = (isRelease) ? FrostWireUtils.getFrostWireJarPath()
					+ File.separator + "fwplayer.exe"
					: "lib/native/fwplayer.exe";
			playerPath = UrlUtils.decode(playerPath);
			
			if (!new File(playerPath).exists()) {
				playerPath = UrlUtils.decode("../lib/native/fwplayer.exe");
			}
			
		} else if (OSUtils.isMacOSX()) {
			String macOSFolder = new File(FrostWireUtils.getFrostWireJarPath())
					.getParentFile().getParent() + File.separator + "MacOS";

			playerPath = (isRelease) ? macOSFolder + File.separator
					+ "fwplayer" : "lib/native/fwplayer";
		} else {
			playerPath = "/usr/bin/mplayer";
		}

		// System.out.println("LimeWirePlayer - player path: ["+playerPath+"]");

		MPlayer.initialise(new File(playerPath));
		mplayer = new MPlayer();
		mplayer.addPositionListener(new PositionListener() {
			public void positionChanged(float currentTimeInSecs) {
				notifyProgress(currentTimeInSecs);
			}
		});
		mplayer.addStateListener(new StateListener() {
			public void stateChanged(MediaPlaybackState newState) {
				if (newState == MediaPlaybackState.Closed) { // This is the case
																// mplayer is
																// done with the
																// current file
					handleNextSong();
				}
			}
		});

		repeatMode = (PlayerSettings.LOOP_PLAYLIST.getValue()) ? RepeatMode.All : RepeatMode.None;
		shuffle = PlayerSettings.SHUFFLE_PLAYLIST.getValue();
		playNextSong = true;
		volume = 0.5;
	}

	public AudioSource getCurrentSong() {
		return currentSong;
	}
	
	public Playlist getCurrentPlaylist() {
	    return currentPlaylist;
	}
	
	public List<AudioSource> getPlaylistFilesView() {
	    return playlistFilesView;
	}

	public RepeatMode getRepeatMode() {
		return repeatMode;
	}

	public void setRepeatMode(RepeatMode repeatMode) {
		this.repeatMode = repeatMode;
		PlayerSettings.LOOP_PLAYLIST.setValue(repeatMode == RepeatMode.All);
	}

	public boolean isShuffle() {
		return shuffle;
	}

	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
		PlayerSettings.SHUFFLE_PLAYLIST.setValue(shuffle);
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

	public MediaPlaybackState getState() {
		return mplayer.getCurrentState();
	}

	/**
	 * Loads a AudioSource into the player to play next
	 */
	public void loadSong(AudioSource source, boolean play, boolean playNextSong, Playlist currentPlaylist, List<AudioSource> playlistFilesView) {
		currentSong = source;
		this.playNextSong = playNextSong;
		this.currentPlaylist = currentPlaylist;
		this.playlistFilesView = playlistFilesView;
		notifyOpened(source);
		if (play) {
			if (currentSong.getFile() != null) {
				LibraryMediator.instance().getLibraryCoverArt().setFile(currentSong.getFile());
			} else if (currentSong.getPlaylistItem() != null) {
			    LibraryMediator.instance().getLibraryCoverArt().setFile(new File(currentSong.getPlaylistItem().getFilePath()));
			}
			playSong();
		}
	}
	
	public void loadSong(AudioSource source, boolean play, boolean playNextSong) {
	    loadSong(source, play, playNextSong, currentPlaylist, playlistFilesView);
	}

	public void loadSong(AudioSource audioSource) {
		loadSong(audioSource, false, false, null, null);
	}

	/**
	 * Begins playing a song
	 */
	public void playSong() {
		mplayer.stop();
		setVolume(volume);

		if (currentSong.getFile() != null) {
			mplayer.open(currentSong.getFile().getAbsolutePath());
		} else if (currentSong.getURL() != null) {
			mplayer.open(currentSong.getURL().toString());
		} else if (currentSong.getPlaylistItem() != null) {
		    mplayer.open(currentSong.getPlaylistItem().getFilePath());
		}

		notifyState(getState());
	}

	/**
	 * Toggle pause the current song
	 */
	public void togglePause() {
		mplayer.togglePause();
		notifyState(getState());
	}

	/**
	 * Stops the current song
	 */
	public void stop() {
		mplayer.stop();
		notifyState(getState());
	}

	/**
	 * Seeks to a new location in the current song
	 */
	public void seek(float timeInSecs) {
		mplayer.seek(timeInSecs);
		notifyState(getState());
	}

	/**
	 * Sets the gain(volume) for the outputline
	 * 
	 * @param gain
	 *            - [0.0 <-> 1.0]
	 * @throws IOException
	 *             - thrown when the soundcard does not support this operation
	 */
	public void setVolume(double fGain) {
		volume = fGain;
		mplayer.setVolume((int) (fGain * 200));
		notifyVolumeChanged();
	}

	protected void notifyVolumeChanged() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireVolumeChanged(volume);
			}
		});
	}

	public static boolean isPlayableFile(File file) {
	    return isPlayableFile(file.getAbsolutePath());
	}
	
	public static boolean isPlayableFile(String filename) {
        String name = filename.toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".ogg")
                || name.endsWith(".wav") || name.endsWith(".wma")
                || name.endsWith(".m4a");
    }
	
	private static boolean isPlayableFile(AudioSource audioSource) {
	    if (audioSource.getFile() != null) {
	        return isPlayableFile(audioSource.getFile());
	    } else if (audioSource.getPlaylistItem() != null) {
	        return isPlayableFile(audioSource.getPlaylistItem().getFilePath());
	    } else {
	        return false;
	    }
	}

	/**
	 * Notify listeners when a new audio source has been opened.
	 * 
	 * @param properties
	 *            - any properties about the source that we extracted
	 */
	protected void notifyOpened(final AudioSource audioSource) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireOpened(audioSource);
			}
		});
	}

	/**
	 * Notify listeners about an AudioPlayerEvent. This creates general state
	 * modifications to the player such as the transition from opened to playing
	 * to paused to end of song.
	 * 
	 * @param code
	 *            - the type of player event.
	 * @param position
	 *            in the stream when the event occurs.
	 * @param value
	 *            if the event was a modification such as a volume update, list
	 *            the new value
	 */
	protected void notifyState(final MediaPlaybackState state) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireState(state);
			}
		});
	}

	/**
	 * fires a progress event off a new thread. This lets us safely fire events
	 * off of the player thread while using a lock on the input stream
	 */
	protected void notifyProgress(final float currentTimeInSecs) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireProgress(currentTimeInSecs);
			}
		});
	}

	/**
	 * This is fired every time a new song is loaded and ready to play. The
	 * properties map contains information about the type of song such as bit
	 * rate, sample rate, media type(MPEG, Streaming,etc..), etc..
	 */
	protected void fireOpened(AudioSource audioSource) {
		for (AudioPlayerListener listener : listenerList) {
			listener.songOpened(this, audioSource);
		}
	}

	/**
	 * Fired every time a byte stream is written to the sound card. This lets
	 * listeners be aware of what point in the entire file is song is currently
	 * playing. This also returns a copy of the written byte[] so it can get
	 * passed along to objects such as a FFT for visual feedback of the song
	 */
	protected void fireProgress(float currentTimeInSecs) {
		for (AudioPlayerListener listener : listenerList) {
			listener.progressChange(this, currentTimeInSecs);
		}
	}

	protected void fireVolumeChanged(double currentVolume) {
		for (AudioPlayerListener listener : listenerList) {
			listener.volumeChange(this, currentVolume);
		}
	}

	/**
	 * Fired every time the state of the player changes. This allows a listener
	 * to be aware of state transitions such as from OPENED -> PLAYING ->
	 * STOPPED -> EOF
	 */
	protected void fireState(MediaPlaybackState state) {
		for (AudioPlayerListener listener : listenerList) {
			listener.stateChange(this, state);
		}
	}

	/**
	 * returns the current state of the player and position of the song being
	 * played
	 */
	public void refresh() {
		notifyState(getState());
	}

	private void handleNextSong() {
		if (!playNextSong) {
			return;
		}

		AudioSource song = null;

		if (getRepeatMode() == RepeatMode.Song) {
			song = currentSong;
		} else if (isShuffle()) {
			song = getNextRandomSong(currentSong);
		} else if (getRepeatMode() == RepeatMode.All) {
			song = getNextContinuousSong(currentSong);
		} else {
			song = getNextSong(currentSong);
		}

		if (song != null) {
			System.out.println(song.getFile());
			loadSong(song, true, true, currentPlaylist, playlistFilesView);
		}
	}

	public boolean isThisBeingPlayed(File file) {

		AudioSource currentSong = getCurrentSong();
		if (currentSong == null) {
			return false;
		}

		File currentSongFile = currentSong.getFile();

		if (currentSongFile != null
				&& file.equals(currentSongFile))
			return true;

		return false;
	}
	
	public boolean isThisBeingPlayed(PlaylistItem playlistItem) {

        AudioSource currentSong = getCurrentSong();
        if (currentSong == null) {
            return false;
        }

        PlaylistItem currentSongFile = currentSong.getPlaylistItem();

        if (currentSongFile != null
                && playlistItem.equals(currentSongFile))
            return true;

        return false;
    }
	
	public synchronized void setPlaylistFilesView(List<AudioSource> playlistFilesView) {
	    this.playlistFilesView = playlistFilesView;
	}
	
	public AudioSource getNextRandomSong(AudioSource currentSong) {
	    if (playlistFilesView == null) {
            return null;
        }

        AudioSource songFile;
        int count = 4;
        while ((songFile = findRandomSongFile(currentSong)) == null && count-- > 0)
            ;

        if (count > 0) {
            lastRandomFiles.add(songFile);
            if (lastRandomFiles.size() > 3) {
                lastRandomFiles.poll();
            }
        } else {
            songFile = currentSong;
            lastRandomFiles.clear();
            lastRandomFiles.add(songFile);
        }

        return songFile;
    }

    public AudioSource getNextContinuousSong(AudioSource currentSong) {
        if (playlistFilesView == null) {
            return null;
        }

        int n = playlistFilesView.size();
        for (int i = 0; i < n; i++) {
            try {
                AudioSource f1 = playlistFilesView.get(i);
                if (currentSong.equals(f1)) {
                    for (int j = 1; j < n; j++) {
                        AudioSource file = playlistFilesView.get((j + i) % n);
                        if (isPlayableFile(file)) {
                            return file;
                        }
                    }
                }
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    public AudioSource getNextSong(AudioSource currentSong) {
        if (playlistFilesView == null) {
            return null;
        }

        int n = playlistFilesView.size();
        for (int i = 0; i < n; i++) {
            try {
                AudioSource f1 = playlistFilesView.get(i);
                if (currentSong.equals(f1)) {
                    for (int j = i + 1; j < n; j++) {
                        AudioSource file = playlistFilesView.get(j);
                        if (isPlayableFile(file)) {
                            return file;
                        }
                    }
                }
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }
    
    public AudioSource getPreviousSong(AudioSource currentSong) {
        if (playlistFilesView == null) {
            return null;
        }

        int n = playlistFilesView.size();
        for (int i = 0; i < n; i++) {
            try {
                AudioSource f1 = playlistFilesView.get(i);
                if (currentSong.equals(f1)) {
                    for (int j = i - 1; j >= 0; j--) {
                        AudioSource file = playlistFilesView.get(j);
                        if (isPlayableFile(file)) {
                            return file;
                        }
                    }
                }
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }
    
    private AudioSource findRandomSongFile(AudioSource excludeFile) {
        if (playlistFilesView == null) {
            return null;
        }
        int n = playlistFilesView.size();
        int index = new Random(System.currentTimeMillis()).nextInt(n);

        for (int i = index; i < n; i++) {
            try {
                AudioSource file = playlistFilesView.get(i);

                if (!lastRandomFiles.contains(file) && !file.equals(excludeFile) && isPlayableFile(file)) {
                    return file;
                }
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

}