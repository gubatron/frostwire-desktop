package com.limegroup.gnutella.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import com.limegroup.gnutella.settings.ApplicationSettings;

/**
 * This class handles the timer that refreshes the gui after every 
 * specified interval.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
public final class RefreshTimer {

	/** 
	 * The interval between statistics updates in milliseconds. 
	 */
	private final int UPDATE_TIME = 1000;

	/** 
	 * The interval between statistics updates in seconds for convenience
	 * and added efficiency.. 
	 */
	private final int UPDATE_TIME_IN_SECONDS = UPDATE_TIME/1000;	

	/**
	 * variable for timer that updates the gui.
	 */
	private Timer _timer;
	  
	/**
	 * Creates the timer and the ActionListener associated with it.
	 */
	public RefreshTimer() {
		ActionListener refreshGUI = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                refreshGUI();
			}
		};
		
		_timer = new Timer(UPDATE_TIME, refreshGUI);
	}

	/**
	 * Starts the timer that updates the gui.
	 */
	public void startTimer() {
		_timer.start();
	}

	/** 
	 * Refreshes all of the gui elements.
	 */
	private void refreshGUI() {
		GUIMediator.instance().refreshGUI();
        
        int totalUptime = ApplicationSettings.TOTAL_UPTIME.getValue() + UPDATE_TIME_IN_SECONDS;
        ApplicationSettings.TOTAL_UPTIME.setValue(totalUptime);
        ApplicationSettings.AVERAGE_UPTIME.setValue(totalUptime/ApplicationSettings.SESSIONS.getValue());
        
	}
}
