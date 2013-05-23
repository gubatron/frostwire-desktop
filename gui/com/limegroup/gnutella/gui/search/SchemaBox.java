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

package com.limegroup.gnutella.gui.search;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.border.Border;

import org.apache.commons.io.FilenameUtils;

import com.frostwire.gui.theme.ThemeMediator;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.ImageManipulator;
import com.limegroup.gnutella.settings.SearchSettings;

/**
 * A group of radio buttons for each schema.
 */
final class SchemaBox extends JPanel {

    private final SearchResultMediator resultPanel;

    private final ButtonGroup buttonGroup;
    private final Map<NamedMediaType, JToggleButton> buttonsMap;

    /**
     * Constructs the SchemaBox.
     */
    public SchemaBox(SearchResultMediator resultPanel) {
        this.resultPanel = resultPanel;

        this.buttonGroup = new ButtonGroup();
        this.buttonsMap = new HashMap<NamedMediaType, JToggleButton>();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        addSchemas();
        add(Box.createHorizontalGlue());

        Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeMediator.LIGHT_BORDER_COLOR);
        setBorder(border);

        Dimension dim = new Dimension(100, 35);
        setPreferredSize(dim);
        setMinimumSize(dim);
    }

    public void applyFilters() {
        AbstractButton button = getSelectedButton();

        if (button != null) {
            button.doClick();
        }
    }

    public void updateCounters(UISearchResult sr) {
        NamedMediaType nmt = NamedMediaType.getFromExtension(sr.getExtension());
        if (nmt != null && buttonsMap.containsKey(nmt)) {
            JToggleButton button = buttonsMap.get(nmt);
            incrementText(button);
        }
    }

    private void incrementText(JToggleButton button) {
        String text = button.getText();
        int n = 0;
        try { // only justified situation of using try-catch for logic flow, since regex is slower
            n = Integer.valueOf(text);
        } catch (Throwable e) {
            // no an integer
        }
        button.setText(String.valueOf(n + 1));
    }

    /**
     * Adds the given schemas as radio buttons.
     */
    private void addSchemas() {
        NamedMediaType nmt;

        // Then add 'Audio'
        nmt = NamedMediaType.getFromDescription(MediaType.SCHEMA_AUDIO);
        addMediaType(nmt, I18n.tr("Search For Audio Files, Including mp3, wav, ogg, and More"));

        // Then add 'Images'
        nmt = NamedMediaType.getFromDescription(MediaType.SCHEMA_IMAGES);
        addMediaType(nmt, I18n.tr("Search For Image Files, Including jpg, gif, png and More"));

        // Then add 'Video'
        nmt = NamedMediaType.getFromDescription(MediaType.SCHEMA_VIDEO);
        addMediaType(nmt, I18n.tr("Search For Video Files, Including avi, mpg, wmv, and More"));

        // Then add 'Documents'
        nmt = NamedMediaType.getFromDescription(MediaType.SCHEMA_DOCUMENTS);
        addMediaType(nmt, I18n.tr("Search for Document Files, Including html, txt, pdf, and More"));

        // Then add 'Programs'
        nmt = NamedMediaType.getFromDescription(MediaType.SCHEMA_PROGRAMS);
        addMediaType(nmt, I18n.tr("Search for Program Files, Including exe, zip, gz, and More"));

        // Then add 'Torrents'
        nmt = NamedMediaType.getFromDescription(MediaType.SCHEMA_TORRENTS);
        addMediaType(nmt, I18n.tr("Search for Torrents!"));
    }

    /**
     * Adds the given NamedMediaType.
     *
     * Marks the 'Any Type' as selected.
     */
    private void addMediaType(NamedMediaType type, String toolTip) {
        Icon icon = type.getIcon();
        Icon disabledIcon = null;
        Icon rolloverIcon = null;
        JToggleButton button = new JRadioButton("0");

        if (icon != null) {
            disabledIcon = ImageManipulator.darken(icon);
            rolloverIcon = ImageManipulator.brighten(icon);
        }
        button.setIcon(disabledIcon);
        button.setRolloverIcon(rolloverIcon);
        button.setRolloverSelectedIcon(rolloverIcon);
        button.setPressedIcon(rolloverIcon);

        button.setSelectedIcon(rolloverIcon);// use the right icon here

        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setMargin(new Insets(0, 6, 0, 0));
        Dimension d = new Dimension(60, 20);
        button.setPreferredSize(d);
        button.setMinimumSize(d);
        button.setOpaque(false);
        if (toolTip != null) {
            button.setToolTipText(toolTip);
        }

        buttonGroup.add(button);
        add(button);

        button.addActionListener(new SchemaButtonActionListener(type));
        button.setSelected(isMediaTypeSelected(type));

        buttonsMap.put(type, button);
    }

    private boolean isMediaTypeSelected(NamedMediaType type) {
        boolean result = false;

        if (SearchSettings.LAST_MEDIA_TYPE_USED.getValue().contains(type.getMediaType().getMimeType())) {
            result = true;
        }

        if (SearchSettings.LAST_MEDIA_TYPE_USED.getValue().isEmpty() && type.getMediaType().equals(MediaType.getAudioMediaType())) {
            result = true;
        }

        return result;
    }

    private AbstractButton getSelectedButton() {
        AbstractButton selectedButton = null;

        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                selectedButton = button;
            }
        }

        return selectedButton;
    }

    private AbstractButton getMediaTypeButton(String ext) {
        AbstractButton selectedButton = null;

        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                selectedButton = button;
            }
        }

        return selectedButton;
    }

    private final class SchemaButtonActionListener implements ActionListener {

        private final NamedMediaType nmt;
        private final MediaTypeFilter filter;

        public SchemaButtonActionListener(NamedMediaType nmt) {
            this.nmt = nmt;

            this.filter = new MediaTypeFilter(nmt);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String mimeType = nmt.getMediaType().getMimeType();
            SearchSettings.LAST_MEDIA_TYPE_USED.setValue(mimeType);

            if (resultPanel != null) {
                resultPanel.filterChanged(filter, 2);
            }
        }
    }
}
