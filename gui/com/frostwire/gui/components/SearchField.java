package com.frostwire.gui.components;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.limewire.i18n.I18nMarker;

import com.frostwire.gui.components.searchfield.JXSearchField;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;
import com.limegroup.gnutella.gui.themes.SkinPopupMenu;

public class SearchField extends JXSearchField {

    private static final long serialVersionUID = 3506693592729700194L;

    /**
     * The sole JPopupMenu that's shared among all the text fields.
     */
    private static JPopupMenu POPUP;

    /**
     * Our UndoManager.
     */
    private UndoManager undoManager;

    public SearchField() {
        init();
    }

    /**
     * Undoes the last action.
     */
    public void undo() {
        try {
            if (undoManager != null)
                undoManager.undoOrRedo();
        } catch (CannotUndoException ignored) {
        } catch (CannotRedoException ignored) {
        }
    }

    /**
     * Sets the UndoManager (but does NOT add it to the document).
     */
    public void setUndoManager(UndoManager undoer) {
        undoManager = undoer;
    }

    /**
     * Gets the undo manager.
     */
    public UndoManager getUndoManager() {
        return undoManager;
    }

    /**
     * Intercept the 'setDocument' so that we can null out our manager
     * and possibly assign a new one.
     */
    public void setDocument(Document doc) {
        if (doc != getDocument())
            undoManager = null;
        super.setDocument(doc);
    }

    /**
     * Initialize the necessary events.
     */
    private void init() {
        setComponentPopupMenu(createPopup());

        undoManager = new UndoManager();
        undoManager.setLimit(1);
        getDocument().addUndoableEditListener(undoManager);
    }

    /**
     * Creates the JPopupMenu that all LimeTextFields will share.
     */
    private static JPopupMenu createPopup() {
        if (POPUP != null) {
            return POPUP;
        }

        // initialize the JPopupMenu with necessary stuff.
        POPUP = new SkinPopupMenu() {
            /**
             * 
             */
            private static final long serialVersionUID = -6004124495511263059L;

            public void show(Component invoker, int x, int y) {
                ((SearchField) invoker).updateActions();
                super.show(invoker, x, y);
            }
        };

        POPUP.add(new SkinMenuItem(UNDO_ACTION));
        POPUP.addSeparator();
        POPUP.add(new SkinMenuItem(CUT_ACTION));
        POPUP.add(new SkinMenuItem(COPY_ACTION));
        POPUP.add(new SkinMenuItem(PASTE_ACTION));
        POPUP.add(new SkinMenuItem(DELETE_ACTION));
        POPUP.addSeparator();
        POPUP.add(new SkinMenuItem(SELECT_ALL_ACTION));
        return POPUP;
    }

    /**
     * Updates the actions in each text just before showing the popup menu.
     */
    private void updateActions() {
        String selectedText = getSelectedText();
        if (selectedText == null)
            selectedText = "";

        boolean stuffSelected = !selectedText.equals("");
        boolean allSelected = selectedText.equals(getText());

        UNDO_ACTION.setEnabled(isEnabled() && isEditable() && isUndoAvailable());
        CUT_ACTION.setEnabled(isEnabled() && isEditable() && stuffSelected);
        COPY_ACTION.setEnabled(isEnabled() && stuffSelected);
        PASTE_ACTION.setEnabled(isEnabled() && isEditable() && isPasteAvailable());
        DELETE_ACTION.setEnabled(isEnabled() && stuffSelected);
        SELECT_ALL_ACTION.setEnabled(isEnabled() && !allSelected);
    }

    /**
     * Determines if an Undo is available.
     */
    private boolean isUndoAvailable() {
        return getUndoManager() != null && getUndoManager().canUndoOrRedo();
    }

    /**
     * Determines if paste is currently available.
     */
    private boolean isPasteAvailable() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            return clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
        } catch (UnsupportedOperationException he) {
            return false;
        } catch (IllegalStateException ise) {
            return false;
        }
    }

    public void addToDictionary() {
        // TODO Auto-generated method stub

    }

    /**
     * Base Action that all LimeTextField actions extend.
     */
    private static abstract class FieldAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = 2309395089029318488L;

        /**
         * Constructs a new FieldAction looking up the name from the MessagesBundles.
         */
        public FieldAction(String name) {
            super(I18n.tr(name));
        }

        /**
         * Gets the LimeTextField for the given ActionEvent.
         */
        protected SearchField getField(ActionEvent e) {
            JMenuItem source = (JMenuItem) e.getSource();
            JPopupMenu menu = (JPopupMenu) source.getParent();
            return (SearchField) menu.getInvoker();
        }
    }

    /**
     * The undo action.
     */
    private static Action UNDO_ACTION = new FieldAction(I18nMarker.marktr("Undo")) {

        /**
         * 
         */
        private static final long serialVersionUID = -4598808952764108125L;

        public void actionPerformed(ActionEvent e) {
            getField(e).undo();
        }
    };

    /**
     * The cut action
     */
    private static Action CUT_ACTION = new FieldAction(I18nMarker.marktr("Cut")) {

        /**
         * 
         */
        private static final long serialVersionUID = 4315097846248426786L;

        public void actionPerformed(ActionEvent e) {
            getField(e).cut();
        }
    };

    /**
     * The copy action.
     */
    private static Action COPY_ACTION = new FieldAction(I18nMarker.marktr("Copy")) {

        /**
         * 
         */
        private static final long serialVersionUID = -6811443826148258282L;

        public void actionPerformed(ActionEvent e) {
            getField(e).copy();
        }
    };

    /**
     * The paste action.
     */
    private static Action PASTE_ACTION = new FieldAction(I18nMarker.marktr("Paste")) {

        /**
         * 
         */
        private static final long serialVersionUID = 5894287146853247748L;

        public void actionPerformed(ActionEvent e) {
            getField(e).paste();
        }
    };

    /**
     * The delete action.
     */
    private static Action DELETE_ACTION = new FieldAction(I18nMarker.marktr("Delete")) {

        /**
         * 
         */
        private static final long serialVersionUID = 5996971351238158202L;

        public void actionPerformed(ActionEvent e) {
            getField(e).replaceSelection("");
        }
    };

    /**
     * The select all action.
     */
    private static Action SELECT_ALL_ACTION = new FieldAction(I18nMarker.marktr("Select All")) {

        /**
         * 
         */
        private static final long serialVersionUID = -4805338416149604566L;

        public void actionPerformed(ActionEvent e) {
            getField(e).selectAll();
        }
    };
}
