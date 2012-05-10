package com.limegroup.gnutella.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

import org.gudy.azureus2.plugins.network.ConnectionManager;
import org.limewire.setting.BooleanSetting;

import com.aelitis.azureus.core.AzureusCore;
import com.frostwire.AzureusStarter;
import com.frostwire.gui.bittorrent.BTDownloadMediator;
import com.limegroup.gnutella.UpdateInformation;
import com.limegroup.gnutella.gui.themes.SkinCheckBoxMenuItem;
import com.limegroup.gnutella.gui.themes.SkinPopupMenu;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;
import com.limegroup.gnutella.settings.ApplicationSettings;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.settings.StatusBarSettings;

/**
 * The component for the space at the bottom of the main application
 * window, including the connected status and the media player.
 */
public final class StatusLine implements ThemeObserver {

	/**
     * The different connection status possibilities.
     */
    public static final int STATUS_DISCONNECTED = 0;
    public static final int STATUS_TURBOCHARGED = 1;

    /**
     * The main container for the status line component.
     */
    private JPanel BAR;
    
    /**
     * The left most panel containing the connection quality.
     * The switcher changes the actual ImageIcons on this panel.
     */
    private JLabel _connectionQualityMeter;
    private final ImageIcon[] _connectionQualityMeterIcons = new ImageIcon[7];

    /**
     * The button for the current language flag to allow language switching
     */
    private LanguageButton _languageButton;
    
    /**
     * The label with the firewall status.
     */
    private JLabel _firewallStatus;
	
	/**
	 * The labels for displaying the bandwidth usage.
	 */
	private JLabel _bandwidthUsageDown;
	private JLabel _bandwidthUsageUp;
	
	private IconButton _twitterButton;
    private IconButton _facebookButton;
    private IconButton _googlePlusButton;
	
	private IconButton seedingStatusButton;
    
    /**
     * Variables for the center portion of the status bar, which can display
     * the StatusComponent (progress bar during program load), the UpdatePanel
     * (notification that a new version of FrostWire is available), and the
     * StatusLinkHandler (ads for going PRO).
     */
    private StatusComponent STATUS_COMPONENT;
    private UpdatePanel _updatePanel;
	private JPanel _centerPanel;
	private Component _centerComponent;

	private CurrentAudioStatusComponent _audioStatusComponent;
    
    ///////////////////////////////////////////////////////////////////////////
    //  Construction
    ///////////////////////////////////////////////////////////////////////////
        
    /**
     * Creates a new status line in the disconnected state.
     */
    public StatusLine() {
        GUIMediator.setSplashScreenString(I18n.tr("Loading Status Window..."));

        getComponent().addMouseListener(STATUS_BAR_LISTENER);
        GUIMediator.getAppFrame().addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent arg0) {
                refresh();
            }

            public void componentMoved(ComponentEvent arg0) {
            }

            public void componentShown(ComponentEvent arg0) {
            }

            public void componentHidden(ComponentEvent arg0) {
            }
        });

        _audioStatusComponent = new CurrentAudioStatusComponent();

        //  make icons and panels for connection quality
        createConnectionQualityPanel();

        //  make the 'Language' button
        createLanguageButton();

        //  make the 'Firewall Status' label
        createFirewallLabel();

        //  make the 'Bandwidth Usage' label
        createBandwidthLabel();

        // make the social buttons
        createFacebookButton();
        createTwitterButton();
        createGooglePlusButton();
        
        // male Seeding status label
        createSeedingStatusLabel();

        //  make the center panel
        createCenterPanel();

        // Set the bars to not be connected.
        setConnectionQuality(0);

        GUIMediator.addRefreshListener(REFRESH_LISTENER);
        ThemeMediator.addThemeObserver(this);
        
        
        refresh();
    }

	private void createTwitterButton() {
	    _twitterButton = new IconButton("TWITTER");
	    initSocialButton(_twitterButton, I18n.tr("Follow us @frostwire"), "https://twitter.com/#!/frostwire");
    }

    private void createFacebookButton() {
        _facebookButton = new IconButton("FACEBOOK");
        initSocialButton(_facebookButton, I18n.tr("Like FrostWire on Facebook and stay in touch with the community. Get Help and Help Others."),
                "http://www.facebook.com/pages/FrostWire/110265295669948");
    }

    private void createGooglePlusButton() {
        _googlePlusButton = new IconButton("GOOGLEPLUS");
        _googlePlusButton.setPreferredSize(new Dimension(19,16));
        initSocialButton(_googlePlusButton, I18n.tr("Circle FrostWire on G+"), "https://plus.google.com/b/101138154526002646407/");
    }
    
    private void initSocialButton(IconButton socialButton, String toolTipText, final String url) {
        socialButton.setToolTipText(I18n.tr(toolTipText));
        socialButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                GUIMediator.openURL(url);
            }
        });
    }
    
    private void createSeedingStatusLabel() {

        seedingStatusButton = new IconButton("","SEEDING",true)  {
            private static final long serialVersionUID = -8985154093868645203L;
            
            @Override
            public String getToolTipText() {
                boolean seedingStatus = SharingSettings.SEED_FINISHED_TORRENTS.getValue();
                
                String tooltip = (seedingStatus) ? I18n.tr("<html><b>Seeding</b><p>completed torrent downloads.</html>") : I18n
                        .tr("<html><b>Not Seeding</b>.<p>File chunks might be shared only during<p>a torrent download.</html>");
                return tooltip;
            }
        };
        
        seedingStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIMediator.instance().setOptionsVisible(true, I18n.tr("Options"));
            }
        });

        ToolTipManager.sharedInstance().registerComponent(seedingStatusButton);
    }

    /**
	 * Redraws the status bar based on changes to StatusBarSettings,
	 * and makes sure it has room to add an indicator before adding it.
	 */
	public void refresh() {
	    if (_audioStatusComponent==null ||
	            _centerComponent==null) {
	        return;
	    }
	    
	    getComponent().removeAll();
        
		//  figure out remaining width, and do not add indicators if no room
		int sepWidth = Math.max(2, createSeparator().getWidth());
		int remainingWidth = BAR.getWidth();
		if (remainingWidth <= 0)
			remainingWidth = ApplicationSettings.APP_WIDTH.getValue();
		
		//  subtract player as needed
		remainingWidth -= sepWidth;
		remainingWidth -= GUIConstants.SEPARATOR / 2;

		remainingWidth -= _audioStatusComponent.getWidth();
		remainingWidth -= GUIConstants.SEPARATOR;
		
		//  subtract center component
		int indicatorWidth = _centerComponent.getWidth();
		if (indicatorWidth <= 0)
            if (_updatePanel.shouldBeShown()) {
                indicatorWidth = 190;
            }
		remainingWidth -= indicatorWidth;

        //  add components to panel, if room
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0,0,0,0);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = GridBagConstraints.RELATIVE;

        //  add connection quality indicator if there's room
        indicatorWidth = GUIConstants.SEPARATOR +
            Math.max((int)_connectionQualityMeter.getMinimumSize().getWidth(),
                    _connectionQualityMeter.getWidth()) + sepWidth;
        if (StatusBarSettings.CONNECTION_QUALITY_DISPLAY_ENABLED.getValue() &&
                remainingWidth > indicatorWidth) {
            BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 2), gbc);
            BAR.add(_connectionQualityMeter, gbc);
            BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 2), gbc);
            BAR.add(createSeparator(), gbc);
            remainingWidth -= indicatorWidth;
        }
        
        //  add the language button if there's room
        indicatorWidth = GUIConstants.SEPARATOR +
        	Math.max((int) _languageButton.getMinimumSize().getWidth(),
        			_languageButton.getWidth()) + sepWidth;
        
        BooleanSetting languageSetting = getLanguageSetting();
        if (languageSetting.getValue() && remainingWidth > indicatorWidth) {
            BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 2), gbc);
            BAR.add(_languageButton, gbc);
            BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 2), gbc);
            BAR.add(createSeparator(), gbc);
            remainingWidth -= indicatorWidth;        
        }
        

        //  then add firewall display if there's room
        indicatorWidth = GUIConstants.SEPARATOR +
            Math.max((int)_firewallStatus.getMinimumSize().getWidth(),
                    _firewallStatus.getWidth()) + sepWidth;
        if (StatusBarSettings.FIREWALL_DISPLAY_ENABLED.getValue() &&
                remainingWidth > indicatorWidth) {
            BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 2), gbc);
            BAR.add(_firewallStatus, gbc);
            BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 2), gbc);
            BAR.add(createSeparator(), gbc);
            remainingWidth -= indicatorWidth;
        }
        
		
		//  add bandwidth display if there's room
		indicatorWidth = GUIConstants.SEPARATOR + GUIConstants.SEPARATOR / 2 + sepWidth +
			Math.max((int)_bandwidthUsageDown.getMinimumSize().getWidth(), _bandwidthUsageDown.getWidth()) +
            Math.max((int)_bandwidthUsageUp.getMinimumSize().getWidth(), _bandwidthUsageUp.getWidth());
        if (StatusBarSettings.BANDWIDTH_DISPLAY_ENABLED.getValue() &&
				remainingWidth > indicatorWidth) {
			BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 2), gbc);
			BAR.add(_bandwidthUsageDown, gbc);
			BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR), gbc);
			BAR.add(_bandwidthUsageUp, gbc);
			BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 2), gbc);
			BAR.add(createSeparator(), gbc);
			remainingWidth -= indicatorWidth;
        }
        
        gbc = new GridBagConstraints();
        gbc.gridx = GridBagConstraints.RELATIVE;
        BAR.add(seedingStatusButton,gbc);
        BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 2), gbc);
        BAR.add(createSeparator(), gbc);
        updateSeedingStatus();

        gbc = new GridBagConstraints();
        gbc.gridx = GridBagConstraints.RELATIVE;
        BAR.add(_facebookButton,gbc);
        BAR.add(_twitterButton,gbc);
        BAR.add(_googlePlusButton,gbc);
        
		BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 2), gbc);
        //  make center panel stretchy
        gbc.weightx = 1;
		BAR.add(_centerPanel, gbc);
        gbc.weightx = 0;
		BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 2), gbc);

		// current song component
		BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR / 2), gbc);
		BAR.add(_audioStatusComponent, gbc);
		BAR.add(Box.createHorizontalStrut(10));
		BAR.add(Box.createHorizontalStrut(GUIConstants.SEPARATOR), gbc);

		BAR.validate();
		BAR.repaint();
	}

	/**
     * Creates a vertical separator for visually separating status bar elements 
     */
    private Component createSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        //  separators need preferred size in GridBagLayout
        sep.setPreferredSize(new Dimension(2, 20));
        sep.setMinimumSize(new Dimension(2, 20));
        return sep;
    }

    /**
     * Sets up _connectionQualityMeter's icons.
     */
    private void createConnectionQualityPanel() {
		updateTheme();  // loads images
		_connectionQualityMeter = new JLabel();
		_connectionQualityMeter.setOpaque(false);
        _connectionQualityMeter.setMinimumSize(new Dimension(34, 20));
        _connectionQualityMeter.setMaximumSize(new Dimension(90, 30));
		//   add right-click listener
		_connectionQualityMeter.addMouseListener(STATUS_BAR_LISTENER);
	}


    /**
	 * Sets up the 'Language' button
	 */
	private void createLanguageButton() {
	    _languageButton = new LanguageButton();
		_languageButton.addMouseListener(STATUS_BAR_LISTENER);
		updateLanguage();
	}

	
	/**
	 * Sets up the 'Firewall Status' label.
	 */
	private void createFirewallLabel() {
	    _firewallStatus = new JLabel();
	    updateFirewall();
		// don't allow easy clipping
		_firewallStatus.setMinimumSize(new Dimension(20, 20));
		// add right-click listener
		_firewallStatus.addMouseListener(STATUS_BAR_LISTENER);
	}
	
	/**
        _lwsStatus = new JLabel();
	 * Sets up the 'Bandwidth Usage' label.
	 */
	private void createBandwidthLabel() {
	    _bandwidthUsageDown = new LazyTooltip(GUIMediator.getThemeImage("downloading_small"));
	    _bandwidthUsageUp = new LazyTooltip(GUIMediator.getThemeImage("uploading_small"));
		//updateBandwidth();
		// don't allow easy clipping
		_bandwidthUsageDown.setMinimumSize(new Dimension(60, 20));
		_bandwidthUsageUp.setMinimumSize(new Dimension(60, 20));
		// add right-click listeners
		_bandwidthUsageDown.addMouseListener(STATUS_BAR_LISTENER);
		_bandwidthUsageUp.addMouseListener(STATUS_BAR_LISTENER);
	}

	/**
	 * Sets up the center panel.
	 */
	private void createCenterPanel() {
	    STATUS_COMPONENT = new StatusComponent();
	    _updatePanel = new UpdatePanel();
	    _centerComponent = _updatePanel;
	    _centerPanel = new JPanel(new GridBagLayout());
	    
		_centerPanel.setOpaque(false);
        _updatePanel.setOpaque(false);
		STATUS_COMPONENT.setProgressPreferredSize(new Dimension(250, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
		_centerPanel.add(STATUS_COMPONENT, gbc);

		//  add right-click listeners
		_centerPanel.addMouseListener(STATUS_BAR_LISTENER);
		_updatePanel.addMouseListener(STATUS_BAR_LISTENER);
		STATUS_COMPONENT.addMouseListener(STATUS_BAR_LISTENER);
	}

	/**
	 * Updates the center panel if non-PRO.  Periodically rotates between
	 * the update panel and the status link handler. 
	 */
	private void updateCenterPanel() {
		long now = System.currentTimeMillis();
		if (_nextUpdateTime > now)
			return;

		_nextUpdateTime = now + 1000 * 5; // update every minute
		_centerPanel.removeAll();
        if (_updatePanel.shouldBeShown()) {
            _centerComponent = _updatePanel;
        } else {
            _centerComponent = new JLabel();
        }
		
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        _centerPanel.add(_centerComponent, gbc);
		
		refresh();
	}
	
	private void updateSeedingStatus() {
	    boolean seedingStatus = SharingSettings.SEED_FINISHED_TORRENTS.getValue();
        seedingStatusButton.setText(seedingStatus ? I18n.tr("<html><b>Seeding</b></html>") : I18n.tr("<html><b>Not Seeding</b></html>"));
        seedingStatusButton.setIcon(seedingStatus ? GUIMediator.getThemeImage("seeding_small") : GUIMediator.getThemeImage("not_seeding_small"));
	}
	
	private long _nextUpdateTime = System.currentTimeMillis();

    /**
     * Tells the status linke that the update panel should be shown with
     * the given update information.
     */
    public void showUpdatePanel(boolean popup, UpdateInformation info) {
        _updatePanel.makeVisible(popup, info);
    }
    
    /**
     * Updates the status text.
     */
    public void setStatusText(final String text) {
        GUIMediator.safeInvokeAndWait(new Runnable() {
            public void run() {
                STATUS_COMPONENT.setText(text);
            }
        });
    }

	/**
	 * Updates the firewall text. 
	 */
	public void updateFirewallLabel(boolean notFirewalled) {
		if (notFirewalled) {
			_firewallStatus.setIcon(GUIMediator.getThemeImage("firewall_no"));
			_firewallStatus.setToolTipText(I18n.tr("FrostWire has not detected a firewall"));
		} else {
			_firewallStatus.setIcon(GUIMediator.getThemeImage("firewall"));
			_firewallStatus.setToolTipText(I18n.tr("FrostWire has detected a firewall"));
		}
	}

	/**
	 * Updates the image on the flag
	 */
	public void updateLanguage() {
		_languageButton.updateLanguageFlag();
	}
	
	/**
	 * Updates the firewall text. 
	 */
	public void updateFirewall() {
		AzureusCore azureusCore = AzureusStarter.getAzureusCore();
		
		if (azureusCore == null) {
			updateFirewallLabel(false);
			return;
		}
		
		int natStatus = azureusCore.getGlobalManager().getNATStatus();
		updateFirewallLabel(natStatus == ConnectionManager.NAT_OK || natStatus == ConnectionManager.NAT_PROBABLY_OK);	
	}
	
    /**
	 * Updates the bandwidth statistics.
	 */
	public void updateBandwidth() {
		
        //  format strings
        String sDown = GUIUtils.rate2speed(GUIMediator.instance().getBTDownloadMediator().getDownloadsBandwidth());
        String sUp = GUIUtils.rate2speed(GUIMediator.instance().getBTDownloadMediator().getUploadsBandwidth());

        // number of uploads (seeding) and downloads
        int downloads = GUIMediator.instance().getCurrentDownloads();
        int uploads = GUIMediator.instance().getCurrentUploads();
		
        
        _bandwidthUsageDown.setText(downloads + " @ " + sDown);
		_bandwidthUsageUp.setText(uploads +   " @ " + sUp);
	}
	
    /**
     * Notification that loading has finished.
     *
     * The loading label is removed and the update notification
     * component is added.  If necessary, the center panel will
     * rotate back and forth between displaying the update
     * notification and displaying the StatusLinkHandler.
     */
    void loadFinished() {
		updateCenterPanel();
		_centerPanel.revalidate();
        _centerPanel.repaint();
		refresh();
    }

	/**
     * Load connection quality theme icons
	 */
	public void updateTheme() {
        _connectionQualityMeterIcons[StatusLine.STATUS_DISCONNECTED] = GUIMediator.getThemeImage("connect_small_0");
        _connectionQualityMeterIcons[StatusLine.STATUS_TURBOCHARGED] = GUIMediator.getThemeImage("connect_small_6");
        
//		if (_mediaPlayer != null)
//			_mediaPlayer.updateTheme();
	}

    /**
     * Alters the displayed connection quality.
     *
     * @modifies this
     */
    public void setConnectionQuality(int quality) {
        // make sure we don't go over our bounds.
        if (quality >= _connectionQualityMeterIcons.length)
            quality = _connectionQualityMeterIcons.length - 1;

        _connectionQualityMeter.setIcon(_connectionQualityMeterIcons[quality]);

        String status = null;
        String tip = null;
        switch(quality) {
            case STATUS_DISCONNECTED:
                	status = I18n.tr("Disconnected");
                    tip = I18n.tr("Check your internet connection, FrostWire can't connect.");
                    break;
            case STATUS_TURBOCHARGED:
                    status = I18n.tr("Turbo-Charged");
                    tip = I18n.tr("Your connection to the network is extremely strong");
                    break;
        }
        _connectionQualityMeter.setToolTipText(tip);
        _connectionQualityMeter.setText(status);
    }

    /**
      * Accessor for the <tt>JComponent</tt> instance that contains all
      * of the panels for the status line.
      *
      * @return the <tt>JComponent</tt> instance that contains all
      *  of the panels for the status line
      */
    public JComponent getComponent() {
        if (BAR == null) {
            BAR = new JPanel(new GridBagLayout());
        }
        return BAR;
    }
	
    /**
     * The refresh listener for updating the bandwidth usage every second.
     */
    private final RefreshListener REFRESH_LISTENER = new RefreshListener() {
        public void refresh() {
            if (StatusBarSettings.BANDWIDTH_DISPLAY_ENABLED.getValue()) {
                updateBandwidth();
            }
            updateCenterPanel();
        }
    };
    
    private BooleanSetting getLanguageSetting() {
        if (GUIMediator.isEnglishLocale()) {
            return StatusBarSettings.LANGUAGE_DISPLAY_ENGLISH_ENABLED;
        } else {
            return StatusBarSettings.LANGUAGE_DISPLAY_ENABLED;
        }
    }
    
    /**
     * The right-click listener for the status bar.
     */
	private final MouseAdapter STATUS_BAR_LISTENER = new MouseAdapter() {
		public void mousePressed(MouseEvent me) { processMouseEvent(me); }
		public void mouseReleased(MouseEvent me) { processMouseEvent(me); }
		public void mouseClicked(MouseEvent me) { processMouseEvent(me); }
		
		public void processMouseEvent(MouseEvent me) {
			if (me.isPopupTrigger()) {
                JPopupMenu jpm = new SkinPopupMenu();
                
                //  add 'Show Connection Quality' menu item
                JCheckBoxMenuItem jcbmi = new SkinCheckBoxMenuItem(new ShowConnectionQualityAction());
                jcbmi.setState(StatusBarSettings.CONNECTION_QUALITY_DISPLAY_ENABLED.getValue());
                jpm.add(jcbmi);

                //  add 'Show International Localization' menu item
                jcbmi = new SkinCheckBoxMenuItem(new ShowLanguageStatusAction());
                jcbmi.setState(getLanguageSetting().getValue());
                jpm.add(jcbmi);

                
                //  add 'Show Firewall Status' menu item
                jcbmi = new SkinCheckBoxMenuItem(new ShowFirewallStatusAction());
                jcbmi.setState(StatusBarSettings.FIREWALL_DISPLAY_ENABLED.getValue());
                jpm.add(jcbmi);
                
                //  add 'Show Bandwidth Consumption' menu item
                jcbmi = new SkinCheckBoxMenuItem(new ShowBandwidthConsumptionAction());
                jcbmi.setState(StatusBarSettings.BANDWIDTH_DISPLAY_ENABLED.getValue());
                jpm.add(jcbmi);
                
                jpm.pack();
                jpm.show(me.getComponent(), me.getX(), me.getY());
            }
		}
	};

	/**
	 * Action for the 'Show Connection Quality' menu item. 
	 */
	private class ShowConnectionQualityAction extends AbstractAction {
		
		/**
         * 
         */
        private static final long serialVersionUID = 7922422377962473634L;

        public ShowConnectionQualityAction() {
			putValue(Action.NAME, I18n.tr
					("Show Connection Quality"));
		}
		
		public void actionPerformed(ActionEvent e) {
			StatusBarSettings.CONNECTION_QUALITY_DISPLAY_ENABLED.invert();
			refresh();
		}
	}
	
	/**
	 * Action for the 'Show Firewall Status' menu item. 
	 */
	private class ShowLanguageStatusAction extends AbstractAction {
		
		/**
         * 
         */
        private static final long serialVersionUID = 726208491122581283L;

        public ShowLanguageStatusAction() {
			putValue(Action.NAME, I18n.tr
					("Show Language Status"));
		}
		
		public void actionPerformed(ActionEvent e) {
            BooleanSetting setting = getLanguageSetting();
            setting.invert();
            
			StatusBarSettings.LANGUAGE_DISPLAY_ENABLED.setValue(setting.getValue());
            StatusBarSettings.LANGUAGE_DISPLAY_ENGLISH_ENABLED.setValue(setting.getValue());
			refresh();
		}
	}
	
	
	/**
	 * Action for the 'Show Firewall Status' menu item. 
	 */
	private class ShowFirewallStatusAction extends AbstractAction {
		
		/**
         * 
         */
        private static final long serialVersionUID = -8489901794229005217L;

        public ShowFirewallStatusAction() {
			putValue(Action.NAME, I18n.tr
					("Show Firewall Status"));
		}
		
		public void actionPerformed(ActionEvent e) {
			StatusBarSettings.FIREWALL_DISPLAY_ENABLED.invert();
			refresh();
		}
	}
    
	/**
	 * Action for the 'Show Bandwidth Consumption' menu item. 
	 */
	private class ShowBandwidthConsumptionAction extends AbstractAction {
		
		/**
         * 
         */
        private static final long serialVersionUID = 1455679943975682049L;

        public ShowBandwidthConsumptionAction() {
			putValue(Action.NAME, I18n.tr("Show Bandwidth Consumption"));
		}
		
		public void actionPerformed(ActionEvent e) {
			StatusBarSettings.BANDWIDTH_DISPLAY_ENABLED.invert();
			refresh();
		}
	}
	
	private class LazyTooltip extends JLabel {
	    
		/**
         * 
         */
        private static final long serialVersionUID = -5759748801999410032L;

        LazyTooltip(ImageIcon icon) {
			super(icon);
			ToolTipManager.sharedInstance().registerComponent(this);
		}

	    @Override
		public String getToolTipText() {
	    	BTDownloadMediator btDownloadMediator = GUIMediator.instance().getBTDownloadMediator();
	    	
	        String sDown = GUIUtils.rate2speed(btDownloadMediator.getDownloadsBandwidth());
	        String sUp = GUIUtils.rate2speed(btDownloadMediator.getUploadsBandwidth());
	        
	        String totalDown = GUIUtils.toUnitbytes(btDownloadMediator.getTotalBytesDownloaded());
	        String totalUp = GUIUtils.toUnitbytes(btDownloadMediator.getTotalBytesUploaded());
	        int downloads = GUIMediator.instance().getCurrentDownloads();
	        
	        int uploads = GUIMediator.instance().getCurrentUploads();
	        
			//  create good-looking table tooltip
			StringBuilder tooltip = new StringBuilder(100);
            tooltip.append("<html><table>")
                   .append("<tr><td>")
                   .append(I18n.tr("Downloads:"))
                       .append("</td><td>")
                       .append(downloads)
                       .append("</td><td>@</td><td align=right>")
                       .append(sDown)
                       .append("</td></tr>")
                   .append("<tr><td>")
                   .append(I18n.tr("Uploads:"))
                       .append("</td><td>")
                       .append(uploads)
                       .append("</td><td>@</td><td align=right>")
                       .append(sUp)
                       .append("</td></tr>")
                   .append("<tr><td>")
                       .append(I18n.tr("Total Downstream:"))
                       .append("</td><td>")
                       .append(totalDown)
                       .append("</td></tr>")
                   .append("<tr><td>")
                       .append(I18n.tr("Total Upstream:"))
                       .append("</td><td>")
                       .append(totalUp)
                       .append("</td></tr>")
                   .append("</table></html>");
            return tooltip.toString();
		}
	}
}
