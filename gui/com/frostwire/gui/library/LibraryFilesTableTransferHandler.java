package com.frostwire.gui.library;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.frostwire.gui.player.AudioPlayer;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.dnd.DNDUtils;
import com.limegroup.gnutella.gui.dnd.FileTransferable;
import com.limegroup.gnutella.gui.dnd.MulticastTransferHandler;

class LibraryFilesTableTransferHandler extends TransferHandler {

    private static final long serialVersionUID = -5962762524077270378L;

    private final LibraryFilesTableMediator mediator;
    private final TransferHandler fallbackTransferHandler;

    public LibraryFilesTableTransferHandler(LibraryFilesTableMediator mediator) {
        this.mediator = mediator;
        this.fallbackTransferHandler = new MulticastTransferHandler(DNDUtils.DEFAULT_TRANSFER_HANDLERS);
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return canImport(support, true);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support, false)) {
            return fallbackTransferHandler.importData(support);
        }

        try {
            File[] files = DNDUtils.getFiles(support.getTransferable());
            LibraryUtils.createNewPlaylist(files);
        } catch (Exception e) {
            return fallbackTransferHandler.importData(support);
        }

        return true;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        AbstractLibraryTableDataLine<?>[] lines = mediator.getSelectedLines();
        List<File> files = new ArrayList<File>(lines.length);
        for (int i = 0; i < lines.length; i++) {
            files.add(lines[i].getFile());
        }
        return new FileTransferable(files);
    }

    private boolean canImport(TransferSupport support, boolean fallback) {
        if (!mediator.getMediaType().equals(MediaType.getAudioMediaType())) {
            return fallback ? fallbackTransferHandler.canImport(support) : false;
        }

        DataFlavor[] flavors = support.getDataFlavors();
        if (DNDUtils.containsFileFlavors(flavors)) {
            try {
                File[] files = DNDUtils.getFiles(support.getTransferable());
                for (File file : files) {
                    if (!AudioPlayer.isPlayableFile(file)) {
                        return fallback ? fallbackTransferHandler.canImport(support) : false;
                    }
                }
            } catch (InvalidDnDOperationException e) {
                // this case seems to be something special with the OS
                return true;
            } catch (Exception e) {
                return fallback ? fallbackTransferHandler.canImport(support) : false;
            }
        }

        return true;
    }
}
