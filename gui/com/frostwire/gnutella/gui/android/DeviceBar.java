package com.frostwire.gnutella.gui.android;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.frostwire.gnutella.gui.android.Device.OnActionFailedListener;
import com.limegroup.gnutella.gui.I18n;

public class DeviceBar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6886611714952957959L;
	
	private Map<Device, DeviceButton> _buttons;
	private ButtonGroup _buttonGroup;
	private MyOnActionFailedListener _deviceListener;
	private MyMouseAdapter _mouseAdapter;
	
	private Device _selectedDevice;
	
	private BufferedImage _imageLeftBorder;
	private BufferedImage _imageRightBorder;
	private BufferedImage _imageBackground;
	private BufferedImage _imageNoDevice;
	
	public DeviceBar() {
	    _buttons = new HashMap<Device, DeviceButton>();
	    _buttonGroup = new ButtonGroup();
        _deviceListener = new MyOnActionFailedListener();
        _mouseAdapter = new MyMouseAdapter();
        
        buildImages();
        
		setupUI();
	}

	public void handleNewDevice(Device device) {
	    
	    handleDeviceStale(device);
	    
		DeviceButton button = new DeviceButton(device);
		button.addMouseListener(_mouseAdapter);
		_buttons.put(device, button);
		_buttonGroup.add(button);
		add(button);
		repaint();
		
		device.setOnActionFailedListener(_deviceListener);
	}

	public void handleDeviceAlive(Device device) {
		DeviceButton button = _buttons.get(device);
		
		if (button == null) {
		    return;
		}
				
		button.refresh();
		revalidate();
		if (button.getDevice().equals(_selectedDevice)) {
		    if (_selectedDevice != null) {
		        AndroidMediator.instance().getDeviceExplorer().refreshHeader();
		    }
		}
	}
	
	public void handleDeviceStale(final Device device) {
		synchronized (_buttons) {
			DeviceButton button = _buttons.remove(device);
			
			if (button == null) {
			    return;
			}

			button.setVisible(false);
			_buttonGroup.remove(button);
			remove(button);
			revalidate();
			
			if (_buttons.size() == 0 || device.equals(_selectedDevice)) {
			    repaint();
				AndroidMediator.instance().getDeviceExplorer().setPanelDevice(false);
			}
		}
	}

	public Device getSelectedDevice() {
		return _selectedDevice;
	}
	
	protected void setupUI() {
		setLayout(new FlowLayout());
		setPreferredSize(new Dimension(300, 140));
	}
	
	@Override
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    
	    int n = getWidth() / _imageBackground.getWidth() + 1;
	    for (int i = 0; i < n; i++) {
	        g.drawImage(_imageBackground, i * _imageBackground.getWidth(), 0, null);
	    }
	    
	    g.drawImage(_imageLeftBorder, 0, 0, null);
	    g.drawImage(_imageRightBorder, getWidth() - _imageRightBorder.getWidth(), 0, null);
	    
	    if (_buttons.size() == 0) {
	        int x = (getWidth() - _imageNoDevice.getWidth()) / 2;
	        int y = (getHeight() - _imageNoDevice.getHeight())  / 2 - 5;
	        
	        if (x >= 0 && y >= 0) {
	            g.drawImage(_imageNoDevice, x, y, null);
	        }
	    }
	}
	
	private void buildImages() {
        BufferedImage image = new UITool().loadImage("device_bar_background");

        _imageLeftBorder = image.getSubimage(0, 0, 15, image.getHeight());

        _imageRightBorder = new BufferedImage(_imageLeftBorder.getWidth(), _imageLeftBorder.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g = null;

        try {

            g = _imageRightBorder.createGraphics();
            g.drawImage(_imageLeftBorder, 0, 0, null);

            AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-_imageLeftBorder.getWidth(), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            _imageRightBorder = op.filter(_imageRightBorder, null);

        } finally {
            if (g != null) {
                g.dispose();
            }
        }
        
        _imageBackground = image.getSubimage(15, 0, image.getWidth() - 15, image.getHeight());
        
        _imageNoDevice = buildTextImage(I18n.tr("No devices nearby"));
	}
	
	private BufferedImage buildTextImage(String text) {
        // TODO: Use parameters for colors.
        
        Font font = getFont();
        font = new Font(font.getFamily(), Font.BOLD, font.getSize() + 20);

        Graphics2D graphicsDummy = null;
        Graphics2D graphics1 = null;
        Graphics2D graphics2 = null;
        
        BufferedImage image = null;

        try {

            BufferedImage imageDummy = new BufferedImage(300, 120, BufferedImage.TYPE_INT_ARGB);
            graphicsDummy = imageDummy.createGraphics();
            graphicsDummy.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphicsDummy.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            FontMetrics metrics = graphicsDummy.getFontMetrics(font);
            int w = metrics.stringWidth(text) + 20;
            int h = metrics.getHeight();

            BufferedImage image1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            graphics1 = image1.createGraphics();
            graphics1.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics1.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            //draw "shadow" text: to be blurred next
            TextLayout textLayout = new TextLayout(text, font, graphics1.getFontRenderContext());
            graphics1.setPaint(Color.BLACK);
            textLayout.draw(graphics1, 11, 34);
            graphics1.dispose();

            //blur the shadow: result is sorted in image2
            float ninth = 1.0f / 9.0f;
            float[] kernel = { ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth };
            ConvolveOp op = new ConvolveOp(new Kernel(3, 3, kernel), ConvolveOp.EDGE_NO_OP, null);
            BufferedImage image2 = op.filter(image1, null);

            //write "original" text on top of shadow
            graphics2 = image2.createGraphics();
            graphics2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics2.setPaint(Color.WHITE);
            textLayout.draw(graphics2, 10, 33);
            
            image = image2;
            
        } finally {
            if (graphicsDummy != null) {
                graphicsDummy.dispose();
            }
            if (graphicsDummy != null) {
                graphics1.dispose();
            }
            if (graphicsDummy != null) {
                graphics2.dispose();
            }
        }

        return image;
    }
	
	private final class MyMouseAdapter extends MouseAdapter {
		public void mouseReleased(MouseEvent e) {
			DeviceButton button = (DeviceButton) e.getComponent();
			_selectedDevice = button.getDevice();
			AndroidMediator.instance().getDeviceExplorer().setDevice(_selectedDevice);
		}
	}
	
	private final class MyOnActionFailedListener implements OnActionFailedListener {
		public void onActionFailed(Device device, int action, Exception e) {
			JComponent dialogParent = AndroidMediator.instance().getComponent();
			if (action == Device.ACTION_UPLOAD) {
				JOptionPane.showMessageDialog(dialogParent, I18n.tr("You are not authorized to upload files to this device"), I18n.tr("From ") + device.getName(), JOptionPane.INFORMATION_MESSAGE);
			} else {
				handleDeviceStale(device);
				JOptionPane.showMessageDialog(dialogParent, I18n.tr("Error connecting to device: ") + (e != null ? e.getMessage() : I18n.tr("undefined")), I18n.tr("From ") + device.getName(), JOptionPane.OK_OPTION);
			}
		}
	}
}
