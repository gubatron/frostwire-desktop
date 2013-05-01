package com.limegroup.gnutella.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
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
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import net.miginfocom.swing.MigLayout;

import com.frostwire.gui.player.MediaPlayerComponent;
import com.frostwire.gui.tabs.Tab;
import com.frostwire.gui.theme.SkinApplicationHeaderUI;
import com.frostwire.gui.theme.ThemeMediator;
import com.frostwire.gui.updates.UpdateMediator;
import com.limegroup.gnutella.gui.GUIMediator.Tabs;

public class ApplicationHeader extends JPanel implements RefreshListener {

    /*
    * The property to store the selected icon in.
    */
    private static final String SELECTED = "SELECTED_ICON";

    /**
     * The property to store the unselected icon in.
     */
    private static final String DESELECTED = "DESELECTED_ICON";

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

    /** Contains the Update Button and the Player */
    private JPanel eastPanel;

    public ApplicationHeader(Map<Tabs, Tab> tabs) {
        setMinimumSize(new Dimension(300, 54));
        setLayout(new MigLayout("", "[][][grow][]"));

        headerButtonBackgroundSelected = GUIMediator.getThemeImage("selected_header_button_background").getImage();
        headerButtonBackgroundUnselected = GUIMediator.getThemeImage("unselected_header_button_background").getImage();

        setSizes();

        addTabButtons(tabs);

        createUpdateButton();
        add(updateButton, "growx");

        logoPanel = new LogoPanel();
        add(logoPanel, "dock east");

        //addAudioPlayerComponent();

        GUIMediator.addRefreshListener(this);

    }

    private void addEastPanel() {
        eastPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        eastPanel.setOpaque(false);
        add(eastPanel, BorderLayout.LINE_END);
    }

    private void setSizes() {
        setMinimumSize(new Dimension(1, 54));
        setPreferredSize(new Dimension(Integer.MAX_VALUE, 54));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
    }

    private void addAudioPlayerComponent() {
        final JPanel mediaPanel = new MediaPlayerComponent().getMediaPanel(true);
        mediaPanel.setMinimumSize(new Dimension(300, 45));
        mediaPanel.setPreferredSize(new Dimension(300, 45));

        mediaPanel.setBorder(BorderFactory.createEmptyBorder(2, 1, 6, 11));

        final Image audioPlayerBackground = GUIMediator.getThemeImage("audio_player_background").getImage();

        @SuppressWarnings("serial")
        final JPanel mediaPanelFrame = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.drawImage(audioPlayerBackground, 0, (getHeight() - mediaPanel.getHeight()) / 2, null);
                super.paintComponent(g);
            }
        };

        //mediaPanelFrame.setBorder(BorderFactory.createEmptyBorder(4,1,6,11));
        mediaPanelFrame.setOpaque(false);
        mediaPanelFrame.add(mediaPanel);

        eastPanel.add(mediaPanelFrame);
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

    private void addTabButtons(Map<Tabs, Tab> tabs) {

        GridLayout gridLayout = new GridLayout(1, GUIMediator.Tabs.values().length);
        gridLayout.setHgap(8);

        JPanel buttonContainer = new JPanel(gridLayout);
        buttonContainer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 32));
        buttonContainer.setOpaque(false);
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
        JPanel buttonContainer = (JPanel) components[0];
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

        @SuppressWarnings("serial")
        final AbstractButton button = new JRadioButton(I18n.tr(t.getTitle())) {
            protected void paintComponent(Graphics g) {
                if (isSelected()) {
                    g.drawImage(headerButtonBackgroundSelected, 0, 0, null);
                } else {
                    g.drawImage(headerButtonBackgroundUnselected, 0, 0, null);
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
        button.addItemListener(HIGHLIGHTER);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 7, 5, 0));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.addMouseListener(CLICK_FORWARDER);
        button.setToolTipText(t.getToolTip());

        //button.putClientProperty(SubstanceTextUtilities.ENFORCE_FG_COLOR, Boolean.TRUE);
        button.setForeground(ThemeMediator.TAB_BUTTON_FOREGROUND_COLOR);

        Dimension buttonDim = new Dimension(107, 34);
        button.setPreferredSize(buttonDim);
        button.setMinimumSize(buttonDim);
        button.setMaximumSize(buttonDim);
        button.setSelected(false);

        return button;
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
            //Animate the button.
            //            final Timeline timeline = new Timeline(new IntermittentButton(updateButton,updateImageButtonOn,updateImageButtonOff));
            //            
            //            timeline.addCallback(new TimelineCallbackAdapter() {
            //                private long lastChange = 0;
            //                private boolean lastState = false;
            //                
            //                @Override
            //                public void onTimelinePulse(float durationFraction, float timelinePosition) {
            //                    int currentSecond = (int) (durationFraction*timeline.getDuration()/1000);
            //                    if (currentSecond != lastChange) {
            //                        lastChange = currentSecond;
            //                        updateButton.setIcon((lastState) ? updateImageButtonOn : updateImageButtonOff);
            //                        lastState = !lastState;
            //                    }
            //                }
            //                
            //                @Override
            //                public void onTimelineStateChanged(TimelineState oldState,
            //                        TimelineState newState, float durationFraction,
            //                        float timelinePosition) {
            //                    if (newState == TimelineState.DONE) {
            //                        updateButton.setIcon(updateImageButtonOn);
            //                    }
            //                }
            //            });
            //            
            //            timeline.setDuration(30000);
            //            timeline.play();
        }
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
}