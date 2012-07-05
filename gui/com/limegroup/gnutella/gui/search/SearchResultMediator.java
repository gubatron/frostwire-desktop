package com.limegroup.gnutella.gui.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.OverlayLayout;
import javax.swing.border.AbstractBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.limewire.i18n.I18nMarker;

import com.frostwire.gui.bittorrent.TorrentUtil;
import com.frostwire.gui.filters.TableLineFilter;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.GUIConstants;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.PaddedPanel;
import com.limegroup.gnutella.gui.actions.SearchAction;
import com.limegroup.gnutella.gui.dnd.DNDUtils;
import com.limegroup.gnutella.gui.dnd.MulticastTransferHandler;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;
import com.limegroup.gnutella.gui.tables.ActionIconAndNameEditor;
import com.limegroup.gnutella.gui.tables.ColumnPreferenceHandler;
import com.limegroup.gnutella.gui.tables.LimeJTable;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.TableSettings;
import com.limegroup.gnutella.gui.themes.SkinCheckBoxMenuItem;
import com.limegroup.gnutella.gui.themes.SkinMenu;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;
import com.limegroup.gnutella.gui.themes.SkinPopupMenu;
import com.limegroup.gnutella.gui.util.PopupUtils;
import com.limegroup.gnutella.settings.BittorrentSettings;
import com.limegroup.gnutella.settings.SearchSettings;
import com.limegroup.gnutella.util.QueryUtils;

public class SearchResultMediator extends AbstractTableMediator<TableRowFilteredModel, SearchResultDataLine, SearchResult> {
    
    protected static final String SEARCH_TABLE = "SEARCH_TABLE";
    
    
    /** Flag that a search has been stopped with a random GUID */
    protected static final GUID STOPPED_GUID = new GUID(GUID.makeGuid());
    
    private static final DateRenderer DATE_RENDERER = new DateRenderer();
    private static final ResultSpeedRenderer RESULT_SPEED_RENDERER = new ResultSpeedRenderer();
    private static final PercentageRenderer PERCENTAGE_RENDERER = new PercentageRenderer();
    
    /**
     * The TableSettings that all ResultPanels will use.
     */
    static final TableSettings SEARCH_SETTINGS =
        new TableSettings("SEARCH_TABLE");
    
    /**
     * The search info of this class.
     */
    private final SearchInformation SEARCH_INFO;
    
    /**
     * The GUID of the last search. (Use this to match up results.)
     *  May be a DummyGUID for the empty result list hack.
     */
    protected volatile GUID guid;
    
    /**
     * The CompositeFilter for this ResultPanel.
     */
    CompositeFilter FILTER;
    
    /**
     * The download listener.
     */
    ActionListener DOWNLOAD_LISTENER;
    
    /**
     * The "download as" listener.
     */
    ActionListener DOWNLOAD_AS_LISTENER;
    
    /**
     * The browse host listener.
     */
    
    MouseAdapter TORRENT_DETAILS_LISTENER;
    
    private ActionListener COPY_MAGNET_ACTION_LISTENER;
    
    private ActionListener COPY_HASH_ACTION_LISTENER;

    ActionListener CONFIGURE_SHARING_LISTENER;
    
    ActionListener DOWNLOAD_PARTIAL_FILES_LISTENER;
    
    ActionListener STOP_SEARCH_LISTENER;
    
    protected Box SOUTH_PANEL;

    private MouseInputListener ACTION_HIGHLIGHT_LISTENER;
    
    public AtomicInteger searchCount = new AtomicInteger(0);
        
    /**
     * Specialized constructor for creating a "dummy" result panel.
     * This should only be called once at search window creation-time.
     */
    SearchResultMediator(JPanel overlay) {
        super(SEARCH_TABLE);
        setupFakeTable(overlay);

        
        SEARCH_INFO = SearchInformation.createKeywordSearch("", null,
                                      MediaType.getAnyTypeMediaType());
        FILTER = null;
        this.guid = STOPPED_GUID;
        setButtonEnabled(SearchButtons.TORRENT_DETAILS_BUTTON_INDEX, false);
        // disable dnd for overlay panel
        TABLE.setDragEnabled(false);
        TABLE.setTransferHandler(null);
        
        SOUTH_PANEL.setVisible(false);
    }
    
    /**
     * Constructor for creating a search panel with a given title.
     * This should be used for "pre-stopped" searches.
     */
    SearchResultMediator(String title, String id) {
        super(id);
        
        this.SEARCH_INFO = SearchInformation.createKeywordSearch(title, null, MediaType
                .getAnyTypeMediaType());
        
        this.guid = STOPPED_GUID;
    }

    /**
     * Constructs a new ResultPanel for search results.
     *
     * @param guid the guid of the query.  Used to match results.
     * @param info the info of the search
     */
    SearchResultMediator(GUID guid, SearchInformation info) {
        super(SEARCH_TABLE);
        SEARCH_INFO = info;
        this.guid = guid;
        setupRealTable();
        resetFilters();
    }    
   
    /**
     * Sets the default renderers to be used in the table.
     */
    protected void setDefaultRenderers() {
        super.setDefaultRenderers();
        TABLE.setDefaultRenderer(ResultSpeed.class, RESULT_SPEED_RENDERER);
        TABLE.setDefaultRenderer(Date.class, DATE_RENDERER);
        TABLE.setDefaultRenderer(Float.class, PERCENTAGE_RENDERER);
        TABLE.setDefaultRenderer(ResultNameHolder.class, getLinkRenderer());
    }

    /**
     * Does nothing.
     */
    protected void updateSplashScreen() { }

    /**
     * Setup the data model 
     */
    protected void setupDataModel() {
        DATA_MODEL = new TableRowFilteredModel(FILTER);
    }
    
    /**
     * Sets up the constants:
     * FILTER, MAIN_PANEL, DATA_MODEL, TABLE, BUTTON_ROW.
     */
    protected void setupConstants() {

        FILTER = new CompositeFilter(4);
        MAIN_PANEL = new PaddedPanel();

        setupDataModel();        
        
        TABLE = new LimeJTable(DATA_MODEL) {
            /*
             * Override the line color methods to show special
             * colors for The LimeWire Store&#8482; song results.
             */
            
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			
			@Override
			protected boolean isOverrideRowColor(int row) {
			    return getLine(row).isOverrideRowColor();
			}

			@Override
            protected Color getEvenRowColor(int row) {
                return getLine(row).getEvenRowColor();
            }
            
            @Override
            protected Color getOddRowColor(int row) {
                return getLine(row).getOddRowColor();
            }
        };
        
        BUTTON_ROW = new SearchButtons(this).getComponent();
    }
    
    
    @Override
    protected void setupDragAndDrop() {
        TABLE.setDragEnabled(true);
        TABLE.setTransferHandler(new MulticastTransferHandler(new ResultPanelTransferHandler(this), DNDUtils.DEFAULT_TRANSFER_HANDLERS));
    }
    
    /**
     * Sets SETTINGS to be the static SEARCH_SETTINGS, instead
     * of constructing a new one for each ResultPanel.
     */
    protected void buildSettings() {
        SETTINGS = SEARCH_SETTINGS;
    }
    
    /**
     * Creates the specialized SearchColumnSelectionMenu menu,
     * which groups XML columns together.
     */
    protected JPopupMenu createColumnSelectionMenu() {
        return (new SearchColumnSelectionMenu(TABLE)).getComponent();
    }
    
    /**
     * Creates the specialized column preference handler for search columns.
     */
    protected ColumnPreferenceHandler createDefaultColumnPreferencesHandler() {
        return new SearchColumnPreferenceHandler(TABLE);
    }    
    
    @Override
    protected void addListeners() {        
        super.addListeners();
        TABLE.addMouseMotionListener(ACTION_HIGHLIGHT_LISTENER);
        TABLE.addMouseListener(ACTION_HIGHLIGHT_LISTENER);
    }
    
    /** Sets all the listeners. */
    protected void buildListeners() {
        super.buildListeners();
        
        ACTION_HIGHLIGHT_LISTENER = new MouseInputAdapter() {
            private final Cursor DEFAULT = Cursor.getDefaultCursor();
            private final Cursor HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            private Cursor lastCursor = Cursor.getDefaultCursor();
            
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                int column = TABLE.columnAtPoint(p);
                int colModel = TABLE.convertColumnIndexToModel(column);
                if(colModel == SearchTableColumns.NAME_IDX) {
                    int row = TABLE.rowAtPoint(p);
                    SearchResultDataLine line = DATA_MODEL.get(row);
                    if(line != null && line.isLink()) {
                        if(lastCursor != HAND) {
                            lastCursor = HAND;
                            TABLE.getTopLevelAncestor().setCursor(HAND);
                        }
                        return;
                    }       
                }
                
                if(lastCursor != DEFAULT) {
                    lastCursor = DEFAULT;
                    TABLE.getTopLevelAncestor().setCursor(DEFAULT);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if(lastCursor != DEFAULT) {
                    lastCursor = DEFAULT;
                    TABLE.getTopLevelAncestor().setCursor(DEFAULT);
                }
            }
        };
        
        DOWNLOAD_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SearchMediator.doDownload(SearchResultMediator.this);
            }
        };
        
        DOWNLOAD_AS_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SearchMediator.doDownloadAs(SearchResultMediator.this);
            }
        };
        
        TORRENT_DETAILS_LISTENER = new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		if (e.getButton() == MouseEvent.BUTTON1) {
        			onLeftClick();
        		} else {
        			onRightClick(e);
        		}
        		
        	}

        	/**
        	 * Show popup menu with option to toggle torrent detail display.
        	 * @param e
        	 */
        	private void onRightClick(MouseEvent e) {
				final JPopupMenu menu = new SkinPopupMenu();
				final JCheckBoxMenuItem menuItem = new SkinCheckBoxMenuItem(I18n.tr("Show Torrent Details page when a download starts"),BittorrentSettings.TORRENT_DETAIL_PAGE_SHOWN_AFTER_DOWNLOAD.getValue());
				menuItem.addChangeListener(new ChangeListener() {
					
					@Override
					public void stateChanged(ChangeEvent e) {
						BittorrentSettings.TORRENT_DETAIL_PAGE_SHOWN_AFTER_DOWNLOAD.setValue(menuItem.isSelected());
					}
				});
				menu.add(menuItem);

				JComponent source = (JComponent) e.getComponent();
				menu.show(source, source.getX()-100, source.getY());

        	}
        	
        	/**
        	 * Show torrent details.
        	 */
            private void onLeftClick() {
            	SearchMediator.showTorrentDetails(SearchResultMediator.this, -1);
            }

        }; 
        
        COPY_MAGNET_ACTION_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SearchResultDataLine[] lines = getAllSelectedLines();
                String str = "";
                for (SearchResultDataLine line : lines) {
                    str += TorrentUtil.getMagnet(line.getInitializeObject().getHash());
                    str += "\n";
                }
                GUIMediator.setClipboardContent(str);
            }
        };
        
        COPY_HASH_ACTION_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SearchResultDataLine[] lines = getAllSelectedLines();
                String str = "";
                for (SearchResultDataLine line : lines) {
                    str += line.getInitializeObject().getHash();
                    str += "\n";
                }
                GUIMediator.setClipboardContent(str);
            }
        };
        
        CONFIGURE_SHARING_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GUIMediator.instance().setOptionsVisible(true, I18n.tr("Options"));
            }
        };
            
        DOWNLOAD_PARTIAL_FILES_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SearchResultDataLine[] lines = getAllSelectedLines();
                if (lines.length == 1 && lines[0] != null) {
                    GUIMediator.instance().openTorrentSearchResult(lines[0].getInitializeObject().getWebSearchResult(), true);
                }
            }
        };
        
        STOP_SEARCH_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                guid = STOPPED_GUID;
                SearchMediator.getSearchResultDisplayer().updateSearchIcon(SearchResultMediator.this, false);
                setButtonEnabled(SearchButtons.STOP_SEARCH_BUTTON_INDEX, !isStopped());
            }
        };
    }
    
    /**
     * Creates the specialized SearchResultMenu for right-click popups.
     *
     * Upgraded access from protected to public for SearchResultDisplayer.
     */
    public JPopupMenu createPopupMenu() {
        return createPopupMenu(getAllSelectedLines());
    }
    
    protected JPopupMenu createPopupMenu(SearchResultDataLine[] lines) {
        //  do not return a menu if right-clicking on the dummy panel
        if (!isKillable())
            return null;
  

        JPopupMenu menu = new SkinPopupMenu();

        if (lines.length > 0) {
            boolean allWithHash = true;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].getHash() == null) {
                    allWithHash = false;
                    break;
                }
            }
            PopupUtils.addMenuItem(I18n.tr("Copy Magnet"), COPY_MAGNET_ACTION_LISTENER, menu, !isStopped() && allWithHash);
            PopupUtils.addMenuItem(I18n.tr("Copy Hash"), COPY_HASH_ACTION_LISTENER, menu, !isStopped() && allWithHash);
        }
        
        menu.add(createSearchAgainMenu(lines.length > 0 ? lines[0] : null));
        
        return (new SearchResultMenu(this)).addToMenu(menu, lines);
    }
    
    /**
     * Returns a menu with a 'repeat search' and 'repeat search no clear' action.
     */
    protected final JMenu createSearchAgainMenu(SearchResultDataLine line) {
        JMenu menu = new SkinMenu(I18n.tr("Search More"));
        menu.add(new SkinMenuItem(new RepeatSearchAction()));

        if (line == null) {
            menu.setEnabled(isRepeatSearchEnabled());
            return menu;
        }

        menu.addSeparator();
        String keywords = QueryUtils.createQueryString(line.getFilename());
        SearchInformation info = SearchInformation.createKeywordSearch(
                keywords, null, MediaType
                .getAnyTypeMediaType());
        if (SearchMediator.validateInfo(info) == SearchMediator.QUERY_VALID) {
            menu.add(new SkinMenuItem(new SearchAction(info, I18nMarker
                    .marktr("Search for Keywords: {0}"))));
        }

//        LimeXMLDocument doc = line.getXMLDocument();
//        if (doc != null) {
//            Action[] actions = ActionUtils.createSearchActions(doc);
//            for (int i = 0; i < actions.length; i++) {
//                menu.add(new SkinMenuItem(actions[i]));
//            }
//        }

        return menu;
    } 
    
    
    /**
     * Do not allow removal of rows.
     */
    public void removeSelection() { }
    
    /**
     * Clears the table and converts the download button into a
     * wishlist button.
     */
    public void clearTable() {
        super.clearTable();
    }
    
    /**
     * Sets the appropriate buttons to be disabled.
     */
    public void handleNoSelection() {
        	setButtonEnabled(SearchButtons.DOWNLOAD_BUTTON_INDEX, false);
        	setButtonEnabled(SearchButtons.TORRENT_DETAILS_BUTTON_INDEX, false);
        	setButtonEnabled(SearchButtons.STOP_SEARCH_BUTTON_INDEX, !isStopped());
    }
    
    /**
     * Sets the appropriate buttons to be enabled.
     */
    public void handleSelection(int i)  { 
    	setButtonEnabled(SearchButtons.DOWNLOAD_BUTTON_INDEX, true);
    	
    	setButtonEnabled(SearchButtons.STOP_SEARCH_BUTTON_INDEX, !isStopped());

    	// Buy button only enabled for single selection.
    	SearchResultDataLine[] allSelectedLines = getAllSelectedLines();
        setButtonEnabled(SearchButtons.TORRENT_DETAILS_BUTTON_INDEX, allSelectedLines != null && allSelectedLines.length == 1);
    }
    
    /**
     * Forwards the event to DOWNLOAD_LISTENER.
     */
    public void handleActionKey() {
        DOWNLOAD_LISTENER.actionPerformed(null);
    }
    
    /**
     * Gets the SearchInformation of this search.
     */
    SearchInformation getSearchInformation() {
        return SEARCH_INFO;
    }
    
    /**
     * Gets the query of the search.
     */
    String getQuery() {
        return SEARCH_INFO.getQuery();
    }
    
    /**
     * Returns the title of the search.
     * @return
     */
    String getTitle() {
        return SEARCH_INFO.getTitle();
    }
    
    /**
     * Gets the rich query of the search.
     */
    String getRichQuery() {
        return SEARCH_INFO.getXML();
    }    
    
    

    /**
     * Shows a LicenseWindow for the selected line.
     */
    void showLicense() {
//        TableLine line = getSelectedLine();
//        if(line == null)
//            return;
//            
//        URN urn = line.getSHA1Urn();
//        LimeXMLDocument doc = line.getXMLDocument();
//        LicenseWindow window = LicenseWindow.create(line.getLicense(), urn, doc, this);
//        GUIUtils.centerOnScreen(window);
//        window.setVisible(true);
    }
    
    /**
     * Determines whether or not this panel is stopped.
     */
    boolean isStopped() {
        if (guid==null || STOPPED_GUID==null) {
            return false;
        }
        return guid.equals(STOPPED_GUID);
    }
    
    /**
     * Determines if this is empty.
     */
    boolean isEmpty() {
        return DATA_MODEL.getRowCount() == 0;
    }
    
    /**
     * Determines if this can be removed.
     */
    boolean isKillable() {
        // the dummy panel has a null filter, and is the only one not killable
        return FILTER != null;
    }
    
    /**
     * Notification that a filter on this panel has changed.
     *
     * Updates the data model with the new list, maintains the selection,
     * and moves the viewport to the first still visible selected row.
     *
     * Note that the viewport moving cannot be done by just storing the first
     * visible row, because after the filters change, the row might not exist
     * anymore.  Thus, it is necessary to store all visible rows and move to
     * the first still-visible one.
     */
    boolean filterChanged(TableLineFilter<SearchResultDataLine> filter, int depth) {
        FILTER.setFilter(depth, filter);
        //if(!FILTER.setFilter(depth, filter))
        //    return false;
        
        // store the selection & visible rows
        int[] rows = TABLE.getSelectedRows();
        SearchResultDataLine[] lines = new SearchResultDataLine[rows.length];
        List<SearchResultDataLine> inView = new LinkedList<SearchResultDataLine>();
        for(int i = 0; i < rows.length; i++) {
            int row = rows[i];
            SearchResultDataLine line = DATA_MODEL.get(row);
            lines[i] = line;
            if(TABLE.isRowVisible(row))
                inView.add(line);
        }
        
        // change the table.
        DATA_MODEL.filtersChanged();
        
        // reselect & move the viewpoint to the first still visible row.
        for(int i = 0; i < rows.length; i++) {
            SearchResultDataLine line = lines[i];
            int row = DATA_MODEL.getRow(line);
            if(row != -1) {
                TABLE.addRowSelectionInterval(row, row);
                if(inView != null && inView.contains(line)) {
                    TABLE.ensureRowVisible(row);
                    inView = null;
                }                    
            }
        }
        
        // update the tab count.
        SearchMediator.setTabDisplayCount(this);
        return true;
    }
    
    /**
     * Returns the total number of sources found for this search.
     */
    int totalSources() {
        return ((ResultPanelModel)DATA_MODEL).getTotalSources();
    }
    
    int totalResults() {
        return ((ResultPanelModel)DATA_MODEL).getTotalResults();
    }
    
    /**
     * Returns the total number of filtered source found for this search.
     */
    int filteredSources() {
        return DATA_MODEL.getFilteredSources();
    }
    
    int filteredResults() {
        return DATA_MODEL.getFilteredResults();
    }

    /**
     * Determines whether or not repeat search is currently enabled.
     * Repeat search will be disabled if, for example, the original
     * search was performed too recently.
     *
     * @return <tt>true</tt> if the repeat search feature is currently
     *  enabled, otherwise <tt>false</tt>
     */
    boolean isRepeatSearchEnabled() {
        return FILTER != null;
    }
    
    void repeatSearch (boolean clearTable) {
        if ( clearTable ) {
          clearTable();
          resetFilters();
        }
        
        SearchMediator.setTabDisplayCount(this);
        SearchMediator.repeatSearch(this, SEARCH_INFO, clearTable);
        setButtonEnabled(SearchButtons.TORRENT_DETAILS_BUTTON_INDEX, false);
        setButtonEnabled(SearchButtons.STOP_SEARCH_BUTTON_INDEX, !isStopped());
    }
    
    void resetFilters() {
        FILTER.reset();
        DATA_MODEL.setJunkFilter(null);
    }
    
    /**
     * Gets the MetadataModel used for results.
     */
    MetadataModel getMetadataModel() {
        return  ((ResultPanelModel)DATA_MODEL).getMetadataModel();
    }

    public List<SearchResultDataLine> getAllData() {
    	return ((TableRowFilteredModel)DATA_MODEL).getAllData();
    }
    
    /** Returns true if this is responsible for results with the given GUID */
    boolean matches(GUID otherGuid) {
        return this.guid.equals(otherGuid);
    }

    /**
     * @modifies this
     * @effects sets this' guid.  This is needed for browse host functionality.
     */
    void setGUID(GUID guid) {
        this.guid=guid;
    }

    /** Returns the guid this is responsible for. */
    byte[] getGUID() {
        return guid.bytes();
    }

    /** Returns the media type this is responsible for. */
    MediaType getMediaType() {
        return SEARCH_INFO.getMediaType();
    }
    
    /**
     * Gets all currently selected TableLines.
     * 
     * @return empty array if no lines are selected.
     */
    SearchResultDataLine[] getAllSelectedLines() {
        int[] rows = TABLE.getSelectedRows();
        if(rows == null)
            return new SearchResultDataLine[0];
        
        SearchResultDataLine[] lines = new SearchResultDataLine[rows.length];
        for(int i = 0; i < rows.length; i++)
            lines[i] = DATA_MODEL.get(rows[i]);
        return lines;
    }
    
    /**
     * Gets the currently selected TableLine.
     * 
     * @return null if there is no selected line.
     */
    SearchResultDataLine getSelectedLine() {
        int selected = TABLE.getSelectedRow();
        if(selected != -1) 
            return DATA_MODEL.get(selected);
        else
            return null;
    }
    
    /**
     * Gets the TableLine at <code>index</code>
     * 
     * @param index index of the line you want
     * @return null if there is no selected line.
     */
    final SearchResultDataLine getLine(int index) {
        return DATA_MODEL.get(index);
    }
    
    /**
     * Sets extra values for non dummy ResultPanels.
     * (Used for all tables that will have results.)
     *
     * Currently:
     * - Sorts the count column, if it is visible & real-time sorting is on.
     * - Adds listeners, so the filters can be displayed when necessary.
     */
    private void setupRealTable() {
        SearchTableColumns columns =
            ((ResultPanelModel)DATA_MODEL).getColumns();
        LimeTableColumn countColumn =
            columns.getColumn(SearchTableColumns.COUNT_IDX);
        if(SETTINGS.REAL_TIME_SORT.getValue() &&
           TABLE.isColumnVisible(countColumn.getId())) {
            DATA_MODEL.sort(SearchTableColumns.COUNT_IDX); // ascending
            DATA_MODEL.sort(SearchTableColumns.COUNT_IDX); // descending
        }
        
        MouseListener filterDisplayer = new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if(e.isConsumed())
                    return;
                e.consume();
                SearchMediator.panelSelected(SearchResultMediator.this);
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        };
        // catches around the button area.
        MAIN_PANEL.addMouseListener(filterDisplayer);
        // catches the blank area before results fill in
        SCROLL_PANE.addMouseListener(filterDisplayer);
        // catches selections on the table
        TABLE.addMouseListener(filterDisplayer);
        // catches the table header
        TABLE.getTableHeader().addMouseListener(filterDisplayer);
    }
    
    
    protected void setupMainPanelBase() {
        if (SearchSettings.ENABLE_SPAM_FILTER.getValue() 
                && MAIN_PANEL != null) {
            MAIN_PANEL.add(getScrolledTablePane());
            addButtonRow();
            MAIN_PANEL.setMinimumSize(ZERO_DIMENSION);
        } else {
            super.setupMainPanel();
        }
    }
    
    /**
     * Overwritten
     */
    protected void setupMainPanel() {
        //MAIN_PANEL.add(createSecurityWarning()); //No warnings
        
        setupMainPanelBase();
    }

    /**
     * Adds the overlay panel into the table & converts the button
     * to 'download'.
     */
    private void setupFakeTable(JPanel overlay) {
        MAIN_PANEL.removeAll();
        
        //Fixes flickering!
        JPanel background = new JPanel() {
			private static final long serialVersionUID = 8931395134232576566L;

			public boolean isOptimizedDrawingEnabled() {
                return false;
              }
        };
        
        background.setLayout(new OverlayLayout(background));
        
        JPanel overlayPanel = new BoxPanel(BoxPanel.Y_AXIS);
        overlayPanel.setOpaque(false);
        overlayPanel.add(Box.createVerticalStrut(20));
        overlayPanel.add(overlay);
        
        overlayPanel.setMinimumSize(new Dimension(0, 0));
        JComponent table = getScrolledTablePane();
        table.setOpaque(false);
        background.add(overlayPanel);
        background.add(table);
        
        MAIN_PANEL.add(background);
        addButtonRow();
        
        MAIN_PANEL.setMinimumSize(ZERO_DIMENSION);
    }
    
    /**
     * Adds the button row and the Spam Button
     */
    private void addButtonRow() {
        if (BUTTON_ROW != null) {
            SOUTH_PANEL = Box.createVerticalBox();
            SOUTH_PANEL.setOpaque(false);
            
            SOUTH_PANEL.add(Box.createVerticalStrut(GUIConstants.SEPARATOR));

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbc.weightx = 1;
            
            buttonPanel.add(BUTTON_ROW, gbc);
            
            buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            SOUTH_PANEL.add(buttonPanel);
            
            MAIN_PANEL.add(SOUTH_PANEL);
        }
    }
    
    public void cleanup() {
    }
    
    protected void setDefaultEditors() {
        TableColumnModel model = TABLE.getColumnModel();
        TableColumn tc;
        tc = model.getColumn(SearchTableColumns.NAME_IDX);
        tc.setCellEditor(new ActionIconAndNameEditor(new Rectangle(3, 3, 13, 13)));
        tc = model.getColumn(SearchTableColumns.SOURCE_IDX);
        tc.setCellEditor(new ActionIconAndNameEditor());
    }

    
    public class WarningBorder extends AbstractBorder {
     
        /**
		 * 
		 */
		private static final long serialVersionUID = 1031178095074065273L;
		private Color lineColor;
        private int offset;

        public WarningBorder(Color lineColor, int offset) {
            this.lineColor = lineColor;
            this.offset = offset;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height) {
            Color oldColor = g.getColor();

            g.setColor(lineColor);

            // top
            g.drawLine(x, y, x + width - 1 - offset, y);
            // left
            g.drawLine(x, y, x, y + height);
            // right
            g.drawLine(x + width - 1 - offset, y, x + width - 1 - offset, y + height);
            
            if (offset > 0) {
                g.setColor(c.getParent().getBackground());
                for (int i = 0; i < offset; i++) {
                    g.drawLine(x + width - 1 - i, y, x + width - 1 - i, y + height);                    
                }
            }
            
            g.setColor(oldColor);
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 0, 1 + offset);
        }
        
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }


    private final class RepeatSearchAction extends AbstractAction {

        /**
		 * 
		 */
		private static final long serialVersionUID = -209446182720400951L;

		public RepeatSearchAction() {
            putValue(Action.NAME, SearchMediator.REPEAT_SEARCH_STRING);
            setEnabled(isRepeatSearchEnabled());
        }

        public void actionPerformed(ActionEvent e) {
            repeatSearch(true);
        }
    }

    public void incrementSearchCount() {
        SearchMediator.getSearchResultDisplayer().updateSearchIcon(this, searchCount.incrementAndGet() > 0);       
    }

    public void decrementSearchCount() {
        SearchMediator.getSearchResultDisplayer().updateSearchIcon(this, searchCount.decrementAndGet() > 0);
    }
}
