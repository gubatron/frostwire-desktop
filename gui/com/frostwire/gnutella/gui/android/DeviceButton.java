package com.frostwire.gnutella.gui.android;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
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
	
	private static final String IMAGES_URL = "http://static1.frostwire.com/images/devices/";
	
	private static final Color FILL_COLOR = new Color(0x8DB2ED);
    private static final Color INNER_BORDER_COLOR = new Color(0x98BFFF);
    private static final Color OUTER_BORDER_COLOR = new Color(0x516688);
	
	private ImageIcon _image;
	private ImageIcon _imageAuthorized;
	private ImageIcon _imagePressed;
	private ImageIcon _imagePressedAuthorized;
	
	private Device _device;
	
	public DeviceButton(Device device) {
		_device = device;
		
		loadImage(getImageName());
		setupUI();
	}

	public Device getDevice() {
		return _device;
	}
	
	public void refresh() {
		setTooltip();
		setImage();
	}
	
	protected void setupUI() {
		////////// visible effect trick
		setBorder(null);
		//setBackground(null);
		setFocusable(false);
		setFocusPainted(false);
		setContentAreaFilled(false);
		////////////////////////////////
		setMinimumSize(new Dimension(100, 140));
		setTooltip();
		setImage();
		setText(" " + _device.getName() + " ");
		setHorizontalTextPosition(SwingConstants.CENTER);
		setVerticalTextPosition(SwingConstants.BOTTOM);
	}
	
	@Override
    protected void paintComponent(Graphics g) {
        
        int w = getWidth();
        int h = getHeight();
        
        if (isSelected()) {
            g.setColor(FILL_COLOR);
            g.fillRoundRect(0, 0, w, h, 5, 5);
            g.setColor(INNER_BORDER_COLOR);
            g.drawRoundRect(1, 1, w - 3, h - 3, 5, 5);
            g.setColor(OUTER_BORDER_COLOR);
            g.drawRoundRect(0, 0, w - 1, h - 1, 5, 5);
        }
        
        super.paintComponent(g);
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
	
	private BufferedImage loadDefaultImage() {
	    return new UITool().loadImage("generic_device");
	}
	
    private void loadImage(String name) {
        try {
            URL url = new URL(IMAGES_URL + _device.getFinger().deviceManufacturer.toLowerCase() + "/" + name);
            
            BufferedImage image = ImageCache.getInstance().getImage(url, new OnLoadedListener() {
                public void onLoaded(URL url, BufferedImage image, boolean fromCache) {
                    buildImages(image);
                    setImage();
                }
            });
            
            //In the meantime show the default image
            if (image == null) {
                image = loadDefaultImage();
                buildImages(image);
                setImage();
            }
        } catch (MalformedURLException e) {
        }        
	}
    
    private void buildImages(BufferedImage image) {
        _image = new ImageIcon(buildImage(image, false, false));
        _imageAuthorized = new ImageIcon(buildImage(image, false, true));
        _imagePressed = new ImageIcon(buildImage(image, true, false));
        _imagePressedAuthorized = new ImageIcon(buildImage(image, true, true));
    }
    
    private BufferedImage buildImage(BufferedImage image, boolean pressed, boolean authorized) {
        int w = image.getWidth(); 
        int h = image.getHeight();
        BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g = newImage.createGraphics();
                
        g.drawImage(image, 0, 0, null);
        
        if (pressed) {
            g.setColor(Color.BLUE);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 0.7f));
            g.fillRect(0, 0, w, h);
        }
        
        if (authorized) {
            BufferedImage phoneAuthorized = new UITool().loadImage("phone_authorized");
            g.drawImage(phoneAuthorized, 60, 10, null);
        }
        
        g.dispose();
        
        return newImage;
    }
	
	private String getImageName() {
	    Finger finger = _device.getFinger();
	    String prefix = finger.deviceManufacturer + "_" + finger.deviceName + "_" + finger.deviceProduct + "_" + finger.deviceModel;
	    prefix = prefix.replace('.', '_').replace(' ', '_');
	    
	    return prefix.toLowerCase() + ".png";
	}
}
