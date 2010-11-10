package com.frostwire.gnutella.gui.android;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import com.limegroup.gnutella.gui.I18n;

public class DeviceButton extends JToggleButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4608372510091566914L;
	
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
		String prefix = getImagePrefix();
		_image = createImageIcon(prefix + ".png");
		_imageAuthorized = createImageIcon(prefix + "_authorized.png");
		_imagePressed = createImageIcon(prefix + "_pressed.png");
		_imagePressedAuthorized = createImageIcon(prefix + "_pressed_authorized.png");
	}
	
	private ImageIcon createImageIcon(String name) {
		return new ImageIcon(getClass().getResource("images/" + name));
	}
	
	private String getImagePrefix() {
		if (DeviceID.isNexusOne(_device)) {
			return "nexus_one";
		} else {
			return "generic_device";
		}
	}
}
