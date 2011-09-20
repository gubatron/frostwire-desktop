package com.frostwire.gui.library;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.TableUI;

import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;
import org.pushingpixels.substance.internal.ui.SubstanceTableUI;
import org.pushingpixels.substance.internal.ui.SubstanceTableUI.TableCellId;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;
import org.pushingpixels.substance.internal.utils.UpdateOptimizationInfo;

import com.limegroup.gnutella.gui.themes.ThemeSettings;

/**
 *  Creates both a renderer and an editor for cells in the playlist table that display the name
 *  of the file being played.
 */
class PlaylistItemStartRenderer extends SubstanceDefaultTableCellRenderer {

    private static final long serialVersionUID = 6800146830099830381L;

    public PlaylistItemStartRenderer() {
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        LibraryPlaylistsTableDataLine line = ((PlaylistItemName) value).getLine();
        PlaylistItemName itemName = (PlaylistItemName) value;
        
        super.getTableCellRendererComponent(table, line.getSongName(), isSelected, hasFocus, row, column);
        //setFontColor(itemName.isPlaying(), line, table, row, column);
        return this;
    }
}