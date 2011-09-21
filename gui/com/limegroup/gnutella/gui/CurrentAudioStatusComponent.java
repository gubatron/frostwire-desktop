package com.limegroup.gnutella.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.frostwire.alexandria.PlaylistItem;
import com.frostwire.gui.library.LibraryMediator;
import com.frostwire.gui.player.AudioPlayer;
import com.frostwire.gui.player.AudioPlayerListener;
import com.frostwire.gui.player.AudioSource;
import com.frostwire.mplayer.MediaPlaybackState;

/*
 * The component at the right bottom of the screen showing what's the current audio being played.
 * Click on it to switch to the library, playlist and scroll to it.
 */
public class CurrentAudioStatusComponent extends JPanel implements AudioPlayerListener {
	
	private static final int MAX_CHARS = 33;
	private static final int BOUND_CHARS = 12;
	
	private static final long serialVersionUID = 9206657876064353272L;
	private Icon speakerIcon;
	private JLabel text;
	private MediaPlaybackState lastState;
	
	public CurrentAudioStatusComponent() {
		AudioPlayer.instance().addAudioPlayerListener(this);
		lastState=MediaPlaybackState.Uninitialized;
		initComponents();
	}

	private void initComponents() {
		Dimension dimension = new Dimension(200,22);
		setPreferredSize(dimension);
		setMinimumSize(dimension);
		
		speakerIcon = GUIMediator.getThemeImage("speaker");

		text = new JLabel();
		Font f = new Font("DIALOG",Font.BOLD,10);
		text.setFont(f);
		text.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				showCurrentSong();
			}
		});
		
		setLayout(new BorderLayout());
		add(text,BorderLayout.LINE_END);
	}
	
	public void showCurrentSong() {
		GUIMediator.instance().setWindow(GUIMediator.Tabs.LIBRARY);
		LibraryMediator.instance().selectCurrentSong();
	}

	@Override
	public void songOpened(AudioPlayer audioPlayer, AudioSource audioSource) {
		
	}

	@Override
	public void progressChange(AudioPlayer audioPlayer, float currentTimeInSecs) {
		
	}

	@Override
	public void volumeChange(AudioPlayer audioPlayer, double currentVolume) {
		
	}

	@Override
	public void stateChange(AudioPlayer audioPlayer, MediaPlaybackState state) {

		if (lastState == state) {
			return;
		}

		lastState = state;
		
		if (state != MediaPlaybackState.Playing && state != MediaPlaybackState.Paused) {

			GUIMediator.safeInvokeLater(new Runnable() {
				@Override
				public void run() {
					text.setIcon(null);
					text.setText("");				
				}
			});
			return;
		}
		

		System.out.println("CurrentAudioStatusComponent.stateChanged() : " + state);
		System.out.println("CurrentAudioStatusComponent.stateChanged() : Let's go!");
		
		//update controls
		AudioSource currentSong = audioPlayer.getCurrentSong();
		PlaylistItem playlistItem = currentSong.getPlaylistItem();
		
		String currentText = null;
		
		if (playlistItem != null) {
			//Playing from Playlist.
			String artistName = playlistItem.getTrackArtist();
			String songTitle = playlistItem.getTrackTitle();
			
			currentText = artistName + " - " + songTitle;
			
		}  else if (currentSong.getFile()!=null) {
			//playing from Audio.
			currentText = AudioPlayer.instance().getCurrentSong().getFile().getName();
		}
		
		//TODO: Make sure text is not too long.
		if (currentText.length() > MAX_CHARS) {
			currentText = currentText.substring(0, BOUND_CHARS) + " ... " + currentText.substring(currentText.length()-BOUND_CHARS);
		}
		
		final String currentTextFinal = currentText;
		
		GUIMediator.safeInvokeLater(new Runnable() {
			
			@Override
			public void run() {
				text.setIcon(speakerIcon);
				text.setText("<html><font color=\"496989\"><u>"+currentTextFinal+"</u></font></html>");				
			}
		});
		
	}
}
