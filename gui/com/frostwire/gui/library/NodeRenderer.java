package com.frostwire.gui.library;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTreeCellRenderer;


public class NodeRenderer extends SubstanceDefaultTreeCellRenderer {

    private static final long serialVersionUID = -1834835893663476044L;

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (value instanceof DirectoryHolderNode) {
            DirectoryHolderNode node = (DirectoryHolderNode) value;
            DirectoryHolder dh = node.getDirectoryHolder();
            setText(dh.getName());
            setToolTipText(dh.getDescription());
            Icon icon = dh.getIcon();
            if (icon != null) {
                setIcon(icon);
            }
        }
        return this;
    }
}
