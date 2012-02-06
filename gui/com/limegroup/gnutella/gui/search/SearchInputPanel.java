package com.limegroup.gnutella.gui.search;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.limewire.setting.BooleanSetting;

import com.frostwire.gui.bittorrent.SendFileProgressDialog;
import com.frostwire.gui.components.GoogleIconSearchField;
import com.frostwire.gui.components.IconSearchField;
import com.frostwire.gui.filters.TableLineFilter;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconButton;
import com.limegroup.gnutella.gui.IconManager;
import com.limegroup.gnutella.gui.InputPanel;
import com.limegroup.gnutella.gui.KeyProcessingTextField;
import com.limegroup.gnutella.gui.actions.FileMenuActions;
import com.limegroup.gnutella.gui.actions.FileMenuActions.OpenMagnetTorrentAction;
import com.limegroup.gnutella.gui.themes.SkinCustomColors;
import com.limegroup.gnutella.gui.themes.SkinHandler;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeSettings;
import com.limegroup.gnutella.gui.themes.fueled.FueledCustomColors;
import com.limegroup.gnutella.settings.ApplicationSettings;
import com.limegroup.gnutella.settings.SearchSettings;

/**
 * Inner panel that switches between the various kinds of
 * searching.
 */
class SearchInputPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -5638062215253666235L;
    
    private final SchemaBox SCHEMA_BOX = new SchemaBox();

    /**
     * The sole input text field that is at the top of all searches.
     */
    //private final SearchField SEARCH_FIELD = new SearchField(14);
    private final IconSearchField SEARCH_FIELD = new GoogleIconSearchField(18,GUIMediator.getThemeImage("search_tab"));

    /**
     * The box that holds the schemas for searching.
     */

    /**
     * The ditherer to use for the tab backgrounds.
     */
    private final Ditherer DITHERER = new Ditherer(62, SkinHandler.getSearchPanelBG1(), SkinHandler.getSearchPanelBG2());

    private JPanel searchEntry;

    /**
     * The listener for new searches.
     */
    private final ActionListener SEARCH_LISTENER = new SearchListener();

	private JXCollapsiblePane SEARCH_OPTIONS_COLLAPSIBLE_PANEL;
	
	private SearchFilterPanel _filterPanel;
	
    SearchInputPanel() {
        super(new BorderLayout(0, 5));

        final ActionListener schemaListener = new SchemaListener();

        searchEntry = createSearchEntryPanel();
        searchEntry.setBorder(BorderFactory.createEmptyBorder(0, 3, 5, 2));
        //panelize(searchEntry);

        add(searchEntry, BorderLayout.PAGE_START);

        schemaListener.actionPerformed(null);
        
        add(createTorrentActionsPanel(), BorderLayout.PAGE_END);
    }

    private JPanel createTorrentActionsPanel() {
        
        JPanel buttons_container = new JPanel();

        //OPEN TORRENT
        IconButton openTorrentButton = new IconButton("Open", "OPEN_TORRENT");
        openTorrentButton.setToolTipText(I18n.tr("Open a .torrent or Magnet link"));
        openTorrentButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                OpenMagnetTorrentAction openMagnetTorrentAction = new FileMenuActions.OpenMagnetTorrentAction();
                openMagnetTorrentAction.actionPerformed(null);
            }
        });
        
        //SEND FILE
        IconButton sendFileButton = new IconButton("Send","SHARE");
        sendFileButton.setToolTipText(I18n.tr("Send a file or folder to a friend (No size limit, No third parties involved)"));
        sendFileButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                SendFileProgressDialog dlg = new SendFileProgressDialog(GUIMediator.getAppFrame());
                dlg.setVisible(true);
            }
        });

        buttons_container.add(openTorrentButton);
        buttons_container.add(sendFileButton);
        
        return buttons_container;
    }

    /**
     * Gets the KeyProcessingTextField that key events can be forwarded to.
     */
    KeyProcessingTextField getForwardingSearchField() {
        if (isNormalSearchType()) {
            return SEARCH_FIELD;
        }
        return null;
    }

    /**
     * Determines if a key event can be forwarded to the search.
     */
    boolean isKeyEventForwardable() {
        return isNormalSearchType();
    }

    /**
     * Determines if keyword is selected.
     */
    boolean isNormalSearchType() {
        return true;//PANE.getSelectedIndex() == 0;
    }

    void requestSearchFocusImmediately() {
        if (SEARCH_FIELD != null) {
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
        if (!ThemeSettings.isNativeTheme()) {
            c.setOpaque(true);
        }

        c.setBorder(BorderFactory.createEmptyBorder(0, 3, 5, 0));
    }

    private JPanel createSearchEntryPanel() {
        SEARCH_FIELD.addActionListener(SEARCH_LISTENER);
        SEARCH_FIELD.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		SEARCH_LISTENER.actionPerformed(null);
        	}
		});
        
        return createDefaultSearchPanel();
    }

    /**
     * Creates the default search input of:
     *    Filename
     *    [   input box  ]
     */
    private JPanel createDefaultSearchPanel() {
        JPanel fullPanel = new BoxPanel(BoxPanel.Y_AXIS);
        fullPanel.add(SCHEMA_BOX);
        fullPanel.add(Box.createVerticalStrut(3));
        fullPanel.add(GUIUtils.left(SEARCH_FIELD));
        fullPanel.add(Box.createVerticalStrut(5));
        fullPanel.add(createSearchButtonPanel());
        fullPanel.add(createSearchOptionsPanel());
        
        
        return fullPanel;
    }
    

    private Component createSearchOptionsPanel() {
		SEARCH_OPTIONS_COLLAPSIBLE_PANEL = new JXCollapsiblePane();
		
		SEARCH_OPTIONS_COLLAPSIBLE_PANEL.setCollapsed(ApplicationSettings.SEARCH_OPTIONS_COLLAPSED.getValue());
		
		SEARCH_OPTIONS_COLLAPSIBLE_PANEL.setLayout(new BorderLayout());
		SEARCH_OPTIONS_COLLAPSIBLE_PANEL.setAnimated(true);
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		
		List<SearchEngine> searchEngines = SearchEngine.getSearchEngines();
		
		JPanel controls = new JPanel();
		controls.putClientProperty(FueledCustomColors.CLIENT_PROPERTY_DARK_DARK_NOISE, true);
		controls.setBorder(new LineBorder(ThemeMediator.CURRENT_THEME.getCustomColors().getDarkBorder(), 1, true));
        controls.setLayout(new GridBagLayout());
        controls.setAlignmentX(0.0f);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        controls.add(new JLabel(I18n.tr("Search Engines")), c);
		setupCheckboxes(searchEngines, controls);
		
		_filterPanel = new SearchFilterPanel();
		_filterPanel.putClientProperty(FueledCustomColors.CLIENT_PROPERTY_DARK_DARK_NOISE, true);
		_filterPanel.setBorder(new LineBorder(ThemeMediator.CURRENT_THEME.getCustomColors().getDarkBorder(), 1, true));
		_filterPanel.setAlignmentX(0.0f);
		p.add(_filterPanel);
		
		p.add(Box.createVerticalStrut(6));
		
		p.add(controls);
		SEARCH_OPTIONS_COLLAPSIBLE_PANEL.add(p);
		

		return SEARCH_OPTIONS_COLLAPSIBLE_PANEL;
	}
    
    private void setupCheckboxes(List<SearchEngine> searchEngines, JPanel parent) {
    	
    	final Map<JCheckBox,BooleanSetting> cBoxes = new HashMap<JCheckBox,BooleanSetting>();
    	
		ItemListener listener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
            	boolean allDeSelected = true;

            	for (JCheckBox cBox : cBoxes.keySet()) {
            		if (cBox.isSelected()) {
            			allDeSelected = false;
            			break;
            		}
            	}

            	if (allDeSelected) {
            		((JCheckBox)e.getItemSelectable()).setSelected(true);
            	}
                
            	for (JCheckBox cBox : cBoxes.keySet()) {
            		cBoxes.get(cBox).setValue(cBox.isSelected());
            	}
                
                updateSearchResults(new SearchEngineFilter());
            }
        };

		for (SearchEngine se : searchEngines) {
			JCheckBox cBox = new JCheckBox(se.getName());
			cBox.setSelected(se.isEnabled());
			
			GridBagConstraints c = new GridBagConstraints();
	        c.fill = GridBagConstraints.HORIZONTAL;
	        c.weightx = 1.0;
	        c.gridwidth = GridBagConstraints.REMAINDER;
			parent.add(cBox, c);
			
			cBoxes.put(cBox,se.getEnabledSetting());
			cBox.addItemListener(listener);
		}

	}

	private void updateSearchResults(TableLineFilter<SearchResultDataLine> filter) {
        List<SearchResultMediator> resultPanels = SearchMediator.getSearchResultDisplayer().getResultPanels();
        for (SearchResultMediator resultPanel : resultPanels) {
            resultPanel.filterChanged(filter, 0);
        }
    }

	/**
     * Creates the search button & inserts it in a panel.
     */
    private JPanel createSearchButtonPanel() {

    	//The Search Button on a row of it's own
        JPanel b = new JPanel(new GridBagLayout());
       
        
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0,0,10,0);
        
        /*JButton searchButton = new JButton(I18n.tr("Search"));
        searchButton.setToolTipText(I18n.tr("Search the Network for the Given Words"));
        searchButton.addActionListener(SEARCH_LISTENER);
        b.add(searchButton,c);*/

        //Apply Filters <Icon Button>
        final ToggleSearchOptionsPanelAction toggleSearchOptionsPanelAction = new ToggleSearchOptionsPanelAction();
        
        JPanel filterLabelIconPanel = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        JLabel filterLabel = new JLabel(I18n.tr("<html><strong>Refine Results</strong></html>"));
        //filterLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        filterLabelIconPanel.add(filterLabel,c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.insets = new Insets(0,3,0,0);

        final JButton iconButton = new JButton();
        iconButton.setAction(toggleSearchOptionsPanelAction);
        iconButton.setIcon((ApplicationSettings.SEARCH_OPTIONS_COLLAPSED.getValue()) ? IconManager.instance().getSmallIconForButton("SEARCH_OPTIONS_MORE") : IconManager.instance().getSmallIconForButton("SEARCH_OPTIONS_LESS"));
        fixIconButton(iconButton);        
       
        filterLabelIconPanel.add(iconButton,c);

        filterLabel.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		ActionEvent evt = new ActionEvent(iconButton,1,null);
        		
        		toggleSearchOptionsPanelAction.actionPerformed(evt);
        	}
		});
        
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.gridy = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        c.insets = new Insets(10,0,10,0);
        b.add(filterLabelIconPanel,c);

        return b;
    }

	private void fixIconButton(JButton iconButton) {
		iconButton.setBorderPainted(false);
        iconButton.setFocusable(false);
        iconButton.setBorder(null);
        iconButton.setFocusPainted(false);
        iconButton.setContentAreaFilled(false);
        iconButton.setPreferredSize(new Dimension(16,16));
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
                    SEARCH_FIELD.setText(SEARCH_FIELD.getText(0, SearchSettings.MAX_QUERY_LENGTH.getValue()));
                } catch (BadLocationException e) {
                }
            }

            requestSearchFocus();
        }
    }

    /**
     * Listener for starting a new search.
     */
    private class SearchListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String query = SEARCH_FIELD.getText();
            final SearchInformation info = SearchInformation.createTitledKeywordSearch(query, null, MediaType.getTorrentMediaType(), query);

            // If the search worked, store & clear it.
            if (SearchMediator.triggerSearch(info) != null) {
                if (info.isKeywordSearch()) {
                    
                        SEARCH_FIELD.addToDictionary();

                    // Clear the existing search.
                    SEARCH_FIELD.setText("");
                }
            }
        }
    }
    
    private class ToggleSearchOptionsPanelAction extends AbstractAction {

    	private final String TOOLTIP_COLLAPSED = I18n.tr("Show search result filter controls");
    	private final String TOOLTIP_SHOWN = I18n.tr("Hide search result filter controls");
    	
    	private static final long serialVersionUID = -2415729526575357348L;
        
        public ToggleSearchOptionsPanelAction() {
        	putValue(SHORT_DESCRIPTION, TOOLTIP_COLLAPSED);
		}

        @Override
		public void actionPerformed(ActionEvent event) {
			SEARCH_OPTIONS_COLLAPSIBLE_PANEL.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION).actionPerformed(event);
			
			JButton iconButton = (JButton) event.getSource();
			
			Icon iconForButton = null;
			
			if (!SEARCH_OPTIONS_COLLAPSIBLE_PANEL.isCollapsed()) {
				iconForButton = IconManager.instance().getSmallIconForButton("SEARCH_OPTIONS_LESS");
				ApplicationSettings.SEARCH_OPTIONS_COLLAPSED.setValue(false);
				putValue(SHORT_DESCRIPTION, TOOLTIP_SHOWN);
			} else {
				iconForButton = IconManager.instance().getSmallIconForButton("SEARCH_OPTIONS_MORE");
				ApplicationSettings.SEARCH_OPTIONS_COLLAPSED.setValue(true);
				putValue(SHORT_DESCRIPTION, TOOLTIP_COLLAPSED);
			}

			iconButton.setIcon(iconForButton);
			fixIconButton(iconButton);
		}
    	
    }

    public void clearFilters() {
        _filterPanel.clearFilters();
    }

    public void setFiltersFor(SearchResultMediator rp) {
        _filterPanel.setFilterFor(rp);
        SCHEMA_BOX.setFilterFor(rp);
    }
    
    /**
     * Resets the FilterPanel for the specified ResultPanel.
     */
    void panelReset(SearchResultMediator rp) {
        _filterPanel.panelReset(rp);
        SCHEMA_BOX.panelReset(rp);
    }
    
    /**
     * Removes the filter associated with the specified result panel.
     */
    boolean panelRemoved(SearchResultMediator rp) {
        return _filterPanel.panelRemoved(rp);
    }
}
