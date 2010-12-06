package com.frostwire.gnutella.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import com.frostwire.HttpFetcher;
import com.frostwire.ImageCache;
import com.frostwire.ImageCache.OnLoadedListener;
import com.frostwire.json.JsonEngine;

public class SlideshowPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -1964953870003850981L;

    private List<Slide> _slides;
    private int _currentSlideIndex;
    private BufferedImage _currentImage;
    private BufferedImage _lastImage;
    private boolean _loadingNextImage;
    private FadeSlideTransition _transition;
    private long _transitionTime;
    
    private boolean _started;
    
    /**
     * Last time stamp a slide was loaded
     */
    private long _lastSlideLoaded;
    
    /**
     * Timer to check if we need to switch slides
     */
    private Timer _timer;
    
    public SlideshowPanel(List<Slide> slides) {
        _slides = slides;
        _currentSlideIndex = -1;
    }
    
    public SlideshowPanel(String url) {
        HttpFetcher fetcher = null;
        
        try {
            fetcher = new HttpFetcher(new URI(url));
        } catch (URISyntaxException e) {
            return; // nothing happens
        }
        
        byte[] jsonBytes = fetcher.fetch();
        
        if (jsonBytes != null) {
            _slides = new JsonEngine().toObject(new String(jsonBytes), SlideList.class).slides;
            _currentSlideIndex = -1;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        
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
            g.drawImage(_currentImage, 0, 0, null);
        }
    }

    private void startAnimation() {
        
        if(_slides == null || _slides.size() == 0) {
            return;
        }
        
        _started = true;
        _lastSlideLoaded = 0;
        
        _timer = new Timer();
        _timer.schedule(new TimerTask() {
            @Override
            public void run() {
                tryMoveNext();
            }
        }, 0, 200); // Check every 200 milliseconds if we should trigger a transition
    }

    private void tryMoveNext() {
        
        if (_loadingNextImage) {
            return;
        }
        
        Slide slide = null;
        
        if (_currentSlideIndex == -1) {
            try {
                ImageCache.getInstance().getImage(new URL(_slides.get(0).imageSrc), new OnLoadedListener() {
                    public void onLoaded(URL url, BufferedImage image, boolean fromCache) {
                        _currentImage = image;
                        repaint();
                    }
                });
            } catch (MalformedURLException e) {
            }
            _currentSlideIndex = 0;
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
