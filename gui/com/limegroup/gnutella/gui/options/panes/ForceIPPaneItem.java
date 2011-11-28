package com.limegroup.gnutella.gui.options.panes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.limewire.util.NetworkUtils;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.GUIUtils.SizePolicy;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.SizedWholeNumberField;
import com.limegroup.gnutella.gui.WholeNumberField;
import com.limegroup.gnutella.settings.ConnectionSettings;

/**
 * This class defines the panel in the options window that allows the user
 * to force their ip address to the specified value.
 */
public final class ForceIPPaneItem extends AbstractPaneItem {

    public final static String TITLE = I18n.tr("Router Configuration");
    
    public final static String LABEL = I18n.tr("FrostWire can configure itself to work from behind a firewall or router. Using Universal Plug \'n Play (UPnP) and other NAT traversal techniques FrostWire can automatically configure your router or firewall for optimal performance. If your router does not support UPnP, FrostWire can be set to advertise an external port manually. (You may also have to configure your router if you choose manual configuration, but FrostWire will try its best so you don't have to.)");

	/**
	 * Constant <tt>WholeNumberField</tt> instance that holds the port 
	 * to force to.
	 */
	private final WholeNumberField TCP_PORT_FIELD = new SizedWholeNumberField();
	
	private final WholeNumberField UDP_PORT_FIELD = new SizedWholeNumberField();
	
    /**
     * Constant handle to the check box that enables or disables this feature.
     */
    private final ButtonGroup BUTTONS = new ButtonGroup();
    private final JRadioButton RANDOM_PORT = new JRadioButton(I18n.tr("Use random port (Recommended)"));
    private final JRadioButton MANUAL_PORT = new JRadioButton(I18n.tr("Manual Port Forward"));
    
    private JLabel _labelTCP;
    private JLabel _labelUDP;
    
	/**
	 * The constructor constructs all of the elements of this 
	 * <tt>AbstractPaneItem</tt>.
	 */
	public ForceIPPaneItem() {
	    super(TITLE, LABEL);
		
		BUTTONS.add(RANDOM_PORT);
		BUTTONS.add(MANUAL_PORT);
		MANUAL_PORT.addItemListener(new LocalPortListener());
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, 0, 6);
		panel.add(RANDOM_PORT, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(MANUAL_PORT, c);
		
		_labelTCP = new JLabel(I18n.tr("TCP:"));
		c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.insets = new Insets(0, 10, 0, 5);
		panel.add(_labelTCP, c);
		c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.WEST;
		panel.add(TCP_PORT_FIELD, c);
		
		_labelUDP = new JLabel(I18n.tr("UDP:"));
		c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.insets = new Insets(0, 10, 0, 5);
		panel.add(_labelUDP, c);
		c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.WEST;
		panel.add(UDP_PORT_FIELD, c);
		
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(Box.createGlue(), c);
		GUIUtils.restrictSize(panel, SizePolicy.RESTRICT_HEIGHT);
		
		add(panel);
	}
	
	private void updateState() {
	    _labelTCP.setEnabled(MANUAL_PORT.isSelected());
	    _labelUDP.setEnabled(MANUAL_PORT.isSelected());
	    TCP_PORT_FIELD.setEnabled(MANUAL_PORT.isSelected());
        UDP_PORT_FIELD.setEnabled(MANUAL_PORT.isSelected());
    }

    /** 
	 * Listener class that responds to the checking and the 
	 * unchecking of the check box specifying whether or not to 
	 * use a local ip configuration.  It makes the other fields 
	 * editable or not editable depending on the state of the
	 * check box.
	 */
    private class LocalPortListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            updateState();
        }
    }

	/**
	 * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
	 *
	 * Sets the options for the fields in this <tt>PaneItem</tt> when the 
	 * window is shown.
	 */
	public void initOptions() {
        if (ConnectionSettings.FORCE_IP_ADDRESS.getValue() && !ConnectionSettings.UPNP_IN_USE.getValue()) {
            MANUAL_PORT.setSelected(true);
        } else {
            RANDOM_PORT.setSelected(true);
        }
	        
        TCP_PORT_FIELD.setValue(ConnectionSettings.TCP_PORT.getValue());
        UDP_PORT_FIELD.setValue(ConnectionSettings.UDP_PORT.getValue());
        
		updateState();
	}

	/**
	 * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
	 *
	 * Applies the options currently set in this window, displaying an
	 * error message to the user if a setting could not be applied.
	 *
	 * @throws IOException if the options could not be applied for some reason
	 */
	public boolean applyOptions() throws IOException {
	    
	    boolean restart = false;
	    boolean oldUPNP = ConnectionSettings.UPNP_IN_USE.getValue();
        //int oldTcpPort = ConnectionSettings.TCP_PORT.getValue();
        //int oldUdpPort = ConnectionSettings.UDP_PORT.getValue();
        //boolean oldForce = ConnectionSettings.FORCE_IP_ADDRESS.getValue();

	    
	    if(RANDOM_PORT.isSelected()) {
	        if(!ConnectionSettings.UPNP_IN_USE.getValue())
	            ConnectionSettings.FORCE_IP_ADDRESS.setValue(false);
	        ConnectionSettings.DISABLE_UPNP.setValue(false);
	        if(!oldUPNP)
	            restart = true;
	        
	        COConfigurationManager.setParameter("upnp.enable", true);
        } else { // PORT.isSelected()
            int forcedTcpPort = TCP_PORT_FIELD.getValue();
            int forcedUdpPort = UDP_PORT_FIELD.getValue();
            if(!NetworkUtils.isValidPort(forcedTcpPort)) {
                GUIMediator.showError(I18n.tr("You must enter a port between 1 and 65535 when manually forcing your TCP port."));
                throw new IOException("bad port: "+forcedTcpPort);
            }
            if(!NetworkUtils.isValidPort(forcedUdpPort)) {
                GUIMediator.showError(I18n.tr("You must enter a port between 1 and 65535 when manually forcing your UDP port."));
                throw new IOException("bad port: "+forcedUdpPort);
            }
            
            ConnectionSettings.DISABLE_UPNP.setValue(false);
            ConnectionSettings.FORCE_IP_ADDRESS.setValue(true);
            ConnectionSettings.UPNP_IN_USE.setValue(false);
            ConnectionSettings.TCP_PORT.setValue(forcedTcpPort);
            ConnectionSettings.UDP_PORT.setValue(forcedUdpPort);
            
            // put upnp port configuration
            COConfigurationManager.setParameter("TCP.Listen.Port", forcedTcpPort);
            COConfigurationManager.setParameter("UDP.Listen.Port", forcedUdpPort);
            COConfigurationManager.setParameter("upnp.enable", false);
            
            restart = true;
        }
	    
	    COConfigurationManager.save();
        
        // Notify that the address changed if:
        //    1) The 'forced address' status changed.
        // or 2) We're forcing and the ports are different.
//        boolean newForce = ConnectionSettings.FORCE_IP_ADDRESS.getValue();
//        int newPort = ConnectionSettings.FORCED_PORT.getValue();        
//        if(oldForce != newForce || (newForce && (oldPort != newPort)))
//            networkManager.addressChanged();
        
        return restart;
    }
    
    public boolean isDirty() {
		
		if(ConnectionSettings.FORCE_IP_ADDRESS.getValue() && 
				!ConnectionSettings.UPNP_IN_USE.getValue()) {
			if (!MANUAL_PORT.isSelected()) {
				return true;
			}
		} else {
			if (!RANDOM_PORT.isSelected()) {
				return true;
			}
		}
		return MANUAL_PORT.isSelected() 
			&& (TCP_PORT_FIELD.getValue() != ConnectionSettings.TCP_PORT.getValue() ||
			    UDP_PORT_FIELD.getValue() != ConnectionSettings.UDP_PORT.getValue());
    }
}
