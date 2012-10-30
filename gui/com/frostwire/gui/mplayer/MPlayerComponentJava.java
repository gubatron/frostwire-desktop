package com.frostwire.gui.mplayer;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.frostwire.gui.player.AudioPlayer;
import com.frostwire.gui.player.AudioPlayerListener;
import com.frostwire.gui.player.AudioSource;
import com.frostwire.mplayer.MediaPlaybackState;
import com.limegroup.gnutella.gui.GUIMediator;

public class MPlayerComponentJava extends Container implements MPlayerComponent, ProgressSliderListener, AudioPlayerListener {

	private static final long serialVersionUID = -5860833860676831251L;
	
	private Canvas video;
	private JPanel controlsOverlay;
	private JFrame fullscreenWindow;
	private JLayeredPane fullscreenPane;
	
	private JSlider volumeSlider;
	private ProgressSlider progressSlider;
	
	private AudioPlayer player;
	
	private boolean isPlayerProgressUpdate = false;
	private JButton playButton, pauseButton;
	
	public MPlayerComponentJava() {
		
		setLayout( new BorderLayout() );
		
		initializeVideoControl();
        initializeControlsOverlay();
        initializeFullscreenWindow();
        
        player = AudioPlayer.instance();
        player.addAudioPlayerListener(this);
    }
	
	private void initializeVideoControl() {
		
		Dimension defaultVideoDim = new Dimension(500,500);
		
		video = new Canvas();
        video.setPreferredSize(defaultVideoDim);
        video.setVisible(true);
        video.setBackground(Color.BLACK);
        video.setIgnoreRepaint(false);
        add(video);
	}
	
	private void initializeControlsOverlay() {
		
		
		ImageIcon bkgndImage = GUIMediator.getThemeImage("fc_background");
	
		controlsOverlay = new JPanel(null);
        controlsOverlay.setBounds( 0, 0, bkgndImage.getIconWidth(), bkgndImage.getIconHeight() );
        controlsOverlay.setOpaque(false);
        
        // background image
        // ------------------
        JLabel bkgnd = new JLabel( bkgndImage );
        bkgnd.setBackground(new Color(0,0,0,0));
        bkgnd.setSize( bkgndImage.getIconWidth(), bkgndImage.getIconHeight());
        	
        // play button
        // ------------
        Point playButtonPos = new Point(236, 13);
        playButton = MPlayerComponentJava.createMPlayerButton("fc_play", playButtonPos );
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MPlayerComponentJava.this.onPlayPressed();
			}
		});
		controlsOverlay.add(playButton);
        
		// pause button
		// --------------
		Point pauseButtonPos = new Point(236, 13);
        pauseButton = MPlayerComponentJava.createMPlayerButton("fc_play", pauseButtonPos );
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MPlayerComponentJava.this.onPausePressed();
			}
		});
		controlsOverlay.add(pauseButton);
		
        // fast forward button
		// --------------------
        Point fastForwardButtonPos = new Point(306, 18);
        JButton fastForwardButton;
		fastForwardButton = MPlayerComponentJava.createMPlayerButton("fc_next", fastForwardButtonPos );
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MPlayerComponentJava.this.onFastForwardPressed();
			}
		});
		controlsOverlay.add(fastForwardButton);
        
        // rewind button
		// --------------
        Point rewindButtonPos = new Point(182, 18);
        JButton rewindButton;
        rewindButton = MPlayerComponentJava.createMPlayerButton("fc_previous", rewindButtonPos);
        rewindButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MPlayerComponentJava.this.onRewindPressed();
			}
        });
		controlsOverlay.add(rewindButton);
            
        // full screen exit button
		// ------------------------
        Point fullscreenButtonPos = new Point(390, 22);
        JButton fullscreenButton;
        fullscreenButton = MPlayerComponentJava.createMPlayerButton("fc_fullscreen_exit", fullscreenButtonPos);
        fullscreenButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MPlayerComponentJava.this.toggleFullScreen();
			}
        });
		controlsOverlay.add(fullscreenButton);
        
        // volume slider
		// --------------
		JPanel volumePanel = new JPanel( new BorderLayout());
		volumePanel.setBounds(20, 20, 125, 25);
		volumePanel.setOpaque(false);
		
		ImageIcon volMinIcon = GUIMediator.getThemeImage("fc_volume_off");
		JLabel volMinLabel = new JLabel( volMinIcon );
		volMinLabel.setOpaque(false);
		volMinLabel.setSize( volMinIcon.getIconWidth(), volMinIcon.getIconHeight());
        volumePanel.add(volMinLabel, BorderLayout.WEST);
        
        volumeSlider = new JSlider();
        volumeSlider.setOpaque(false);
        volumeSlider.setFocusable(false);
        volumeSlider.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				MPlayerComponentJava.this.onVolumeChanged(((JSlider)e.getSource()).getValue());
			}
        });
        volumePanel.add(volumeSlider, BorderLayout.CENTER);
        
        ImageIcon volMaxIcon = GUIMediator.getThemeImage("fc_volume_on");
		JLabel volMaxLabel = new JLabel( volMaxIcon );
		volMaxLabel.setSize( volMaxIcon.getIconWidth(), volMaxIcon.getIconHeight());
        volumePanel.add(volMaxLabel, BorderLayout.EAST);
        
        controlsOverlay.add(volumePanel);
        
        
        // progress slider
        // ----------------
        progressSlider = new ProgressSlider();
        progressSlider.addProgressSliderListener(this);
        progressSlider.setLocation(20, 70);
        controlsOverlay.add(progressSlider);
        
        controlsOverlay.add(bkgnd);
        
        // initialize default overlay position
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle overlayRect = controlsOverlay.getBounds();
		overlayRect.x = (int) (screen.width*0.5 - overlayRect.width/2);
		overlayRect.y = (int) (screen.height*0.75 - overlayRect.height/2);
		controlsOverlay.setBounds( overlayRect );
	}
	
	private void initializeFullscreenWindow() {
		
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        
		fullscreenWindow = new JFrame();
        fullscreenWindow.setLayout(null);
        fullscreenWindow.setUndecorated(true);
        fullscreenWindow.setSize( screenDim );
        fullscreenWindow.setResizable(false);
        fullscreenWindow.setAlwaysOnTop(true);
        fullscreenWindow.setVisible(false);
        
        fullscreenPane = new JLayeredPane();
        fullscreenPane.setBounds(0, 0, screenDim.width, screenDim.height);
        fullscreenPane.add(controlsOverlay, JLayeredPane.PALETTE_LAYER);
        fullscreenWindow.getContentPane().add(fullscreenPane);
	}
	
	public static JButton createMPlayerButton(final String image, final Point pos) {
		
		ImageIcon buttonImage = GUIMediator.getThemeImage(image);
        
		// create button
		JButton button = new JButton();
		
		// customize UI
		button.setIcon( buttonImage );
        button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setRolloverEnabled(true);
		button.setMargin(new Insets(0,0,0,0));
		button.setFocusPainted(false);
		button.setSize( new Dimension(buttonImage.getIconWidth(), buttonImage.getIconHeight()) );
        button.setLocation( pos );
        
		return button;
	}
	
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public void toggleFullScreen() {

		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		
		if (video.getParent() == this) { // entering fullscreen
			
			MediaPlaybackState priorState = player.getState();
			player.stop();
			
			video.setSize( screenDim );
			fullscreenPane.add(video, JLayeredPane.DEFAULT_LAYER);
			fullscreenWindow.setVisible(true);

			player.reopenAndContinue( priorState );
			
		} else { // leaving fullscreen
			
			MediaPlaybackState priorState = player.getState();
			System.out.println("toggle fullscreen: " + priorState.toString());
			player.stop();
				
			fullscreenWindow.setVisible(false);
			
			video.setSize( getSize() );
			add(video);
			validate();
			
			player.reopenAndContinue( priorState );
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public long getWindowID() {
		long hWnd = 0;
		
		try {
	
			Class<?> cl = Class.forName("sun.awt.windows.WComponentPeer");
	        java.lang.reflect.Field f = cl.getDeclaredField("hwnd");
	        f.setAccessible(true); //little reflection hack to access the hwnd from windows.
	        hWnd = f.getLong(video.getPeer());
    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return hWnd;
	}

	/*
	 * UI control event handlers
	 */
	public void onPlayPressed() {
		player.togglePause();
	}
	
	public void onPausePressed() {
		player.togglePause();
	}
	
	public void onFastForwardPressed() {
		player.fastForward();
	}
	
	public void onRewindPressed() {
		player.rewind();
	}

	private void onVolumeChanged( int value) {
		
	}

	@Override
	public void onProgressSliderValueChange(int seconds) {
		if ( !isPlayerProgressUpdate) {
			player.seek((float)seconds);
		}
	}

	/*
	 * AudioPlayerListener events 
	 */
	
	@Override
	public void songOpened(AudioPlayer audioPlayer, AudioSource audioSource) {
		// TODO Auto-generated method stub
	}

	@Override
	public void progressChange(AudioPlayer audioPlayer, float currentTimeInSecs) {
		
		isPlayerProgressUpdate = true;
		progressSlider.setTotalTime((int) player.getDurationInSecs() );
		progressSlider.setCurrentTime((int)currentTimeInSecs);
		isPlayerProgressUpdate = false;
	}

	@Override
	public void volumeChange(AudioPlayer audioPlayer, double currentVolume) {
	}

	@Override
	public void stateChange(AudioPlayer audioPlayer, MediaPlaybackState state) {
		switch(state) {
		case Playing:
			pauseButton.setVisible(true);
			playButton.setVisible(false);
			break;
		case Paused:
		case Stopped:
			pauseButton.setVisible(false);
			playButton.setVisible(true);
			break;
		default:
			break;
		}
	}

	@Override
	public void icyInfo(AudioPlayer audioPlayer, String data) {
		// TODO Auto-generated method stub
		
	}

}
