package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;

public class AndroidMediator implements ThemeObserver {

	/**
	 * Singleton instance of this class.
	 */
	private static final AndroidMediator INSTANCE = new AndroidMediator();
	
	/**
	 * The primary panel that contains all of the library elements.
	 */
	private JPanel MAIN_PANEL;
	
	private DeviceBar DEVICE_BAR;
	
	private DesktopExplorer DESKTOP_EXPLORER;
	
	private DeviceExplorer DEVICE_EXPLORER;
	
	public static LocalFile SELECTED_DESKTOP_FOLDER;
    
	/**
	 * @return the <tt>AndroidMediator</tt> instance
	 */
	public static AndroidMediator instance() { return INSTANCE; }
	
	private AndroidMediator() {
    	GUIMediator.setSplashScreenString(I18n.tr("Loading phone and tablets..."));
    	ThemeMediator.addThemeObserver(this);
    	
    	setupUIElements();
    }

	private void setupUIElements() {
		
		MAIN_PANEL = new JPanel(new BorderLayout());
		DESKTOP_EXPLORER = new DesktopExplorer();
		DEVICE_EXPLORER = new DeviceExplorer();
		DEVICE_BAR = new DeviceBar(DEVICE_EXPLORER);
		
		MAIN_PANEL.add(DEVICE_BAR, BorderLayout.PAGE_START);
		
		JPanel center = new JPanel(new GridBagLayout());
		
		GridBagConstraints c;
		
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.7;
		c.weighty = 10;
		center.add(DESKTOP_EXPLORER, c);
		
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.3;
		c.weighty = 10;
		center.add(DEVICE_EXPLORER, c);
		
		MAIN_PANEL.add(center, BorderLayout.CENTER);
	}

	@Override
	public void updateTheme() {
	}
	
	/**
	 * Returns the <tt>JComponent</tt> that contains all of the elements of
	 * the Chat.
	 *
	 * @return the <tt>JComponent</tt> that contains all of the elements of
	 * the Chat.
	 */
	public JComponent getComponent() {
		return MAIN_PANEL;
	}

	public static void handleNewDevice(Device device) {
		INSTANCE.DEVICE_BAR.handleNewDevice(device);
	}
	
	public static void handleDeviceAlive(Device device) {
		INSTANCE.DEVICE_BAR.handleDeviceAlive(device);
	}

	public static void handleDeviceStale(Device device) {
		INSTANCE.DEVICE_BAR.handleDeviceStale(device);
	}
	
	public static void startAndroidClerk() {
		PeerDiscoveryClerk clerk = new PeerDiscoveryClerk();
		clerk.start();
	}
	
	public static void main(String[] args) {
		AndroidMediator.startAndroidClerk();
		AndroidMediator mediator = AndroidMediator.instance();

		JFrame frame = new JFrame();
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize((int) (screenSize.width * 0.6), (int) (screenSize.height * 0.6));
		frame.getContentPane().add(mediator.getComponent());
		frame.pack();

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(1);
			}
		});

		frame.setVisible(true);
	}
}
