package com.limegroup.gnutella.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.plaf.TabbedPaneUI;

import org.limewire.setting.SettingsGroupManager;
import org.limewire.util.OSUtils;

import com.frostwire.gui.ChatMediator;
import com.frostwire.gui.bittorrent.BTDownloadMediator;
import com.frostwire.gui.library.LibraryMediator;
import com.frostwire.gui.tabs.ChatTab;
import com.frostwire.gui.tabs.LibraryTab;
import com.frostwire.gui.tabs.SearchDownloadTab;
import com.frostwire.gui.tabs.Tab;
import com.limegroup.gnutella.gui.dnd.DNDUtils;
import com.limegroup.gnutella.gui.dnd.TransferHandlerDropTargetListener;
import com.limegroup.gnutella.gui.menu.MenuMediator;
import com.limegroup.gnutella.gui.options.OptionsMediator;
import com.limegroup.gnutella.gui.search.MagnetClipboardListener;
import com.limegroup.gnutella.gui.search.SearchMediator;
import com.limegroup.gnutella.gui.themes.SkinCustomColors;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;
import com.limegroup.gnutella.settings.ApplicationSettings;

/**
 * This class constructs the main <tt>JFrame</tt> for the program as well as 
 * all of the other GUI classes.  
 */
public final class MainFrame implements RefreshListener, ThemeObserver {

    /**
     * Handle to the <tt>JTabbedPane</tt> instance.
     */
    private JTabbedPane TABBED_PANE;

    /**
     * Constant handle to the <tt>SearchMediator</tt> class that is
     * responsible for displaying search results to the user.
     */
    private SearchMediator SEARCH_MEDIATOR;

    private BTDownloadMediator BT_DOWNLOAD_MEDIATOR;

    /**
     * Constant handle to the <tt>LibraryView</tt> class that is
     * responsible for displaying files in the user's repository.
     */
    private LibraryMediator LIBRARY_MEDIATOR;

    private ChatMediator CHAT_MEDIATOR;
    
    /**
     * Constant handle to the <tt>OptionsMediator</tt> class that is
     * responsible for displaying customizable options to the user.
     */
    private OptionsMediator OPTIONS_MEDIATOR;

    /**
     * Constant handle to the <tt>StatusLine</tt> class that is
     * responsible for displaying the status of the network and
     * connectivity to the user.
     */
    private StatusLine STATUS_LINE;

    /**
     * Handle the <tt>MenuMediator</tt> for use in changing the menu
     * depending on the selected tab.
     */
    private MenuMediator MENU_MEDIATOR;

    /**
     * The main <tt>JFrame</tt> for the application.
     */
    private final JFrame FRAME;

    /**
     * Constant for the <tt>LogoPanel</tt> used for displaying the
     * lime/spinning lime search status indicator and the logo.
     */
    private LogoPanel LOGO_PANEL;

    /**
     * The array of tabs in the main application window.
     */
    private Map<GUIMediator.Tabs, Tab> TABS = new HashMap<GUIMediator.Tabs, Tab>(7);
    
    /**
     * The last state of the X/Y location and the time it was set.
     * This is necessary to preserve the maximize size & prior size,
     * as on Windows a move event is occasionally triggered when
     * maximizing, prior to the state actually becoming maximized.
     */
    private WindowState lastState = null;
    
    /** simple state. */
    private static class WindowState {
        private final int x;
        private final int y;
        private final long time;
        WindowState() {
            x = ApplicationSettings.WINDOW_X.getValue();
            y = ApplicationSettings.WINDOW_Y.getValue();
            time = System.currentTimeMillis();
        }
    }

    /** 
     * Initializes the primary components of the main application window,
     * including the <tt>JFrame</tt> and the <tt>JTabbedPane</tt>
     * contained in that window.
     */
    MainFrame(JFrame frame) {
        //starts the Frostwire update manager, and will trigger a task in 5 seconds.
        // RELEASE
        com.frostwire.gui.updates.UpdateManager.scheduleUpdateCheckTask(0);
        
        // DEBUG
        //com.frostwire.gui.updates.UpdateManager.scheduleUpdateCheckTask(0,"http://update1.frostwire.com/example.php");

        FRAME = frame;
        new DropTarget(FRAME, new TransferHandlerDropTargetListener(DNDUtils.DEFAULT_TRANSFER_HANDLER));

        TABBED_PANE = new JTabbedPane();
        TABBED_PANE.putClientProperty(SkinCustomColors.CLIENT_PROPERTY_LIGHT_NOISE, true);
        
        // Add a listener for saving the dimensions of the window &
        // position the search icon overlay correctly.
        FRAME.addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent e) {}
            
            public void componentShown(ComponentEvent e) {
            }
            
            public void componentMoved(ComponentEvent e) {
                lastState = new WindowState();
                saveWindowState();
            }

            public void componentResized(ComponentEvent e) {
                saveWindowState();
            }
        });

        // Listen for the size/state changing.
        FRAME.addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                saveWindowState();
            }
        });
 
        // Listen for the window closing, to save settings.
        FRAME.addWindowListener(new WindowAdapter() {
            public void windowDeiconified(WindowEvent e) {
                // Handle reactivation on systems which do not support
                // the system tray.  Windows systems call the
                // WindowsNotifyUser.restoreApplication()
                // method to restore applications from minimize and
                // auto-shutdown modes.  Non-windows systems restore
                // the application using the following code.
                if(!OSUtils.supportsTray() || !ResourceManager.instance().isTrayIconAvailable())
                    GUIMediator.restoreView();
            }

            public void windowClosing(WindowEvent e) {
                saveWindowState();
                SettingsGroupManager.instance().save();
                GUIMediator.close(true);
            }

        });

        FRAME.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setFrameDimensions();

        FRAME.setJMenuBar(getMenuMediator().getMenuBar());
        JPanel contentPane = new JPanel();
        
        FRAME.setContentPane(contentPane);
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        int logoTopPadding = (OSUtils.isMacOSX()) ? 2 : 0;
        
        //ADD LOGO
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = new Insets(logoTopPadding,0,0,5); //padding
        gbc.anchor = GridBagConstraints.NORTHEAST;
        LOGO_PANEL = new LogoPanel();
        contentPane.add(LOGO_PANEL, gbc);
        
        //ADD TABBED PANE
        gbc = new GridBagConstraints();
        gbc.gridwidth = 2; //spans all the way
        gbc.gridx = 0;
        gbc.gridy = 0;	
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipady = 100;
        contentPane.add(TABBED_PANE, gbc);
        
        //ADD STATUS LINE
        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(getStatusLine().getComponent(), gbc);

        ThemeMediator.addThemeObserver(this);
        GUIMediator.addRefreshListener(this);

        if (ApplicationSettings.MAGNET_CLIPBOARD_LISTENER.getValue()) {
            FRAME.addWindowListener(MagnetClipboardListener.getInstance());
        }
        
        PowerManager pm = new PowerManager();
        FRAME.addWindowListener(pm);
        GUIMediator.addRefreshListener(pm);
        
        //GuiCoreMediator.getCoreBackgroundExecutor().execute(new Runnable() {
        //	public void run() {
        //		GuiFrostWireUtils.verifySharedTorrentFolderCorrecteness();
        //	}
        //});
    }
    
    /** Saves the state of the Window to settings. */
    void saveWindowState() {
        int state = FRAME.getExtendedState();
        if(state == Frame.NORMAL) {
            // save the screen size and location 
            Dimension dim = GUIMediator.getAppSize();
            if((dim.height > 100) && (dim.width > 100)) {
                Point loc = GUIMediator.getAppLocation();
                ApplicationSettings.APP_WIDTH.setValue(dim.width);
                ApplicationSettings.APP_HEIGHT.setValue(dim.height);
                ApplicationSettings.WINDOW_X.setValue(loc.x);
                ApplicationSettings.WINDOW_Y.setValue(loc.y);
                ApplicationSettings.MAXIMIZE_WINDOW.setValue(false);
            }
        } else if( (state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            ApplicationSettings.MAXIMIZE_WINDOW.setValue(true);
            if(lastState != null && lastState.time == System.currentTimeMillis()) {
                ApplicationSettings.WINDOW_X.setValue(lastState.x);
                ApplicationSettings.WINDOW_Y.setValue(lastState.y);
                lastState = null;
            }
        }
    }

    // inherit doc comment
    public void updateTheme() {
        FRAME.setJMenuBar(getMenuMediator().getMenuBar());
        //LOGO_PANEL.updateTheme();
        //setSearchIconLocation();
        //updateLogoHeight();
        for(GUIMediator.Tabs tab : GUIMediator.Tabs.values())
            updateTabIcon(tab);
	}
    
    /**
     * Build the Tab Structure based on advertising mode and Windows
     */
    public void buildTabs() {
    	//Enable right click on Tabs to hide/show tabs
    	TABBED_PANE.addMouseListener(com.frostwire.gui.tabs.TabRightClickAdapter.getInstance());
    	
    	SEARCH_MEDIATOR = new SearchMediator();
        
    	TABS.put(GUIMediator.Tabs.SEARCH, new SearchDownloadTab(SEARCH_MEDIATOR, getBTDownloadMediator()));
        TABS.put(GUIMediator.Tabs.LIBRARY, new LibraryTab(getLibraryMediator()));
        TABS.put(GUIMediator.Tabs.CHAT, new ChatTab(getChatMediator()));
	    
	    TABBED_PANE.setPreferredSize(new Dimension(10000, 10000));
	    
	    // listener for updating the tab's titles & tooltips.
        PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Tab tab = (Tab)evt.getSource();
                int idx = getTabIndex(tab);
                if(idx != -1) {
                    if("title".equals(evt.getPropertyName()))
                        TABBED_PANE.setTitleAt(idx, (String)evt.getNewValue());
                    else if("tooltip".equals(evt.getPropertyName()))
                        TABBED_PANE.setToolTipTextAt(idx, (String)evt.getNewValue());
                }
            }
        };
        
        // add all tabs initially....
        for(GUIMediator.Tabs tab : GUIMediator.Tabs.values()) {
            Tab t = TABS.get(tab);
            if(t != null) {
                this.addTab(t);
                t.addPropertyChangeListener(propertyChangeListener);
            }
        }

        TABBED_PANE.setRequestFocusEnabled(false);

        TABBED_PANE.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                TabbedPaneUI ui = TABBED_PANE.getUI();
                int idx = ui.tabForCoordinate(TABBED_PANE, e.getX(), e.getY());
                if(idx != -1) {
                    Tab tab = getTabForIndex(idx);
                    if(tab != null)
                        tab.mouseClicked();
                }

            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
        });          

        // remove tabs according to Settings Manager...
        if (!ApplicationSettings.LIBRARY_VIEW_ENABLED.getValue())
            this.setTabVisible(GUIMediator.Tabs.LIBRARY, false);
        if (!ApplicationSettings.CHAT_VIEW_ENABLED.getValue())
            this.setTabVisible(GUIMediator.Tabs.CHAT, false);

    }

    
    /**
     * Adds a tab to the <tt>JTabbedPane</tt> based on the data supplied
     * in the <tt>Tab</tt> instance.
     *
     * @param tab the <tt>Tab</tt> instance containing data for the tab to
     *  add
     */
    private void addTab(Tab tab) {
        TABBED_PANE.addTab(tab.getTitle(), tab.getIcon(),
                           tab.getComponent(), tab.getToolTip());
    }

    /**
     * Inserts a tab in the <tt>JTabbedPane</tt> at the specified index, 
     * based on the data supplied in the <tt>Tab</tt> instance.
     *
     * @param tab the <tt>Tab</tt> instance containing data for the tab to
     *  add
     */
    private void insertTab(Tab tab, int index) {
        TABBED_PANE.insertTab(tab.getTitle(), tab.getIcon(),
                              tab.getComponent(), tab.getToolTip(),
                              index);
        // the component tree must be updated so that the new tab
        // fits the current theme (if the theme was changed at runtime)
        SwingUtilities.updateComponentTreeUI(TABBED_PANE);
        ThemeMediator.updateThemeObservers();
    }

    /**
     * Sets the selected index in the wrapped <tt>JTabbedPane</tt>.
     *
     * @param index the tab index to select
     */
    public final void setSelectedTab(GUIMediator.Tabs tab) {
        int i = getTabIndex(tab);
        if (i == -1)
            return;
        TABBED_PANE.setSelectedIndex(i);
    }
    
    public final GUIMediator.Tabs getSelectedTab() {
        Tab tab = getTabForIndex(TABBED_PANE.getSelectedIndex());
        for (Entry<GUIMediator.Tabs, Tab> entry : TABS.entrySet()) {
            if (entry.getValue() == tab) {
                return entry.getKey();
            }
        }
        
        return null;
    }

    /** Updates the icon in a tab. */
    void updateTabIcon(GUIMediator.Tabs tab) {
        int i = getTabIndex(tab);
        if (i != -1) {
            Tab t = TABS.get(tab);
            if(t != null)
                TABBED_PANE.setIconAt(i, t.getIcon());
        }
    }

    /**
     * Sets the x,y location as well as the height and width of the main
     * application <tt>Frame</tt>.
     */
    private final void setFrameDimensions() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        int locX = 0;
        int locY = 0;

        int appWidth  = Math.min(screenSize.width-insets.left-insets.right, ApplicationSettings.APP_WIDTH.getValue());
        int appHeight = Math.min(screenSize.height-insets.top-insets.bottom, ApplicationSettings.APP_HEIGHT.getValue());
        
        // Set the location of our window based on whether or not
        // the user has run the program before, and therefore may have 
        // modified the location of the main window.
        if(ApplicationSettings.RUN_ONCE.getValue()) {
            locX = Math.max(insets.left, ApplicationSettings.WINDOW_X.getValue());
            locY = Math.max(insets.top, ApplicationSettings.WINDOW_Y.getValue());
        } else {
            locX = (screenSize.width - appWidth) / 2;
            locY = (screenSize.height - appHeight) / 2;
        }
        
        // Make sure the Window is visible and not for example 
        // somewhere in the very bottom right corner.
        if (locX+appWidth > screenSize.width) {
            locX = Math.max(insets.left, screenSize.width - insets.left - insets.right - appWidth);
        }
        
        if (locY+appHeight > screenSize.height) {
            locY = Math.max(insets.top, screenSize.height - insets.top - insets.bottom - appHeight);
        }
        
        FRAME.setLocation(locX, locY);
        FRAME.setSize(new Dimension(appWidth, appHeight));
        FRAME.getContentPane().setSize(new Dimension(appWidth, appHeight));
        ((JComponent)FRAME.getContentPane()).setPreferredSize(new Dimension(appWidth, appHeight));
        
        //re-maximize if we shutdown while maximized.
        if(ApplicationSettings.MAXIMIZE_WINDOW.getValue() 
                && Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
            FRAME.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
    }


    /**
     * Sets the visible/invisible state of the tab associated with the
     * specified index.  The indices correspond to the order of the
     * tabs whether or not they are visible, as specified in 
     * <tt>GUIMediator</tt>.
     *
     * @param TAB_INDEX the index of the tab to make visible or 
     *  invisible
     * @param VISIBLE the visible/invisible state to set the tab to
     */
    void setTabVisible(GUIMediator.Tabs tabItem, boolean visible) {
        Tab tab = TABS.get(tabItem);
        Component comp = tab.getComponent();
        int tabCount = TABBED_PANE.getTabCount();
        
        if (!visible) {
            // remove the tab from the tabbed pane
            for (int i = 0; i < tabCount; i++) {
                if (comp.equals(TABBED_PANE.getComponentAt(i))) {
                    TABBED_PANE.remove(i);
                    break;
                }
            }
        } else {
            // make sure the current one is invisible.
       //     JComponent selComp =
       //         (JComponent)TABBED_PANE.getSelectedComponent();
       //     selComp.setVisible(false);
            
            // We need to insert the tab in the right order,
            // according to the ordinal value of the enum.
            // To do this, we iterate through the visible tabs
            // and insert the new tab once we encounter
            // a visible tab whose 'Tab' counterpart has an
            // ordinal higher than ours.
            // (If we reached the end of the visible tabs
            //  without finding a higher ordinal, we insert
            //  at the end.)
            
            
            int ordinal = tabItem.ordinal();
            
            // add the tab to the tabbed pane
            for (int i = 0; i < tabCount; i++) {                
                Component c = TABBED_PANE.getComponentAt(i);
                int o = getOrdinalForTabComponent(c);
                if(o > ordinal) { // reached a higher tab
                    insertTab(tab, i);
                    break;
                } else if(i == tabCount - 1) { // at end of list
                    insertTab(tab, i+1);
                }
            }
            
            JComponent jcomp = (JComponent)comp;
            jcomp.invalidate();
            jcomp.revalidate();
            jcomp.repaint();
        }

        tabItem.setEnabled(visible);
        tab.storeState(visible);
    }
    
    /**
     * Returns the ordinal of the enum that points to the tab
     * holding the given component.
     */
    private int getOrdinalForTabComponent(Component c) {
        for(Map.Entry<GUIMediator.Tabs, Tab> entry : TABS.entrySet()) {
            if(entry.getValue().getComponent().equals(c))
                return entry.getKey().ordinal();
        }
        return -1;
    }

    /**
     * Returns the index in the tabbed pane of the specified "real" index
     * argument.  The values for this argument are listed in
     * <tt>GUIMediator</tt>.
     *
     * @param index the "real" index of the tab, meaning that this index
     *  is independent of what is currently visible in the tab
     * @return the index in the tabbed pane of the specified real index,
     *  or -1 if the specified index is not found
     */
    private int getTabIndex(GUIMediator.Tabs tab) {
        Tab t = TABS.get(tab);
        if(t != null) {
            return getTabIndex(t);
        } else {
            return -1;
        }
    }
    
    private int getTabIndex(Tab tab) {
        int tabCount = TABBED_PANE.getTabCount();
        Component comp = tab.getComponent();
        for (int i = 0; i < tabCount; i++) {
            Component tabComp = TABBED_PANE.getComponentAt(i);
            if (tabComp.equals(comp))
                return i;
        }
        return -1;
    }
    
    /**
     * Returns the tab associated with the visual index.
     * 
     * @param idx
     * @return
     */
    private Tab getTabForIndex(int idx) {
       Component c = TABBED_PANE.getComponentAt(idx);
       if(c == null)
           return null;
       
       for(Tab tab : TABS.values()) {
           if(tab.getComponent() != null && tab.getComponent().equals(c))
               return tab;
       }
       
       return null;
    }
    
    /**
     * Should be called whenever state may have changed, so MainFrame can then
     * re-layout window (if necessary).
     */
    public void refresh() {
    }
    
    final BTDownloadMediator getBTDownloadMediator() {
        if (BT_DOWNLOAD_MEDIATOR == null) {
            BT_DOWNLOAD_MEDIATOR = BTDownloadMediator.instance();
        }
        return BT_DOWNLOAD_MEDIATOR;
    }
    
    /**
     * Returns a reference to the <tt>LibraryMediator</tt> instance.
     *
     * @return a reference to the <tt>LibraryMediator</tt> instance
     */
    final com.frostwire.gui.library.LibraryMediator getLibraryMediator() {
        if (LIBRARY_MEDIATOR == null) {
            LIBRARY_MEDIATOR = LibraryMediator.instance();
        }
        return LIBRARY_MEDIATOR;
    }

    final ChatMediator getChatMediator() {
        if (CHAT_MEDIATOR == null) {
            CHAT_MEDIATOR = ChatMediator.instance();
        }
        return CHAT_MEDIATOR;
    }

    /**
     * Returns a reference to the <tt>StatusLine</tt> instance.
     *
     * @return a reference to the <tt>StatusLine</tt> instance
     */
    final StatusLine getStatusLine() {
        if (STATUS_LINE == null) {
            STATUS_LINE = new StatusLine();
        }
        return STATUS_LINE;
    }

    /**
     * Returns a reference to the <tt>MenuMediator</tt> instance.
     *
     * @return a reference to the <tt>MenuMediator</tt> instance
     */
    public final MenuMediator getMenuMediator() {
        if (MENU_MEDIATOR == null) {
            MENU_MEDIATOR = MenuMediator.instance();
        }
        return MENU_MEDIATOR;
    }

    /**
     * Returns a reference to the <tt>OptionsMediator</tt> instance.
     *
     * @return a reference to the <tt>OptionsMediator</tt> instance
     */
    final OptionsMediator getOptionsMediator() {
        if (OPTIONS_MEDIATOR == null) {
            OPTIONS_MEDIATOR = OptionsMediator.instance();
        }
        return OPTIONS_MEDIATOR;
    }

    /**
     * Sets the searching or not searching status of the application.
     *
     * @param searching the searching status of the application
     */
    public final void setSearching(boolean searching) {    
        LOGO_PANEL.setSearching(searching);
		refresh();
    }
}
