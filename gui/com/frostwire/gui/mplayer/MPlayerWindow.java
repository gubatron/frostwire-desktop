package com.frostwire.gui.mplayer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.peer.ComponentPeer;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.limewire.util.OSUtils;
import org.limewire.util.SystemUtils;

import sun.awt.windows.WComponentPeer;

import com.frostwire.gui.player.AudioSource;
import com.frostwire.gui.player.MediaPlayer;
import com.frostwire.gui.player.MediaPlayerListener;
import com.frostwire.mplayer.MediaPlaybackState;
import com.limegroup.gnutella.gui.LimeJFrame;

public class MPlayerWindow extends JFrame implements MediaPlayerListener {

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
    private boolean handleVideoResize = true;
    
	public MPlayerWindow() {
		initializeUI();	
        
        player = MediaPlayer.instance();
        player.addMediaPlayerListener(this);
    }
	
	private void initializeUI () {
		
		Dimension d = new Dimension(800, 600);
        
        // initialize auto-hide timer
		hideTimer = new Timer(HIDE_DELAY, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MPlayerWindow.this.onHideTimerExpired();
			}
		});
		hideTimer.setRepeats(false);
		
		// initialize window
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setTitle("Frostwire Media Player");
        setBackground(new Color(0,0,0));
        initWindowIcon();
        
        
        // initialize events
        addMouseMotionListener(new MPlayerMouseMotionAdapter());
        addComponentListener(new MPlayerComponentHandler());
        addWindowListener( new MPlayerWindowAdapter());
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new MPlayerKeyEventDispatcher() ); 
        
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
        videoCanvas.addMouseMotionListener(new MPlayerMouseMotionAdapter());
        pane.add(videoCanvas);
        
        // adjust frame size
        pack();
        
        // initialize overlay controls
        overlayControls = new MPlayerOverlayControls(hideTimer);
        overlayControls.setVisible(false);
        overlayControls.setAlwaysOnTop(true);
        overlayControls.setIsFullscreen(isFullscreen);
        overlayControls.addMouseListener(new MPlayerMouseAdapter() );
        overlayControls.addMouseMotionListener(new MPlayerMouseMotionAdapter());
        
        // initialize animation alpha thread
        animateAlphaThread = new AlphaAnimationThread(overlayControls);
        animateAlphaThread.setDaemon(true);
        animateAlphaThread.start();


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
	        if (w.getParent()==null && w instanceof LimeJFrame) {
	            List<Image> iconImages = w.getIconImages();
	            if (iconImages.size() > 0) {
	            	Image image = iconImages.get(0);
	            	setIconImage(image);
	            	return;
	            }
	        }
	    }
    }

	/**
	 * correctly set visibility and positioning of window and control overlay
	 */
	@Override
	public void setVisible( boolean visible ) {
		
		if ( visible != isVisible() ) {
		
			super.setVisible(visible);
			overlayControls.setVisible(visible);
			
			if ( visible ) {
				
				centerOnScreen();
				positionOverlayControls();
				
				showOverlay(false);
				
			} else {
			
				hideOverlay(false);
			}
		}
		
		if ( visible ) {
			// make sure window is on top of visible windows
			toFront();
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
	
    private void resizeCanvas() {
        
		Dimension videoSize = MediaPlayer.instance().getCurrentVideoSize(); 
    	Dimension contentSize = getContentPane().getSize();
        if (contentSize == null || videoSize == null) {
            return; // can not resize until videoSize is available
        }
        
        Dimension canvasSize = new Dimension(contentSize);
        float targetAspectRatio = (float)videoSize.width / (float)videoSize.height;
        
        if (canvasSize.width / targetAspectRatio < contentSize.height) {
        	canvasSize.height = (int) (canvasSize.width / targetAspectRatio);
        } else {
        	canvasSize.width = (int) (canvasSize.height * targetAspectRatio);
        }
        
        Point tl = new Point();
        tl.x = (int) ((float)(contentSize.width - canvasSize.width) / 2.0f);
        tl.y = (int) ((float)(contentSize.height - canvasSize.height) / 2.0f);
        
        videoCanvas.setBounds(tl.x, tl.y, canvasSize.width, canvasSize.height);
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
		
		if ( isVisible() ) {
			Dimension controlsSize = overlayControls.getSize();
			Dimension windowSize = getSize();
			Point windowPos = getLocationOnScreen();
			
			Point controlPos = new Point();
			controlPos.x = (int) ((windowSize.width - controlsSize.width) * 0.5 + windowPos.x);
			controlPos.y = (int) ((windowSize.height - controlsSize.height) - 20 + windowPos.y);
			
			overlayControls.setLocation(controlPos);
		}
	}

	private void onHideTimerExpired() {
		animateAlphaThread.animateToTransparent();
	}

	@Override
    public void dispose() {
        animateAlphaThread.setDisposed();
        super.dispose();
    }
	
	@Override
	public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));
        
        super.paint(g2);
        
        g2.dispose();
    }
	
	private void showOverlay(boolean animate) {
		if (animate)
			animateAlphaThread.animateToOpaque();
		else
			overlayControls.setVisible(true);
		
		hideTimer.restart();
	}
	
	private void hideOverlay(boolean animate) {
		if (animate)
			animateAlphaThread.animateToTransparent();
		else
			overlayControls.setVisible(false);
		
		hideTimer.stop();
	}
    
	private class MPlayerComponentHandler extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
        	resizeCanvas();
            positionOverlayControls();
        }
        
        @Override
        public void componentMoved(ComponentEvent e) {
        	positionOverlayControls();
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
	
	private class MPlayerMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			showOverlay(false);
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			showOverlay(false);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			showOverlay(false);
		}
	}
	
	private class MPlayerMouseMotionAdapter extends MouseMotionAdapter {
		@Override
        public void mouseMoved(MouseEvent e) {
			
			if (MPlayerWindow.this.isActive()) {
				Point2D currMousePosition = e.getPoint();
				
				if ( prevMousePosition == null ) {
					prevMousePosition = currMousePosition;
				}
				
				double distance = currMousePosition.distance(prevMousePosition);
	
		    	if (distance > 10) {
		            showOverlay(true);
		        }
		    	
		        prevMousePosition = currMousePosition;
			}
        }
	}
	
	private class MPlayerWindowAdapter extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent e) {
			player.stop();
		}
		
		@Override
		public void windowDeactivated(WindowEvent e) {
			if (e.getOppositeWindow() == overlayControls) {
				requestFocus();
			} else {
				hideOverlay(false);
			}
		}
		
		@Override
		public void windowActivated(WindowEvent e) {
			if (e.getOppositeWindow() != overlayControls) {
				showOverlay(false);
			}
		}	
	}

	@Override
	public void mediaOpened(MediaPlayer audioPlayer, AudioSource audioSource) {	}

	@Override
	public void progressChange(MediaPlayer audioPlayer, float currentTimeInSecs) { }

	@Override
	public void volumeChange(MediaPlayer audioPlayer, double currentVolume) { }

	@Override
	public void stateChange(MediaPlayer audioPlayer, MediaPlaybackState state) {
		
		if ( state == MediaPlaybackState.Playing && handleVideoResize ) {
			handleVideoResize = false;
			resizeCanvas();
		}
		
		if ( state != MediaPlaybackState.Playing ){
			handleVideoResize = true;
		}
	}

	@Override
	public void icyInfo(MediaPlayer audioPlayer, String data) { }
}
