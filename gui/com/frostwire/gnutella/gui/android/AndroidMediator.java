package com.frostwire.gnutella.gui.android;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

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
	private static AndroidMediator INSTANCE;
	
	/**
	 * The primary panel that contains all of the library elements.
	 */
	private JPanel MAIN_PANEL;
	
	private DeviceBar DEVICE_BAR;
	
	private DesktopExplorer DESKTOP_EXPLORER;
	
	private DeviceExplorer DEVICE_EXPLORER;
	
	private ProgressPanel PROGRESS_PANEL;
	
	private TaskProcessor TASK_PROCESSOR;

	private JPanel EXPLORERS_CONTAINER;

	private JSplitPane SPLIT_PANE;
    
	/**
	 * @return the <tt>AndroidMediator</tt> instance
	 */
	public static AndroidMediator instance() { 
		if (INSTANCE == null) {
			 INSTANCE = new AndroidMediator();
		}
		return INSTANCE; 
	}
	
	private AndroidMediator() {
    	GUIMediator.setSplashScreenString(I18n.tr("Loading phones and tablets..."));
    	ThemeMediator.addThemeObserver(this);
    	
    	TASK_PROCESSOR = new TaskProcessor();
    	TASK_PROCESSOR.start();
    	
    	setupUI();
    }

	protected void setupUI() {
		DEVICE_BAR = new DeviceBar();
		DESKTOP_EXPLORER = new DesktopExplorer();
		DEVICE_EXPLORER = new DeviceExplorer();
		PROGRESS_PANEL = new ProgressPanel();

		MAIN_PANEL = new JPanel(new BorderLayout());
		MAIN_PANEL.add(DEVICE_BAR, BorderLayout.PAGE_START);
		
		EXPLORERS_CONTAINER = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.fill=GridBagConstraints.BOTH;
		c.weightx = 99;
		c.weighty=1.0;
		c.anchor=GridBagConstraints.NORTHWEST;
		EXPLORERS_CONTAINER.add(DESKTOP_EXPLORER, c);
		
		//up and down
		SPLIT_PANE = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		SPLIT_PANE.setAutoscrolls(true);
		SPLIT_PANE.setDividerSize(4);
		SPLIT_PANE.add(DEVICE_EXPLORER);
		SPLIT_PANE.add(PROGRESS_PANEL);
		
		c = new GridBagConstraints();
		c.gridx=GridBagConstraints.RELATIVE;
		c.gridy=0;
		c.weightx=0;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.fill=GridBagConstraints.VERTICAL;
		c.anchor=GridBagConstraints.EAST;
		c.weightx = 0.1;
		c.weighty=1.0;

		EXPLORERS_CONTAINER.add(SPLIT_PANE,c);

		MAIN_PANEL.add(EXPLORERS_CONTAINER,BorderLayout.CENTER);
		SPLIT_PANE.setDividerLocation(300);
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
	
	public static void addTask(Task task) {
		INSTANCE.PROGRESS_PANEL.addTask(task);
		INSTANCE.TASK_PROCESSOR.addTask(task);
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
}
