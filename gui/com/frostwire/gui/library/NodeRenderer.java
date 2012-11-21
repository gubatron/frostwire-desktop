/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
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
package com.frostwire.gui.library;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTreeCellRenderer;

import com.limegroup.gnutella.gui.I18n;

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
        } else if (value instanceof DevicesNode) {
            DevicesNode node = (DevicesNode) value;
            setIcon(leaf ? node.getMinusDevices() : node.getPlusDevices());
        } else if (value instanceof DeviceNode) {
            DeviceNode node = (DeviceNode) value;
            setIcon(leaf ? node.getMinusIcon() : node.getPlusIcon());
            if (node.getDevice().isLocal()) {
                setText(I18n.tr("My files"));
            }
        } else if (value instanceof DeviceFileTypeTreeNode) {
            DeviceFileTypeTreeNode node = (DeviceFileTypeTreeNode) value;
            setIcon(node.getIcon());
        }

        return this;
    }
}
