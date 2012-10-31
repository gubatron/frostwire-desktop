package com.frostwire.gui.mplayer;


public class AlphaAnimationThread extends Thread {
    
	private static final int TARGET_ALPHA = 90 * 255 / 100;
    private static final int ALPHA_STEP = 20;

    private boolean disposed = false;
    private Object animationStart = new Object();
    private boolean isHiding;
    private boolean isShowing;
    private int currentAlpha = TARGET_ALPHA;
    private boolean stopAlphaThread = false;
    
    private AlphaTarget target;
    
    public AlphaAnimationThread ( AlphaTarget target ) {
    	this.target = target;
    }
	
    public void setDisposed() {
    	disposed = true;
    }
    
    public void animateToTransparent() {
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

    public void animateToOpaque() {
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
    
    private float currentAlphaValue( ) {
    	return (currentAlpha * 1f) / TARGET_ALPHA;
    }
    
	public void run() {
        while (!stopAlphaThread && !disposed) {
            if (isHiding) {
                if (currentAlpha > 0) {
                    if (currentAlpha >= ALPHA_STEP) {
                        currentAlpha -= ALPHA_STEP;
                    } else {
                        currentAlpha = 0;
                    }

                    target.setAlpha( currentAlphaValue() );
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
                    target.setAlpha( currentAlphaValue() );
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
    }
}
