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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.frostwire.gui.AlphaIcon;
import com.frostwire.gui.bittorrent.CreativeCommonsSelectorPanel.LicenseToggleButton.LicenseIcon;
import com.frostwire.torrent.CreativeCommonsLicense;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LimeTextField;

public class CreativeCommonsSelectorPanel extends JPanel implements LicenseToggleButtonOnToggleListener {

    private final JCheckBox confirmRightfulUseOfLicense;

    private final JLabel authorsNameLabel;
    private final LimeTextField authorsName;
    private final JLabel titleLabel;
    private final LimeTextField title;
    private final JLabel attributionUrlLabel;
    private final LimeTextField attributionUrl;

    private final LicenseToggleButton ccButton;
    private final LicenseToggleButton byButton;
    private final LicenseToggleButton ncButton;
    private final LicenseToggleButton ndButton;
    private final LicenseToggleButton saButton;

    public CreativeCommonsSelectorPanel() {
        setLayout(new MigLayout("fill"));
        GUIUtils.setTitledBorderOnPanel(this, I18n.tr("Choose a Creative Commons License for this work"));

        confirmRightfulUseOfLicense = new JCheckBox("<html><strong>"
                + I18n.tr("I am the Content Creator of this work or I have been granted the rights to share this content under the following license by the Content Creator(s).") + "</strong></html>");

        authorsNameLabel = new JLabel("<html>" + I18n.tr("Author's Name") + "</html>");
        authorsName = new LimeTextField();
        authorsName.setToolTipText(I18n.tr("The name of the creator or creators of this work."));

        titleLabel = new JLabel("<html>" + I18n.tr("Work's Title") + "</html>");
        title = new LimeTextField();
        title.setToolTipText(I18n.tr("The name of this work, i.e. the titleLabel of a music album, the titleLabel of a book, the titleLabel of a movie, etc."));

        attributionUrlLabel = new JLabel("<html><b>" + I18n.tr("Attribution URL") + "</b></html>");
        attributionUrl = new LimeTextField();
        attributionUrl.setToolTipText(I18n.tr("The Content Creator's website to give attribution about this work if shared by others."));

        ccButton = new LicenseToggleButton(
                LicenseToggleButton.LicenseIcon.CC,
                "Creative Commons",
                "Offering your work under a Creative Commons license does not mean giving up your copyright. It means offering some of your rights to any member of the public but only under certain conditions.",
                true, false);
        byButton = new LicenseToggleButton(LicenseToggleButton.LicenseIcon.BY, "Attribution",
                "You let others copy, distribute, display, and perform your copyrighted work but only if they give credit the way you request.", true, false);
        ncButton = new LicenseToggleButton(LicenseToggleButton.LicenseIcon.NC, "NonCommercial",
                "You let others copy, distribute, display, and perform your work — and derivative works based upon it — but for noncommercial purposes only.", true, true);
        ndButton = new LicenseToggleButton(LicenseToggleButton.LicenseIcon.ND, "NoDerivatives",
                "You let others copy, distribute, display, and perform only verbatim copies of your work, not derivative works based upon it.", false, true);
        saButton = new LicenseToggleButton(LicenseToggleButton.LicenseIcon.SA, "Share-Alike",
                "You allow others to distribute derivative works only under a license identical to the license that governs your work.", true, true);

        initListeners();
        initComponents();
    }

    @Override
    public void onButtonToggled(LicenseToggleButton button) {
        //logic to auto disable/enable CC license buttons depending on what's on.
        if (button.getLicenseIcon() == LicenseIcon.ND && button.isSelected()) {
            saButton.setSelected(false);
        } else if (button.getLicenseIcon() == LicenseIcon.SA && button.isSelected()) {
            ndButton.setSelected(false);
        }
    }

    public boolean hasConfirmedRightfulUseOfLicense() {
        return confirmRightfulUseOfLicense.isSelected();
    }

    public CreativeCommonsLicense getCreativeCommonsLicense() {
        CreativeCommonsLicense result = null;

        if (hasConfirmedRightfulUseOfLicense()) {
            result = new CreativeCommonsLicense(saButton.isSelected(), ncButton.isSelected(), ndButton.isSelected(), title.getText(), authorsName.getText(), attributionUrl.getText());
        }

        return result;
    }

    private void initListeners() {
        confirmRightfulUseOfLicense.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onConfirmRightfulUseOfLicenseAction();
            }
        });

        ncButton.setOnToggleListener(this);
        ndButton.setOnToggleListener(this);
        saButton.setOnToggleListener(this);
    }

    private void initComponents() {
        confirmRightfulUseOfLicense.setSelected(false);

        add(confirmRightfulUseOfLicense, "growx, north, gapbottom 8, wrap");

        add(authorsNameLabel, "gapbottom 5px, pushx, wmin 215px");
        add(titleLabel, "gapbottom 5px, wmin 215px, pushx, wrap");

        add(authorsName, "gapbottom 5px, growx 50, push, wmin 215px, span 1");
        add(title, "gapbottom 5px, growx 50, push, wmin 215px, span 1, wrap");

        JPanel attribPanel = new JPanel(new MigLayout("fillx, insets 0 0 0 0"));
        attribPanel.add(attributionUrlLabel, "width 110px!, alignx left");
        attribPanel.add(attributionUrl, "alignx left, growx, pushx");
        add(attribPanel, "aligny top, growx, gapbottom 10px, span 2, wrap");

        add(new JLabel("<html><strong>" + I18n.tr("Select what people can and can't do with this work") + "</strong></html>"), "span 2, alignx center, growx, push, wrap");

        JPanel licenseButtonsPanel = new JPanel(new MigLayout("fillx, insets 0 0 0 0"));
        licenseButtonsPanel.add(ccButton, "aligny top");
        licenseButtonsPanel.add(byButton, "aligny top");
        licenseButtonsPanel.add(ncButton, "aligny top");
        licenseButtonsPanel.add(ndButton, "aligny top");
        licenseButtonsPanel.add(saButton, "aligny top, wrap");
        add(licenseButtonsPanel, "aligny top, span 2, growx, pushy, wrap");

        confirmRightfulUseOfLicense.setSelected(false);
        onConfirmRightfulUseOfLicenseAction();
    }

    protected void onConfirmRightfulUseOfLicenseAction() {
        boolean creativeCommonsEnabled = confirmRightfulUseOfLicense.isSelected();

        authorsNameLabel.setEnabled(creativeCommonsEnabled);
        authorsName.setEnabled(creativeCommonsEnabled);
        titleLabel.setEnabled(creativeCommonsEnabled);
        title.setEnabled(creativeCommonsEnabled);
        attributionUrlLabel.setEnabled(creativeCommonsEnabled);
        attributionUrl.setEnabled(creativeCommonsEnabled);

        ccButton.setSelected(creativeCommonsEnabled);
        byButton.setSelected(creativeCommonsEnabled);
        ncButton.setSelected(creativeCommonsEnabled);

        if (creativeCommonsEnabled) {
            ndButton.setSelected(false);
            saButton.setSelected(true);
        } else {
            ndButton.setSelected(creativeCommonsEnabled);
            saButton.setSelected(creativeCommonsEnabled);
        }
    }

    public static class LicenseToggleButton extends JPanel {
        private boolean selected;
        private final boolean toggleable;

        private final ImageIcon selectedIcon;
        private final AlphaIcon unselectedIcon;

        private JLabel iconLabel;
        private JLabel titleLabel;
        private JLabel descriptionLabel;

        private LicenseIcon licenseIcon;

        private LicenseToggleButtonOnToggleListener listener;

        public enum LicenseIcon {
            CC, BY, SA, ND, NC
        }

        public LicenseToggleButton(LicenseIcon iconName, String text, String description, boolean selected, boolean toggleable) {
            this.toggleable = toggleable;
            setMeUp();

            licenseIcon = iconName;
            selectedIcon = getIcon(iconName);
            unselectedIcon = new AlphaIcon(selectedIcon, 0.2f);

            iconLabel = new JLabel((selected) ? selectedIcon : unselectedIcon);
            titleLabel = new JLabel("<html><b>" + text + "</b></html>");
            descriptionLabel = new JLabel("<html><small>" + description + "</small></html>");

            setLayout(new MigLayout("fill, wrap 1"));
            add(iconLabel, "alignx center, wrap");
            add(titleLabel, "growx, alignx center, wrap");
            add(descriptionLabel, "alignx center");

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (listener != null && listener instanceof CreativeCommonsSelectorPanel) {
                        CreativeCommonsSelectorPanel parentPanel = (CreativeCommonsSelectorPanel) listener;
                        if (parentPanel.hasConfirmedRightfulUseOfLicense()) {
                            onToggle();
                        }
                    }
                }
            });
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

        private void onToggle() {
            if (toggleable) {
                selected = !selected;
                updateComponents();

                if (listener != null) {
                    listener.onButtonToggled(this);
                }
            }
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
        }

        private static ImageIcon getIcon(LicenseIcon iconName) {
            String name = "CC";
            switch (iconName) {
            case CC:
                name = "CC";
                break;
            case BY:
                name = "BY";
                break;
            case SA:
                name = "SA";
                break;
            case ND:
                name = "ND";
                break;
            case NC:
                name = "NC";
                break;
            }
            return GUIMediator.getThemeImage(name + ".png");
        }

    }
}