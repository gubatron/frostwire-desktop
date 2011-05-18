package com.limegroup.gnutella.gui.search;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
     * The card layout switching between searching or filtering.
     */
    private final CardLayout MAIN_CARDS = new CardLayout();

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

        getComponent().removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(5, 0, 0, 0);
        getComponent().add(MAIN_PANEL, c);
    }

    void rebuild() {
        updateTheme();
    }

    void goToSearch() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showSearchCard(false);
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
        }
        return COMPONENT_PANEL;
    }

    /**
     * Resets the FilterPanel for the specified ResultPanel.
     */
    void panelReset(ResultPanel rp) {
    }

    /**
     * Removes the filter associated with the specified result panel.
     */
    void panelRemoved(ResultPanel rp) {
    }

    /**
     * Displays the search card.
     */
    private void showSearchCard(boolean immediate) {
        MAIN_CARDS.first(getMainPanel());
        requestSearchFocus(immediate);
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
            MAIN_PANEL = new JPanel(MAIN_CARDS);
        }
        return MAIN_PANEL;
    }
}
