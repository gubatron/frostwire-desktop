package com.frostwire.gnutella.gui;

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
import com.frostwire.json.JsonEngine;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.settings.ApplicationSettings;

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
    private boolean _started;
    
    /**
     * Last time stamp a slide was loaded
     */
    private long _lastTimeSlideLoaded;
    
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
        	e.printStackTrace();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    	g.setColor(getBackground());
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
                            GUIMediator.instance().openTorrentURI(new URI(slide.torrent), null);
                        } else if (slide.torrent.toLowerCase().startsWith("magnet:?")) {
                            GUIMediator.instance().openTorrentMagnet(slide.torrent);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void startAnimation() {
        
        if(_slides == null || _slides.size() == 0) {
            return;
        }
        
        _started = true;
        _lastTimeSlideLoaded = 0;
        
        if (_slides.size() == 1) {
            try {
                ImageCache.getInstance().getImage(new URL(_slides.get(0).imageSrc), new OnLoadedListener() {
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
            _loadingNextImage = true;
            try {
                ImageCache.getInstance().getImage(new URL(_slides.get(_currentSlideIndex).imageSrc), new OnLoadedListener() {
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
            if (slide.duration + _lastTimeSlideLoaded + _transitionTime < System.currentTimeMillis()) {
                _currentSlideIndex = (_currentSlideIndex + 1) % _slides.size();
                slide = _slides.get(_currentSlideIndex);
            } else {
                slide = null;
            }
        }
        
        if (slide != null) {
            _loadingNextImage = true;
            try {
                ImageCache.getInstance().getImage(new URL(slide.imageSrc), new OnLoadedListener() {
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
            BufferedImage bImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
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
			if (isMessageEligibleForMyLang(slide.language) &&
				isMessageEligibleForMyOs(slide.os)) {
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
}
