package com.frostwire.gui.mplayer;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.peer.ComponentPeer;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.gudy.azureus2.core3.util.AESemaphore;
import org.gudy.azureus2.core3.util.SystemTime;
import org.limewire.util.SystemUtils;

import sun.awt.windows.WComponentPeer;

import com.sun.awt.AWTUtilities;

public class MPlayerApp extends JFrame {

    private static final long serialVersionUID = -2478006227527836645L;

    private MediaPlayerControls controls;
    private Canvas canvas;
    private Player player;

    private Point prevMousePosition;

    public MPlayerApp() {
        setupUI();
    }

    public synchronized Player getPlayer() {
        if (player == null && isVisible()) {
            player = new Player(getCanvasHwnd()) {
                @Override
                protected void onVideoSize(Dimension size) {
                    resizeCanvas(size);
                }
            };
        }

        return player;
    }

    protected void setupUI() {
        Dimension d = new Dimension(800, 600);
        setPreferredSize(d);
        setSize(d);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("MPlayerApp");

        canvas = new Canvas();
        canvas.setSize(d);

        getContentPane().setLayout(null);
        getContentPane().add(canvas);

        getContentPane().setBackground(Color.BLACK);
        setBackground(Color.BLACK);

        controls = new MediaPlayerControls(this);
        controls.setVisible(true);

        animateTransition.setDaemon(true);
        animateTransition.start();

        delayHideControls();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (player != null) {
                    resizeCanvas(player.getVideoSize());
                    repositionControls();
                }
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e);
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e);
            }
        });

        //If you
        
        //TODO: Use MPlayerKeyDispatcher, or an implementation that uses a player which has all the commands
        //required. Not sure under what circumstances this code is being used, or if it's being used at all.
        //I suppose this file MPlayerApp.java is what we did as proof of concept. 
        //If this class is no longer being used, let's remove it from the source code.
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
            	
            	/** Warning, you're probably looking at the wrong code.
            	 *  Go to: MPlayerWindow.MPlayerKeyEventDispatcher for the actual code handling the window player keys.
            	 */
           	
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    switch (e.getKeyCode()) {
                    case KeyEvent.VK_P:
                    case KeyEvent.VK_SPACE:
                        togglePause();
                        return true;

                    	
                    case KeyEvent.VK_F:
                        toggleFullScreen();
                        return true;
                    }
                }
                
                //volume up, volume down
                
                return false;
            }
        });
    }

    private Dimension getClientSize() {
        return getContentPane().getSize();
    }

    private void resizeCanvas(Dimension videoSize) {
        Dimension c = getClientSize();
        if (c == null || videoSize == null) {
            return; // too early
        }
        Dimension r = aspectResize(c, videoSize);

        if (r.width < c.width) {
            int dx = (c.width - r.width) / 2;
            canvas.setBounds(dx, 0, r.width, c.height);
        }
        if (r.height < c.height) {
            int dy = (c.height - r.height) / 2;
            canvas.setBounds(0, dy, c.width, r.height);
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

    private void handleMouseMove(MouseEvent e) {
        Point currMousePosition = e.getPoint();
        if (prevMousePosition != null) {
            if (!prevMousePosition.equals(currMousePosition)) {
                showControls();
                delayHideControls();
            }
        }
        prevMousePosition = currMousePosition;
    }

    private void repositionControls() {
        int x = (getClientSize().width - controls.getWidth()) / 2;
        int y = getClientSize().height - controls.getHeight() - 20;
        controls.setLocation(x, y);
    }

    private void delayHideControls() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // ignore
                }
                hideControls();
            }
        });

        t.start();
    }

    private void togglePause() {
        if (player != null) {
            if (player.isPaused()) {
                player.doResume();
            } else {
                player.doPause();
            }
        }
    }
    
    private void volumeUp() {
    	
    }

    private void toggleFullScreen() {
        SystemUtils.toggleFullScreen(getHwnd());
    }

    @Override
    public void dispose() {
        disposed = true;
        super.dispose();
    }

    // BEGIN: code for animated overlay
    private static final int TARGET_ALPHA = 90 * 255 / 100;
    private static final int ALPHA_STEP = 20;

    private boolean disposed = false;
    private Object animationStart = new Object();
    private boolean isHiding;
    private boolean isShowing;
    private int currentAlpha = TARGET_ALPHA;
    private boolean stopAlphaThread = false;
    private Thread animateTransition = new Thread("Player Controls Alpha Animation") {
        public void run() {
            while (!stopAlphaThread && !disposed) {
                if (isHiding) {
                    if (currentAlpha > 0) {
                        if (currentAlpha >= ALPHA_STEP) {
                            currentAlpha -= ALPHA_STEP;
                        } else {
                            currentAlpha = 0;
                        }

                        setAlpha(currentAlpha);
                    } else {
                        isHiding = false;
                    }
                }
                if (isShowing) {
                    if (currentAlpha < TARGET_ALPHA) {
                        if (currentAlpha <= TARGET_ALPHA - ALPHA_STEP) {
                            currentAlpha += ALPHA_STEP;
                        } else {
                            currentAlpha = TARGET_ALPHA;
                        }
                        setAlpha(currentAlpha);
                    } else {
                        isShowing = false;
                    }
                }

                try {
                    if (isShowing || isHiding) {
                        Thread.sleep(50);
                    } else {
                        synchronized (animationStart) {

                            if (stopAlphaThread) {
                                return;
                            }

                            animationStart.wait();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    };

    private void setAlpha(final int alpha) {
        if (disposed) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                AWTUtilities.setWindowOpacity(controls, (alpha * 1f) / TARGET_ALPHA);
                if (alpha == 0 && controls.isVisible()) {
                    controls.setVisible(false);
                }
                if (alpha > 0 && !controls.isVisible()) {
                    controls.setVisible(true);
                }
            }
        });
    }

    // END

    private void hideControls() {
        if (isHiding) {
            return;
        }
        if (isShowing) {
            isShowing = false;
        }
        isHiding = true;
        synchronized (animationStart) {
            animationStart.notify();
        }
    }

    private void showControls() {
        if (isShowing) {
            return;
        }
        if (isHiding) {
            isHiding = false;
        }
        isShowing = true;
        synchronized (animationStart) {
            animationStart.notify();
        }
    }

    private long getHwnd() {
        @SuppressWarnings("deprecation")
        ComponentPeer cp = getPeer();
        if ((cp instanceof WComponentPeer)) {
            return ((WComponentPeer) cp).getHWnd();
        } else {
            return 0;
        }
    }

    private long getCanvasHwnd() {
        @SuppressWarnings("deprecation")
        ComponentPeer cp = canvas.getPeer();
        if ((cp instanceof WComponentPeer)) {
            return ((WComponentPeer) cp).getHWnd();
        } else {
            return 0;
        }
    }

    public class MediaPlayerControls extends JDialog {

        private static final long serialVersionUID = 7921630139241359325L;

        public MediaPlayerControls(Frame frame) {
            super(frame);
            setupUI();
        }

        protected void setupUI() {
            Dimension d = new Dimension(300, 100);
            setPreferredSize(d);
            setSize(d);
            setUndecorated(true);

            Container panel = getContentPane();

            getContentPane().setBackground(Color.BLUE);

            JButton togglePauseButton = new JButton("TP");
            togglePauseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    togglePause();
                }
            });
            panel.add(togglePauseButton, BorderLayout.LINE_START);

            JButton toggleFullScreen = new JButton("TFS");
            toggleFullScreen.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    toggleFullScreen();
                }
            });
            panel.add(toggleFullScreen, BorderLayout.LINE_END);
        }
    }

    public static class Player {

        private final long hwnd;

        private Dimension videoSize;

        public Player(long hwnd) {
            this.hwnd = hwnd;
        }

        public Dimension getVideoSize() {
            return videoSize;
        }

        public boolean isPaused() {
            return paused;
        }

        private List<String> commands = new LinkedList<String>();
        private int pending_sleeps;
        private volatile long seekingSendTime;
        private volatile Process mPlayerProcess;
        private boolean paused;
        private AESemaphore command_sem = new AESemaphore("EMP:C");

        public void play(String mplayerPath, String videoPath) {
            try {
                List<String> cmdList = new ArrayList<String>();

                cmdList.add(mplayerPath);

                cmdList.add("-slave");

                cmdList.add("-vo");
                cmdList.add("direct3d");

                cmdList.add("-identify");

                cmdList.add("-prefer-ipv4");

                cmdList.add("-osdlevel");
                cmdList.add("0");

                cmdList.add("-noautosub");

                cmdList.add("-priority");
                cmdList.add("high");

                cmdList.add("-framedrop");

                cmdList.add("-wid");
                cmdList.add(String.valueOf(hwnd));

                cmdList.add(videoPath);

                String[] cmd = cmdList.toArray(new String[0]);
                mPlayerProcess = Runtime.getRuntime().exec(cmd);

                InputStream stdOut = mPlayerProcess.getInputStream();
                InputStream stdErr = mPlayerProcess.getErrorStream();
                OutputStream stdIn = mPlayerProcess.getOutputStream();

                final BufferedReader brStdOut = new BufferedReader(new InputStreamReader(stdOut));
                final BufferedReader brStdErr = new BufferedReader(new InputStreamReader(stdErr));
                final PrintWriter pwStdIn = new PrintWriter(new OutputStreamWriter(stdIn));

                Thread stdOutReader = new Thread("Player Console Out Reader") {
                    public void run() {
                        try {
                            String line;
                            while ((line = brStdOut.readLine()) != null) {
                                //System.out.println("<- " + line);

                                if (line.contains("VO: [direct3d]")) {
                                    parseVideoSize(line);
                                }
                            }
                        } catch (Exception e) {
                            // e.printStackTrace();
                        }
                    };
                };
                stdOutReader.setDaemon(true);
                stdOutReader.start();

                Thread stdErrReader = new Thread("Player Console Err Reader") {
                    public void run() {
                        try {
                            String line;
                            while ((line = brStdErr.readLine()) != null) {
                                System.out.println("<- " + line);
                            }
                        } catch (Exception e) {
                            // e.printStackTrace();
                        }
                    };
                };
                stdErrReader.setDaemon(true);
                stdErrReader.start();

                Thread stdInWriter = new Thread("Player Console In Writer") {
                    public void run() {
                        try {
                            while (true) {

                                command_sem.reserve();

                                String toBeSent;

                                synchronized (Player.this) {
                                    if (commands.isEmpty()) {
                                        break;
                                    }

                                    toBeSent = commands.remove(0);
                                }

                                System.out.println("-> " + toBeSent);

                                if (toBeSent.startsWith("sleep ") || toBeSent.startsWith("pausing_keep_force sleep ")) {

                                    int millis = Integer.parseInt(toBeSent.substring(toBeSent.startsWith("p") ? 25 : 6));

                                    try {
                                        Thread.sleep(millis);
                                    } catch (Throwable e) {
                                        // ignore
                                    }

                                    synchronized (Player.this) {
                                        pending_sleeps -= millis;
                                    }
                                } else if (toBeSent.startsWith("seek") || toBeSent.startsWith("pausing_keep_force seek")) {
                                    seekingSendTime = SystemTime.getMonotonousTime();
                                }

                                toBeSent = toBeSent.replaceAll("\\\\", "\\\\\\\\");

                                pwStdIn.write(toBeSent + "\n");

                                pwStdIn.flush();

                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        } finally {
                            // stop_sem.releaseForever();
                        }
                    };
                };
                stdInWriter.setDaemon(true);
                stdInWriter.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void sendCommand(String cmd, boolean pauseKeep) {
            synchronized (this) {

                // if ( stopped ){
                //
                // return;
                // }

                commands.add((pauseKeep && paused ? "pausing_keep_force " : "") + cmd);

                command_sem.release();
            }
        }

        protected boolean doPause() {
            synchronized (this) {

                if (paused) {

                    return (false);
                }

                paused = true;

                //pausedStateChanging();

                sendCommand("pause", false);

                return (true);
            }
        }

        protected boolean doResume() {
            synchronized (this) {

                if (!paused) {

                    return (false);
                }

                paused = false;

                // pausedStateChanging();

                sendCommand("pause", false);

                return (true);
            }
        }

        protected void sendCommand(String cmd) {
            sendCommand(cmd, true);
        }

        private void parseVideoSize(String line) {
            String[] arr = line.split(" ")[2].split("x");
            int w = Integer.parseInt(arr[0]);
            int h = Integer.parseInt(arr[1]);

            videoSize = new Dimension(w, h);

            onVideoSize(videoSize);
        }

        protected void onVideoSize(Dimension size) {

        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MPlayerApp t = new MPlayerApp();

                t.setVisible(true);

                t.getPlayer().play("mplayer.exe", "C:\\Users\\erichpleny\\Documents\\test.avi");
            }
        });
    }
}
