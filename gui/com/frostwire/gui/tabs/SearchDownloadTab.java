package com.frostwire.gui.tabs;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import com.frostwire.gui.bittorrent.BTDownloadMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.search.SearchMediator;
import com.limegroup.gnutella.settings.UISettings;

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
	public SearchDownloadTab(SearchMediator searchMediator, BTDownloadMediator downloadMediator) {
		super(I18n.tr("Search"), I18n.tr("Search and Download Files"), "search_tab");

        searchDownloadSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, SearchMediator.getResultComponent(), downloadMediator.getComponent());
        searchDownloadSplitPane.setContinuousLayout(true);
        searchDownloadSplitPane.setResizeWeight(0.6);
        searchDownloadSplitPane.setDividerLocation(UISettings.UI_TRANSFERS_DIVIDER_LOCATION.getValue());

		JComponent searchBoxPanel = SearchMediator.getSearchComponent();
        
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, searchBoxPanel, searchDownloadSplitPane);
		mainSplitPane.setDividerSize(0);		
		
		searchDownloadSplitPane.addPropertyChangeListener(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                JSplitPane splitPane = (JSplitPane) evt.getSource();
                int current = splitPane.getDividerLocation();
                if (splitPane.getSize().height - current < BTDownloadMediator.MIN_HEIGHT) {
                    splitPane.setDividerLocation(splitPane.getSize().height - BTDownloadMediator.MIN_HEIGHT);
                }
                UISettings.UI_TRANSFERS_DIVIDER_LOCATION.setValue(splitPane.getDividerLocation());
                if (SearchMediator.getSearchResultDisplayer().getSlideshowPanel() != null) {
                    SearchMediator.getSearchResultDisplayer().getSlideshowPanel().repaint();
                }
            }
        });
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
