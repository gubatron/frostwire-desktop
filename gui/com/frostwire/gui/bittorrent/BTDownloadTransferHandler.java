package com.frostwire.gui.bittorrent;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

import com.limegroup.gnutella.gui.dnd.DNDUtils;
import com.limegroup.gnutella.gui.dnd.DropInfo;
import com.limegroup.gnutella.gui.dnd.FileTransferable;
import com.limegroup.gnutella.gui.dnd.LimeTransferHandler;
import com.limegroup.gnutella.gui.search.SearchMediator;
import com.limegroup.gnutella.gui.search.SearchResultDataLine;
import com.limegroup.gnutella.gui.search.SearchResultMediator;
import com.limegroup.gnutella.gui.search.SearchResultTransferable;

class BTDownloadTransferHandler extends LimeTransferHandler {

    /**
     * 
     */
    private static final long serialVersionUID = 7090230440259575371L;

    BTDownloadTransferHandler() {
        super(COPY);
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        if (DNDUtils.contains(transferFlavors, SearchResultTransferable.dataFlavor)) {
        	return true;
        }
    	return DNDUtils.DEFAULT_TRANSFER_HANDLER.canImport(comp, transferFlavors);
    }
    
    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors, DropInfo ddi) {
    	return canImport(c, flavors);
    }

    public boolean importData(JComponent comp, Transferable t) {
        if (DNDUtils.contains(t.getTransferDataFlavors(), SearchResultTransferable.dataFlavor)) {
            try {
                SearchResultTransferable srt =
                        (SearchResultTransferable) t.getTransferData(SearchResultTransferable.dataFlavor);
                SearchResultMediator rp = srt.getResultPanel();
                SearchResultDataLine[] lines = srt.getTableLines();
                SearchMediator.downloadFromPanel(rp, lines);
                return true;
            } catch (UnsupportedFlavorException e) {
            } catch (IOException e) {
            }
        }
        return DNDUtils.DEFAULT_TRANSFER_HANDLER.importData(comp, t);
    }
    
    @Override
    public boolean importData(JComponent c, Transferable t, DropInfo ddi) {
    	return importData(c, t);
    }
    
    @Override
    protected Transferable createTransferable(JComponent c) {
        BTDownload[] downloads = BTDownloadMediator.instance().getSelectedBTDownloads();

        if (downloads.length > 0) {
            List<File> filesToDrop = getListOfFilesFromBTDowloads(downloads);
            return new FileTransferable(filesToDrop);
        }
        return null;
    }

    private List<File> getListOfFilesFromBTDowloads(BTDownload[] downloads) {
        List<File> files = new LinkedList<File>();
        
        for (BTDownload download : downloads) {
            File saveLocation = download.getSaveLocation();
            addFilesRecursively(files, saveLocation);
        }
        return files;
    }

    /**
     * TODO: Not sure how this will handle partially downloaded torrents, it'll probably include the incomplete files as well.
     * @param files
     * @param saveLocation
     */
    private void addFilesRecursively(List<File> files, File saveLocation) {
        if (saveLocation.isFile()) {
            files.add(saveLocation);
            return;
        } else {
            File[] listFiles = saveLocation.listFiles();
            for (File f : listFiles) {
                addFilesRecursively(files, f);
            }
        }        
    }
}