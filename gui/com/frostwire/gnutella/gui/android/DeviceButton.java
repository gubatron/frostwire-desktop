package com.frostwire.gnutella.gui.android;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import com.frostwire.ImageCache;
import com.frostwire.ImageCache.OnLoadedListener;
import com.limegroup.gnutella.gui.I18n;

public class DeviceButton extends JRadioButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4608372510091566914L;
	
	private static final String IMAGES_URL = "http://192.168.1.107/~atorres/";
	
	private ImageIcon _image;
	private ImageIcon _imageAuthorized;
	private ImageIcon _imagePressed;
	private ImageIcon _imagePressedAuthorized;
	
	private Device _device;
	
	public DeviceButton(Device device) {
		_device = device;
		
		loadImages();
		setupUI();
	}

	public Device getDevice() {
		return _device;
	}
	
	public void refresh() {
		setTooltip();
		setImage();
		revalidate();
	}
	
	private void setupUI() {
		////////// visible effect trick
		setBorder(null);
		setBackground(null);
		setFocusable(false);
		setFocusPainted(false);
		setContentAreaFilled(false);
		////////////////////////////////
		setPreferredSize(new Dimension(100, 130));
		setTooltip();
		setImage();
		setText(_device.getName());
		setHorizontalTextPosition(SwingConstants.CENTER);
		setVerticalTextPosition(SwingConstants.BOTTOM);
	}
	
	private void setTooltip() {
		setToolTipText(I18n.tr("Connected") + (_device.isTokenAuthorized() ? I18n.tr(" and authorized") : ""));
	}
	
	private void setImage() {
		if (_device.isTokenAuthorized()) {
			setIcon(_imageAuthorized);
			setPressedIcon(_imagePressedAuthorized);
		} else {
			setIcon(_image);
			setPressedIcon(_imagePressed);
		}
	}
	
	private void loadImages() {
		BufferedImage defaultImage = loadImage(getImagePrefix(true) + ".png", null);
		BufferedImage image = loadImage(getImagePrefix(false) + ".png", defaultImage);
		buildImages(image);
	}

    private BufferedImage loadImage(String name, BufferedImage defaultImage) {
	    URL url = null;
        try {
            url = new URL(IMAGES_URL + name);
        } catch (MalformedURLException e) {
        }
        
	    BufferedImage image = ImageCache.getInstance().getImage(url, new OnLoadedListener() {
            public void onLoaded(URL url, BufferedImage image) {
                buildImages(image);
                setImage();
            }
        });
	    
	    url = getClass().getResource("images/" + name);
	    if (image == null && url != null) {
	        image = ImageCache.getInstance().getImage(url, null);
	    }
	    
	    if (image == null) {
	        image = defaultImage;
	    }
	    
		return image;
	}
    
    private void buildImages(BufferedImage image) {
        _image = new ImageIcon(buildImage(image, false, false));
        _imageAuthorized = new ImageIcon(buildImage(image, false, true));
        _imagePressed = new ImageIcon(buildImage(image, true, false));
        _imagePressedAuthorized = new ImageIcon(buildImage(image, true, true));
    }
    
    private BufferedImage buildImage(BufferedImage image, boolean pressed, boolean authorized) {
        int width = image.getWidth(); 
        int height = image.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2 = newImage.createGraphics();
        
        g2.drawImage(image, 0, 0, null);
        
        if (pressed) {
            g2.setColor(Color.BLUE);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 0.7f));
            g2.fillRect(0, 0, width, height);
        }
        
        if (authorized) {
            g2.setColor(Color.GREEN);
            g2.drawOval(0, 0, 40, 40);
        }
        
        g2.dispose();
        
        return newImage;
    }
	
	private String getImagePrefix(boolean defaultPrefix) {
	    
	    if (defaultPrefix) {
	        return "generic_device";
	    }
	    
	    Finger finger = _device.getFinger();
	    String prefix = finger.deviceManufacturer + "/" + finger.deviceManufacturer + "_" + finger.deviceProduct + "_" + finger.deviceModel;
	    prefix = prefix.replace('.', '_').replace(' ', '_');
	    
	    return prefix.toLowerCase();
	}
}
