package com.frostwire.gui.mplayer;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.frostwire.gui.player.AudioSource;
import com.frostwire.gui.player.MPlayerUIEventHandler;
import com.frostwire.gui.player.MediaPlayer;
import com.frostwire.gui.player.MediaPlayerListener;
import com.frostwire.mplayer.MediaPlaybackState;
import com.limegroup.gnutella.gui.GUIMediator;
import com.sun.awt.AWTUtilities;

public class MPlayerOverlayControls extends JDialog implements ProgressSliderListener, AlphaTarget, MediaPlayerListener {

	private static final long serialVersionUID = -6148347816829785754L;

	private JSlider volumeSlider;
	private ProgressSlider progressSlider;
	private JButton playButton, pauseButton, fullscreenExitButton, fullscreenEnterButton;
	
	private MediaPlayer player;
	
	private double durationInSeconds = 0.0;
	private double currentTimeInSeconds = 0.0;
	private boolean isHandlingProgressChange = false;
	
	public MPlayerOverlayControls() {
		
		player = MediaPlayer.instance();
		player.addMediaPlayerListener(this);
		
		setupUI();
    }

    protected void setupUI() {
        
    	Container panel = getContentPane();

        ImageIcon bkgndImage = GUIMediator.getThemeImage("fc_background");
        Dimension winSize = new Dimension( bkgndImage.getIconWidth(), bkgndImage.getIconHeight());
        
    	setPreferredSize(winSize);
        setSize(winSize);
        setUndecorated(true);
        setBackground(new Color(0,0,0,0));
        
        // osx specific (won't harm windows/linux)
        getRootPane().putClientProperty("apple.awt.draggableWindowBackground", Boolean.FALSE);
        
        panel.setLayout(null);
    	panel.setBounds(0, 0, winSize.width, winSize.height);
    	
        // background image
        // ------------------
        JLabel bkgnd = new JLabel( bkgndImage );
        bkgnd.setOpaque(false);
        bkgnd.setSize( bkgndImage.getIconWidth(), bkgndImage.getIconHeight());
        	
        // play button
        // ------------
        Point playButtonPos = new Point(236, 13);
        playButton = MPlayerOverlayControls.createMPlayerButton("fc_play", playButtonPos );
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MPlayerOverlayControls.this.onPlayPressed();
			}
		});
		panel.add(playButton);
		
        
		// pause button
		// --------------
		Point pauseButtonPos = new Point(236, 13);
        pauseButton = MPlayerOverlayControls.createMPlayerButton("fc_pause", pauseButtonPos );
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MPlayerOverlayControls.this.onPausePressed();
			}
		});
		panel.add(pauseButton);
		
        // fast forward button
		// --------------------
        Point fastForwardButtonPos = new Point(306, 18);
        JButton fastForwardButton;
		fastForwardButton = MPlayerOverlayControls.createMPlayerButton("fc_next", fastForwardButtonPos );
		fastForwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MPlayerOverlayControls.this.onFastForwardPressed();
			}
		});
		panel.add(fastForwardButton);
        
        // rewind button
		// --------------
        Point rewindButtonPos = new Point(182, 18);
        JButton rewindButton;
        rewindButton = MPlayerOverlayControls.createMPlayerButton("fc_previous", rewindButtonPos);
        rewindButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MPlayerOverlayControls.this.onRewindPressed();
			}
        });
		panel.add(rewindButton);
            
        // full screen exit button
		// ------------------------
        Point fullscreenButtonPos = new Point(390, 22);
        fullscreenExitButton = MPlayerOverlayControls.createMPlayerButton("fc_fullscreen_exit", fullscreenButtonPos);
        fullscreenExitButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MPlayerOverlayControls.this.onFullscreenExitPressed();
			}
        });
		panel.add(fullscreenExitButton);
		
		// full screen enter button
		// ------------------------
        fullscreenEnterButton = MPlayerOverlayControls.createMPlayerButton("fc_fullscreen_enter", fullscreenButtonPos);
        fullscreenEnterButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MPlayerOverlayControls.this.onFullscreenEnterPressed();
			}
        });
		panel.add(fullscreenEnterButton);
		
        
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
				MPlayerOverlayControls.this.onVolumeChanged(((JSlider)e.getSource()).getValue());
			}
        });
        volumePanel.add(volumeSlider, BorderLayout.CENTER);
        
        ImageIcon volMaxIcon = GUIMediator.getThemeImage("fc_volume_on");
		JLabel volMaxLabel = new JLabel( volMaxIcon );
		volMaxLabel.setSize( volMaxIcon.getIconWidth(), volMaxIcon.getIconHeight());
        volumePanel.add(volMaxLabel, BorderLayout.EAST);
        
        panel.add(volumePanel);
        
        
        // progress slider
        // ----------------
        progressSlider = new ProgressSlider();
        progressSlider.addProgressSliderListener(this);
        progressSlider.setLocation(20, 70);
        panel.add(progressSlider);
        
        panel.add(bkgnd);
        
    }
    
    @Override
	public void setAlpha( final float alpha) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	AWTUtilities.setWindowOpacity(MPlayerOverlayControls.this, alpha);
            }
        });
	}
    
    private static JButton createMPlayerButton(final String image, final Point pos) {
		
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
    
    public void setIsFullscreen(boolean fullscreen) {
    	fullscreenExitButton.setVisible(fullscreen);
    	fullscreenEnterButton.setVisible(!fullscreen);
    }
    
    public void onPlayPressed() {
    	MPlayerUIEventHandler.instance().onPlayPressed();
	}
	
	public void onPausePressed() {
		MPlayerUIEventHandler.instance().onPausePressed();
    }
	
	public void onFastForwardPressed() {
		MPlayerUIEventHandler.instance().onFastForwardPressed();
	}
	
	public void onRewindPressed() {
		MPlayerUIEventHandler.instance().onRewindPressed();
	}

	private void onVolumeChanged( int value) {
		MPlayerUIEventHandler.instance().onVolumeChanged((float)value / 100.0f);
	}

	public void onProgressSliderTimeValueChange(int seconds) {
		if ( isHandlingProgressChange == false ) {
			MPlayerUIEventHandler.instance().onSeekToTime((float)seconds);
		}
	}
	
	public void onFullscreenEnterPressed() {
		MPlayerUIEventHandler.instance().onToggleFullscreenPressed();
	}
	
	public void onFullscreenExitPressed() {
		MPlayerUIEventHandler.instance().onToggleFullscreenPressed();
	}

	@Override
	public void mediaOpened(MediaPlayer mediaPlayer, AudioSource audioSource) {
	}

	@Override
	public void progressChange(MediaPlayer mediaPlayer, float currentTimeInSecs) {
		durationInSeconds = mediaPlayer.getDurationInSecs();
		currentTimeInSeconds = currentTimeInSecs;
		
		// adjust progress slider
		isHandlingProgressChange = true;
		progressSlider.setTotalTime((int)durationInSeconds);
		progressSlider.setCurrentTime((int)currentTimeInSeconds);
		isHandlingProgressChange = false;
	}

	@Override
	public void volumeChange(MediaPlayer mediaPlayer, double currentVolume) {
		volumeSlider.setValue((int) (currentVolume * 100));
	}

	@Override
	public void stateChange(MediaPlayer mediaPlayer, MediaPlaybackState state) {
		switch( state ) {
		case Playing:
			pauseButton.setVisible(true);
			playButton.setVisible(false);
			break;
		case Paused:
			pauseButton.setVisible(false);
			playButton.setVisible(true);
			break;
		case Closed:
			playButton.setVisible(true);
			pauseButton.setVisible(false);
		case Failed:
		case Opening:
		case Stopped:
		case Uninitialized:
		default:
			break;
		
		}
	}

	@Override
	public void icyInfo(MediaPlayer mediaPlayer, String data) {
	}
	
	
	public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, AWTUtilities.getWindowOpacity(this)));
        
        super.paint(g2);
        
        g2.dispose();
    }
    

}
