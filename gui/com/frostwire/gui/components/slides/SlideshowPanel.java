package com.frostwire.gui.components.slides;

import javax.swing.JPanel;

public interface SlideshowPanel {
    
    public void setListener(SlideshowListener listener);
    
    public int getCurrentSlideIndex();
    
    public void switchToSlide(int slideIndex);
    
    public int getNumSlides();
    
    public void setVisible(boolean visible);
    
    public void setupContainerAndControls(JPanel container, boolean useControls);
    
    public interface SlideshowListener {
        public void onSlideChanged();
    }

    
}
