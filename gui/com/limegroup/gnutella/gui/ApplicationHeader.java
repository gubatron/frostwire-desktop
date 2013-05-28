/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.limegroup.gnutella.gui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.BadLocationException;

import net.miginfocom.swing.MigLayout;

import com.frostwire.gui.library.LibrarySearch;
import com.frostwire.gui.player.MediaPlayerComponent;
import com.frostwire.gui.searchfield.GoogleSearchField;
import com.frostwire.gui.searchfield.SearchField;
import com.frostwire.gui.tabs.LibraryTab;
import com.frostwire.gui.tabs.Tab;
import com.frostwire.gui.theme.SkinApplicationHeaderUI;
import com.frostwire.gui.theme.ThemeMediator;
import com.frostwire.gui.updates.UpdateMediator;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator.Tabs;
import com.limegroup.gnutella.gui.actions.FileMenuActions;
import com.limegroup.gnutella.gui.search.SearchInformation;
import com.limegroup.gnutella.gui.search.SearchMediator;
import com.limegroup.gnutella.settings.SearchSettings;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class ApplicationHeader extends JPanel implements RefreshListener {

    /*
    * The property to store the selected icon in.
    */
    private static final String SELECTED = "SELECTED_ICON";

    /**
     * The property to store the unselected icon in.
     */
    private static final String DESELECTED = "DESELECTED_ICON";

    private static final String CLOUD_SEARCH_FIELD = "cloud_search_field";

    private static final String LIBRARY_SEARCH_FIELD = "library_search_field";

    /**
     * The clicker forwarder.
     */
    private final MouseListener CLICK_FORWARDER = new Clicker();

    /**
     * The listener for changing the highlighting of buttons.
     */
    private final ItemListener HIGHLIGHTER = new Highlighter();

    /** Button background for selected button */
    private final Image headerButtonBackgroundSelected;

    /** Button background for unselected button */
    private final Image headerButtonBackgroundUnselected;

    private LogoPanel logoPanel;

    private JLabel updateButton;
    private ImageIcon updateImageButtonOn;
    private ImageIcon updateImageButtonOff;
    private long updateButtonAnimationStartedTimestamp;

    private GoogleSearchField cloudSearchField;
    private SearchField librarySearchField;
    private final JPanel searchPanels;

    public ApplicationHeader(Map<Tabs, Tab> tabs) {
        setMinimumSize(new Dimension(300, 54));
        setLayout(new MigLayout("insets 0 10 0 0, ay 50%, filly", "[][][][grow][]"));

        headerButtonBackgroundSelected = GUIMediator.getThemeImage("selected_header_button_background").getImage();
        headerButtonBackgroundUnselected = GUIMediator.getThemeImage("unselected_header_button_background").getImage();

        searchPanels = createSearchPanel();
        add(searchPanels, "w 200!");

        addTabButtons(tabs);

        createUpdateButton();
        add(updateButton, "growx");

        JComponent player = new MediaPlayerComponent().getMediaPanel();
        add(player, "dock east, growy, gapright 5");

        logoPanel = new LogoPanel();

        GUIMediator.addRefreshListener(this);

        final ActionListener schemaListener = new SchemaListener();
        schemaListener.actionPerformed(null);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new CardLayout());
        createCloudSearchField();
        createLibrarySearchField();

        panel.add(cloudSearchField, CLOUD_SEARCH_FIELD);
        panel.add(librarySearchField, LIBRARY_SEARCH_FIELD);

        return panel;
    }

    private void createLibrarySearchField() {
        librarySearchField = new LibrarySearch().getSearchField();
    }

    private void createCloudSearchField() {
        cloudSearchField = new GoogleSearchField();
        cloudSearchField.addActionListener(new SearchListener());
        cloudSearchField.setPrompt(I18n.tr("Search or enter URL"));
        Font origFont = cloudSearchField.getFont();
        Font newFont = origFont.deriveFont(origFont.getSize2D() + 2f);
        cloudSearchField.setFont(newFont);
    }

    private void createUpdateButton() {
        updateImageButtonOn = GUIMediator.getThemeImage("update_button_on");
        updateImageButtonOff = GUIMediator.getThemeImage("update_button_off");

        updateButton = new JLabel(updateImageButtonOn);
        updateButton.setToolTipText(I18n.tr("A new update has been downloaded."));
        Dimension d = new Dimension(32, 32);

        updateButton.setVisible(false);
        updateButton.setSize(d);
        updateButton.setPreferredSize(d);
        updateButton.setMinimumSize(d);
        updateButton.setMaximumSize(d);
        updateButton.setBorder(null);
        updateButton.setOpaque(false);
        
        updateButtonAnimationStartedTimestamp = -1;

        updateButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                UpdateMediator.instance().showUpdateMessage();
            }
        });
    }

    public LogoPanel getLogoPanel() {
        return logoPanel;
    }

    private void addTabButtons(final Map<Tabs, Tab> tabs) {
        JPanel buttonContainer = new JPanel(new MigLayout("insets 0"));
        ButtonGroup group = new ButtonGroup();

        Font buttonFont = new Font("Helvetica", Font.BOLD, 14);

        for (Tabs t : GUIMediator.Tabs.values()) {
            final Tabs lameFinalT = t; //java...
            AbstractButton button = createTabButton(tabs.get(t));
            button.setFont(buttonFont);

            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    GUIMediator.instance().setWindow(lameFinalT);
                    showSearchField(tabs.get(lameFinalT));
                }
            });

            group.add(button);
            buttonContainer.add(button);

            button.setSelected(t.equals(GUIMediator.Tabs.SEARCH));
        }

        add(buttonContainer, "");
    }

    /** Given a Tab mark that button as selected 
     * 
     * Since we don't keep explicit references to the buttons this method
     * walks over the components in the ApplicationHeader until it finds
     * the AbstractButton that has the Tab object as a client property named "tab" 
     * @see MainFrame#setSelectedTab(Tabs)
     */
    public void selectTab(Tab t) {
        Component[] components = getComponents();
        JPanel buttonContainer = (JPanel) components[1];
        Component[] buttons = buttonContainer.getComponents();
        for (Component c : buttons) {
            if (c instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) c;
                if (b.getClientProperty("tab").equals(t)) {
                    b.setSelected(true);
                    return;
                }
            }
        }
    }

    private AbstractButton createTabButton(Tab t) {
        Icon icon = t.getIcon();
        Icon disabledIcon = null;
        Icon rolloverIcon = null;

        final JRadioButton button = new JRadioButton(I18n.tr(t.getTitle())) {
            protected void paintComponent(Graphics g) {
                if (isSelected()) {
                    g.drawImage(headerButtonBackgroundSelected, 0, 1, null);
                } else {
                    g.drawImage(headerButtonBackgroundUnselected, 0, 1, null);
                }
                super.paintComponent(g);
            }
        };

        button.putClientProperty("tab", t);

        button.putClientProperty(SELECTED, icon);
        if (icon != null) {
            disabledIcon = ImageManipulator.darken(icon);
            rolloverIcon = ImageManipulator.brighten(icon);
        }
        button.putClientProperty(DESELECTED, disabledIcon);
        button.setIcon(disabledIcon);
        button.setRolloverIcon(rolloverIcon);
        button.setPressedIcon(rolloverIcon);
        button.addItemListener(HIGHLIGHTER);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 7, 5, 0));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.addMouseListener(CLICK_FORWARDER);
        button.setToolTipText(t.getToolTip());

        //button.putClientProperty(SubstanceTextUtilities.ENFORCE_FG_COLOR, Boolean.TRUE);
        button.setForeground(ThemeMediator.TAB_BUTTON_FOREGROUND_COLOR);

        Dimension buttonDim = new Dimension(107, 35);
        button.setPreferredSize(buttonDim);
        button.setMinimumSize(buttonDim);
        button.setMaximumSize(buttonDim);
        button.setSelected(false);

        return button;
    }

    private void showSearchField(Tab t) {
        cloudSearchField.setText("");
        librarySearchField.setText("");
        CardLayout cl = (CardLayout) (searchPanels.getLayout());
        if (t instanceof LibraryTab) {
            cl.show(searchPanels, LIBRARY_SEARCH_FIELD);
        } else {
            cl.show(searchPanels, CLOUD_SEARCH_FIELD);
        }
    }

    /*
    * Listener for ItemEvent, so that the buttons can be highlighted or not
    * when selected (or not).
    */
    private static class Highlighter implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            AbstractButton button = (AbstractButton) e.getSource();
            //DitherPanel parent = (DitherPanel) button.getParent();
            if (e.getStateChange() == ItemEvent.SELECTED) {
                button.setIcon((Icon) button.getClientProperty(SELECTED));
                //parent.setDithering(true);
            } else {
                button.setIcon((Icon) button.getClientProperty(DESELECTED));
                //parent.setDithering(false);
            }
        }
    }

    /**
     * Forwards click events from a panel to the panel's component.
     */
    private static class Clicker implements MouseListener {
        public void mouseEntered(MouseEvent e) {
            /*
            JComponent c = (JComponent) e.getSource();
            AbstractButton b;
            if (c instanceof AbstractButton) {
                b = (AbstractButton) c;
                c = (JComponent) c.getParent();
            } else {
                b = (AbstractButton) c.getComponent(0);
            }
            //           if (!b.isSelected())
            //               setIfNotNull(c, "TabbedPane.selected");
            */
        }

        public void mouseExited(MouseEvent e) {
            /**
            JComponent c = (JComponent) e.getSource();
            AbstractButton b;
            if (c instanceof AbstractButton) {
                b = (AbstractButton) c;
                c = (JComponent) c.getParent();
            } else {
                b = (AbstractButton) c.getComponent(0);
            }
            */
            //           if (!b.isSelected())
            //               setIfNotNull(c, "TabbedPane.background");
        }

        public void mouseClicked(MouseEvent e) {
            JComponent c = (JComponent) e.getSource();
            if (!(c instanceof AbstractButton)) {
                AbstractButton b = (AbstractButton) c.getComponent(0);
                b.doClick();
            }
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }
    }

    @Override
    public void refresh() {
        showUpdateButton(!UpdateMediator.instance().isUpdated() && UpdateMediator.instance().isUpdateDownloaded());
    }

    public static class IntermittentButton {
        private JLabel buttonReference;
        private ImageIcon imgOn;
        private ImageIcon imgOff;

        /** Pass the label you want to animate */
        public IntermittentButton(JLabel button, ImageIcon on, ImageIcon off) {
            buttonReference = button;
            imgOn = on;
            imgOff = off;
        }

        public void setImage(final boolean on) {
            GUIMediator.safeInvokeLater(new Runnable() {
                @Override
                public void run() {
                    buttonReference.setIcon((on) ? imgOn : imgOff);
                }
            });
        }
    }

    private void showUpdateButton(boolean show) {
        if (updateButton.isVisible() == show) {
            return;
        }

        updateButton.setVisible(show);

        if (show) {
            //Start animating the button for 30 seconds.
            if (updateButtonAnimationStartedTimestamp == -1) {
                startUpdateButtonIntermittentAnimation();
            }
            
            
            
        }
    }

    private void startUpdateButtonIntermittentAnimation() {
        updateButtonAnimationStartedTimestamp = System.currentTimeMillis();

        //start animation thread.
        Thread t = new Thread("update-button-animation") {
            private final long  ANIMATION_DURATION = 30000;
            private final long ANIMATION_INTERVAL = 1000;
            private long updateButtonAnimationLastChange;
            
            public void run() {
                long now = System.currentTimeMillis();
                updateButtonAnimationLastChange = now;
                
                boolean buttonState = true;
                while (now - updateButtonAnimationStartedTimestamp < ANIMATION_DURATION) {
                    if (now - updateButtonAnimationLastChange >= ANIMATION_INTERVAL) {
                        switchButtonImage(buttonState);
                        buttonState = !buttonState;
                    }
                    try {
                        sleep(ANIMATION_INTERVAL);
                    } catch (InterruptedException e) {
                    }
                    now = System.currentTimeMillis();
                }
                switchButtonImage(false);
            }
            
            public void switchButtonImage(final boolean state) {
                updateButtonAnimationLastChange = System.currentTimeMillis();
                GUIMediator.safeInvokeAndWait(new Runnable() {
                    public void run() {
                        updateButton.setIcon(state ? updateImageButtonOn : updateImageButtonOff);
                    }
                });
            }
        };
        t.start();
    }

    @Override
    public void updateUI() {
        ComponentUI ui = UIManager.getUI(this);
        if (ui == null) {
            ui = new SkinApplicationHeaderUI();
        }
        setUI(ui);
    }

    @Override
    public String getUIClassID() {
        return "ApplicationHeaderUI";
    }

    public void requestSearchFocusImmediately() {
        if (cloudSearchField != null) {
            cloudSearchField.requestFocus();
        }
    }

    public void requestSearchFocus() {
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

    private class SearchListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String query = cloudSearchField.getText();

            //start a download from the search box by entering a URL.
            if (FileMenuActions.openMagnetOrTorrent(query)) {
                cloudSearchField.setText("");
                cloudSearchField.hidePopup();
                return;
            }

            final SearchInformation info = SearchInformation.createTitledKeywordSearch(query, null, MediaType.getTorrentMediaType(), query);

            // If the search worked, store & clear it.
            if (SearchMediator.instance().triggerSearch(info) != 0) {
                if (info.isKeywordSearch()) {

                    cloudSearchField.addToDictionary();

                    // Clear the existing search.
                    cloudSearchField.setText("");
                    cloudSearchField.hidePopup();
                }
            }
        }
    }

    private class SchemaListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            SearchSettings.MAX_QUERY_LENGTH.revertToDefault();

            //Truncate if you have too much text for a gnutella search
            if (cloudSearchField.getText().length() > SearchSettings.MAX_QUERY_LENGTH.getValue()) {
                try {
                    cloudSearchField.setText(cloudSearchField.getText(0, SearchSettings.MAX_QUERY_LENGTH.getValue()));
                } catch (BadLocationException e) {
                }
            }

            requestSearchFocus();
        }
    }
}