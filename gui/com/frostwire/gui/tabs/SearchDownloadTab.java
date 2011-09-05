package com.frostwire.gui.tabs;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.search.SearchMediator;
import com.limegroup.gnutella.gui.tables.ComponentMediator;

/**
 * This class constructs the search/download tab, including all UI elements.
 */
public final class SearchDownloadTab extends AbstractTab {
	
	/**
	 * Split pane for the split between the search input panel and the 
	 * search results panel.
	 */
	private final JSplitPane mainSplitPane;
	
	/**
	 * Split pane for the split between the search and download sections
	 * of the window.
	 */
	private final JSplitPane searchDownloadSplitPane;

	/**
	 * Constructs the tab for searches and downloads.
	 *
	 * @param SEARCH_MEDIATOR the <tt>SearchMediator</tt> instance for 
	 *  obtaining the necessary ui components to add
	 * @param downloadMediator the <tt>DownloadMediator</tt> instance for 
	 *  obtaining the necessary ui components to add
	 */
	public SearchDownloadTab(SearchMediator searchMediator, ComponentMediator<?> downloadMediator) {
		super(I18n.tr("Search"), I18n.tr("Search and Download Files"), "search_tab");

        searchDownloadSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, SearchMediator.getResultComponent(), downloadMediator.getComponent());
        searchDownloadSplitPane.setContinuousLayout(true);
        searchDownloadSplitPane.setOneTouchExpandable(true);
        searchDownloadSplitPane.setResizeWeight(0.6);
        searchDownloadSplitPane.setDividerLocation(1000);

		JComponent searchBoxPanel = SearchMediator.getSearchComponent();
        
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, searchBoxPanel, searchDownloadSplitPane);
		mainSplitPane.setDividerSize(0);
	}

	/**
	 * Sets the location of the search/download divider.
	 *
	 * @param loc the location to set the divider to
	 */
	public void setDividerLocation(int loc) {
		searchDownloadSplitPane.setDividerLocation(loc);
	}

	/**
	 * Sets the location of the search/download divider.
	 *
	 * @param loc the location to set the divider to
	 */
	public void setDividerLocation(double loc) {
		searchDownloadSplitPane.setDividerLocation(loc);
	}
	
	/**
	 * Returns the divider location of the search/download divider.
	 * @return
	 */
	public int getDividerLocation() {
		return searchDownloadSplitPane.getDividerLocation();
	}

	public void storeState(boolean state) {
		// the search tab can never be invisible, so this isn't necessary
	}

	public JComponent getComponent() {
		return mainSplitPane;
	}
	
	public void mouseClicked() {
	    SearchMediator.showSearchInput();
    }	    
}
