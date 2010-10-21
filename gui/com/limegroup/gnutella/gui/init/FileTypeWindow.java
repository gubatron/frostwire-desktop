package com.limegroup.gnutella.gui.init;

import javax.swing.JComponent;

import com.limegroup.gnutella.gui.sharing.FileTypeSharingPanelManager;

/**
 * Composed panel for the first start setup sequence that uses a FileTypeSharingPanelManager to
 *  manage file type extensions sharing.  
 */
public final class FileTypeWindow extends SetupWindow {
    
    private FileTypeSharingPanelManager manager;
    
    FileTypeWindow(SetupManager manager) {
        super(manager, FileTypeSharingPanelManager.TITLE, FileTypeSharingPanelManager.LABEL,
                FileTypeSharingPanelManager.URL);
        this.manager = new FileTypeSharingPanelManager(this._manager.getOwnerComponent());
        this.manager.initOptions();
    }
    
    /**
     * Overriden to also add the language options.
     */
    protected void createWindow() {
        super.createWindow();
        
        manager.buildUI();
        
        setSetupComponent((JComponent)this.manager.getContainer());
    }
    
    /**
     * Overrides applySettings in SetupWindow superclass.
     * Applies the settings handled in this window.
     */
    public void applySettings(boolean loadCoreComponents) {
        this.manager.applyOptions();
    }

}
