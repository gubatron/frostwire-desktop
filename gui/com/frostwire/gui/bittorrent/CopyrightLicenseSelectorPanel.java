/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.bittorrent;

import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import com.frostwire.gui.AlphaIcon;
import com.frostwire.gui.bittorrent.CopyrightLicenseSelectorPanel.LicenseToggleButton.LicenseIcon;
import com.frostwire.torrent.CopyrightLicense;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LimeTextField;

public class CopyrightLicenseSelectorPanel extends JPanel {

    private static final String CREATIVE_COMMONS_CARD_NAME = "Creative Commons";

    private static final String OPEN_SOURCE_CARD_NAME = "Open Source";

    private static final String PUBLIC_DOMAIN_CARD_NAME = "Public Domain";

    private final JCheckBox confirmRightfulUseOfLicense;

    private final JLabel authorsNameLabel;
    private final LimeTextField authorsName;
    private final JLabel titleLabel;
    private final LimeTextField title;
    private final JLabel attributionUrlLabel;
    private final LimeTextField attributionUrl;
    
    private final JPanel licenseTypesCardLayoutContainer;
    private final JRadioButton licenseTypeCC;
    private final JRadioButton licenseTypeOpenSource;
    private final JRadioButton licenseTypePublicDomain;

    //CC License modifiers.
    private final LicenseToggleButton ccButton;
    private final LicenseToggleButton byButton;
    private final LicenseToggleButton ncButton;
    private final LicenseToggleButton ndButton;
    private final LicenseToggleButton saButton;
    
    //OpenSource License buttons
    private final LicenseToggleButton apacheButton;
    private final LicenseToggleButton bsd2ClauseButton;
    private final LicenseToggleButton bsd3ClauseButton;
    private final LicenseToggleButton gpl3Button;
    private final LicenseToggleButton lgplButton;
    private final LicenseToggleButton mitButton;
    private final LicenseToggleButton mozillaButton;
    private final LicenseToggleButton cddlButton;
    private final LicenseToggleButton eclipseButton;
    
    private final List<LicenseToggleButton> openSourceLicenseButtons;
    
    private final JButton pickedLicenseLabel;
    
    private CopyrightLicense creativeCommonsLicense;
    

    public CopyrightLicenseSelectorPanel() {
        setLayout(new MigLayout("fill"));
        GUIUtils.setTitledBorderOnPanel(this, I18n.tr("Choose a Copyright License for this work"));

        confirmRightfulUseOfLicense = new JCheckBox("<html><strong>"
                + I18n.tr("I am the Content Creator of this work or I have been granted the rights to share this content under the following license by the Content Creator(s).") + "</strong></html>");

        authorsNameLabel = new JLabel("<html>" + I18n.tr("Author's Name") + "</html>");
        authorsName = new LimeTextField();
        authorsName.setToolTipText(I18n.tr("The name of the creator or creators of this work."));

        titleLabel = new JLabel("<html>" + I18n.tr("Work's Title") + "</html>");
        title = new LimeTextField();
        title.setToolTipText(I18n.tr("The name of this work, i.e. the titleLabel of a music album, the titleLabel of a book, the titleLabel of a movie, etc."));
        title.setPrompt(I18n.tr("album name, movie title, book title, game title."));
        
        attributionUrlLabel = new JLabel("<html>" + I18n.tr("Attribution URL") + "</html>");
        attributionUrl = new LimeTextField();
        attributionUrl.setToolTipText(I18n.tr("The Content Creator's website to give attribution about this work if shared by others."));
        attributionUrl.setPrompt("http://www.contentcreator.com/website/here");

        licenseTypesCardLayoutContainer = new JPanel(new CardLayout());
        
        licenseTypeCC = new JRadioButton(I18n.tr(CREATIVE_COMMONS_CARD_NAME));
        licenseTypeOpenSource = new JRadioButton(I18n.tr(OPEN_SOURCE_CARD_NAME));
        licenseTypePublicDomain = new JRadioButton(I18n.tr(PUBLIC_DOMAIN_CARD_NAME));
        
        ccButton = new LicenseToggleButton(
                LicenseToggleButton.LicenseIcon.CC,
                CREATIVE_COMMONS_CARD_NAME,
                "Offering your work under a Creative Commons license does not mean giving up your copyright. It means offering some of your rights to any member of the public but only under certain conditions.",
                true, false);
        byButton = new LicenseToggleButton(LicenseIcon.BY, "Attribution",
                "You let others copy, distribute, display, and perform your copyrighted work but only if they give credit the way you request.", true, false);
        ncButton = new LicenseToggleButton(LicenseIcon.NC, "NonCommercial",
                "<strong>No commercial use allowed.</strong><br>You let others copy, distribute, display, and perform your work — and derivative works based upon it — but for noncommercial purposes only.", true, true);
        ndButton = new LicenseToggleButton(LicenseIcon.ND, "NoDerivatives",
                "<strong>No remixing allowed.</strong><br>You let others copy, distribute, display, and perform only verbatim copies of your work, not derivative works based upon it.", false, true);
        saButton = new LicenseToggleButton(LicenseIcon.SA, "Share-Alike",
                "You allow others to distribute derivative works only under a license identical to the license that governs your work.", true, true);

        apacheButton = new LicenseToggleButton(LicenseIcon.APACHE,"Apache 2.0","Apache License 2.0",true,true);
        bsd2ClauseButton = new LicenseToggleButton(LicenseIcon.BSD, "BSD 2-Clause", "BSD 2-Clause \"Simplified\" or \"FreeBSD\" license.", false, true);
        bsd3ClauseButton = new LicenseToggleButton(LicenseIcon.BSD, "BSD 3-Clause", "BSD 3-Clause \"New\" or \"Revised\" license.", false, true);
        gpl3Button = new LicenseToggleButton(LicenseIcon.GPL3, "GPLv3", "GNU General Public License (GPL) version 3", false, true);
        lgplButton = new LicenseToggleButton(LicenseIcon.LGPL3, "LGPL", "GNU Library or \"Lesser\" General Public License (LGPL)", false, true);
        mozillaButton = new LicenseToggleButton(LicenseIcon.MOZILLA, "Mozilla 2.0", "Mozilla Public License 2.0", false, true);
        mitButton = new LicenseToggleButton(LicenseIcon.OPENSOURCE, "MIT", "MIT license", false, true);
        cddlButton = new LicenseToggleButton(LicenseIcon.OPENSOURCE, "CDDL-1.0", "Common Development and Distribution License (CDDL-1.0)", false, true);
        eclipseButton = new LicenseToggleButton(LicenseIcon.OPENSOURCE, "EPL-1.0", "Eclipse Public License, Vesion 1.0 (EPL-1.0)", false, true);

        openSourceLicenseButtons = new LinkedList<LicenseToggleButton>();
        initOpenSourceButtonList();
        
        pickedLicenseLabel = new JButton();
        
        initListeners();
        initComponents();
    }


    private void initOpenSourceButtonList() {
        openSourceLicenseButtons.add(apacheButton);
        openSourceLicenseButtons.add(bsd2ClauseButton);
        openSourceLicenseButtons.add(bsd3ClauseButton);
        openSourceLicenseButtons.add(gpl3Button);
        openSourceLicenseButtons.add(lgplButton);
        openSourceLicenseButtons.add(mozillaButton);
        openSourceLicenseButtons.add(mitButton);
        openSourceLicenseButtons.add(cddlButton);
        openSourceLicenseButtons.add(eclipseButton);
    }


    public void onCreativeCommonsButtonToggled(LicenseToggleButton button) {
        if (button.getLicenseIcon() == LicenseIcon.ND && button.isSelected()) {
            saButton.setSelected(false);
        } else if (button.getLicenseIcon() == LicenseIcon.SA && button.isSelected()) {
            ndButton.setSelected(false);
        }
        updatePickedLicenseLabel();
    }
    
    protected void onOpenSourceButtonToggled(LicenseToggleButton button) {
        for (LicenseToggleButton b : openSourceLicenseButtons) {
            if (b != button) {
                b.setSelected(false);
                System.out.println("unselected " + b.getTitle());
            } else {
                System.out.println("left alone " + b.getTitle());
            }
        }
        
        updatePickedLicenseLabel();
    }



    private void updatePickedLicenseLabel() {
        //TODO: reflect other licenses picked.
        if (licenseTypeCC.isSelected()) {
            updateCreativeCommonsPickedLicenseLabel();
        } else if (licenseTypeOpenSource.isSelected()) {
            updateOpenSourcePickedLicenseLabel();
        } else if (licenseTypePublicDomain.isSelected()) {
            updatePublicDomainPickedLicenseLabel();
        }
    }


    private void updateCreativeCommonsPickedLicenseLabel() {
        getCreativeCommonsLicense();
        if (creativeCommonsLicense != null) {
            pickedLicenseLabel.setText("<html>" + I18n.tr("You have selected the following License") + ":<br> <a href=\"" + creativeCommonsLicense.licenseUrl + "\">"
                    + creativeCommonsLicense.getLicenseName() + "</a>");
            ActionListener[] actionListeners = pickedLicenseLabel.getActionListeners();
            if (actionListeners != null) {
                for (ActionListener listener : actionListeners) {
                    pickedLicenseLabel.removeActionListener(listener);
                }
            }
            pickedLicenseLabel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    GUIMediator.openURL(creativeCommonsLicense.licenseUrl);
                }
            });
        }
    }
    
    private void updateOpenSourcePickedLicenseLabel() {

    }

    private void updatePublicDomainPickedLicenseLabel() {
        // TODO Auto-generated method stub
    }
    
    public boolean hasConfirmedRightfulUseOfLicense() {
        return confirmRightfulUseOfLicense.isSelected();
    }

    public CopyrightLicense getCreativeCommonsLicense() {
        creativeCommonsLicense = null;

        if (hasConfirmedRightfulUseOfLicense()) {
            creativeCommonsLicense = new CopyrightLicense(saButton.isSelected(), ncButton.isSelected(), ndButton.isSelected(), title.getText(), authorsName.getText(), attributionUrl.getText());
        }

        return creativeCommonsLicense;
    }

    private void initListeners() {
        confirmRightfulUseOfLicense.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onConfirmRightfulUseOfLicenseAction();
            }
        });

        initLicenseTypeRadioButtonsListener();
        initCreativeCommonsLicenseToggleListeners();
        initOpenSourceLicensesToggleListeners();
    }

    private void initOpenSourceLicensesToggleListeners() {
        LicenseToggleButtonOnToggleListener openSourceToggleListener = new LicenseToggleButtonOnToggleListener() {
            
            @Override
            public void onButtonToggled(LicenseToggleButton button) {
                onOpenSourceButtonToggled(button);
            }
        };
        
        apacheButton.setOnToggleListener(openSourceToggleListener);
        bsd2ClauseButton.setOnToggleListener(openSourceToggleListener);
        bsd3ClauseButton.setOnToggleListener(openSourceToggleListener);
        cddlButton.setOnToggleListener(openSourceToggleListener);
        eclipseButton.setOnToggleListener(openSourceToggleListener);
        gpl3Button.setOnToggleListener(openSourceToggleListener);
        lgplButton.setOnToggleListener(openSourceToggleListener);
        mitButton.setOnToggleListener(openSourceToggleListener);
        mozillaButton.setOnToggleListener(openSourceToggleListener);
        
        updatePickedLicenseLabel();
    }

    private void initCreativeCommonsLicenseToggleListeners() {
        LicenseToggleButtonOnToggleListener ccToggleListener = new LicenseToggleButtonOnToggleListener() {
            @Override
            public void onButtonToggled(LicenseToggleButton button) {
                onCreativeCommonsButtonToggled(button);                
            }
        };
        
        ncButton.setOnToggleListener(ccToggleListener);
        ndButton.setOnToggleListener(ccToggleListener);
        saButton.setOnToggleListener(ccToggleListener);
    }

    private void initLicenseTypeRadioButtonsListener() {
        ActionListener licenseTypeChangeListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onLicenseTypeChanged();
            }
        };

        licenseTypeCC.addActionListener(licenseTypeChangeListener);
        licenseTypeOpenSource.addActionListener(licenseTypeChangeListener);
        licenseTypePublicDomain.addActionListener(licenseTypeChangeListener);
    }

    private void initComponents() {
        initCommonComponents();
        
        initCreativeCommonsLicensePanel();
        initOpenSourceLicensesPanel();
        //initPublicDomainLicensePanel();
        
        add(licenseTypesCardLayoutContainer,"aligny top, span 2, grow, pushy, gapbottom 5px, wrap");

        pickedLicenseLabel.setHorizontalAlignment(SwingConstants.LEFT);
        pickedLicenseLabel.setBorderPainted(false);
        pickedLicenseLabel.setOpaque(false);
        pickedLicenseLabel.setContentAreaFilled(false);
        pickedLicenseLabel.setFocusPainted(false);
        add(pickedLicenseLabel,"alignx center, growx, span 2, pushx");
    }

    private void initCreativeCommonsLicensePanel() {
        JPanel licenseButtonsPanel = new JPanel(new MigLayout("fillx, insets 0 0 0 0"));
        JLabel label = new JLabel("<html>" + I18n.tr("Select what people can and can't do with this work") + "</html>");
        label.setEnabled(false);
        licenseButtonsPanel.add(label, "span 5, alignx center, pushy, aligny bottom, wrap");
        licenseButtonsPanel.add(ccButton, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2");
        licenseButtonsPanel.add(byButton, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2");
        licenseButtonsPanel.add(ncButton, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2");
        licenseButtonsPanel.add(ndButton, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2");
        licenseButtonsPanel.add(saButton, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2, wrap");
        licenseTypesCardLayoutContainer.add(licenseButtonsPanel, CREATIVE_COMMONS_CARD_NAME);
    }

    private void initOpenSourceLicensesPanel() {
        JPanel licenseButtonsPanel = new JPanel(new MigLayout("fillx, insets 0 0 0 0"));
        licenseButtonsPanel.add(apacheButton, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2");
        licenseButtonsPanel.add(bsd3ClauseButton, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2");
        licenseButtonsPanel.add(bsd2ClauseButton, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2");
        licenseButtonsPanel.add(gpl3Button, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2, wrap");
        licenseButtonsPanel.add(lgplButton, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2");
        licenseButtonsPanel.add(mitButton, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2");
        licenseButtonsPanel.add(mozillaButton, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2");
        licenseButtonsPanel.add(cddlButton, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2, wrap");
        licenseButtonsPanel.add(eclipseButton, "wmin 130px, aligny top, pushy, grow, gap 2 2 2 2");
        JScrollPane scrollPane = new JScrollPane(licenseButtonsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Dimension scrollPaneDimensions = new Dimension(800,250);
        scrollPane.setMinimumSize(scrollPaneDimensions);
        scrollPane.setPreferredSize(scrollPaneDimensions);
        scrollPane.setMaximumSize(scrollPaneDimensions);
        scrollPane.getVerticalScrollBar().setVisible(false);
        
        licenseTypesCardLayoutContainer.add(scrollPane, OPEN_SOURCE_CARD_NAME);
    }

    private void initPublicDomainLicensePanel() {
        //TODO: public domain license panel - CC0 and Public Domain Mark
        JPanel publicDomainLicensePanel = new JPanel();
        publicDomainLicensePanel.add(new JLabel("You are using a tool for freeing your own work of copyright restrictions around the world. You may use this tool even if your work is free of copyright in some jurisdictions, if you want to ensure it is free everywhere."));
        //licenseTypesCardLayoutContainer.add(publicDomainLicensePanel, "grow, span 2, wrap");
        licenseTypesCardLayoutContainer.add(publicDomainLicensePanel, PUBLIC_DOMAIN_CARD_NAME);
        //CardLayout cardLayout = (CardLayout) licenseTypesCardLayoutContainer.getLayout();
        //cardLayout.addLayoutComponent(publicDomainLicensePanel, "Public Domain");
    }

    private void initCommonComponents() {
        confirmRightfulUseOfLicense.setSelected(false);

        add(confirmRightfulUseOfLicense, "growx, north, gapbottom 8, wrap");
        confirmRightfulUseOfLicense.setSelected(false);
        onConfirmRightfulUseOfLicenseAction();

        add(authorsNameLabel, "gapbottom 5px, pushx, wmin 215px");
        add(titleLabel, "gapbottom 5px, wmin 215px, pushx, wrap");

        add(authorsName, "gapbottom 5px, growx 50, aligny top, pushy, wmin 215px, height 30px, span 1");
        add(title, "gapbottom 5px, growx 50, aligny top, pushy, wmin 215px, span 1, height 30px, wrap");

        JPanel attribPanel = new JPanel(new MigLayout("fillx, insets 0 0 0 0"));
        attribPanel.add(attributionUrlLabel, "width 110px!, alignx left");
        attribPanel.add(attributionUrl, "alignx left, growx, pushx");
        add(attribPanel, "aligny top, pushy, growx, gapbottom 10px, span 2, wrap");
        
        JPanel licenseRadioButtonsContainer = new JPanel(new MigLayout("fillx, insets 0 0 0 0"));
        ButtonGroup group = new ButtonGroup();
        licenseTypeCC.setSelected(true);
        licenseTypeOpenSource.setSelected(false);
        licenseTypePublicDomain.setSelected(false);
        
        licenseTypeCC.setEnabled(false);
        licenseTypeOpenSource.setEnabled(false);
        licenseTypePublicDomain.setEnabled(false);
        
        group.add(licenseTypeCC);
        group.add(licenseTypeOpenSource);
        group.add(licenseTypePublicDomain);
        licenseRadioButtonsContainer.add(licenseTypeCC);
        licenseRadioButtonsContainer.add(licenseTypeOpenSource);
        licenseRadioButtonsContainer.add(licenseTypePublicDomain);
        add(new JLabel(I18n.tr("License type:")));
        add(licenseRadioButtonsContainer,"growx, span 2, wrap");
    }
    
    private void onLicenseTypeChanged() {
        if (confirmRightfulUseOfLicense.isSelected()) {
            System.out.println("onLicenseTypeChanged()");
            CardLayout deck = (CardLayout) licenseTypesCardLayoutContainer.getLayout();
            String currentPanelName = null;
            if (licenseTypeCC.isSelected()) {
                currentPanelName = CREATIVE_COMMONS_CARD_NAME;
            } else if (licenseTypeOpenSource.isSelected()) {
                currentPanelName = OPEN_SOURCE_CARD_NAME;
            } else if (licenseTypePublicDomain.isSelected()) {
                currentPanelName = PUBLIC_DOMAIN_CARD_NAME;
            }
            deck.show(licenseTypesCardLayoutContainer, currentPanelName);
        }
    }

    protected void onConfirmRightfulUseOfLicenseAction() {
        boolean rightfulUseConfirmed = confirmRightfulUseOfLicense.isSelected();

        authorsNameLabel.setEnabled(rightfulUseConfirmed);
        authorsName.setEnabled(rightfulUseConfirmed);
        titleLabel.setEnabled(rightfulUseConfirmed);
        title.setEnabled(rightfulUseConfirmed);
        attributionUrlLabel.setEnabled(rightfulUseConfirmed);
        attributionUrl.setEnabled(rightfulUseConfirmed);

        ccButton.setSelected(rightfulUseConfirmed);
        byButton.setSelected(rightfulUseConfirmed);
        ncButton.setSelected(rightfulUseConfirmed);
        
        ncButton.setToggleable(rightfulUseConfirmed);
        ndButton.setToggleable(rightfulUseConfirmed);
        saButton.setToggleable(rightfulUseConfirmed);
        
        licenseTypeCC.setEnabled(rightfulUseConfirmed);
        licenseTypeOpenSource.setEnabled(rightfulUseConfirmed);
        licenseTypePublicDomain.setEnabled(rightfulUseConfirmed);

        licenseTypesCardLayoutContainer.setEnabled(rightfulUseConfirmed);
        updateOpenSourceLicensesToggleability(rightfulUseConfirmed);
        
        pickedLicenseLabel.setVisible(rightfulUseConfirmed);
        
        if (rightfulUseConfirmed) {
            ndButton.setSelected(false);
            saButton.setSelected(true);
            updatePickedLicenseLabel();
        } else {
            ndButton.setSelected(rightfulUseConfirmed);
            saButton.setSelected(rightfulUseConfirmed);
        }
    }

    private void updateOpenSourceLicensesToggleability(boolean toggleable) {
        for (LicenseToggleButton button : openSourceLicenseButtons) {
            button.setToggleable(toggleable);
        }
    }

    public static class LicenseToggleButton extends JPanel {
        private boolean selected;
        private boolean toggleable;

        private final ImageIcon selectedIcon;
        private final AlphaIcon unselectedIcon;

        private final String title;
        private JLabel iconLabel;
        private JLabel titleLabel;
        private JLabel descriptionLabel;

        private LicenseIcon licenseIcon;

        private LicenseToggleButtonOnToggleListener listener;

        public enum LicenseIcon {
            CC, BY, SA, ND, NC,
            APACHE,
            BSD,
            GPL3,
            LGPL3,
            MOZILLA,
            OPENSOURCE
        }

        public LicenseToggleButton(LicenseIcon iconName, String text, String description, boolean selected, boolean toggleable) {
            this.toggleable = toggleable;
            setMeUp();

            licenseIcon = iconName;
            selectedIcon = getIcon(iconName);
            unselectedIcon = new AlphaIcon(selectedIcon, 0.2f);

            iconLabel = new JLabel((selected) ? selectedIcon : unselectedIcon);
            title = text;
            titleLabel = new JLabel("<html><b>" + text + "</b></html>");
            descriptionLabel = new JLabel("<html><small>" + description + "</small></html>");

            setLayout(new MigLayout("fill, wrap 1"));
            add(iconLabel, "top, aligny top, alignx center, wrap");
            add(titleLabel, "top, aligny top, alignx center, wrap");
            add(descriptionLabel, "top, aligny top, pushy, alignx center");

            initEventListeners();
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setToggleable(boolean t) {
            toggleable = t;
        }

        public LicenseIcon getLicenseIcon() {
            return licenseIcon;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            updateComponents();
        }

        public void setOnToggleListener(LicenseToggleButtonOnToggleListener listener) {
            this.listener = listener;
        }
        
        private void onMouseEntered() {
            if (toggleable) {
                setOpaque(true);
                setBackground(Color.WHITE);
                BasicStroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                setBorder(BorderFactory.createStrokeBorder(stroke,Color.GRAY));
            }
        }

        private void onMouseExited() {
            if (toggleable) {
                setMeUp();
            }
        }

        private void onToggle() {
            if (toggleable) {
                selected = !selected;
                updateComponents();

                if (listener != null) {
                    listener.onButtonToggled(this);
                }
            }
        }
        
        private void initEventListeners() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (listener != null && listener instanceof CopyrightLicenseSelectorPanel) {
                        //magic tricks
                        CopyrightLicenseSelectorPanel parentPanel = (CopyrightLicenseSelectorPanel) listener;
                        if (parentPanel.hasConfirmedRightfulUseOfLicense()) {
                            onToggle();
                        }
                    }
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    onMouseEntered();
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    onMouseExited();
                }
            });
        }

        private void updateComponents() {
            if (iconLabel != null && selectedIcon != null && unselectedIcon != null) {
                iconLabel.setIcon((selected) ? selectedIcon : unselectedIcon);
            }

            if (titleLabel != null) {
                titleLabel.setEnabled(selected);
            }

            if (descriptionLabel != null) {
                descriptionLabel.setEnabled(selected);
            }
        }

        private void setMeUp() {
            setBackground(null);
            setOpaque(false);
            setBorder(null);
        }
        
        private static ImageIcon getIcon(LicenseIcon iconName) {
            return GUIMediator.getThemeImage(iconName.toString() + ".png");
        }
    }
}