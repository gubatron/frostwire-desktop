package com.frostwire.gui.mplayer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
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

import com.limegroup.gnutella.gui.GUIMediator;
import com.sun.awt.AWTUtilities;

public class MPlayerOverlayControls extends JDialog implements ProgressSliderListener, AlphaTarget {

	private static final long serialVersionUID = -6148347816829785754L;

	private JSlider volumeSlider;
	private ProgressSlider progressSlider;
	private JButton playButton, pauseButton, fullscreenButton;
	
	public MPlayerOverlayControls(Frame frame) {
        super(frame);
        setupUI();
    }

    protected void setupUI() {
        
    	Container panel = getContentPane();

        ImageIcon bkgndImage = GUIMediator.getThemeImage("fc_background");
        Dimension winSize = new Dimension( bkgndImage.getIconWidth(), bkgndImage.getIconHeight());
        
    	setPreferredSize(winSize);
        setSize(winSize);
        setUndecorated(true);
        
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
        fullscreenButton = MPlayerOverlayControls.createMPlayerButton("fc_fullscreen_exit", fullscreenButtonPos);
        fullscreenButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MPlayerOverlayControls.this.onFullscreenPressed();
			}
        });
		panel.add(fullscreenButton);
        
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
                if (alpha == 0 && MPlayerOverlayControls.this.isVisible()) {
                	MPlayerOverlayControls.this.setVisible(false);
                }
                if (alpha > 0 && !MPlayerOverlayControls.this.isVisible()) {
                	MPlayerOverlayControls.this.setVisible(true);
                }
            }
        });
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
    
    public void setIsFullscreen(boolean fullscreen) {
    	fullscreenButton.setVisible(fullscreen);
    }
    
    public void onPlayPressed() {
		
	}
	
	public void onPausePressed() {
		
	}
	
	public void onFastForwardPressed() {
		
	}
	
	public void onRewindPressed() {
		
	}

	private void onVolumeChanged( int value) {
		
	}

	public void onProgressSliderValueChange(int seconds) {
		
	}
	
	public void onFullscreenPressed() {
		
	}
}
