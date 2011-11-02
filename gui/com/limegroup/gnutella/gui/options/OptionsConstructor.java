package com.limegroup.gnutella.gui.options;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.limewire.setting.IntSetting;
import org.limewire.setting.SettingsGroupManager;
import org.limewire.util.OSUtils;

import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.DialogOption;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconTextField;
import com.limegroup.gnutella.gui.PaddedPanel;
import com.limegroup.gnutella.gui.options.panes.AbstractPaneItem;
import com.limegroup.gnutella.gui.options.panes.AssociationPreferencePaneItem;
import com.limegroup.gnutella.gui.options.panes.AudioPlayerPaneItem;
import com.limegroup.gnutella.gui.options.panes.AutoCompletePaneItem;
import com.limegroup.gnutella.gui.options.panes.AutomaticInstallerDownloadPaneItem;
import com.limegroup.gnutella.gui.options.panes.ShutdownPaneItem;
import com.limegroup.gnutella.gui.options.panes.SmartSearchDBPaneItem;
import com.limegroup.gnutella.gui.options.panes.TorrentGlobalSpeedPaneItem;
import com.limegroup.gnutella.gui.options.panes.TorrentConnectionPaneItem;
import com.limegroup.gnutella.gui.options.panes.TorrentDetailsPaneItem;
import com.limegroup.gnutella.gui.options.panes.BrowserPaneItem;
import com.limegroup.gnutella.gui.options.panes.BugsPaneItem;
import com.limegroup.gnutella.gui.options.panes.ChatCommunityPaneItem;
import com.limegroup.gnutella.gui.options.panes.ForceIPPaneItem;
import com.limegroup.gnutella.gui.options.panes.IgnoreResultsPaneItem;
import com.limegroup.gnutella.gui.options.panes.ImageViewerPaneItem;
import com.limegroup.gnutella.gui.options.panes.MaximumSearchesPaneItem;
import com.limegroup.gnutella.gui.options.panes.NetworkInterfacePaneItem;
import com.limegroup.gnutella.gui.options.panes.NotificationsPaneItem;
import com.limegroup.gnutella.gui.options.panes.LibraryPreferencePaneItem;
import com.limegroup.gnutella.gui.options.panes.PopupsPaneItem;
import com.limegroup.gnutella.gui.options.panes.ProxyLoginPaneItem;
import com.limegroup.gnutella.gui.options.panes.ProxyPaneItem;
import com.limegroup.gnutella.gui.options.panes.ShowPromoOverlaysPaneItem;
import com.limegroup.gnutella.gui.options.panes.StartupPaneItem;
import com.limegroup.gnutella.gui.options.panes.StatusBarBandwidthPaneItem;
import com.limegroup.gnutella.gui.options.panes.StatusBarConnectionQualityPaneItem;
import com.limegroup.gnutella.gui.options.panes.StatusBarFirewallPaneItem;
import com.limegroup.gnutella.gui.options.panes.TorrentSaveFolderPaneItem;
import com.limegroup.gnutella.gui.options.panes.TorrentSeedingSettingPaneItem;
import com.limegroup.gnutella.gui.options.panes.VideoPlayerPaneItem;
import com.limegroup.gnutella.gui.options.panes.iTunesPreferencePaneItem;
import com.limegroup.gnutella.gui.shell.FrostAssociations;
import com.limegroup.gnutella.gui.themes.ThemeSettings;
import com.limegroup.gnutella.settings.UISettings;

/**
 * This class constructs the options tree on the left side of the options dialog.
 * <p>
 * The panes that show up when a leaf in the tree is selected are created
 * lazily in {@link OptionsPaneFactory}.
 * <p>
 * If you want to add a new {@link OptionsPane}, 
 * add a call to {@link #addOption(String, String)} in the constructor here
 * and add the construction of the pane to 
 * {@link OptionsPaneFactory#createOptionsPane(String)}.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
public final class OptionsConstructor {
	/**
	 * Handle to the top-level <tt>JDialog</tt window that contains all
	 * of the other GUI components.
	 */
	private final JDialog DIALOG;

	/**
	 * Stored for convenience to allow using this in helper methods
	 * during construction.
	 */
	private final OptionsTreeManager TREE_MANAGER;
	
	/**
	 * Stored for convenience to allow using this in helper methods
	 * during construction.
	 */
	private final OptionsPaneManager PANE_MANAGER;

	private final JTextField filterTextField;

	static final String SAVE_KEY           = "OPTIONS_SAVE_MAIN_TITLE";
	static final String SAVE_BASIC_KEY     = "OPTIONS_SAVE_BASIC_MAIN_TITLE";
	static final String SAVE_ADVANCED_KEY  = "OPTIONS_SAVE_ADVANCED_MAIN_TITLE";
	public static final String SHARED_KEY         = "OPTIONS_SHARED_MAIN_TITLE";
	static final String SHARED_BASIC_KEY   = "OPTIONS_SHARED_BASIC_TITLE";
	static final String SHARED_ADVANCED_KEY = "OPTIONS_SHARED_ADVANCED_MAIN_TITLE";
	static final String SHARED_TYPES_KEY = "OPTIONS_SHARED_TYPES_MAIN_TITLE";
	static final String SPEED_KEY          = "OPTIONS_SPEED_MAIN_TITLE";
    static final String DOWNLOAD_KEY       = "OPTIONS_DOWNLOAD_MAIN_TITLE";
	static final String UPLOAD_KEY         = "OPTIONS_UPLOAD_MAIN_TITLE";
	static final String UPLOAD_BASIC_KEY   = "OPTIONS_UPLOAD_BASIC_MAIN_TITLE";
	static final String UPLOAD_SLOTS_KEY   = "OPTIONS_UPLOAD_SLOTS_MAIN_TITLE";
	static final String CONNECTIONS_KEY    = "OPTIONS_CONNECTIONS_MAIN_TITLE";
	static final String BITTORRENT_KEY     = "OPTIONS_BITTORRENT_MAIN_TITLE";
    static final String BITTORRENT_BASIC_KEY = "OPTIONS_BITTORRENT_BASIC_TITLE";
    static final String BITTORRENT_ADVANCED_KEY = "OPTIONS_BITTORRENT_ADVANCED_TITLE";
	static final String SHUTDOWN_KEY       = "OPTIONS_SHUTDOWN_MAIN_TITLE";
	static final String UPDATE_KEY         = "OPTIONS_UPDATE_MAIN_TITLE";
	static final String CHAT_KEY           = "OPTIONS_CHAT_MAIN_TITLE";
	static final String PLAYER_KEY         = "OPTIONS_PLAYER_MAIN_TITLE";
    static final String STATUS_BAR_KEY     = "OPTIONS_STATUS_BAR_MAIN_TITLE";
    static final String ITUNES_KEY		   = "OPTIONS_ITUNES_MAIN_TITLE";
    static final String ITUNES_IMPORT_KEY  = "OPTIONS_ITUNES_PREFERENCE_MAIN_TITLE";
    static final String ITUNES_DAAP_KEY    = "OPTIONS_ITUNES_DAAP_MAIN_TITLE";
	static final String POPUPS_KEY         = "OPTIONS_POPUPS_MAIN_TITLE";
	static final String BUGS_KEY           = "OPTIONS_BUGS_MAIN_TITLE";
	static final String APPS_KEY           = "OPTIONS_APPS_MAIN_TITLE";
	static final String SEARCH_KEY         = "OPTIONS_SEARCH_MAIN_TITLE";
	static final String SEARCH_LIMIT_KEY   = "OPTIONS_SEARCH_LIMIT_MAIN_TITLE";
	static final String SEARCH_QUALITY_KEY = "OPTIONS_SEARCH_QUALITY_MAIN_TITLE";
	static final String SEARCH_SPEED_KEY   = "OPTIONS_SEARCH_SPEED_MAIN_TITLE";
    public static final String CONTENT_FILTER_KEY = "OPTIONS_CONTENT_FILTER_MAIN_TITLE";
    static final String SEARCH_JUNK_KEY    = "OPTIONS_SEARCH_JUNK_MAIN_TITLE";
	static final String FILTERS_KEY        = "OPTIONS_FILTERS_MAIN_TITLE";
	static final String RESULTS_KEY        = "OPTIONS_RESULTS_MAIN_TITLE";
	static final String MESSAGES_KEY       = "OPTIONS_MESSAGES_MAIN_TITLE";
    static final String ADVANCED_KEY       = "OPTIONS_ADVANCED_MAIN_TITLE";
	static final String PREFERENCING_KEY   = "OPTIONS_PREFERENCING_MAIN_TITLE";
	static final String FIREWALL_KEY       = "OPTIONS_FIREWALL_MAIN_TITLE";
    static final String GUI_KEY            = "OPTIONS_GUI_MAIN_TITLE";
    static final String AUTOCOMPLETE_KEY   = "OPTIONS_AUTOCOMPLETE_MAIN_TITLE";
    static final String SHOW_PROMOTION_OVERLAYS_KEY = "OPTIONS_SHOW_PROMOTION_OVERLAYS_MAIN_TITLE";
    static final String STARTUP_KEY        = "OPTIONS_STARTUP_MAIN_TITLE";   
    static final String PROXY_KEY          = "OPTIONS_PROXY_MAIN_TITLE";
    static final String NETWORK_INTERFACE_KEY = "OPTIONS_NETWORK_INTERFACE_MAIN_TITLE";
    static final String ASSOCIATIONS_KEY = "OPTIONS_ASSOCIATIONS_MAIN_TITLE";
    static final String PERFORMANCE_KEY    = "OPTIONS_PERFORMANCE_MAIN_TITLE";
    static final String STORE_KEY          = "OPTIONS_STORE_MAIN_TITLE";
    static final String STORE_BASIC_KEY    = "OPTIONS_STORE_BASIC_MAIN_TITLE";
    static final String STORE_ADVANCED_KEY = "OPTIONS_STORE_ADVANCED_MAIN_TITLE";
    
	
	/**
	 * The constructor create all of the options windows and their
	 * components.
	 *
	 * @param treeManager the <tt>OptionsTreeManager</tt> instance to
	 *                    use for constructing the main panels and
	 *                    adding elements
	 * @param paneManager the <tt>OptionsPaneManager</tt> instance to
	 *                    use for constructing the main panels and
	 *                    adding elements
	 */
	public OptionsConstructor(final OptionsTreeManager treeManager, 
			final OptionsPaneManager paneManager) {
		TREE_MANAGER = treeManager;
		PANE_MANAGER = paneManager;
		final String title = I18n.tr("Options");
        final boolean shouldBeModal = !OSUtils.isMacOSX();

		DIALOG = new JDialog(GUIMediator.getAppFrame(), title, shouldBeModal);
		DIALOG.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		GUIUtils.addHideAction((JComponent)DIALOG.getContentPane());

		if (UISettings.UI_OPTIONS_DIALOG_HEIGHT.getValue() < UISettings.UI_OPTIONS_DIALOG_HEIGHT.getDefaultValue()) {
			UISettings.UI_OPTIONS_DIALOG_HEIGHT.revertToDefault();
		}
		
		if (UISettings.UI_OPTIONS_DIALOG_WIDTH.getValue() < UISettings.UI_OPTIONS_DIALOG_WIDTH.getDefaultValue()) {
			UISettings.UI_OPTIONS_DIALOG_WIDTH.revertToDefault();
		}
		
		DialogSizeSettingUpdater.install(DIALOG, UISettings.UI_OPTIONS_DIALOG_WIDTH,
		        UISettings.UI_OPTIONS_DIALOG_HEIGHT);

		// most Mac users expect changes to be saved when the window
		// is closed, so save them
		DIALOG.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
				    DialogOption answer = null;
				    if(OptionsMediator.instance().isDirty()) {
				        answer = GUIMediator.showYesNoCancelMessage(I18n.tr("You have made changes to some of FrostWire's settings. Would you like to save these changes?"));
				        if(answer == DialogOption.YES) {
				            OptionsMediator.instance().applyOptions();
					        SettingsGroupManager.instance().save();
					    }
                    }
                    if(answer != DialogOption.CANCEL) {
                        DIALOG.dispose();
						OptionsMediator.instance().disposeOptions();
                    }
				} catch(IOException ioe) {
					// nothing we should do here.  a message should
					// have been displayed to the user with more
					// information
				}
			}
        });

		PaddedPanel mainPanel = new PaddedPanel();

		Box splitBox = new Box(BoxLayout.X_AXIS);

		BoxPanel treePanel = new BoxPanel(BoxLayout.Y_AXIS);
		
		BoxPanel filterPanel = new BoxPanel(BoxLayout.X_AXIS);
		treePanel.add(filterPanel);
		
		
		IconTextField iconTextField = new IconTextField(GUIMediator.getThemeImage("browse_host_generic"), 10);
		filterTextField = iconTextField.getTextField();
		
        // set text before adding the document listener
        filterTextField.setText(I18n.tr("Search here"));
        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent event) {
				filter();
			}
			public void insertUpdate(DocumentEvent e) {
				filter();
			}
			public void removeUpdate(DocumentEvent e) {
				filter();
			}
		});
        filterTextField.setForeground(Color.lightGray);
        filterTextField.addFocusListener(new FocusAdapter() {
            private boolean initialized = false;
            @Override
            public void focusGained(FocusEvent e) {
                if (initialized) {
                    return;
                }
                filterTextField.setForeground(UIManager.getColor("TextField.foreground"));
                filterTextField.setText("");
                initialized = true;
            }
        });
		filterPanel.add(iconTextField);

		filterPanel.add(Box.createHorizontalStrut(2));
		
		treePanel.add(Box.createVerticalStrut(3));
		
		Component treeComponent = TREE_MANAGER.getComponent();
		treePanel.add(treeComponent);
		
		Component paneComponent = PANE_MANAGER.getComponent();

		splitBox.add(treePanel);
		splitBox.add(paneComponent);
		mainPanel.add(splitBox);

		mainPanel.add(Box.createVerticalStrut(17));
		mainPanel.add(new OptionsButtonPanel().getComponent());

		DIALOG.getContentPane().add(mainPanel);

		OptionsTreeNode node = initializePanels();	
		PANE_MANAGER.show(node);
	}

    @SuppressWarnings({ "unchecked" })
    private OptionsTreeNode initializePanels() {
        //bittorrent
    	addGroupTreeNode(OptionsMediator.ROOT_NODE_KEY,BITTORRENT_KEY, I18n.tr("BitTorrent")); 
    	OptionsTreeNode node = addOption(BITTORRENT_KEY,BITTORRENT_BASIC_KEY,I18n.tr("Basic"),TorrentSaveFolderPaneItem.class, TorrentSeedingSettingPaneItem.class, TorrentDetailsPaneItem.class);
    	addOption(BITTORRENT_KEY,BITTORRENT_ADVANCED_KEY,I18n.tr("Advanced"),TorrentGlobalSpeedPaneItem.class, TorrentConnectionPaneItem.class);

        //community chat
		addOption(OptionsMediator.ROOT_NODE_KEY, CHAT_KEY, I18n.tr("Community Chat"), ChatCommunityPaneItem.class);

		// library
        addOption(OptionsMediator.ROOT_NODE_KEY, PLAYER_KEY, I18n.tr("Library"), LibraryPreferencePaneItem.class);    
        
        //status bar
        addOption(OptionsMediator.ROOT_NODE_KEY, STATUS_BAR_KEY, I18n.tr("Status Bar"), StatusBarConnectionQualityPaneItem.class, StatusBarFirewallPaneItem.class, StatusBarBandwidthPaneItem.class); // Removed Lime Store
        
        //itunes
        if (OSUtils.isMacOSX() || OSUtils.isWindows()) {
            addGroupTreeNode(OptionsMediator.ROOT_NODE_KEY, ITUNES_KEY, I18n.tr("iTunes"));
			addOption(ITUNES_KEY, ITUNES_IMPORT_KEY, I18n.tr("Importing"), iTunesPreferencePaneItem.class); 
		}
		
		if (!OSUtils.isWindows() && !OSUtils.isAnyMac()) {
			addOption(OptionsMediator.ROOT_NODE_KEY, APPS_KEY, I18n.tr("Helper Apps"), BrowserPaneItem.class, ImageViewerPaneItem.class, VideoPlayerPaneItem.class, AudioPlayerPaneItem.class);
		}

		addOption(OptionsMediator.ROOT_NODE_KEY, BUGS_KEY, I18n.tr("Bug Reports"), BugsPaneItem.class);

		//view options
		addOption(OptionsMediator.ROOT_NODE_KEY, GUI_KEY, I18n.tr("View"), PopupsPaneItem.class, NotificationsPaneItem.class, ShowPromoOverlaysPaneItem.class, AutoCompletePaneItem.class);

		// search options
		addOption(OptionsMediator.ROOT_NODE_KEY, SEARCH_KEY, I18n.tr("Searching"), MaximumSearchesPaneItem.class, SmartSearchDBPaneItem.class);
        
		// filter options
		addGroupTreeNode(OptionsMediator.ROOT_NODE_KEY, FILTERS_KEY, I18n.tr("Filters"));
		addOption(FILTERS_KEY, RESULTS_KEY, I18n.tr("Keywords"), IgnoreResultsPaneItem.class); 
        
		// advanced options
		addGroupTreeNode(OptionsMediator.ROOT_NODE_KEY, ADVANCED_KEY, I18n.tr("Advanced"));        
		addOption(ADVANCED_KEY, PREFERENCING_KEY, I18n.tr("Updates"), AutomaticInstallerDownloadPaneItem.class);
		addOption(ADVANCED_KEY, FIREWALL_KEY, I18n.tr("Firewall"), ForceIPPaneItem.class);
		addOption(ADVANCED_KEY, PROXY_KEY, I18n.tr("Proxy"), ProxyPaneItem.class, ProxyLoginPaneItem.class);
        addOption(ADVANCED_KEY, NETWORK_INTERFACE_KEY, I18n.tr("Network Interface"), NetworkInterfacePaneItem.class);
        if (FrostAssociations.anyAssociationsSupported()) {
        	addOption(ADVANCED_KEY, ASSOCIATIONS_KEY, I18n.tr("File Associations"), AssociationPreferencePaneItem.class);
        }
              
        if (GUIUtils.shouldShowStartOnStartupWindow()) {
            addOption(ADVANCED_KEY, STARTUP_KEY, I18n.tr("System Boot"), StartupPaneItem.class); 
        }
        
        addOption(OptionsMediator.ROOT_NODE_KEY, SHUTDOWN_KEY, I18n.tr("System Tray"), ShutdownPaneItem.class);
        return node;
	}
	
	/**
	 * Adds a parent node to the tree.  This node serves navigational
	 * purposes only, and so has no corresponding <tt>OptionsPane</tt>.
	 * This method allows for multiple tiers of parent nodes, not only
	 * top-level parents.
	 *
	 * @param parentKey the key of the parent node to add this parent
	 *                  node to
	 * @param childKey the key of the new parent node that is a child of
	 *                 the <tt>parentKey</tt> argument
	 */
	private final void addGroupTreeNode(final String parentKey,
			final String childKey, String label) {
		TREE_MANAGER.addNode(parentKey, childKey, label, label);
	}

	/**
	 * Adds the specified key and <tt>OptionsPane</tt> to current set of
	 * options. This adds this <tt>OptionsPane</tt> to the set of
	 * <tt>OptionsPane</tt>s the user can select.
	 * 
	 * @param parentKey the key of the parent node to add the new node to
	 */
	private final OptionsTreeNode addOption(final String parentKey, final String childKey,
	        final String label, Class<? extends AbstractPaneItem>... clazzes) {
		StringBuilder sb = new StringBuilder();
	    sb.append(label);
				sb.append(" ");
	    sb.append(extractLabels(clazzes));

	    OptionsTreeNode node = TREE_MANAGER.addNode(parentKey, childKey, label, sb.toString()); 
	    node.setClasses(clazzes);
	    return node;	    
	}

	private String extractLabels(Class<?>... clazzes) {
	    StringBuilder sb = new StringBuilder();
	    for (Class<?> clazz : clazzes) {
	        Field[] fields = clazz.getFields();
	        for (Field field : fields) {
	            if ((field.getModifiers() & Modifier.FINAL) != 0 && field.getType() == String.class) {
	                try {
	                    sb.append(field.get(null));
				sb.append(" ");
	                } catch (Exception e) {
	                    // ignore
			}
		}
	}
	    }
	    return sb.toString();
	}

	/**
	 * Makes the options window either visible or not visible depending on the
	 * boolean argument.
	 *
	 * @param visible <tt>boolean</tt> value specifying whether the options
	 *				window should be made visible or not visible
	 * @param key the unique identifying key of the panel to show
	 */
	final void setOptionsVisible(boolean visible, final String key) {
	    if(!visible) {
	        DIALOG.dispose();
			OptionsMediator.instance().disposeOptions();
        } else {
            GUIUtils.centerOnScreen(DIALOG);
			//  initial tree selection
			if (key == null)
				TREE_MANAGER.setDefaultSelection();
			else
				TREE_MANAGER.setSelection(key);

            // make tree component the default component instead of the search field
			TREE_MANAGER.getComponent().requestFocusInWindow();
            
    		DIALOG.setVisible(true);
        }
	}	
	
	/** Returns if the Options Box is visible.
	 *  @return true if the Options Box is visible.
	 */
	public final boolean isOptionsVisible() {
		return DIALOG.isVisible();
	}

	/**
	 * Returns the main <tt>JDialog</tt> instance for the options window,
	 * allowing other components to position themselves accordingly.
	 *
	 * @return the main options <tt>JDialog</tt> window
	 */
	JDialog getMainOptionsComponent() {
		return DIALOG;
	}
	
	private void filter() {
		TREE_MANAGER.setFilterText(filterTextField.getText());
	}

	/**
	 * Inner class that computes meaningful default dialog sizes for the options
	 * dialog for different font size increments.
	 * 
	 * It also updates the width and height setting if the user changes the dialog
	 * size manually.
	 */
	public static class DialogSizeSettingUpdater {

	    public static void install(JDialog dialog, IntSetting widthSetting, IntSetting heightSetting) {
	        int increment = ThemeSettings.FONT_SIZE_INCREMENT.getValue();
	        int width = widthSetting.isDefault() ? getWidthForFontIncrement(widthSetting, dialog, increment) : widthSetting.getValue();
	        int height = heightSetting.isDefault() ? getHeightForFontIncrement(heightSetting, dialog, increment) : heightSetting.getValue();
	        dialog.setSize(width, height);
	        dialog.addComponentListener(new SizeChangeListener(widthSetting, heightSetting));
	    }
	    
	    private static int getWidthForFontIncrement(IntSetting widthSetting, Component component, int increment) {
	        if (increment > 0) { 
	            return widthSetting.getValue() + 20 * increment + 4 * increment^2;
	        }
	        return widthSetting.getValue();
	    }
	    
	    private static int getHeightForFontIncrement(IntSetting heightSetting, Component component, int increment) {
	        if (increment > 0) {
	            return heightSetting.getValue() + 10 * increment + 18 * increment^2;
	        }
	        return heightSetting.getValue();
	    }
	    
	    private static class SizeChangeListener extends ComponentAdapter {
	        
	        private IntSetting widthSetting;
	        private IntSetting heightSetting;
	        
	        public SizeChangeListener(IntSetting widthSetting, IntSetting heightSetting) {
	            this.widthSetting = widthSetting;
	            this.heightSetting = heightSetting;
	        }
	        
	        @Override
	        public void componentResized(ComponentEvent e) {
	            int increment = ThemeSettings.FONT_SIZE_INCREMENT.getValue();
	            Component c = e.getComponent();
	            if (c.getWidth() != getWidthForFontIncrement(widthSetting, c, increment)
	                    || c.getHeight() != getHeightForFontIncrement(heightSetting, c, increment)) {
	                widthSetting.setValue(c.getWidth());
	                heightSetting.setValue(c.getHeight());
	            }
	        }
	    }
	}
}
