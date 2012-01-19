package com.frostwire.gui.library;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

import com.limegroup.gnutella.gui.GUIMediator;

class PlayableIconCellRenderer extends SubstanceDefaultTableCellRenderer {

    private static final long serialVersionUID = -6392689488563533358L;

    private static final Icon speaker;

    static {
        speaker = GUIMediator.getThemeImage("speaker");
    }

    public PlayableIconCellRenderer() {
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        PlayableIconCell cell = (PlayableIconCell) value;
        JLabel component = (JLabel) super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
        component.setIcon(cell.isPlaying() ? speaker : cell.getIcon());
        component.setHorizontalTextPosition(JLabel.CENTER);
        component.setHorizontalAlignment(SwingConstants.CENTER);
        return component;
    }
}