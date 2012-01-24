package com.frostwire.gui.library;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import com.limegroup.gnutella.gui.tables.MouseObserver;

final class TreeMouseObserver implements MouseObserver {

    private final JTree tree;
    private final JPopupMenu popup;

    public TreeMouseObserver(JTree tree, JPopupMenu popup) {
        this.tree = tree;
        this.popup = popup;
    }

    public void handleMouseClick(MouseEvent e) {
    }

    /**
     * Handles when the mouse is double-clicked.
     */
    public void handleMouseDoubleClick(MouseEvent e) {
    }

    /**
     * Handles a right-mouse click.
     */
    public void handleRightMouseClick(MouseEvent e) {
    }

    /**
     * Handles a trigger to the popup menu.
     */
    public void handlePopupMenu(MouseEvent e) {
        TreePath path = tree.getUI().getClosestPathForLocation(tree, e.getPoint().x, e.getPoint().y);
        if (path != null) {
            tree.setSelectionPath(path);
            popup.show(tree, e.getX(), e.getY());
        }
    }
}
