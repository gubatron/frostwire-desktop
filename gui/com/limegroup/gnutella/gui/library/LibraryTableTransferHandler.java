package com.limegroup.gnutella.gui.library;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JComponent;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.dnd.CompositeTransferable;
import com.limegroup.gnutella.gui.dnd.DNDUtils;
import com.limegroup.gnutella.gui.dnd.DropInfo;
import com.limegroup.gnutella.gui.dnd.FileTransferable;
import com.limegroup.gnutella.gui.dnd.LimeTransferHandler;

/**
 * A TransferHandler specifically for the library table.
 * This will ensure that after a drag is finished, the library is updated
 * to show the file was removed.
 * 
 * TODO dnd: Offer real drop support (move, copy, etc..).
 */
public class LibraryTableTransferHandler extends LimeTransferHandler {

    /**
     * 
     */
    private static final long serialVersionUID = 5039095245636389805L;

    /** Constructs a new TransferHandler that supports COPY & MOVE. */
    public LibraryTableTransferHandler() {
        super(COPY_OR_MOVE);
    }
    
    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
    	if (!DNDUtils.containsLibraryFlavors(transferFlavors)) {
    		return false;
    	}
    	return DNDUtils.DEFAULT_TRANSFER_HANDLER.canImport(comp, transferFlavors);	
    }
    
    /**
     * Returns true if the drop can be imported.
     */
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors, DropInfo info) {
    	return false;
    	//return canImport(comp, transferFlavors);
    }
    
    @Override
    public boolean importData(JComponent comp, Transferable t) {
    	return DNDUtils.DEFAULT_TRANSFER_HANDLER.importData(comp, t);
    }
    
    /**
     * Imports incoming data.
     */
    public boolean importData(JComponent comp, Transferable t, DropInfo info) {
        return importData(comp, t);
        
    }
    
    /**
     *  Moves the holder from point A to point B IFF they were SharedFileHolders,
     *  otherwise nothing happens.
     * @param tree
     * @param holder
     * @return
     */
//    private boolean transferFromTree(File dstParent, DirectoryHolder holder, DropInfo info) {
//        File src = holder.getDirectory();
//        File dst = new File(dstParent, src.getName());
//        if(!src.renameTo(dst))
//            return false;
//        
//        RouterService.getFileManager().loadSettings();
//        return true;
//    }
    
    // TODO: allow COPY
//    private boolean transferFromTable(File dst, LibraryTableDataLine[] lines, DropInfo info) {
//        FileManager fman = RouterService.getFileManager();
//        
//        boolean reload = false;
//        for(int i = 0; i < lines.length; i++) {
//            File oldFile = lines[i].getFile();
//            File newFile = new File(dst, oldFile.getName());
//            if(oldFile.equals(newFile))
//                continue;
//            
//            boolean renamed = oldFile.renameTo(newFile);
//            if(renamed) {
//                if(fman.isFileShared(oldFile))
//                    fman.renameFileIfShared(oldFile, newFile);
//                else if(newFile.isDirectory())
//                    reload = true;
//                else
//                    fman.addFileIfShared(newFile);
//            }
//        }
//        
//        if(reload)
//            fman.loadSettings();
//        
//        return true;
//    }
    
    /**
     * Shares the list of files.
     * 
     * @param tree
     * @param files
     * @return
     */
    // TODO dnd merge table & plain files and do correct MOVE or COPY
    // for now, default handlers take care of files and ask user to share them
//    private boolean transferPlainFiles(File dst, File[] files, DropInfo info) {
//    	return false;
//    }        

    /**
     * Unshares files that were moved out.
     */
    protected void exportDone(JComponent source, Transferable data, int action) {
        // We only want to process a move, because that's the only kind of export that
        // could have changed our shared files.
        if(action != MOVE)
            return;
        
        if(!DNDUtils.containsFileFlavors(data.getTransferDataFlavors()))
        	return;
            
        File[] files;
        try {
        	files = DNDUtils.getFiles(data);
        } catch(UnsupportedFlavorException ufe) {
        	return;
        } catch(IOException ioe) {
        	return;
        }
        
        //boolean reloaded = false;
        //FileManager fileManager = GuiCoreMediator.getFileManager();
        for(File f : files) {
			// TODO race conditions can occur here when the native drop
			// receiver did not move the file before exportDone is called
        	// file or directory has been moved, happens on Linux with Konqueror
        	if (!f.exists()) {
        		//FileDesc fileDesc = fileManager.removeFileIfShared(f);
        		// was not a file, must have been a directory then
        		//if (fileDesc == null) {
            		// If we find a directory, we must reload all settings, because
            		// the directory may have contained subdirectories and all wacky
            		// things, and we'd like to remove them all from being
            		// shared, which is difficult to do now that we don't know where
            		// the directory moved to.
        			//reloaded = true;
            		//fileManager.loadSettings();
            		//break;
        		//}
        	}
        }
        
        // If we didn't already tell files to reload, then do a quick refresh to
        // immediately show that we moved some things.
//        if(!reloaded)
//        	LibraryMediator.instance().forceRefresh();
    
        // notify status line of changed files
        GUIMediator.instance().refreshGUI();
    }
    
    @Override
    protected Transferable createTransferable(JComponent c) {
        LibraryTableDataLine[] lines = LibraryTableMediator.instance().getSelectedLibraryLines();
        if (lines.length == 0) {
        	return null;
        }
        Transferable lineTransferable = new LibraryTableTransferable(lines);
        Transferable fileTransferable =
        	new FileTransferable(FileTransferable.EMPTY_FILE_LIST, Arrays.asList(lines));
        return new CompositeTransferable(lineTransferable, fileTransferable);
    }

}
