package com.frostwire.gui.components;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import org.limewire.util.OSUtils;

import com.frostwire.HttpFetcher;
import com.frostwire.ImageCache;
import com.frostwire.ImageCache.OnLoadedListener;
import com.frostwire.JsonEngine;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.settings.ApplicationSettings;

public class SlideshowPanel extends JPanel {

    private static final long serialVersionUID = -1964953870003850981L;

    private List<Slide> _slides;
    private boolean _randomStart;
    private int _currentSlideIndex;
    private BufferedImage _currentImage;
    private BufferedImage _lastImage;
    private BufferedImage _masterImage1;
    private BufferedImage _masterImage2;
    private boolean masterFlag;
    private boolean _loadingNextImage;
    private FadeSlideTransition _transition;
    private long _transitionTime;
    private boolean _started;

    private boolean _stoppedTransitions;

    public interface SlideshowListener {
        public void onSlideChanged();
    }

    private List<SlideshowListener> _listeners;

    /**
     * Last time stamp a slide was loaded
     */
    private long _lastTimeSlideLoaded;

    /**
     * Timer to check if we need to switch slides
     */
    private Timer _timer;

    private JPanel _controlsContainer;

    private boolean _useControls;

    public SlideshowPanel(List<Slide> slides, boolean randomStart) {
        setup(slides, false);
    }

    public SlideshowPanel(final String url) {
        new Thread(new Runnable() {
            public void run() {
                load(url);
            }
        }).start();
    }

    private void load(final String url) {
        try {
            HttpFetcher fetcher = new HttpFetcher(new URI(url));

            byte[] jsonBytes = fetcher.fetch();

            if (jsonBytes != null) {
                final SlideList slideList = new JsonEngine().toObject(new String(jsonBytes), SlideList.class);
                GUIMediator.safeInvokeLater(new Runnable() {
                    public void run() {
                        try {
                            setup(slideList.slides, slideList.randomStart);
                            repaint();
                        } catch (Exception e) {
                            System.out.println("Failed load of Slide Show:" + url);
                            _slides = null;
                            // nothing happens
                            e.printStackTrace();
                        }
                    }
                });

            }
            //System.out.println("Loaded Slide Show for:" + url);
        } catch (Exception e) {
            System.out.println("Failed load of Slide Show:" + url);
            _slides = null;
            // nothing happens
            e.printStackTrace();
        }
    }

    @Override
    public int getHeight() {
        return super.getHeight() - 1;
    }

    @Override
    protected void paintComponent(Graphics g) {

        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight() + 4);

        if (!_started && !_stoppedTransitions) {
            startAnimation();
        }

        if (_transition != null) {
            _transition.paint(g);
            if (!_transition.isRunning()) {
                _transition = null;
            }
        }

        if (_transition == null && _currentImage != null) {
            g.drawImage(_currentImage, 0, 0, null);
        }
    }

    private void setup(List<Slide> slides, boolean randomStart) {
        _slides = filter(slides);

        _randomStart = randomStart;
        _currentSlideIndex = -1;

        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {

                    if (_currentImage == null) {
                        return;
                    }

                    int actualSlideIndex;
                    if (_currentSlideIndex == -1 && _slides != null && _slides.size() > 0) {
                        actualSlideIndex = 0;
                    } else {
                        actualSlideIndex = _currentSlideIndex;
                    }

                    Slide slide = _slides.get(actualSlideIndex);
                    if (slide.url != null) {
                        GUIMediator.openURL(slide.url);
                    }
                    if (slide.torrent != null) {
                        if (slide.torrent.toLowerCase().startsWith("http")) {
                            GUIMediator.instance().openTorrentURI(slide.torrent);
                        } else if (slide.torrent.toLowerCase().startsWith("magnet:?")) {
                            GUIMediator.instance().openTorrentURI(slide.torrent);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        if (_controlsContainer != null && _useControls) {
            _controlsContainer.add(new SlideshowPanelControls(this), BorderLayout.PAGE_END);
        }

    }

    private void startAnimation() {

        if (_slides == null || _slides.size() == 0 || _stoppedTransitions) {
            return;
        }

        _started = true;
        _lastTimeSlideLoaded = 0;

        if (_slides.size() == 1) {
            try {
                ImageCache.instance().getImage(new URL(_slides.get(0).imageSrc), new OnLoadedListener() {
                    public void onLoaded(URL url, BufferedImage image, boolean fromCache, boolean fail) {
                        _currentImage = image;
                        repaint();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            _timer = new Timer();
            _timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    tryMoveNext(false);
                }
            }, 0, 200); // Check every 200 milliseconds if we should trigger a transition
        }
    }

    private void tryMoveNext(boolean forceCurrentIndex) {

        if (_loadingNextImage) {
            return;
        }

        Slide slide = null;

        if (_currentSlideIndex == -1) {
            if (_randomStart) {
                _currentSlideIndex = new Random(System.currentTimeMillis()).nextInt(_slides.size());
            } else {
                _currentSlideIndex = 0;
            }
            _loadingNextImage = true;
            try {
                ImageCache.instance().getImage(new URL(_slides.get(_currentSlideIndex).imageSrc), new OnLoadedListener() {
                    public void onLoaded(URL url, BufferedImage image, boolean fromCache, boolean fail) {
                        _currentImage = image;
                        _loadingNextImage = false;
                        if (_currentImage != null) {
                            _lastImage = _currentImage;
                            _lastTimeSlideLoaded = System.currentTimeMillis();
                            repaint();
                        }
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            slide = _slides.get(_currentSlideIndex);
            if (forceCurrentIndex) {
                //Switch Image without 
                try {
                    ImageCache.instance().getImage(new URL(slide.imageSrc), new OnLoadedListener() {
                        public void onLoaded(URL url, BufferedImage image, boolean fromCache, boolean fail) {
                            _currentImage = prepareImage(image);

                            if (_transition != null) {
                                _transition.stop();
                            }

                            _transition = null;
                            repaint();
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return;
            } else if (slide.duration + _lastTimeSlideLoaded + _transitionTime < System.currentTimeMillis()) {
                _currentSlideIndex = (_currentSlideIndex + 1) % _slides.size();
                slide = _slides.get(_currentSlideIndex);
            } else {
                slide = null;
            }
        }

        if (slide != null) {
            _loadingNextImage = true;
            try {
                ImageCache.instance().getImage(new URL(slide.imageSrc), new OnLoadedListener() {
                    public void onLoaded(URL url, BufferedImage image, boolean fromCache, boolean fail) {
                        _currentImage = prepareImage(image);
                        if (_lastImage != null && _currentImage != null) {
                            _transition = new FadeSlideTransition(SlideshowPanel.this, _lastImage, _currentImage);
                            _transitionTime = _transition.getEstimatedDuration();
                            _transition.start();
                        }
                        _loadingNextImage = false;
                        if (_currentImage != null) {
                            _lastImage = _currentImage;
                            _lastTimeSlideLoaded = System.currentTimeMillis();
                            repaint();
                        }
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    protected BufferedImage prepareImage(BufferedImage image) {
        if (image == null) {
            return null;
        }

        try {
            BufferedImage bImage = getMasterImage(800, 400);
            Graphics2D g = null;

            try {
                g = bImage.createGraphics();

                int x = 0;
                int y = 0;

                //will try to center images that are smaller than the container.
                if (image.getHeight() < getHeight()) {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                    y = (getHeight() - image.getHeight()) / 2;
                }

                if (image.getWidth() < getWidth()) {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                    x = (getWidth() - image.getWidth()) / 2;
                }

                g.drawImage(image, x, y, null);

            } finally {
                if (g != null) {
                    g.dispose();
                }
            }

            return bImage;
        } catch (Exception e) {
            System.out.println("Error creating image for SlideShow " + "(" + getWidth() + ", " + getHeight() + ")");
            e.printStackTrace();
            return null;
        }
    }

    private List<Slide> filter(List<Slide> slides) {
        List<Slide> result = new ArrayList<Slide>(slides.size());

        for (Slide slide : slides) {
            if (isMessageEligibleForMyLang(slide.language) && isMessageEligibleForMyOs(slide.os)) {
                result.add(slide);
            }
        }

        return result;
    }

    /*
     * Examples of when this returns true
     * given == lang in app
     * es_ve == es_ve
     * es == es_ve
     * * == es_ve
     */
    private boolean isMessageEligibleForMyLang(String lang) {

        if (lang == null || lang.equals("*"))
            return true;

        String langinapp = ApplicationSettings.getLanguage().toLowerCase();

        if (lang.length() == 2)
            return langinapp.toLowerCase().startsWith(lang.toLowerCase());

        return lang.equalsIgnoreCase(langinapp);
    }

    private boolean isMessageEligibleForMyOs(String os) {
        if (os == null)
            return true;

        boolean im_mac_msg_for_me = os.equals("mac") && OSUtils.isMacOSX();

        boolean im_windows_msg_for_me = os.equals("windows") && OSUtils.isWindows();

        boolean im_linux_msg_for_me = os.equals("linux") && OSUtils.isLinux();

        return im_mac_msg_for_me || im_windows_msg_for_me || im_linux_msg_for_me;
    }

    public boolean hasSlides() {
        return _slides != null && _slides.size() > 0;
    }

    private BufferedImage getMasterImage(int w, int h) {
        masterFlag = !masterFlag;
        if (masterFlag) {
            if (_masterImage1 == null) {
                _masterImage1 = null;
                _masterImage1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            }
            return _masterImage1;
        } else {
            if (_masterImage2 == null) {
                _masterImage2 = null;
                _masterImage2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            }
            return _masterImage2;
        }
    }

    public void onTransitionStarted() {
        //notify listeners.
        if (_listeners == null || _listeners.size() == 0) {
            return;
        }

        for (SlideshowListener listener : _listeners) {
            listener.onSlideChanged();
        }

    }

    public int getNumSlides() {
        if (_slides == null) {
            return -1;
        }

        return _slides.size();
    }

    public int getCurrentSlideIndex() {
        return _currentSlideIndex;
    }

    /**
     * This method is for a user who wants to jump at a random slide.
     * It'll stop the timer.
     * 
     * @param index
     */
    public void switchToSlide(int index) {
        stopTransitions();
        _currentSlideIndex = index;
        tryMoveNext(true);
    }

    public void stopTransitions() {
        _stoppedTransitions = true;
        _loadingNextImage = false;
        if (_timer != null) {
            _timer.cancel();
        }
        _lastTimeSlideLoaded = 0;
    }

    public void addListener(SlideshowListener myDummyListener) {
        if (_listeners == null) {
            _listeners = new ArrayList<SlideshowPanel.SlideshowListener>();
        }

        _listeners.add(myDummyListener);
    }

    public void removeListener(SlideshowListener myDummyListener) {
        if (_listeners == null) {
            return;
        }

        _listeners.remove(myDummyListener);
    }

    public void setupContainerAndControls(JPanel container, boolean useControls) {
        _controlsContainer = container;
        _useControls = useControls;
    }
}
