package com.limegroup.gnutella.gui.playlist;

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
 *  Numbers each row in the table
 */
class NumberCellRenderer extends SubstanceDefaultTableCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = -195070901980682985L;

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int column) {
        NumberCell cell = (NumberCell) value;
        String formatted = Integer.toString(row + 1);
        super.getTableCellRendererComponent(table, formatted, isSel, hasFocus, row, column);
        setFontColor(cell.isPlaying(), table, row, column);
        return this;
    }
    
    /**
     * Check what font color to use if this song is playing. 
     */
    private void setFontColor(boolean playing, JTable table, int row, int column) {

        if (playing) {
            setForeground(ThemeSettings.PLAYING_DATA_LINE_COLOR.getValue());
        } else {
            Color color = Color.BLACK;
            if (SubstanceLookAndFeel.isCurrentLookAndFeel()) {
                color = getSubstanceForegroundColor(table, row, column);
            } else {
                color = UIManager.getColor("Table.foreground");
            }

            setForeground(color);
        }
    }

    private Color getSubstanceForegroundColor(JTable table, int row, int column) {
        TableUI tableUI = table.getUI();
        SubstanceTableUI ui = (SubstanceTableUI) tableUI;
        TableCellId cellId = new TableCellId(row, column);
        ComponentState currState = ui.getCellState(cellId);

        SubstanceColorScheme scheme = getColorSchemeForState(table, ui, currState);

        return scheme.getForegroundColor();
    }

    private SubstanceColorScheme getColorSchemeForState(JTable table, SubstanceTableUI ui, ComponentState state) {
        UpdateOptimizationInfo updateOptimizationInfo = ui.getUpdateOptimizationInfo();
        if (state == ComponentState.ENABLED) {
            if (updateOptimizationInfo == null) {
                return SubstanceColorSchemeUtilities.getColorScheme(table, state);
            } else {
                return updateOptimizationInfo.getDefaultScheme();
            }
        } else {
            if (updateOptimizationInfo == null) {
                return SubstanceColorSchemeUtilities.getColorScheme(table, ColorSchemeAssociationKind.HIGHLIGHT, state);
            } else {
                return updateOptimizationInfo.getHighlightColorScheme(state);
            }
        }
    }
}
