package com.frostwire.gui.mplayer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.peer.ComponentPeer;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.limewire.util.SystemUtils;

import sun.awt.windows.WComponentPeer;

import com.frostwire.gui.player.MediaPlayer;

public class MPlayerWindow extends JFrame {

	private static final long serialVersionUID = -9154474667503959284L;

	private MPlayerOverlayControls overlayControls;
    private MPlayerComponent mplayerComponent;
	private Component videoCanvas;
    private boolean isFullscreen = false;
    
    private AlphaAnimationThread animateAlphaThread;
    
	private Timer hideTimer;
	private static final int HIDE_DELAY = 3000;
	
	private MediaPlayer player;
	
    private Point2D prevMousePosition = null;

	public MPlayerWindow() {
        initializeUI();
        
        player = MediaPlayer.instance();
    }
	
	private void initializeUI () {
		
		Dimension d = new Dimension(800, 600);
        
		// initialize window
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setTitle("Frostwire Media Player");
        setBackground(Color.black);
		setPreferredSize(d);
        setSize(d);
        
        // initialize events
        addMouseMotionListener(new MPlayerMouseMotionAdapter());
        addComponentListener(new MPlayerComponentHandler());
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new MPlayerKeyEventDispatcher() ); 
        
        // initialize content pane & video canvas
		Container pane = getContentPane();
        pane.setBackground(Color.black);
        pane.setLayout(null);
        
        mplayerComponent = MPlayerComponentFactory.instance().createPlayerComponent();
        videoCanvas = mplayerComponent.getComponent();
        videoCanvas.setBackground(Color.black);
        videoCanvas.setSize(d);
        videoCanvas.addMouseMotionListener(new MPlayerMouseMotionAdapter());
        pane.add(videoCanvas);
        
        // initialize overlay controls
        overlayControls = new MPlayerOverlayControls(this);
        overlayControls.setVisible(false);
        overlayControls.setAlwaysOnTop(true);
        overlayControls.setIsFullscreen(isFullscreen);
        
        // initialize animation alpha thread
        animateAlphaThread = new AlphaAnimationThread(overlayControls);
        animateAlphaThread.setDaemon(true);
        animateAlphaThread.start();

        // initialize auto-hide timer
		hideTimer = new Timer(HIDE_DELAY, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MPlayerWindow.this.onHideTimerExpired();
			}
		});
		hideTimer.setRepeats(false);
	}

	/**
	 * correctly set visibility and positioning of window and control overlay
	 */
	@Override
	public void setVisible( boolean visible ) {
		
		if ( visible != isVisible() ) {
		
			super.setVisible(visible);
		
			if ( isVisible() ) {
				
				overlayControls.setVisible(true);
				hideTimer.start();
				
				centerOnScreen();
				positionOverlayControls();
			
			} else {
			
				overlayControls.setVisible(false);
				hideTimer.stop();
			}
		}
	}
	
	public void toggleFullScreen() {
		if ( isVisible() ) {
			isFullscreen = !isFullscreen;
			overlayControls.setIsFullscreen(isFullscreen);
			
			if (!mplayerComponent.toggleFullScreen()) {
				SystemUtils.toggleFullScreen(getHwnd());
			}
			
			positionOverlayControls();
		}
    }
	
	public long getCanvasComponentHwnd() {
        @SuppressWarnings("deprecation")
        ComponentPeer cp = videoCanvas.getPeer();
        if ((cp instanceof WComponentPeer)) {
            return ((WComponentPeer) cp).getHWnd();
        } else {
            return 0;
        }
    }
	
	public long getHwnd() {
		@SuppressWarnings("deprecation")
        ComponentPeer cp = getPeer();
        if ((cp instanceof WComponentPeer)) {
            return ((WComponentPeer) cp).getHWnd();
        } else {
            return 0;
        }
    }
	
    private void resizeCanvas(Dimension videoSize) {
        Dimension c = getContentPane().getSize();
        if (c == null || videoSize == null) {
            return; // too early
        }
        Dimension r = aspectResize(c, videoSize);

        if (r.width < c.width) {
            int dx = (c.width - r.width) / 2;
            videoCanvas.setBounds(dx, 0, r.width, c.height);
        }
        if (r.height < c.height) {
            int dy = (c.height - r.height) / 2;
            videoCanvas.setBounds(0, dy, c.width, r.height);
        }
    }

    // not perfect, take in consideration smaller videos, works fine for 1080p videos
    private Dimension aspectResize(Dimension c, Dimension v) {
        Dimension r = new Dimension();

        float ratioW = c.width * 1.0f / v.width;
        float ratioH = c.height * 1.0f / v.height;

        float ratio = ratioW < ratioH ? ratioW : ratioH;

        r.width = (int) (v.width * ratio);
        r.height = (int) (v.height * ratio);

        return r;
    }
	
	
	/**
	 * centers the window in the current screen
	 */
	private void centerOnScreen() {	
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension window = getSize();
		
		Point pos = new Point((screen.width - window.width) / 2, (screen.height - window.height) / 2);
		setLocation( pos );	
	}
	
	/**
	 * positions the overlay control centered horizontally and 80% down vertically
	 */
	private void positionOverlayControls() {
		
		Dimension controlsSize = overlayControls.getSize();
		Dimension windowSize = getSize();
		Point windowPos = getLocationOnScreen();
		
		Point controlPos = new Point();
		controlPos.x = (int) ((windowSize.width - controlsSize.width) * 0.5 + windowPos.x);
		controlPos.y = (int) ((windowSize.height - controlsSize.height) - 20 + windowPos.y);
		
		overlayControls.setLocation(controlPos);
	}

	private void onHideTimerExpired() {
		animateAlphaThread.animateToTransparent();
	}

	@Override
    public void dispose() {
        animateAlphaThread.setDisposed();
        super.dispose();
    }
    
	private class MPlayerComponentHandler extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
        	Dimension size = MediaPlayer.instance().getCurrentVideoSize();
            if (size != null) {
                resizeCanvas(size);
                positionOverlayControls();
        	}
        }
    }
	
	private class MPlayerKeyEventDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED && isVisible()) {
            	switch (e.getKeyCode()) {
	            	case KeyEvent.VK_P: player.togglePause(); return true;
	                case KeyEvent.VK_F: toggleFullScreen(); return true;
	                case KeyEvent.VK_RIGHT:
	                case KeyEvent.VK_PERIOD: player.fastForward(); return true;
	                case KeyEvent.VK_LEFT:
	                case KeyEvent.VK_COMMA: player.rewind(); return true;
                }
            }
            return false;
        }
	}
	
	private class MPlayerMouseMotionAdapter extends MouseMotionAdapter {
		@Override
        public void mouseMoved(MouseEvent e) {
			
			Point2D currMousePosition = e.getPoint();
			
			if ( prevMousePosition == null ) {
				prevMousePosition = currMousePosition;
			}
			
			double distance = currMousePosition.distance(prevMousePosition);

	    	if (distance > 10) {
	            hideTimer.restart();
	        	animateAlphaThread.animateToOpaque();
	        }
	    	
	        prevMousePosition = currMousePosition;
        }
	}
}
