package com.limegroup.gnutella.gui.search;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;

import org.limewire.io.NetworkInstanceUtils;
import org.limewire.io.NetworkUtils;

import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.NetworkManager;
import com.limegroup.gnutella.gui.AutoCompleteTextField;
import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.ClearableAutoCompleteTextField;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.KeyProcessingTextField;
import com.limegroup.gnutella.gui.MySharedFilesButton;
import com.limegroup.gnutella.gui.actions.FileMenuActions;
import com.limegroup.gnutella.gui.actions.FileMenuActions.OpenMagnetTorrentAction;
import com.limegroup.gnutella.gui.themes.SkinHandler;
import com.limegroup.gnutella.gui.themes.ThemeSettings;
import com.limegroup.gnutella.gui.xml.InputPanel;
import com.limegroup.gnutella.settings.SearchSettings;
import com.limegroup.gnutella.xml.LimeXMLSchema;

/**
 * Inner panel that switches between the various kinds of
 * searching.
 */
class SearchInputPanel extends JPanel {
    
    /**
     * 
     */
    private static final long serialVersionUID = -5638062215253666235L;

    /**
     * The current search label in normal search.
     */
    private final JLabel SEARCH_TYPE_LABEL = new JLabel();
    
    /**
     * The sole input text field that is at the top of all searches.
     */
    private final SearchField SEARCH_FIELD = new SearchField(14);
    
    /**
     * The text area that contains the information about direct-connecting
     * to this host.
     */
    private final JTextArea IP_TEXT = new JTextArea();
    
    /**
     * The input field for browse-host searches
     */
    private final AutoCompleteTextField BROWSE_HOST_FIELD =
        new ClearableAutoCompleteTextField();
    
    
    /**
     * The JTabbedPane that switches between types of searches.
     */
    private final JTabbedPane PANE = new JTabbedPane(JTabbedPane.BOTTOM);
        
    /**
     * The CardLayout that switches between the detailed
     * search input information for each meta-type.
     */
    private final CardLayout META_CARDS = new CardLayout();
    
    /**
     * The panel that the META_CARDS layout uses to layout
     * the detailed search input fields.
     */
    private final JPanel META_PANEL = new JPanel(META_CARDS);
    
    /**
     * The name to use for the default panel that has no meta-data.
     */
    private static final String DEFAULT_PANEL_KEY = "defaultPanel";
    
    /**
     * The box that holds the schemas for searching.
     */
    
    /**
     * The ditherer to use for the tab backgrounds.
     */
    private final Ditherer DITHERER =
            new Ditherer(62,
                        SkinHandler.getSearchPanelBG1(), 
                        SkinHandler.getSearchPanelBG2()
                        );
                    
	private JPanel searchEntry;
    
    /**
     * The listener for new searches.
     */
    private final ActionListener SEARCH_LISTENER = new SearchListener();
    
	/**
	 * Holds the keys of the already created input panels.
	 */
	private Set<String> inputPanelKeys = null;
    
    /**
     * A HashMap for each input panel's preferred dimension 
     * where the key is the <tt>NameMediaType</tt> of the panel
     */
    private Map<NamedMediaType, Dimension> inputPanelDimensions = new HashMap<NamedMediaType, Dimension>();
    
    private final NetworkManager networkManager;
    private final NetworkInstanceUtils networkInstanceUtils;
        
    SearchInputPanel(NetworkManager networkManager, NetworkInstanceUtils networkInstanceUtils) {
        super(new BorderLayout(0, 5));
        
        this.networkManager = networkManager; 
        this.networkInstanceUtils = networkInstanceUtils;

        final ActionListener schemaListener = new SchemaListener();
        //SCHEMA_BOX.addSelectionListener(schemaListener);


        searchEntry = createSearchEntryPanel();
        panelize(searchEntry);
        
        PANE.add(I18n.tr("Search"), searchEntry);
        PANE.setRequestFocusEnabled(false);
        PANE.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestSearchFocusImmediately();
            }
        });

        add(PANE, BorderLayout.CENTER);

        JPanel viewSharedFilesPanel = new BoxPanel(BoxPanel.X_AXIS);
        viewSharedFilesPanel.add(new JLabel(GUIMediator.getThemeImage("shared_folder")));
        viewSharedFilesPanel.add(Box.createHorizontalStrut(5));
        viewSharedFilesPanel.add(new MySharedFilesButton());
        viewSharedFilesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 0));
        add(viewSharedFilesPanel, BorderLayout.SOUTH);
        
        Font bold = UIManager.getFont("Table.font.bold");
        Font bolder =
            new Font(bold.getName(), bold.getStyle(), bold.getSize() + 5);
        SEARCH_TYPE_LABEL.setFont(bolder);
        SEARCH_TYPE_LABEL.setPreferredSize(new Dimension(130, 20));
        schemaListener.actionPerformed(null);
    }
    
    /**
     * Gets the KeyProcessingTextField that key events can be forwarded to.
     */
    KeyProcessingTextField getForwardingSearchField() {
        if(isNormalSearchType()) {
//            if(SCHEMA_BOX.getSelectedSchema() != null) {
//                return getInputPanel().getFirstTextField();
//            }
            return SEARCH_FIELD;
        }
		if(isBrowseHostSearchType())
            return BROWSE_HOST_FIELD;
		return null;
    }
    
    /**
     * Determines if a key event can be forwarded to the search.
     */
    boolean isKeyEventForwardable() {
        return isNormalSearchType() ||
               isBrowseHostSearchType();
    }
    
    /**
     * Determines if browse-host is selected.
     */
    boolean isBrowseHostSearchType() {
        return PANE.getSelectedIndex() == 2;
    }
    
    /**
     * Determines if what is new is selected.
     */
    boolean isWhatIsNewSearchType() {
        return PANE.getSelectedIndex() == 1;
    }
    
    /**
     * Determines if keyword is selected.
     */
    boolean isNormalSearchType() {
        return PANE.getSelectedIndex() == 0;
    }
    
    /**
     * Notification that the addr has changed.
     */
	void addressChanged() {
        updateIpText();
        invalidate();
        revalidate();
    }
    
    void requestSearchFocusImmediately() {
    	if (getInputPanel() != null) {
    		getInputPanel().requestFirstFocus();
    	} else if (SEARCH_FIELD != null) {
    		SEARCH_FIELD.requestFocus();
    	}
    }
    
    void requestSearchFocus() {
        // Workaround for bug manifested on Java 1.3 where FocusEvents
        // are improperly posted, causing BasicTabbedPaneUI to throw an
        // ArrayIndexOutOfBoundsException.
        // See:
        // http://developer.java.sun.com/developer/bugParade/bugs/4523606.html
        // http://developer.java.sun.com/developer/bugParade/bugs/4379600.html
        // http://developer.java.sun.com/developer/bugParade/bugs/4128120.html
        // for related problems.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                requestSearchFocusImmediately();
            }
        });
    }
    
    /**
     * Sets all components in this component to be not opaque
     * and sets the correct background panel.
     */
    private void panelize(JComponent c) {
        GUIUtils.setOpaque(false, c);
        if(!ThemeSettings.isNativeTheme()) {
            c.setOpaque(true);
        }

        c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
    }
    
        
    private void updateIpText() {
        if(networkManager.acceptedIncomingConnection() &&
           !networkInstanceUtils.isPrivate()) {
            IP_TEXT.setText(
        			I18n.tr("When your friends want to connect to you, they should enter")+" \""+
					NetworkUtils.ip2string(networkManager.getAddress())+":"
					+networkManager.getPort()+"\""
			       );
        } else {
            IP_TEXT.setText(I18n.tr("Your computer is behind a firewall or router and cannot receive direct connections."));
        }
    }
    
   
    private JPanel createSearchEntryPanel() {
        SEARCH_FIELD.addActionListener(SEARCH_LISTENER);

        // add the default search input panel to the meta cards
        META_PANEL.add(createDefaultSearchPanel(), DEFAULT_PANEL_KEY);

		// other mediatype panels are added lazily on demand

        JPanel search = new DitherPanel(DITHERER);
        search.setLayout(new BoxLayout(search, BoxLayout.Y_AXIS));
        search.add(GUIUtils.left(SEARCH_TYPE_LABEL));
        search.add(Box.createVerticalStrut(5));
        search.add(META_PANEL);
        
        JButton openTorrentButton = new JButton("Open a Torrent");
        openTorrentButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				OpenMagnetTorrentAction openMagnetTorrentAction = new FileMenuActions.OpenMagnetTorrentAction();
				openMagnetTorrentAction.actionPerformed(null);
			}
		});
        
        search.add(GUIUtils.center(openTorrentButton));
        return search;
    }
    
    private void cleanupPaneActions(ActionMap map) {
        if(map == null)
            return;
        
        Object[] keys = map.allKeys();
        for(int i = 0; i < keys.length; i++) {
            Action action = map.get(keys[i]);
            if(action == null)
                continue;
            Object o = action.getValue(Action.NAME);
            if(!(o instanceof String))
                return;
            String name = (String)o;
            if(name.equals("scrollHome") ||
               name.equals("scrollEnd") ||
               name.equals("scrollLeft") ||
               name.equals("scrollRight") ||
               name.equals("unitScrollLeft") ||
               name.equals("unitScrollRight")) {
                map.remove(keys[i]);
                if(map.get(keys[i]) != null)
                    cleanupPaneActions(map.getParent());
            }
        }
    }
        
	private Set<String> getInputPanelKeys() {
		if (inputPanelKeys == null)
			inputPanelKeys = new HashSet<String>();
		return inputPanelKeys;
    }
    
    /**
     * Creates the default search input of:
     *    Filename
     *    [   input box  ]
     */
    private JPanel createDefaultSearchPanel() {
        JPanel fullPanel = new BoxPanel(BoxPanel.Y_AXIS);
        fullPanel.add(Box.createVerticalStrut(3));
        fullPanel.add(GUIUtils.left(SEARCH_FIELD));
        fullPanel.add(Box.createVerticalStrut(5));
        fullPanel.add(createSearchButtonPanel());
        return GUIUtils.left(fullPanel);
    }
    
    /**
     * Creates the search button & inserts it in a panel.
     */
    private JPanel createSearchButtonPanel() {
        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton searchButton = new JButton(
            I18n.tr("Search"));        
        searchButton.setToolTipText(
            I18n.tr("Search the Network for the Given Words"));

        searchButton.addActionListener(SEARCH_LISTENER);
      
        b.add(Box.createHorizontalGlue());
        b.add(searchButton);
        return b;
    }
    
    /**
     * Gets the visible component in META_PANEL.
     */
    private JComponent getVisibleComponent() {
        for(int i = 0; i < META_PANEL.getComponentCount(); i++) {
            Component current = META_PANEL.getComponent(i);
            if(current.isVisible())
                return (JComponent)current;
        }
        return null;
    }
    
    /**
     * Gets the visible scrollpane.
     */
    private JScrollPane getVisibleScrollPane() {
        JComponent parent = (JComponent)getVisibleComponent().getComponent(0);
        	for(int i = 0; i < parent.getComponentCount(); i++) {
            Component current = parent.getComponent(i);
            if(current.isVisible() && current instanceof JScrollPane)
                return (JScrollPane)current;
        }
        return null;
    }
    
    /**
     * Retrieves the InputPanel that is currently visible.
     */
    private InputPanel getInputPanel() {
        JScrollPane pane = getVisibleScrollPane();
        if(pane == null)
            return null;
        else
            return (InputPanel)pane.getViewport().getView();
    }
    
	/**
	 * Listener for selecting a new schema.
	 */
	private class SchemaListener implements ActionListener {
	    public void actionPerformed(ActionEvent event) {
	    	SearchSettings.MAX_QUERY_LENGTH.revertToDefault();

	        //Truncate if you have too much text for a gnutella search
	        if (SEARCH_FIELD.getText().length() > SearchSettings.MAX_QUERY_LENGTH.getValue()) {
	        	try {
					SEARCH_FIELD.setText(SEARCH_FIELD.getText(0,SearchSettings.MAX_QUERY_LENGTH.getValue()));
				} catch (BadLocationException e) {
				}
	        }
	        
            SEARCH_TYPE_LABEL.setText(I18n.tr("Search Files"));
            requestSearchFocus();
        }
    }
    
    /**
     * Listener for starting a new search.
     */
    private class SearchListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            InputPanel panel = getInputPanel();
            String query = SEARCH_FIELD.getText();
            final SearchInformation info =
            SearchInformation.createTitledKeywordSearch(query, null, MediaType.TYPE_TORRENTS,query);

            
            // If the search worked, store & clear it.
            if(SearchMediator.triggerSearch(info) != null) {
                if(info.isKeywordSearch()) {
                    // Add the necessary stuff for autocompletion.
                    if(panel != null) {
                        panel.storeInput();
                        panel.clear();
                    } else {
                        SEARCH_FIELD.addToDictionary();
                    }
    
                    // Clear the existing search.
                    SEARCH_FIELD.setText("");
                }
            }
        }
    }
    
    /**
     * Listener for 'more options'.
     */
    private class MoreOptionsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JCheckBox box = (JCheckBox)e.getSource();
            JComponent c = (JComponent)getVisibleComponent().getComponent(0);
            JComponent pane = (JComponent)((JComponent)c.getComponent(0)).getComponent(0);
            if(c instanceof JPanel && pane instanceof JViewport) {
            	if(box.isSelected()) {
            		c.setMaximumSize(null);
	            }
            	else {
                    Dimension dim = inputPanelDimensions.get(MediaType.TYPE_TORRENTS);
            		if(dim != null)
            		c.setMaximumSize(dim);
            	}
            }
            invalidate();
            revalidate();
            repaint();
        }
    }
}    
