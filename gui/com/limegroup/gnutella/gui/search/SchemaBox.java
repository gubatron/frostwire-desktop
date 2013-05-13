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

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

import com.frostwire.gui.filters.TableLineFilter;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.ImageManipulator;
import com.limegroup.gnutella.settings.SearchSettings;

/**
 * A group of radio buttons for each schema.
 */
final class SchemaBox extends JPanel {

    /**
     * The property that the media type is stored in.
     */
    private static final String MEDIA = "NAMED_MEDIA_TYPE";

    /**
     * The property to store the selected icon in.
     */
    private static final String SELECTED = "SELECTED_ICON";

    /**
     * The property to store the unselected icon in.
     */
    private static final String DESELECTED = "DESELECTED_ICON";

    /**
     * The listener for changing the highlighting of buttons.
     */
    private final ItemListener HIGHLIGHTER = new Highlighter();

    /**
     * The clicker forwarder.
     */
    private final MouseListener CLICK_FORWARDER = new Clicker();

    private Set<AbstractButton> buttons = new HashSet<AbstractButton>();

    private AbstractButton lastSelectedButton;

    /**
     * Constructs the SchemaBox.
     */
    SchemaBox() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        List<NamedMediaType> allSchemas = NamedMediaType.getAllNamedMediaTypes();
        addSchemas(allSchemas);
        add(Box.createHorizontalGlue());
    }

    /**
     * Adds the given schemas as radio buttons.
     */
    private void addSchemas(List<? extends NamedMediaType> schemas) {
        //We first add specific ones in a certain order.
        //After that, leave it to random chance.
        NamedMediaType current;

        // Then add 'Audio'
        current = NamedMediaType.getFromDescription(MediaType.SCHEMA_AUDIO);
        schemas.remove(current);
        addMediaType(current, I18n.tr("Search For Audio Files, Including mp3, wav, ogg, and More"));

        // Then add 'Images'
        current = NamedMediaType.getFromDescription(MediaType.SCHEMA_IMAGES);
        schemas.remove(current);
        addMediaType(current, I18n.tr("Search For Image Files, Including jpg, gif, png and More"));

        // Then add 'Video'
        current = NamedMediaType.getFromDescription(MediaType.SCHEMA_VIDEO);
        schemas.remove(current);
        addMediaType(current, I18n.tr("Search For Video Files, Including avi, mpg, wmv, and More"));

        // Then add 'Documents'
        current = NamedMediaType.getFromDescription(MediaType.SCHEMA_DOCUMENTS);
        schemas.remove(current);
        addMediaType(current, I18n.tr("Search for Document Files, Including html, txt, pdf, and More"));

        // Then add 'Programs'
        current = NamedMediaType.getFromDescription(MediaType.SCHEMA_PROGRAMS);
        schemas.remove(current);
        addMediaType(current, I18n.tr("Search for Program Files, Including exe, zip, gz, and More"));

        // Then add 'Torrents'
        current = NamedMediaType.getFromDescription(MediaType.SCHEMA_TORRENTS);
        schemas.remove(current);
        addMediaType(current, I18n.tr("Search for Torrents!"));
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
        final AbstractButton button = new JRadioButton(type.getName());

        button.putClientProperty(MEDIA, type);
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
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setOpaque(false);
        button.addMouseListener(CLICK_FORWARDER);
        if (toolTip != null) {
            button.setToolTipText(toolTip);
        }

        add(button);

        if (SearchSettings.LAST_MEDIA_TYPE_USED.getValue().contains(type.getMediaType().getMimeType())) {
            button.setSelected(true);
            lastSelectedButton = button;
        }

        if (SearchSettings.LAST_MEDIA_TYPE_USED.getValue().isEmpty() && type.getMediaType().equals(MediaType.getAudioMediaType())) {
            button.setSelected(true);

        }

        buttons.add(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onFileTypeChanged(button);
            }
        });
    }

    /**
     * Listener for ItemEvent, so that the buttons can be highlighted or not
     * when selected (or not).
     */
    private static class Highlighter implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            AbstractButton button = (AbstractButton) e.getSource();
            if (e.getStateChange() == ItemEvent.SELECTED) {
                button.setIcon((Icon) button.getClientProperty(SELECTED));
            } else {
                button.setIcon((Icon) button.getClientProperty(DESELECTED));
            }
        }
    }

    private static void setIfNotNull(JComponent c, String col) {
        Color color = UIManager.getColor(col);
        if (color != null)
            c.setBackground(color);
    }

    protected void onFileTypeChanged(AbstractButton button) {
        NamedMediaType type = (NamedMediaType) button.getClientProperty(MEDIA);

        String mimeType = type.getMediaType().getMimeType();
        SearchSettings.LAST_MEDIA_TYPE_USED.setValue(mimeType);

        if (lastSelectedButton != null) {
            lastSelectedButton.setSelected(false);
        }

        lastSelectedButton = button;
        lastSelectedButton.setSelected(true);

        updateSearchResults(new MediaTypeFilter());
    }

    private void updateSearchResults(TableLineFilter<SearchResultDataLine> filter) {
        List<SearchResultMediator> resultPanels = SearchMediator.getSearchResultDisplayer().getResultPanels();
        for (SearchResultMediator resultPanel : resultPanels) {
            resultPanel.filterChanged(filter, 2);
        }
    }

    /**
     * Forwards click events from a panel to the panel's component.
     */
    private static class Clicker implements MouseListener {
        public void mouseEntered(MouseEvent e) {
            JComponent c = (JComponent) e.getSource();
            AbstractButton b;
            if (c instanceof AbstractButton) {
                b = (AbstractButton) c;
                c = (JComponent) c.getParent();
            } else {
                b = (AbstractButton) c.getComponent(0);
            }
            if (!b.isSelected())
                setIfNotNull(c, "TabbedPane.selected");
        }

        public void mouseExited(MouseEvent e) {
            JComponent c = (JComponent) e.getSource();
            AbstractButton b;
            if (c instanceof AbstractButton) {
                b = (AbstractButton) c;
                c = (JComponent) c.getParent();
            } else {
                b = (AbstractButton) c.getComponent(0);
            }
            if (!b.isSelected())
                setIfNotNull(c, "TabbedPane.background");
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

    public void setFilterFor(SearchResultMediator rp) {
        rp.filterChanged(new MediaTypeFilter(), 2);
    }

    public void panelReset(SearchResultMediator rp) {
        rp.filterChanged(new MediaTypeFilter(), 2);
    }
}
