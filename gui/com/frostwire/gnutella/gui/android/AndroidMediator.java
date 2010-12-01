package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

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
	
	private ProgressPanel PROGRESS_PANEL;
	
	private TaskProcessor ACTIVITY_PROCESSOR;

	private JPanel EXPLORERS_CONTAINER;

	private JSplitPane SPLIT_PANE;
    
	/**
	 * @return the <tt>AndroidMediator</tt> instance
	 */
	public static AndroidMediator instance() { return INSTANCE; }
	
	private AndroidMediator() {
    	GUIMediator.setSplashScreenString(I18n.tr("Loading phone and tablets..."));
    	ThemeMediator.addThemeObserver(this);
    	
    	ACTIVITY_PROCESSOR = new TaskProcessor();
    	ACTIVITY_PROCESSOR.start();
    	
    	setupUIElements();
    }

	private void setupUIElements() {
		
		MAIN_PANEL = new JPanel(new BorderLayout());
		DESKTOP_EXPLORER = new DesktopExplorer();
		DEVICE_EXPLORER = new DeviceExplorer();
		DEVICE_BAR = new DeviceBar();
		PROGRESS_PANEL = new ProgressPanel();
		
		MAIN_PANEL.add(DEVICE_BAR, BorderLayout.PAGE_START);
		
		//up and down
		SPLIT_PANE = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		SPLIT_PANE.setAutoscrolls(true);
		SPLIT_PANE.setDividerSize(4);

		
		EXPLORERS_CONTAINER = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.fill=GridBagConstraints.BOTH;
		c.weightx = 0.9;
		c.weighty=1.0;
		c.anchor=GridBagConstraints.NORTHWEST;
		
		DESKTOP_EXPLORER.setMinimumSize(new Dimension(700,700));

		EXPLORERS_CONTAINER.add(DESKTOP_EXPLORER, c);
		
		c = new GridBagConstraints();
		c.gridx=GridBagConstraints.RELATIVE;
		c.gridy=0;
		c.weightx=0;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.fill=GridBagConstraints.VERTICAL;
		c.anchor=GridBagConstraints.EAST;
		c.weightx = 0.1;
		c.weighty=1.0;
		EXPLORERS_CONTAINER.add(DEVICE_EXPLORER,c);

		SPLIT_PANE.add(EXPLORERS_CONTAINER);
		SPLIT_PANE.add(PROGRESS_PANEL);

		MAIN_PANEL.add(SPLIT_PANE,BorderLayout.CENTER);
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
	
	public static void addActivity(Task activity) {
		INSTANCE.PROGRESS_PANEL.addActivity(activity);
		INSTANCE.ACTIVITY_PROCESSOR.addTask(activity);
	}
	
	public DeviceBar getDeviceBar() {
		return DEVICE_BAR;
	}
	
	public DesktopExplorer getDesktopExplorer() {
		return DESKTOP_EXPLORER;
	}
	
	public DeviceExplorer getDeviceExplorer() {
		return DEVICE_EXPLORER;
	}
	
	public ProgressPanel getProgressPanel() {
		return PROGRESS_PANEL;
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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();

		frame.setVisible(true);
	}
}
