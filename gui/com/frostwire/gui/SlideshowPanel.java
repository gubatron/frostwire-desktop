package com.frostwire.gui;

import java.awt.Graphics;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;


public class SlideshowPanel extends JPanel {
	
	private int _currentSlide;
	
	private List<Slide> _slides;

	/** Timer to check if we need to switch slides */
	private Timer _timer;
	
	/** Task to trigger a transition start */
	private TimerTask _timerTask;
	
	/** Last time stamp a slide was loaded */
	private long _lastSlideLoaded;

	private SlideTransition _transition;
	
	public SlideshowPanel(List<Slide> slides, SlideTransition transition) {
		_slides = slides;
		
		_transition = transition;
		
		_lastSlideLoaded = System.currentTimeMillis();
		
		_timerTask = new TimerTask() {
			
			@Override
			public void run() {
				Slide currentSlide = _slides.get(_currentSlide);
				
				//Time to start a transition timer.
				if (currentSlide.duration + _lastSlideLoaded > System.currentTimeMillis()) {
					_currentSlide = (_currentSlide+1) % _slides.size();
					Slide nextSlide = _slides.get(_currentSlide);
					_transition.start(currentSlide, nextSlide, SlideshowPanel.this);
				}
				
			}
		};
		
		/** Check every 1/2 second if we should trigger a transition */
		_timer = new Timer();
		_timer.schedule(null, 500);
	}
	
	@Override
	public void paint(Graphics g) {
		if (_transition.isTransitioning()) {
			_transition.paint(g);
		} else {
			//paint the current slide as the background
			
		}
			
		super.paint(g);
	}
}
