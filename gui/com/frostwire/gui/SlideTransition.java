package com.frostwire.gui;

import java.awt.Graphics;

public interface SlideTransition {
	
	/**
	 * Triggers the transition effect.
	 * Takes in consideration the panel object so that we can invoke the repaint() method on it
	 * as we progress.
	 * 
	 * Initially I'm thinking these repaints would be invoked by an Internal TimerTask implemented in the
	 * SlideTransition logic. If we want to achieve a look 30fps, we should be able to invoke those repaints from the timer every 33ms.
	 * 
	 * @param lastSlide
	 * @param nextSlide
	 * @param panel
	 */
	public void start(Slide lastSlide, Slide nextSlide, SlideshowPanel panel);
	
	/** Invoke this to stop a transition at any time */
	public void end();

	/**
	 * Returns true while transitioning.
	 * @return
	 */
	public boolean isTransitioning();
	
	/**
	 * This method should transform the Graphics object every time it's invoked until the transition is over.
	 * @param g
	 */
	public void paint(Graphics g);
}
