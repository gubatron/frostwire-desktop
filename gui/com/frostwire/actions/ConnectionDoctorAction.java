package com.frostwire.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import com.frostwire.gnutella.connectiondoctor.ConnectionDoctor;
import com.limegroup.gnutella.connection.ConnectionLifecycleEvent;
import com.limegroup.gnutella.connection.ConnectionLifecycleListener;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import org.limewire.i18n.I18nMarker;
import com.limegroup.gnutella.gui.actions.AbstractAction;

import org.limewire.concurrent.ThreadExecutor;

/**
 * ConnectionDoctorAction, aka "Refresh Connections"
 * @author gubatron
 *
 */
public class ConnectionDoctorAction extends AbstractAction implements ConnectionLifecycleListener {

	/**
     * 
     */
    private static final long serialVersionUID = 5631616258511822844L;
    private final short MAX_USES = 3;
	private short timesUsed = 0;
	private ConnectionDoctor doctor;
	private boolean loadingHosts = false;

	private static ConnectionDoctorAction INSTANCE = null;
	
	//Gotta make it a singleton, so they don't have different attempt numbers.
	public static ConnectionDoctorAction getInstance() {
	    if (INSTANCE == null)
	        INSTANCE = new ConnectionDoctorAction();
	    
	    return INSTANCE;
	}
	
	private ConnectionDoctorAction() {
		super(I18n.tr("&Refresh Connections"));
		putValue(LONG_DESCRIPTION, I18nMarker.marktr("Fix Connection Problems. Get a fresh list of peers"));
        GuiCoreMediator.getConnectionManager().addEventListener(this);
		doctor = new ConnectionDoctor();
	}

	private void refreshMenuName() {
		putValue(Action.NAME, I18nMarker.marktr("&Refresh Connections ("+(MAX_USES-timesUsed)+" attempts left)"));
	}

	public void actionPerformed(ActionEvent e) {
		timesUsed++;
		putValue(Action.NAME, I18nMarker.marktr("&Refreshing Connections ("+(MAX_USES-timesUsed)+" attempts left)"));
		setEnabled(false);

		ThreadExecutor.startThread(new Runnable() {
			public void run() {
				loadingHosts=true;
				try {
					doctor.loadHosts();
				}
				catch (Exception e) {
					refreshMenuName();
					timesUsed--; //won't count
					setEnabled(true);
					loadingHosts=false;
					return;
				}
				loadingHosts=false;

				setEnabled(false);
			}
		},"CONNECTION_DOCTOR_FROM_MENU");
	}

	public void handleConnectionLifecycleEvent(ConnectionLifecycleEvent evt) {
		refreshMenuName();
		
		if (timesUsed == MAX_USES ||
			loadingHosts || 
			GuiCoreMediator.getConnectionServices().isConnecting() ||
			GuiCoreMediator.getConnectionManager().isConnecting()) {
			setEnabled(false);
			return;
		} 

		setEnabled(timesUsed < MAX_USES);
	} //handleConnectionLifecycleEvent
} //ConnectionDoctorAction
