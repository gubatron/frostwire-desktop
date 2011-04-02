package com.limegroup.gnutella.gui.xml.editor;

import java.util.List;

import javax.swing.JPanel;

import org.limewire.collection.NameValue;


public abstract class AbstractMetaEditorPanel extends JPanel {
    
    /**
     * 
     */
    private static final long serialVersionUID = 4769803894264003557L;

    public AbstractMetaEditorPanel() {
        super();
        setOpaque(false);
    }
    
    public abstract boolean checkInput();
    
    public abstract List<NameValue<String>> getInput();
    
}
