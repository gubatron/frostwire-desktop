package com.limegroup.gnutella.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;

/**
 * This is a skinnable JSlider. It uses a custom UI that takes a series of
 * images and paints the jSlider. The track is made up of three images, a left,
 * center and right. In most cases, the center will be a single pixel wide or
 * tall depending on the orientation of the slider. This is to conserve space
 * and speed up processing. The center image will be stretched by the UI to
 * paint the entire track in between the left and right images of the track.
 * 
 * The thumb has two images, pressed and unpressed. If pressed is null, the
 * default is unpressed. If any image besides pressed is null, the default
 * component will be painted instead.
 */
public class MediaSlider extends JSlider implements ThemeObserver, ChangeListener {

    private static final long serialVersionUID = 6544159559093274923L;

	private Image backgroundImage;

    public MediaSlider(String backgroundImageName) {
        this.setFocusable(false);
        ThemeMediator.addThemeObserver(this);

        backgroundImage = GUIMediator.getThemeImage(backgroundImageName).getImage();
        
        this.addChangeListener(this);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	if (backgroundImage!=null) {
    		g.drawImage(backgroundImage, (getWidth()-backgroundImage.getWidth(null))/2, 0, null);
    	}
    	
    }

    /**
     * This only allows UIs that are subclassed from our own MediaSliderUI
     */
//    @Override
//    public void setUI(SliderUI sliderUI) {
//        if (sliderUI instanceof MediaSliderUI)
//            super.setUI(sliderUI);
//    }



    /**
     * When the theme changes, load the new images and repaint the buffered
     * track image
     */
    public void updateTheme() {
    }

    /**
     * Converts the image stored in an ImageIcon into a BufferedImage. If the
	 * image is null or has not been completely loaded or loaded with errors
	 * returns null;
     */
    public static BufferedImage convertIconToImage(ImageIcon icon) {

		// make sure we have a valid image and it is loaded already
        if( icon == null || icon.getImageLoadStatus() != MediaTracker.COMPLETE )
            return null;
        BufferedImage image = new BufferedImage(icon.getIconWidth(),
                icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D bufImageGraphics = image.createGraphics();
        bufImageGraphics.drawImage(icon.getImage(), 0, 0, null);

        bufImageGraphics.dispose();

        return image;
    }

    public void stateChanged(ChangeEvent e) {
        this.setToolTipText(Integer.toString(getValue()));
    }
}