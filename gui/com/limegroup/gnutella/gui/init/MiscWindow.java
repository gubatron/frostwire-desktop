package com.limegroup.gnutella.gui.init;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.limewire.i18n.I18nMarker;
import org.limewire.util.OSUtils;

import com.frostwire.gnutella.gui.chat.ChatMediator;
import com.limegroup.gnutella.SpeedConstants;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.gui.SizedTextField;
import com.limegroup.gnutella.gui.WindowsUtils;
import com.limegroup.gnutella.settings.ChatSettings;
import com.limegroup.gnutella.settings.ConnectionSettings;
import com.limegroup.gnutella.settings.DownloadSettings;
import com.limegroup.gnutella.settings.StartupSettings;
import com.limegroup.gnutella.util.MacOSXUtils;

/**
 * This class displays a window to the user allowing them to specify
 * their connection speed.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
final class MiscWindow extends SetupWindow {

    /**
     * The four buttons that represent the speeds, and their button group.
     */
    private ButtonGroup _speedGroup;

    private JRadioButton _modem;

    private JRadioButton _cable;

    private JRadioButton _t1;
    
    private JRadioButton _t3;
    
    /**
     * The chat Community nickname field.
     */
    private JTextField _ircNickField; 

    /*
     * System Startup
     */
    private JCheckBox _startup;

    /**
     * The checkbox that determines whether or not to use content management.
     */
    private JCheckBox _filter;

    /**
     * Creates the window and its components.
     */
    MiscWindow(SetupManager manager) {
        super(
                manager,
                I18nMarker.marktr("Miscellaneous Settings"),
                I18nMarker
                        .marktr("Below, are several options that affect the performance and functionality of FrostWire."));
    }

    protected void createWindow() {
        super.createWindow();

        JPanel mainPanel = new JPanel(new GridBagLayout());

        // Connection Speed
        {
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel buttonPanel = new JPanel(new GridBagLayout());
            
            buttonPanel.setBorder(new TitledBorder(I18n.tr("Network Speed")));
            
            _speedGroup = new ButtonGroup();
            _t1 = new JRadioButton(I18n.tr("T1"));
            _t3 = new JRadioButton(I18n.tr("T3"));
            _modem = new JRadioButton(I18n.tr("Dial Up"));
            _cable = new JRadioButton(I18n.tr("Broadband (or unsure)"));
            
            _speedGroup.add(_t3);
            _speedGroup.add(_t1);
            _speedGroup.add(_cable);
            _speedGroup.add(_modem);
            
            MultiLineLabel speedDesc = new MultiLineLabel(
                    I18n.tr("Please choose the speed of your internet connection. Setting this speed correctly is important for optimum network performance."));
            speedDesc.setOpaque(false);
            speedDesc.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 5));
            speedDesc.setForeground(Color.black);
            speedDesc.setFont(speedDesc.getFont().deriveFont(Font.PLAIN));
            
            gbc.anchor = GridBagConstraints.NORTHEAST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = GridBagConstraints.REMAINDER;
            buttonPanel.add(speedDesc, gbc);
            
            gbc.weightx = 1;
            gbc.anchor = GridBagConstraints.SOUTHWEST;
            gbc.gridwidth = 1;
            buttonPanel.add(_t3, gbc);

            gbc.weightx = 1;
            gbc.gridwidth = 1;
            buttonPanel.add(_t1, gbc);
            
            gbc.weightx = 1;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            buttonPanel.add(_cable, gbc);
            
            gbc.insets = new Insets(0, 20, 0, 0);
            gbc.weightx = 1;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            buttonPanel.add(_modem, gbc);
            
            gbc.insets = new Insets(0, 0, 10, 0);
            gbc.weightx = 1;
            mainPanel.add(buttonPanel, gbc);
        }

	//System.out.println("*******STARTUP DEBUG: initializing verification System Startup...");
        // System Startup
        if (GUIUtils.shouldShowStartOnStartupWindow()) {
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel startupPanel = new JPanel(new GridBagLayout());
            
            startupPanel.setBorder(new TitledBorder(I18n.tr("System Startup")));
            
            _startup = new JCheckBox(I18n.tr("Start Automatically"));
            _startup.setSelected(StartupSettings.RUN_ON_STARTUP.getValue());
	    System.out.println("********START UP AUTOMAGICALLY?: ******" + StartupSettings.RUN_ON_STARTUP.getValue());
            
            MultiLineLabel desc = new MultiLineLabel(
                    I18n.tr("Would you like FrostWire to start when you log into your computer? This will cause FrostWire to start faster when you use it later."));
            desc.setOpaque(false);
            desc.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 5));
            desc.setForeground(Color.black);
            desc.setFont(desc.getFont().deriveFont(Font.PLAIN));
            
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            
            startupPanel.add(desc, gbc);
            startupPanel.add(_startup, gbc);
            
            gbc.insets = new Insets(0, 0, 10, 0);
            mainPanel.add(startupPanel, gbc);
        }
        
        // Content Filtering
        /** No content filtering for FrostWire
        {
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel filterPanel = new JPanel(new GridBagLayout());
            
            filterPanel.setBorder(new TitledBorder(I18n.tr("Content Filtering")));
            
            _filter = new JCheckBox(I18n.tr("Enable Content Filtering"));
            _filter.setSelected(ContentSettings.USER_WANTS_MANAGEMENTS.getValue());
            
            MultiLineLabel desc = new MultiLineLabel(
                    I18n.tr("FrostWire can filter files that copyright owners request not be shared. By enabling filtering, you are telling FrostWire to confirm all files you download or share with a list of removed content. You can change this at any time by choosing Filters -> Configure Content Filters from the main menu."));
            desc.setOpaque(false);
            desc.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 5));
            desc.setForeground(Color.black);
            desc.setFont(desc.getFont().deriveFont(Font.PLAIN));
            
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            
            JLabel url= new URLLabel(ContentSettings.LEARN_MORE_URL, I18n.tr("Learn more about this option..."));
            url.setOpaque(false);
            url.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            url.setForeground(Color.black);
            url.setOpaque(false);
            url.setAlignmentY( 1.0f );

            
            filterPanel.add(desc, gbc);
            gbc.insets = new Insets(0, 0, 5, 0);
            filterPanel.add(url, gbc);
            gbc.insets = new Insets(0, 0, 0, 0);
            filterPanel.add(_filter, gbc);
            
            gbc.insets = new Insets(0, 0, 10, 0);
            mainPanel.add(filterPanel, gbc);
        }
        */

        // Chat Community
        {

            JPanel chatCommunityPanel = new JPanel(new GridLayout(2,0));
            
            chatCommunityPanel.setBorder(new TitledBorder(I18n.tr("Chat Community")));
            
            //create multiline to describe why the chat needs a nick (descChat)
            MultiLineLabel descChat = new MultiLineLabel(
                    I18n.tr("FrostWire's Community Chat Tab requires you to have a nickname to communicate with others in the chatrooms."));
            descChat.setOpaque(false);
            descChat.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 5));
            descChat.setForeground(Color.black);
            descChat.setFont(descChat.getFont().deriveFont(Font.PLAIN));

            _ircNickField = new SizedTextField(new Dimension(100,SizedTextField.STANDARD_HEIGHT));
            
    	    LabeledComponent textField = 
    	        new LabeledComponent( I18n.tr("Type your chat nickname here (any name):"), 
    	        		_ircNickField, LabeledComponent.RIGHT_GLUE, LabeledComponent.LEFT);
    	    textField.getBoxPanelComponent().setBorder(new javax.swing.border.EmptyBorder(0,10,5,5));

            //time to add the components

            //add the description. upper left
            chatCommunityPanel.add(descChat);
            chatCommunityPanel.add(textField.getComponent());

            //when we add the panel, its part of a bigger layout so we need to set its GridBagConstraints
            GridBagConstraints outerLayoutConstraints = new GridBagConstraints();
            //outerLayoutConstraints.anchor = GridBagConstraints.NORWEST;
            outerLayoutConstraints.gridwidth = GridBagConstraints.REMAINDER;
            outerLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            outerLayoutConstraints.gridy = GridBagConstraints.RELATIVE;
            mainPanel.add(chatCommunityPanel, outerLayoutConstraints);
        }

        // Vertical Filler
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        mainPanel.add(new JPanel(), gbc);

        setSetupComponent(mainPanel);

        // set the radio button selection state to the
        // current setting, such that it doesn't just
        // get reset every time the window is drawn.
        //
        {
            int speed = ConnectionSettings.CONNECTION_SPEED.getValue();

            if (SpeedConstants.MODEM_SPEED_INT == speed)
                _modem.setSelected(true);
            else if (SpeedConstants.CABLE_SPEED_INT == speed)
                _cable.setSelected(true);
            else if (SpeedConstants.T1_SPEED_INT == speed)
            	_t1.setSelected(true);
            else if (SpeedConstants.T3_SPEED_INT == speed)
            	_t3.setSelected(true);
            else
                _cable.setSelected(true);
        }
    }

    /**
     * Overrides applySettings in SetupWindow superclass.
     * Applies the settings handled in this window.
     */
    public void applySettings(boolean loadCoreComponents) {
        // Connection Speed
        {
            int speed = getSpeed();
            setDownloadSlots(speed);

            if (speed < SpeedConstants.MIN_SPEED_INT || SpeedConstants.MAX_SPEED_INT < speed) {
                throw new IllegalArgumentException();
            }

            ConnectionSettings.CONNECTION_SPEED.setValue(speed);
        }

        // System Startup
        if (GUIUtils.shouldShowStartOnStartupWindow()) {
            boolean allow = _startup.isSelected();

            if (OSUtils.isMacOSX())
                MacOSXUtils.setLoginStatus(allow);
            else if (WindowsUtils.isLoginStatusAvailable())
                WindowsUtils.setLoginStatus(allow);

            StartupSettings.RUN_ON_STARTUP.setValue(allow);
        }

        // Content Filtering
        {
            //ContentSettings.USER_WANTS_MANAGEMENTS.setValue(_filter.isSelected());
        }
        
        // Community Settings
        {
        	ChatSettings.CHAT_IRC_NICK.setValue(_ircNickField.getText());
        	//the chat could be loaded by now with the default nickname, the user won't be allowed to login
        	//if this happens, so we try to reinitialize the IRCApplication.
        	ChatMediator.instance().reloadConfiguration();
        }
    }

    /**
     * Returns the selected speed value.  If no speed was selected, 
     * it returns the MODEM_SPEED.
     *
     * @return the selected speed value.  If no speed was selected, 
     * it returns the MODEM_SPEED
     */
    private int getSpeed() {
        if (_cable.isSelected())
            return SpeedConstants.CABLE_SPEED_INT;
        else if (_t1.isSelected())
        	return SpeedConstants.T1_SPEED_INT;
        else if (_t3.isSelected())
        	return SpeedConstants.T3_SPEED_INT;
        else
            return SpeedConstants.MODEM_SPEED_INT;
    }

    /**
     * Sets the number of download slots based on the connection
     * speed the user entered.
     * 
     * @param speed the speed of the connection to use for setting
     *  the download slots
     */
    private void setDownloadSlots(int speed) {

        if (speed == SpeedConstants.MODEM_SPEED_INT) {
            DownloadSettings.MAX_SIM_DOWNLOAD.setValue(3);
        } else if (speed == SpeedConstants.CABLE_SPEED_INT) {
            DownloadSettings.MAX_SIM_DOWNLOAD.setValue(8);
        } else if (speed == SpeedConstants.T1_SPEED_INT) {
        	DownloadSettings.MAX_SIM_DOWNLOAD.setValue(12);
        } else if (speed == SpeedConstants.T3_SPEED_INT) {
        	DownloadSettings.MAX_SIM_DOWNLOAD.setValue(16);
        } else {
            DownloadSettings.MAX_SIM_DOWNLOAD.setValue(3);
        }
    }
}
