package com.limegroup.gnutella.gui.init;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JPanel;

import org.limewire.i18n.I18nMarker;
import org.limewire.util.CommonUtils;

/** State Your Intent. */
final class IntentWindow extends SetupWindow {
    
    /**
     * 
     */
    private static final long serialVersionUID = 5196113358670223989L;
    private boolean setWillNot = false;
    private Properties properties;

	IntentWindow(SetupManager manager) {
		super(manager, 
			  I18nMarker.marktr("State Your Intent"), 
			  I18nMarker.marktr("One more thing..."));
    }
	
	private boolean isCurrentVersionChecked() {
	    if(properties == null) {
	        properties = new Properties();
	        try {
	            properties.load(new FileInputStream(getPropertiesFile()));
	        } catch(IOException iox) {
	        	System.out.println("Could not load properties from property file.");
	        	return false;
	        }
	    }
	    
	    String exists = properties.getProperty("willnot");
	    return exists != null && exists.equals("true");
	}
	
	boolean isConfirmedWillNot() {
	    return isCurrentVersionChecked() || setWillNot;
	}
    
    @Override
    protected void createWindow() {
        super.createWindow();
        
        JPanel innerPanel = new JPanel(new BorderLayout());
        final IntentPanel intentPanel = new IntentPanel();
        innerPanel.add(intentPanel, BorderLayout.CENTER);        
        setSetupComponent(innerPanel);
        
        setNext(null);
        intentPanel.addButtonListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(intentPanel.hasSelection()) {
                    setNext(IntentWindow.this);
                    setWillNot = intentPanel.isWillNot();
                    _manager.enableActions(getAppropriateActions());
                }
            }
        });
	}

	@Override
    public void applySettings(boolean loadCoreComponents) {
	    if(setWillNot) {
	        properties.put("willnot", "true");
	        try {
	            properties.store(new FileOutputStream(getPropertiesFile()), "Started & Ran Versions");
	        } catch(IOException ignored) {
	        	System.out.println(ignored);
	        }
	    }	    
	}
	
	private File getPropertiesFile() {
	    return new File(CommonUtils.getUserSettingsDir(), "intent.props");
	}
}