package com.frostwire.gui.library;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.AbstractAction;

/**
 * Removes selected root folders from a {@link RecursiveLibraryDirectoryPanel}.
 */
public class RemoveLibraryDirectoryAction extends AbstractAction {

    private static final long serialVersionUID = -6729288511779797455L;
    
    private final RecursiveLibraryDirectoryPanel recursiveSharingPanel;

    public RemoveLibraryDirectoryAction(RecursiveLibraryDirectoryPanel recursiveSharingPanel) {
        super(I18n.tr("Remove"));
        this.recursiveSharingPanel = recursiveSharingPanel;
        setEnabled(false);
        recursiveSharingPanel.getTree().addTreeSelectionListener(new EnablementSelectionListener());
    }
    
    public void actionPerformed(ActionEvent e) {
        File dir = (File) recursiveSharingPanel.getTree().getSelectionPath().getLastPathComponent();
        recursiveSharingPanel.removeRoot(dir);
    }
    
    /**
     * Enables action when a root folder is selected.
     */
    private class EnablementSelectionListener implements TreeSelectionListener {

        public void valueChanged(TreeSelectionEvent e) {
            Object obj = e.getPath().getLastPathComponent();
            if (obj instanceof File) {
                setEnabled(recursiveSharingPanel.isRoot(((File)obj)));
            } else {
                setEnabled(false);
            }
        }

    }

}