package com.frostwire.gnutella.gui.android;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
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
		revalidate();
		
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
	}
	
	private void buildImages() {
        BufferedImage image = new UITool().loadImage("device_bar_background");

        _imageLeftBorder = image.getSubimage(0, 0, 15, image.getHeight());

        _imageRightBorder = new BufferedImage(_imageLeftBorder.getWidth(), _imageLeftBorder.getHeight(), _imageLeftBorder.getType());

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
