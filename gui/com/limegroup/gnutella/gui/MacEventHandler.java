/*
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

package com.limegroup.gnutella.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import net.roydesign.event.ApplicationEvent;
import net.roydesign.mac.MRJAdapter;

import com.limegroup.gnutella.ExternalControl;

/**
 * This class handles Macintosh specific events. The handled events  
 * include the selection of the "About" option in the Mac file menu,
 * the selection of the "Quit" option from the Mac file menu, and the
 * dropping of a file on LimeWire on the Mac, which LimeWire would be
 * expected to handle in some way.
 */
public class MacEventHandler {
    
    private static MacEventHandler INSTANCE;
    
    public static synchronized MacEventHandler instance() {
        if (INSTANCE==null)
            INSTANCE = new MacEventHandler();
        
        return INSTANCE;
    }
    
    private volatile File lastFileOpened = null;
    private volatile boolean enabled;
    private volatile ExternalControl externalControl = null;
    private volatile Initializer initializer = null;
    
    /** Creates a new instance of MacEventHandler */
    private MacEventHandler() {
        
        MRJAdapter.addAboutListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                handleAbout();
            }
        });
        
        MRJAdapter.addQuitApplicationListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                handleQuit();
            }
        });
        
        MRJAdapter.addOpenDocumentListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                File file = ((ApplicationEvent)evt).getFile();
                handleOpenFile(file);
            }
        });
        
        MRJAdapter.addReopenApplicationListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                handleReopen();
            }
        });
    } 
    
    public void enable(ExternalControl externalControl, Initializer initializer) {
        this.externalControl = externalControl;
        this.initializer = initializer;
        this.enabled = true;
        if(lastFileOpened != null)
            runFileOpen(lastFileOpened);
    }
    
    /**
     * Enable preferences.
     */
    public void enablePreferences() {
        MRJAdapter.setPreferencesEnabled(true);
        
        MRJAdapter.addPreferencesListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                handlePreferences();
            }
        });
    }
    
    /**
    * This responds to the selection of the about option by displaying the
    * about window to the user.  On OSX, this runs in a new ManagedThread to handle
    * the possibility that event processing can become blocked if launched
    * in the calling thread.
    */
    private void handleAbout() {      
        GUIMediator.showAboutWindow();
    }
    
    /**
    * This method responds to a quit event by closing the application in
    * the whichever method the user has configured (closing after completed
    * file transfers by default).  On OSX, this runs in a new ManagedThread to handle
    * the possibility that event processing can become blocked if launched
    * in the calling thread.
    */
    private void handleQuit() {
        GUIMediator.applyWindowSettings();
        GUIMediator.close(false);
    }
    
    /**
     * This method handles a request to open the specified file.
     */
    private void handleOpenFile(File file) {
        if(!enabled) {
            lastFileOpened = file;
        } else {
            runFileOpen(file);
        }
    }
    
    private void runFileOpen(File file) {
        String filename = file.getPath();
        if (filename.endsWith("limestart")) {
            initializer.setStartup();
        } else if (filename.startsWith("magnet")) { 
            if (!GUIMediator.isConstructed() || !GuiCoreMediator.getLifecycleManager().isStarted())
                externalControl.enqueueControlRequest(file.getAbsolutePath());
            else if (file.getAbsolutePath().startsWith("magnet:?xt=urn:btih")) {
            	GUIMediator.instance().openTorrentURI(file.getAbsolutePath(), false);
            }
        }
        else if (filename.endsWith("torrent")) {
            if (!GUIMediator.isConstructed() || !GuiCoreMediator.getLifecycleManager().isStarted())
                externalControl.enqueueControlRequest(file.getAbsolutePath());
            else
                GUIMediator.instance().openTorrentFile(file, false);
        } else {
            //PackagedMediaFileLauncher.launchFile(filename, false);
        }
    }
    
    private void handleReopen() {
        GUIMediator.handleReopen();
    }
    
    private void handlePreferences() {
        GUIMediator.instance().setOptionsVisible(true);
    }
}
