package com.limegroup.gnutella.gui.search;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.limegroup.gnutella.gui.themes.SkinCustomUI;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;

/**
 * Manages input for the search, including filters for active searches.
 */
final class SearchInputManager implements ThemeObserver {
    /**
     * The panel that contains all input information for searching.
     * This includes both 'input boxes' and 'filter boxes'.
     */
    private JPanel COMPONENT_PANEL;

    /**
     * The panel containing either search input or filters.
     */
    private JPanel MAIN_PANEL;

    /**
     * The search input panel.
     */
    private SearchInputPanel SEARCH;

    /**
     * Constructs a new search input manager class, including all displayed
     * elements for search input.
     */
    SearchInputManager() {
        updateTheme();
        ThemeMediator.addThemeObserver(this);
    }

    public void updateTheme() {
        SEARCH = new SearchInputPanel();

        getMainPanel().removeAll();
        getMainPanel().add(SEARCH, "search");
        getMainPanel().putClientProperty(SkinCustomUI.CLIENT_PROPERTY_DARK_NOISE, true);

        getComponent().removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 0);
        getComponent().add(MAIN_PANEL, c);
    }

    void rebuild() {
        updateTheme();
    }

    void goToSearch() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                requestSearchFocus(false);
            }
        });
    }

    void requestSearchFocus() {
        requestSearchFocus(false);
    }

    /**
     * Returns the <tt>JComponent</tt> instance containing the UI elements
     * for the search input section of the search tab.
     *
     * @return the <tt>JComponent</tt> instance containing the UI elements
     *  for the search input section of the search tab
     */
    JComponent getComponent() {
        if (COMPONENT_PANEL == null) {
            COMPONENT_PANEL = new JPanel(new GridBagLayout());
            
            COMPONENT_PANEL.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, ThemeMediator.CURRENT_THEME.getCustomUI().getDarkBorder()));
        }
        return COMPONENT_PANEL;
    }

    /**
     * Resets the FilterPanel for the specified ResultPanel.
     */
    void panelReset(SearchResultMediator rp) {
        SEARCH.panelReset(rp);
    }
    
    /**
     * Removes the filter associated with the specified result panel.
     */
    void panelRemoved(SearchResultMediator rp) {
        if(SEARCH.panelRemoved(rp))
            requestSearchFocus(false);
    }

    /**
     * Requests focus for the search field.
     */
    private void requestSearchFocus(boolean immediate) {
        if (immediate)
            SEARCH.requestSearchFocusImmediately();
        else
            SEARCH.requestSearchFocus();
    }

    private JPanel getMainPanel() {
        if (MAIN_PANEL == null) {
            MAIN_PANEL = new JPanel();
        }
        return MAIN_PANEL;
    }

    public void clearFilters() {
        SEARCH.clearFilters();
    }

    public void setFiltersFor(SearchResultMediator rp) {
        SEARCH.setFiltersFor(rp);
    }
}
