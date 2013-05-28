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
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.border.Border;

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
    private final Map<NamedMediaType, String> tooltipPlaceHolders;

    /**
     * Constructs the SchemaBox.
     */
    public SchemaBox(SearchResultMediator resultPanel) {
        this.resultPanel = resultPanel;

        this.buttonGroup = new ButtonGroup();
        this.buttonsMap = new HashMap<NamedMediaType, JToggleButton>();
        this.tooltipPlaceHolders = new HashMap<NamedMediaType,String>();

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
            incrementText(button,nmt);
        }
    }

    private void incrementText(JToggleButton button, NamedMediaType nmt) {
        String text = button.getText();
        int n = 0;
        try { // only justified situation of using try-catch for logic flow, since regex is slower
            n = Integer.valueOf(text);
        } catch (Throwable e) {
            // no an integer
        }
        String incrementedCounterValue = String.valueOf(n + 1);
        button.setText(incrementedCounterValue);
        button.setToolTipText(String.format(tooltipPlaceHolders.get(nmt),incrementedCounterValue));
    }

    /**
     * Adds the given schemas as radio buttons.
     */
    private void addSchemas() {
        NamedMediaType nmt;

        // Then add 'Audio'
        nmt = NamedMediaType.getFromDescription(MediaType.SCHEMA_AUDIO);
        tooltipPlaceHolders.put(nmt, I18n.tr("%s Audio files found (including .mp3, .wav, .ogg, and more)"));
        addMediaType(nmt, String.format(tooltipPlaceHolders.get(nmt),0));

        // Then add 'Images'
        nmt = NamedMediaType.getFromDescription(MediaType.SCHEMA_IMAGES);
        tooltipPlaceHolders.put(nmt, I18n.tr("%s Image files found (including .jpg, .gif, .png and more)"));
        addMediaType(nmt, String.format(tooltipPlaceHolders.get(nmt),0));

        // Then add 'Video'
        nmt = NamedMediaType.getFromDescription(MediaType.SCHEMA_VIDEO);
        tooltipPlaceHolders.put(nmt,I18n.tr("%s Video files found (including .avi, .mpg, .wmv, and more)"));
        addMediaType(nmt, String.format(tooltipPlaceHolders.get(nmt),0));
        
        // Then add 'Documents'
        nmt = NamedMediaType.getFromDescription(MediaType.SCHEMA_DOCUMENTS);
        tooltipPlaceHolders.put(nmt,I18n.tr("%s Document files found (including .html, .txt, .pdf, and more)"));
        addMediaType(nmt, String.format(tooltipPlaceHolders.get(nmt),0));
        
        // Then add 'Programs'
        nmt = NamedMediaType.getFromDescription(MediaType.SCHEMA_PROGRAMS);
        tooltipPlaceHolders.put(nmt,I18n.tr("%s Program files found (including .exe, .zip, .gz, and more)"));
        addMediaType(nmt, String.format(tooltipPlaceHolders.get(nmt),0));
        
        // Then add 'Torrents'
        nmt = NamedMediaType.getFromDescription(MediaType.SCHEMA_TORRENTS);
        tooltipPlaceHolders.put(nmt,I18n.tr("%s Torrent files found (includes only .torrent files. Torrent files point to collections of files shared on the BitTorrent network.)"));
        addMediaType(nmt, String.format(tooltipPlaceHolders.get(nmt),0));
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

    /**
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
    */

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
