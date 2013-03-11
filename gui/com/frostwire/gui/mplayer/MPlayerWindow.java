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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import org.limewire.util.OSUtils;

import com.frostwire.gui.player.MediaPlayerAdapter;
import com.frostwire.gui.player.MediaSource;
import com.frostwire.gui.player.MPlayerUIEventHandler;
import com.frostwire.gui.player.MediaPlayer;
import com.frostwire.gui.player.ScreenSaverDisabler;
import com.frostwire.mplayer.MediaPlaybackState;
import com.limegroup.gnutella.gui.LimeJFrame;

public class MPlayerWindow extends JFrame {

    private static final long serialVersionUID = -9154474667503959284L;

    private MPlayerOverlayControls overlayControls;
    protected MPlayerComponent mplayerComponent;
    protected Component videoCanvas;
    private boolean isFullscreen = false;
    private MediaPlayer player;
    private boolean handleVideoResize = true;
    private ScreenSaverDisabler screenSaverDisabler;
    
    protected MPlayerWindow() {
        initializeUI();

        screenSaverDisabler = new ScreenSaverDisabler();
        
        player = MediaPlayer.instance();
        player.addMediaPlayerListener(new MediaPlayerAdapter() {
            @Override
            public void mediaOpened(MediaPlayer mediaPlayer, MediaSource mediaSource) {
                MPlayerWindow.this.setPlayerWindowTitle();
            }

            @Override
            public void stateChange(MediaPlayer audioPlayer, MediaPlaybackState state) {

                if (state == MediaPlaybackState.Playing && handleVideoResize) {
                    handleVideoResize = false;
                    resizeCanvas();
                }

                if (state != MediaPlaybackState.Playing) {
                    handleVideoResize = true;
                }
            }
        });
    }

    public static MPlayerWindow createMPlayerWindow() {
        if (OSUtils.isWindows()) {
            return new MPlayerWindowWin32();
        } else if (OSUtils.isLinux()) {
            return new MPlayerWindowLinux();
        } else if (OSUtils.isMacOSX()) {
            return new MPlayerWindowOSX();
        } else {
            return null;
        }
    }

    public MediaPlayer getMediaPlayer() {
        return player;
    }

    private void initializeUI() {

        Dimension d = new Dimension(800, 600);

        // initialize window
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setPlayerWindowTitle();
        setBackground(new Color(0, 0, 0));
        setVisible(false);
        initWindowIcon();

        // initialize events
        addComponentListener(new MPlayerComponentHandler());
        addWindowListener(new WindowAdapter(){
            @Override public void windowClosing(WindowEvent e) {
                player.stop();
            }
        });
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new MPlayerKeyEventDispatcher());

        // initialize content pane & video canvas
        Container pane = getContentPane();
        pane.setBackground(Color.black);
        pane.setLayout(null);
        pane.setSize(d);
        pane.setPreferredSize(d);
        
        mplayerComponent = MPlayerComponentFactory.instance().createPlayerComponent();
        videoCanvas = mplayerComponent.getComponent();
        videoCanvas.setBackground(Color.black);
        videoCanvas.setSize(d);
        videoCanvas.setPreferredSize(d);
        pane.add(videoCanvas);

        // adjust frame size
        pack();

        // initialize overlay controls
        overlayControls = new MPlayerOverlayControls(this);
        overlayControls.setIsFullscreen(isFullscreen);
    }

    /**
     * Gets the application icon from the main window and puts it on the player window.
     */
    private void initWindowIcon() {
        if (OSUtils.isMacOSX()) {
            //no need.
            return;
        }

        for (Window w : getWindows()) {
            if (w.getParent() == null && w instanceof LimeJFrame) {
                List<Image> iconImages = w.getIconImages();
                if (iconImages.size() > 0) {
                    Image image = iconImages.get(0);
                    setIconImage(image);
                    return;
                }
            }
        }
    }

    private void setPlayerWindowTitle() {
        MediaSource source = MediaPlayer.instance().getCurrentMedia();

        if (source != null) {
            setTitle("FrostWire Media Player -  " + source.getTitleText());
        } else {
            setTitle("FrostWire Media Player");
        }
    }

    /**
     * correctly set visibility and positioning of window and control overlay
     */
    @Override
    public void setVisible(boolean visible) {

        if (visible != isVisible()) {

            super.setVisible(visible);

            if (visible) {
                centerOnScreen();
                // disable screen saver
                screenSaverDisabler.start();
            } else {
                // enable screen saver
                screenSaverDisabler.stop();
            }
        }
    }

    public void toggleFullScreen() {
        if (isVisible()) {
            isFullscreen = !isFullscreen;
            overlayControls.setIsFullscreen(isFullscreen);
        }
    }

    public long getCanvasComponentHwnd() {
        return 0;
    }

    public long getHwnd() {
        return 0;
    }

    private void resizeCanvas() {

        if (isVisible()) {
            Dimension videoSize = MediaPlayer.instance().getCurrentVideoSize();
            Dimension contentSize = getContentPane().getSize();
            Point origPos = getContentPane().getLocationOnScreen();
            if (contentSize == null || videoSize == null) {
                return; // can not resize until videoSize is available
            }
    
            Dimension canvasSize = new Dimension(contentSize);
            float targetAspectRatio = (float) videoSize.width / (float) videoSize.height;
    
            if (canvasSize.width / targetAspectRatio < contentSize.height) {
                canvasSize.height = (int) (canvasSize.width / targetAspectRatio);
            } else {
                canvasSize.width = (int) (canvasSize.height * targetAspectRatio);
            }
    
            Point tl = new Point();
            tl.x = (int) ((float) (contentSize.width - canvasSize.width) / 2.0f);
            tl.y = (int) ((float) (contentSize.height - canvasSize.height) / 2.0f);
    
            videoCanvas.setBounds(tl.x, tl.y, canvasSize.width, canvasSize.height);
            overlayControls.setBounds(origPos.x, origPos.y, contentSize.width, contentSize.height);
        }
    }

    /**
     * centers the window in the current screen
     */
    private void centerOnScreen() {

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension window = getSize();

        Point pos = new Point((screen.width - window.width) / 2, (screen.height - window.height) / 2);
        setLocation(pos);
    }

    private class MPlayerComponentHandler extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            resizeCanvas();
        }
    }

    private class MPlayerKeyEventDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {

            if (!isVisible()) {
                return false;
            }

            // limit keyboard processing for only when the MPlayerWindow is the focused window
            Window focusWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
            if (focusWindow != MPlayerWindow.this &&
                focusWindow != overlayControls) {
                return false;
            }
            
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                switch (e.getKeyCode()) {
                case KeyEvent.VK_P:
                case KeyEvent.VK_SPACE:
                    MPlayerUIEventHandler.instance().onTogglePlayPausePressed();
                    return true;
                case KeyEvent.VK_W:
                    if (OSUtils.isMacOSX() && e.isMetaDown()) {
                        player.stop();
                        MPlayerWindow.this.setVisible(false);
                        return true;
                    }
                case KeyEvent.VK_F:
                    toggleFullScreen();
                    return true;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_PERIOD:
                    MPlayerUIEventHandler.instance().onFastForwardPressed();
                    return true;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_COMMA:
                    MPlayerUIEventHandler.instance().onRewindPressed();
                    return true;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_PLUS:
                    MPlayerUIEventHandler.instance().onVolumeIncremented();
                    return true;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_MINUS:
                    MPlayerUIEventHandler.instance().onVolumeDecremented();
                    return true;
                case KeyEvent.VK_ESCAPE:
                    if (isFullscreen) {
                        MPlayerUIEventHandler.instance().onToggleFullscreenPressed();
                    }
                }

                // shft + - for volume increment
                if (e.getKeyCode() == KeyEvent.VK_EQUALS) {
                    MPlayerUIEventHandler.instance().onVolumeIncremented();
                    return true;
                }

                // Alt+Enter, Ctrl+Enter full screen shorcuts - seen in other players.
                if ((e.isAltDown() || e.isMetaDown() || e.isControlDown()) && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    MPlayerUIEventHandler.instance().onToggleFullscreenPressed();
                    return true;
                }
            }

            return false;
        }
    }
}
