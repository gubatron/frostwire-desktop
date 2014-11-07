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

import com.apple.eawt.*;
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

        Application app = Application.getApplication();

        app.setAboutHandler(new AboutHandler() {
            @Override
            public void handleAbout(AppEvent.AboutEvent aboutEvent) {
                MacEventHandler.this.handleAbout();
            }
        });

        app.setQuitHandler(new QuitHandler() {
            @Override
            public void handleQuitRequestWith(AppEvent.QuitEvent quitEvent, QuitResponse quitResponse) {
                handleQuit();
            }
        });

        app.setOpenFileHandler(new OpenFilesHandler() {
            @Override
            public void openFiles(AppEvent.OpenFilesEvent openFilesEvent) {

            }
        });

        /*

        MRJAdapter.addOpenDocumentListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                File file = ((ApplicationEvent)evt).getFile();
                handleOpenFile(file);
            }
        });
        */

        app.addAppEventListener(new AppReOpenedListener() {
            @Override
            public void appReOpened(AppEvent.AppReOpenedEvent appReOpenedEvent) {
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
        Application app = Application.getApplication();

        app.setPreferencesHandler(new PreferencesHandler() {
            @Override
            public void handlePreferences(AppEvent.PreferencesEvent preferencesEvent) {
                MacEventHandler.this.handlePreferences();
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
