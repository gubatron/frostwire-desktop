package com.limegroup.gnutella.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.limewire.setting.BooleanSetting;

import com.limegroup.gnutella.FileEventListener;
import com.limegroup.gnutella.FileManagerEvent;
import com.limegroup.gnutella.gui.actions.MySharedFilesAction;
import com.limegroup.gnutella.gui.notify.Notification;
import com.limegroup.gnutella.gui.notify.NotifyUser;
import com.limegroup.gnutella.settings.QuestionsHandler;
import com.limegroup.gnutella.settings.SharingSettings;

/**
 * Catches FileManagerEvents and passes appropriate warnings to the GUI.
 */
public final class FileManagerWarningManager implements FileEventListener {

    private boolean showDepth = true;
    
    private final NotifyUser notifier;
    
    BooleanSetting numberSwitch;
    BooleanSetting depthSwitch;

    public FileManagerWarningManager(NotifyUser notifier, BooleanSetting numberSwitch, BooleanSetting depthSwitch) {
        this.notifier = notifier;
        
        this.numberSwitch = numberSwitch;
        this.depthSwitch  = depthSwitch;
    }
    
    public FileManagerWarningManager(NotifyUser notifier) {
        this(notifier, 
             QuestionsHandler.DONT_WARN_SHARING_NUMBER, 
             QuestionsHandler.DONT_WARN_SHARING_DEPTH);
    }
    
    private void doWarning(final String warning, final BooleanSetting ignoreSwitch) {
        if (!ignoreSwitch.getValue()) {
            GUIMediator.safeInvokeLater(new Runnable() {
                public void run() {
                    Notification notification = new Notification(warning, null, true,
                            new MySharedFilesAction(I18n.tr("View My Shared Files.")),
                            new IgnoreAction(ignoreSwitch));
                    
                    notifier.showMessage(notification);
                }
            });
        }

    }

    /**
     * Catches and processes FileManagerEvents.
     */
    public synchronized void handleFileEvent(FileManagerEvent evt) {

        switch (evt.getType()) {

        case FILEMANAGER_LOADING:

            this.showDepth = true;
            break;
        
        case ADD_FILE:
            
            break;
           
        case ADD_FOLDER:

            if (this.showDepth
                    && evt.getRelativeDepth() == SharingSettings.DEPTH_FOR_WARNING.getValue()) {

                this.showDepth = false;
                doWarning(I18n.tr(
                                "You are sharing many subfolders within your shared folder: {0}. This indicates a potential security problem, so please review your \"My Shared Files\" to ensure you aren't sharing any sensitive files.",
                                evt.getRootShare()), this.depthSwitch);

            }

        default:
            
            return;
        }

        if (evt.getFileManager().getNumFiles() == SharingSettings.FILES_FOR_WARNING.getValue()) {

            doWarning(I18n.tr(
                            "You are sharing a lot of files through FrostWire, {0} files and counting. Do you mean to share all these files? Please review your sharing settings and which files are shared to ensure you aren't sharing any sensitive files.",
                            SharingSettings.FILES_FOR_WARNING.getValue()), this.numberSwitch);

        }

    }

    private static class IgnoreAction extends AbstractAction {
        /**
         * 
         */
        private static final long serialVersionUID = -3892490387425217951L;
        BooleanSetting ignoreSwitch;
        public IgnoreAction(BooleanSetting ignoreSwitch) {
            this.ignoreSwitch = ignoreSwitch;
            
            putValue(Action.NAME, I18n.tr("Do not display this message again."));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Do not display this message again."));
        }

        public void actionPerformed(ActionEvent e) {
            this.ignoreSwitch.setValue(true);
        }
    }

}
