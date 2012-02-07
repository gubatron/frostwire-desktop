package com.limegroup.gnutella.gui.search;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.frostwire.gui.bittorrent.SendFileProgressDialog;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconButton;
import com.limegroup.gnutella.gui.actions.FileMenuActions;
import com.limegroup.gnutella.gui.actions.FileMenuActions.OpenMagnetTorrentAction;
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
        getMainPanel().add(SEARCH, BorderLayout.PAGE_START);
        getMainPanel().putClientProperty(SkinCustomUI.CLIENT_PROPERTY_DARK_NOISE, true);
        
        getMainPanel().add(createTorrentActionsPanel(), BorderLayout.PAGE_END);

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
            MAIN_PANEL = new JPanel(new BorderLayout());
        }
        return MAIN_PANEL;
    }

    public void clearFilters() {
        SEARCH.clearFilters();
    }

    public void setFiltersFor(SearchResultMediator rp) {
        SEARCH.setFiltersFor(rp);
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

}
