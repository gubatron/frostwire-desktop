package com.frostwire.gnutella.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import com.frostwire.HttpFetcher;
import com.frostwire.ImageCache;
import com.frostwire.ImageCache.OnLoadedListener;
import com.frostwire.json.JsonEngine;
import com.limegroup.gnutella.gui.GUIMediator;

public class SlideshowPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -1964953870003850981L;

    private List<Slide> _slides;
    private boolean _randomStart;
    private int _currentSlideIndex;
    private BufferedImage _currentImage;
    private BufferedImage _lastImage;
    private boolean _loadingNextImage;
    private FadeSlideTransition _transition;
    private long _transitionTime;
    private Color _colorBackground;
    
    private boolean _started;
    
    /**
     * Last time stamp a slide was loaded
     */
    private long _lastSlideLoaded;
    
    /**
     * Timer to check if we need to switch slides
     */
    private Timer _timer;
    
    public SlideshowPanel(List<Slide> slides, boolean randomStart) {
        setup(slides, false);
    }
    
    public SlideshowPanel(String url) {
        try {
            HttpFetcher fetcher = new HttpFetcher(new URI(url));
            
            byte[] jsonBytes = fetcher.fetch();
            
            if (jsonBytes != null) {
                SlideList slideList = new JsonEngine().toObject(new String(jsonBytes), SlideList.class);
                setup(slideList.slides, slideList.randomStart);
            }    
        } catch (Exception e) {
            // nothing happens
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        
        g.setColor(_colorBackground);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        if (!_started) {
            startAnimation();
        }
        
        if (_transition != null) {
            _transition.paint(g);
            if (!_transition.isRunning()) {
                _transition = null;
            }
        }
        
        if (_transition == null && _currentImage != null) {
            int x = (getWidth() - _currentImage.getWidth()) / 2;
            int y = (getHeight() - _currentImage.getHeight()) / 2;
            g.drawImage(_currentImage, x, y, null);
        }
    }
    
    private void setup(List<Slide> slides, boolean randomStart) {
        _slides = slides;
        _randomStart = randomStart;
        _currentSlideIndex = -1;
        _colorBackground = getBackground();
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    Slide slide = _slides.get(_currentSlideIndex);
                    if (slide.url != null) {
                        GUIMediator.openURL(slide.url);
                    }
                    if (slide.torrent != null) {
                        if (slide.torrent.toLowerCase().startsWith("http")) {
                            GUIMediator.instance().openTorrentURI(new URI(slide.torrent));
                        } else if (slide.torrent.toLowerCase().startsWith("magnet:?")) {
                            GUIMediator.instance().openTorrentMagnet(slide.torrent);
                        }
                    }
                } catch (Exception ex) {
                }
            }
        });
    }

    private void startAnimation() {
        
        if(_slides == null || _slides.size() == 0) {
            return;
        }
        
        _started = true;
        _lastSlideLoaded = 0;
        
        if (_slides.size() == 1) {
            try {
                ImageCache.getInstance().getImage(new URL(_slides.get(0).imageSrc), new OnLoadedListener() {
                    public void onLoaded(URL url, BufferedImage image, boolean fromCache) {
                        _currentImage = image;
                        repaint();
                    }
                });
            } catch (MalformedURLException e) {
            }
        } else {
            _timer = new Timer();
            _timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    tryMoveNext();
                }
            }, 0, 200); // Check every 200 milliseconds if we should trigger a transition
        }
    }

    private void tryMoveNext() {
        
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
            try {
                ImageCache.getInstance().getImage(new URL(_slides.get(_currentSlideIndex).imageSrc), new OnLoadedListener() {
                    public void onLoaded(URL url, BufferedImage image, boolean fromCache) {
                        _currentImage = image;
                        repaint();
                    }
                });
            } catch (MalformedURLException e) {
            }
        } else {
            slide = _slides.get(_currentSlideIndex);
            if (slide.duration + _lastSlideLoaded + _transitionTime < System.currentTimeMillis()) {
                _currentSlideIndex = (_currentSlideIndex + 1) % _slides.size();
            } else {
                slide = null;
            }
        }
        
        if (slide != null) {
            _loadingNextImage = true;
            try {
                ImageCache.getInstance().getImage(new URL(slide.imageSrc), new OnLoadedListener() {
                    public void onLoaded(URL url, BufferedImage image, boolean fromCache) {
                        _currentImage = image;
                        if (_lastImage != null && _currentImage != null) {
                            _transition = new FadeSlideTransition(SlideshowPanel.this, _lastImage, _currentImage);
                            _transitionTime = _transition.getEstimatedDuration();
                            _transition.start();
                        }
                        _lastImage = _currentImage;
                        _loadingNextImage = false;
                        _lastSlideLoaded = System.currentTimeMillis();
                        repaint();
                    }
                });
            } catch (MalformedURLException e) {
            }
        }
    }
}
