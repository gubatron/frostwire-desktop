/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml), Erich Pleny (erichpleny)
 * Copyright (c) 2012, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.mplayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.limewire.util.OSUtils;

import com.frostwire.gui.player.MPlayerUIEventHandler;
import com.frostwire.gui.player.MediaPlayer;
import com.frostwire.gui.player.MediaPlayerListener;
import com.frostwire.gui.player.MediaSource;
import com.frostwire.mplayer.MediaPlaybackState;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.MPlayerMediator;
import com.sun.awt.AWTUtilities;

public class MPlayerOverlayControls extends JDialog implements ProgressSliderListener, AlphaTarget, MediaPlayerListener {

    private static final Color TRANSPARENT = new Color(0,0,0,0);

    private static final long serialVersionUID = -6148347816829785754L;

    private JSlider volumeSlider;
    private ProgressSlider progressSlider;
    private JButton playButton, pauseButton, fullscreenExitButton, fullscreenEnterButton;

    private MediaPlayer player;

    private double durationInSeconds = 0.0;
    private double currentTimeInSeconds = 0.0;
    private static final int HIDE_DELAY = 3000;

    private Timer hideTimer;
    private AlphaAnimationThread animateAlphaThread;
    private boolean isOverlayVisible;
    private Container controlsContainer;
    private MPlayerWindow playerWindow;
    
    public MPlayerOverlayControls(MPlayerWindow playerWindow) {

        this.playerWindow = playerWindow;
        player = MediaPlayer.instance();

        setupUI();
        
        player.addMediaPlayerListener(this);
        
        // initialize animation alpha thread
        animateAlphaThread = new AlphaAnimationThread(this);
        animateAlphaThread.setDaemon(true);
        animateAlphaThread.start();
        
        // initialize auto-hide timer
        hideTimer = new Timer(HIDE_DELAY, new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                MPlayerOverlayControls.this.onHideTimerExpired();
            }
        });
        hideTimer.setRepeats(false);
        
        this.playerWindow.addComponentListener( new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                MPlayerOverlayControls.this.updateCurrentPositionAndSize();
            }
            @Override public void componentMoved(ComponentEvent e) {
                MPlayerOverlayControls.this.updateCurrentPositionAndSize();
            }
            @Override public void componentShown(ComponentEvent e) {
                MPlayerOverlayControls.this.setVisible(true);
                MPlayerOverlayControls.this.updateCurrentPositionAndSize();
                MPlayerOverlayControls.this.showOverlay(false);
            }
            @Override public void componentHidden(ComponentEvent e) {
                MPlayerOverlayControls.this.hideOverlay(false);
                MPlayerOverlayControls.this.setVisible(false);
            }
        });
        
        this.playerWindow.addWindowListener( new WindowAdapter() {
            @Override public void windowIconified(WindowEvent e) {
                MPlayerOverlayControls.this.setVisible(false);
            }
            
            @Override public void windowDeiconified(WindowEvent e) {
                MPlayerOverlayControls.this.setVisible(true);
                showOverlay(false);
            }
            
            @Override public void windowActivated(WindowEvent e) {
                MPlayerOverlayControls.this.setVisible(true);
                showOverlay(false);
            }
        });
        
        this.addWindowListener( new WindowAdapter() {
            @Override public void windowDeactivated(WindowEvent e) {
                MPlayerOverlayControls.this.setVisible(false);
            }
        });
        
        this.addMouseListener(new OverlayControlsMouseAdapter());
        this.addMouseMotionListener(new OverlayControlsMouseAdapter());
    }


    protected void setupUI() {
        Container contentPanel = getContentPane();

        ImageIcon bkgndImage = GUIMediator.getThemeImage(OSUtils.isLinux() ? "fc_background_linux" : "fc_background");
        Dimension bkgndSize = new Dimension(bkgndImage.getIconWidth(), bkgndImage.getIconHeight());

        setUndecorated(true);
        
        if (OSUtils.isWindows() || OSUtils.isMacOSX()) {
            AWTUtilities.setWindowOpaque(this, false);
        }

        setPreferredSize(bkgndSize);
        setSize(bkgndSize);
        setMinimumSize(bkgndSize);
        setMaximumSize(bkgndSize);
        
        setVisible(true);
        setAlwaysOnTop(true);
        setAlpha(0.0f);
        setOpacity(0.0f);

        if (OSUtils.isLinux()) {
            setType(Type.POPUP);
        }

        // osx specific (won't harm windows/linux)
        getRootPane().putClientProperty("apple.awt.draggableWindowBackground", Boolean.FALSE);
        getRootPane().putClientProperty("Window.alpha", new Float(0.25));

        contentPanel.setLayout(null);
        contentPanel.setBounds(0, 0, bkgndSize.width, bkgndSize.height);
        contentPanel.setBackground(TRANSPARENT);
        
        controlsContainer = new Container();
        controlsContainer.setBounds(0, 0, bkgndSize.width, bkgndSize.height);
        controlsContainer.setVisible(true);
        controlsContainer.setBackground(TRANSPARENT);
        
        // play button
        // ------------
        Point playButtonPos = new Point(236, 13);
        playButton = MPlayerOverlayControls.createMPlayerButton("fc_play", playButtonPos);
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MPlayerOverlayControls.this.onPlayPressed();
            }
        });
        
        OverlayControlsMouseAdapter overlayControlsMouseAdapter = new OverlayControlsMouseAdapter();
        
        playButton.addMouseListener(overlayControlsMouseAdapter);
        controlsContainer.add(playButton);

        // pause button
        // --------------
        Point pauseButtonPos = new Point(236, 13);
        pauseButton = MPlayerOverlayControls.createMPlayerButton("fc_pause", pauseButtonPos);
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MPlayerOverlayControls.this.onPausePressed();
            }
        });
        pauseButton.addMouseListener(overlayControlsMouseAdapter);
        pauseButton.addMouseMotionListener(overlayControlsMouseAdapter);
        controlsContainer.add(pauseButton);

        // fast forward button
        // --------------------
        Point fastForwardButtonPos = new Point(306, 18);
        JButton fastForwardButton;
        fastForwardButton = MPlayerOverlayControls.createMPlayerButton("fc_next", fastForwardButtonPos);
        fastForwardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MPlayerOverlayControls.this.onFastForwardPressed();
            }
        });
        fastForwardButton.addMouseListener(overlayControlsMouseAdapter);
        fastForwardButton.addMouseMotionListener(overlayControlsMouseAdapter);
        controlsContainer.add(fastForwardButton);

        // rewind button
        // --------------
        Point rewindButtonPos = new Point(182, 18);
        JButton rewindButton;
        rewindButton = MPlayerOverlayControls.createMPlayerButton("fc_previous", rewindButtonPos);
        rewindButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                MPlayerOverlayControls.this.onRewindPressed();
            }
        });
        rewindButton.addMouseListener(overlayControlsMouseAdapter);
        rewindButton.addMouseMotionListener(overlayControlsMouseAdapter);
        controlsContainer.add(rewindButton);

        // full screen exit button
        // ------------------------
        Point fullscreenButtonPos = new Point(390, 22);
        fullscreenExitButton = MPlayerOverlayControls.createMPlayerButton("fc_fullscreen_exit", fullscreenButtonPos);
        fullscreenExitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                MPlayerOverlayControls.this.onFullscreenExitPressed();
            }
        });
        fullscreenExitButton.addMouseListener(overlayControlsMouseAdapter);
        fullscreenExitButton.addMouseMotionListener(overlayControlsMouseAdapter);
        controlsContainer.add(fullscreenExitButton);

        // full screen enter button
        // ------------------------
        fullscreenEnterButton = MPlayerOverlayControls.createMPlayerButton("fc_fullscreen_enter", fullscreenButtonPos);
        fullscreenEnterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                MPlayerOverlayControls.this.onFullscreenEnterPressed();
            }
        });
        fullscreenEnterButton.addMouseListener(overlayControlsMouseAdapter);
        fullscreenEnterButton.addMouseMotionListener(overlayControlsMouseAdapter);
        controlsContainer.add(fullscreenEnterButton);

        // volume slider
        // --------------
        JPanel volumePanel = new JPanel(new BorderLayout());
        volumePanel.setBounds(20, 20, 125, 25);
        volumePanel.setOpaque(false);

        ImageIcon volMinIcon = GUIMediator.getThemeImage("fc_volume_off");
        JLabel volMinLabel = new JLabel(volMinIcon);
        volMinLabel.setOpaque(false);
        volMinLabel.setSize(volMinIcon.getIconWidth(), volMinIcon.getIconHeight());
        volumePanel.add(volMinLabel, BorderLayout.WEST);

        volumeSlider = new JSlider();
        volumeSlider.setOpaque(false);
        volumeSlider.setFocusable(false);
        volumeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                MPlayerOverlayControls.this.onVolumeChanged(((JSlider) e.getSource()).getValue());
            }
        });
        volumeSlider.addMouseListener(overlayControlsMouseAdapter);
        volumeSlider.addMouseMotionListener(overlayControlsMouseAdapter);
        volumePanel.add(volumeSlider, BorderLayout.CENTER);

        ImageIcon volMaxIcon = GUIMediator.getThemeImage("fc_volume_on");
        JLabel volMaxLabel = new JLabel(volMaxIcon);
        volMaxLabel.setSize(volMaxIcon.getIconWidth(), volMaxIcon.getIconHeight());
        volumePanel.add(volMaxLabel, BorderLayout.EAST);

        controlsContainer.add(volumePanel);

        // progress slider
        // ----------------
        progressSlider = new ProgressSlider();
        progressSlider.addProgressSliderListener(this);
        progressSlider.setLocation(20, 70);
        progressSlider.addMouseListener(overlayControlsMouseAdapter);
        progressSlider.addMouseMotionListener(overlayControlsMouseAdapter);
        controlsContainer.add(progressSlider);


        // background image
        // ------------------
        JLabel bkgnd = new JLabel(bkgndImage);
        bkgnd.setOpaque(false);
        bkgnd.setSize(bkgndSize.width, bkgndSize.height);
        controlsContainer.add(bkgnd);
        
        contentPanel.add(controlsContainer);

    }

    private void updateCurrentPositionAndSize() {
        if (playerWindow.isVisible()) {
            Dimension size = playerWindow.getContentPane().getSize();
            Point loc = playerWindow.getContentPane().getLocationOnScreen();
            MPlayerOverlayControls.this.setBounds(loc.x, loc.y, size.width, size.height);
        }
    }
    
    public void setBounds(int xScreen, int yScreen, int width, int height) {
        super.setBounds(xScreen, yScreen, width, height);
        positionControlsContainer();
    }
    
    private void positionControlsContainer() {
        
        if (controlsContainer != null) {
            Dimension controlsSize = controlsContainer.getSize();
            Dimension windowSize = getSize();
            
            Point controlPos = new Point();
            controlPos.x = (int) ((windowSize.width - controlsSize.width) * 0.5);
            controlPos.y = (int) ((windowSize.height - controlsSize.height) * 0.9);
    
            controlsContainer.setLocation(controlPos);
        }
    }

    @Override
    public void setAlpha(final float alpha) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                // only on windows/mac handle alpha
                if (!OSUtils.isLinux()) {
                    AWTUtilities.setWindowOpacity(MPlayerOverlayControls.this, alpha);
                }
                
                isOverlayVisible = (alpha != 0);
            }
        });
    }

    private static JButton createMPlayerButton(final String image, final Point pos) {

        ImageIcon buttonImage = GUIMediator.getThemeImage(image);

        // create button
        JButton button = new JButton();

        // customize UI
        button.setIcon(buttonImage);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setRolloverEnabled(true);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusPainted(false);
        button.setSize(new Dimension(buttonImage.getIconWidth(), buttonImage.getIconHeight()));
        button.setLocation(pos);

        return button;
    }

    public void setIsFullscreen(boolean fullscreen) {
        fullscreenExitButton.setVisible(fullscreen);
        fullscreenEnterButton.setVisible(!fullscreen);
    }
    
    private void showOverlay(boolean animate) {
        
        if (!isOverlayVisible) {
            if (animate) {
                animateAlphaThread.animateToOpaque();
            } else {
                setAlpha(1.0f);
            }
            toFront();
        }
        
        hideTimer.restart();
    }

    private void hideOverlay(boolean animate) {
        
        if(isOverlayVisible) {
            if (animate) {
                animateAlphaThread.animateToTransparent();
            } else {
                setAlpha(0.0f);
            }
        }

        hideTimer.stop();
    }
    
    /**
    @Override
    public void paint(Graphics g) {
        
        // clear background

        Graphics2D g2d = (Graphics2D) g;
        try {
            g2d.setColor(TRANSPARENT);
            g2d.setComposite(AlphaComposite.Clear);
            //((Graphics2D)gg).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.0f));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            System.out.println("paint(): Filling TRANSPARENT " + getWidth() + "," + getHeight());

        } finally {
            //g2d.dispose();
        }
        
        // paint sub components
        super.paintComponents(g);
    }
    */
    
    private void onHideTimerExpired() {
        hideOverlay(true);
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

    private void onVolumeChanged(int value) {
        MPlayerUIEventHandler.instance().onVolumeChanged((float) value / 100.0f);
    }

    public void onProgressSliderTimeValueChange(float seconds) {
        MPlayerUIEventHandler.instance().onSeekToTime(seconds);
    }

    @Override
    public void onProgressSliderMouseDown() {
        MPlayerUIEventHandler.instance().onProgressSlideStart();
        hideTimer.stop();
    }

    @Override
    public void onProgressSliderMouseUp() {
        MPlayerUIEventHandler.instance().onProgressSlideEnd();
        hideTimer.restart();
    }

    public void onFullscreenEnterPressed() {
        MPlayerUIEventHandler.instance().onToggleFullscreenPressed();
    }

    public void onFullscreenExitPressed() {
        MPlayerUIEventHandler.instance().onToggleFullscreenPressed();
    }

    @Override
    public void mediaOpened(MediaPlayer mediaPlayer, MediaSource mediaSource) {
    }

    @Override
    public void progressChange(MediaPlayer mediaPlayer, float currentTimeInSecs) {
        durationInSeconds = mediaPlayer.getDurationInSecs();
        currentTimeInSeconds = currentTimeInSecs;

        // adjust progress slider
        progressSlider.setTotalTime((int) Math.round(durationInSeconds));
        progressSlider.setCurrentTime((int) Math.round(currentTimeInSeconds));
    }

    @Override
    public void volumeChange(MediaPlayer mediaPlayer, double currentVolume) {
        volumeSlider.setValue((int) (currentVolume * 100));
    }

    @Override
    public void stateChange(MediaPlayer mediaPlayer, MediaPlaybackState state) {
        switch (state) {
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
            break;
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
    

    @Override
    public void dispose() {
        animateAlphaThread.setDisposed();
        super.dispose();
    }


    /*
     * overrides for mouse input processing of client controls
     */
    
    private class OverlayControlsMouseAdapter extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            hideTimer.stop();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            hideTimer.restart();
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            showOverlay(true);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            showOverlay(true);
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 ) {
                
                // touch point screen location
                Point loc = e.getLocationOnScreen();
                
                // screen location of controlsContainer
                Rectangle rect = controlsContainer.getBounds();
                Point containerLoc = controlsContainer.getLocationOnScreen();
                rect.x = containerLoc.x;
                rect.y = containerLoc.y;
                
                // only go fullscreen when clicked outside bounds
                if (!rect.contains(loc)) {
                    MPlayerUIEventHandler.instance().onToggleFullscreenPressed();
                }
            }
        }
    
    }
    
    public static void main(String[] args) {
        MPlayerMediator.instance().showPlayerWindow(true);
    }

}
