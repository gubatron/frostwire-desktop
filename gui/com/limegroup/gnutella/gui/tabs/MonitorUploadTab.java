package com.limegroup.gnutella.gui.tabs;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

// for pop-up menu
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.Action;
import org.limewire.setting.BooleanSetting;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.MonitorView;
import com.limegroup.gnutella.gui.tables.ComponentMediator;
import com.limegroup.gnutella.gui.util.DividerLocationSettingUpdater;
import com.limegroup.gnutella.settings.ApplicationSettings;
import com.limegroup.gnutella.settings.UISettings;

/**
 * This class contains all elements of the tab for the monitor and upload
 * displays.
 */
public final class MonitorUploadTab extends AbstractTab {

	/**
	 * Constant for the <tt>JSplitPane</tt> instance separating the 
	 * monitor from the uploads.
	 */
	private final JSplitPane SPLIT_PANE;

	/**
	 * Constructs the tab for monitors and uploads.
	 *
	 * @param MONITOR_VIEW the <tt>MonitorView</tt> instance containing
	 *  all component for the monitor display and handling
	 * @param UPLOAD_MEDIATOR the <tt>UploadMediator</tt> instance containing
	 *  all component for the monitor display and handling 
	 */
	public MonitorUploadTab(final MonitorView MONITOR_VIEW, 
							final ComponentMediator UPLOAD_MEDIATOR) {
		super(I18n.tr("Monitor"),
		        I18n.tr("View Searches and Uploads"), "monitor_tab");
		SPLIT_PANE = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
									MONITOR_VIEW, 
									UPLOAD_MEDIATOR.getComponent());
		// FTA: LISTENER RIGHT-CLICK POPUP MENU DISABLED FOR RELEASE 4.18.5
		//SPLIT_PANE.addMouseListener(TESTING_MENU_LISTENER);
        SPLIT_PANE.setContinuousLayout(true);
		SPLIT_PANE.setOneTouchExpandable(true);
		DividerLocationSettingUpdater.install(SPLIT_PANE,
				UISettings.UI_MONITOR_UPLOAD_TAB_DIVIDER_LOCATION);
		//MONITOR_VIEW.addMouseListener(TESTING_MENU_LISTENER);
		
	}

	public void storeState(boolean visible) {
        ApplicationSettings.MONITOR_VIEW_ENABLED.setValue(visible);
	}

	public JComponent getComponent() {
		return SPLIT_PANE;
	}
	
	 /**
     * FTA: pop-up TEST feature while discovering how delays when changing tabs are happening.
     */
	private final MouseAdapter TESTING_MENU_LISTENER = new MouseAdapter() {
		public void mousePressed(MouseEvent me) { processMouseEvent(me); }
		public void mouseReleased(MouseEvent me) { processMouseEvent(me); }
		public void mouseClicked(MouseEvent me) { processMouseEvent(me); }
		
		public void processMouseEvent(MouseEvent me) {
			if (me.isPopupTrigger()) {
                JPopupMenu jpm = new JPopupMenu();
                
                // TODO: THIS CODE WILL BE REMOVED AFTER TESTS REGARDING THE DELAY WHEN CHANGING TABS
                //  add 'Show Connection Quality' menu item
                JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem(new ShowTestFTAction());
                //jcbmi.setState(StatusBarSettings.CONNECTION_QUALITY_DISPLAY_ENABLED.getValue());
                jcbmi.setState(true);
                jpm.add(jcbmi);

                              
                jpm.addSeparator();
                
                
                jpm.pack();
                jpm.show(me.getComponent(), me.getX(), me.getY());
            }
		}
	};

	/**
	 * Action for the 'Test action' menu item. 
	 */
	private class ShowTestFTAction extends AbstractAction {
		
		public ShowTestFTAction() {
			putValue(Action.NAME, I18n.tr
					("Test action!"));
		}
		
		public void actionPerformed(ActionEvent e) {
			//StatusBarSettings.CONNECTION_QUALITY_DISPLAY_ENABLED.invert();
			//refresh();
			System.out.print("test action!!");
		}
	}
	
	
	//////////
}
